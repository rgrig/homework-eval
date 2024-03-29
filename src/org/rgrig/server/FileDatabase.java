package org.rgrig.server;

import java.io.*;
import java.text.*;
import java.util.*;
import org.rgrig.client.*;

class Account {
  public String id;
  public int passwdHash;
  public Account(String id, int passwdHash) {
    this.id = id;
    this.passwdHash = passwdHash;
  }
}

/**
 * Reads/writes data from {@code location} directory.
 */
public class FileDatabase implements Database {
  private File location;

  public FileDatabase(String loc) throws IOException {
    location = new File(loc);
  }

  private File file(String ... path) throws FileNotFoundException {
    File result = location;
    for (int i = 0; i < path.length; ++i)
      result = new File(result, path[i]);
    return result;
  }

  private String propertyName(String a, String b, String k) {
    return String.format("%s/%s:%s", a, b, k);
  }

  private String getProperty(String a, String b, String k)
      throws ServerException
  {
    try {
      FileInputStream fis1 = new FileInputStream(file("config"));
      FileInputStream fis2 = new FileInputStream(file(a,b,"config"));

      Properties defaults = new Properties();
      defaults.load(fis1);
      Properties p = new Properties(defaults);
      p.load(fis2);
      fis1.close(); fis2.close();

      String result = p.getProperty(k);
      if (result == null) {
        throw new ServerException(
            "ADMIN: Please add property " + propertyName(a, b, k));
      }

      return p.getProperty(k);
    } catch (IOException e) {
      throw UtilSrv.se("Can't read property " + propertyName(a, b, k), e);
    }
  }

  private String getPbProperty(String problem, String key)
      throws ServerException
  {
    return getProperty("problems", problem, key);
  }

  // TODO Read the config files only once!
  public PbProperties getProblemProperties(String problem)
      throws ServerException
  {
    return PbProperties.empty()
      .withName(getPbProperty(problem, "name"))
      .withPenalty(Double.valueOf(getPbProperty(problem, "penalty")))
      .withPoints(Double.valueOf(getPbProperty(problem, "points")))
      .withMemoryLimit(Integer.valueOf(getPbProperty(problem, "memlimit")))
      .withTimeLimit(Integer.valueOf(getPbProperty(problem, "timelimit")))
      .withDeadline(parseDate(getPbProperty(problem, "deadline")))
      .withStart(parseDate(getPbProperty(problem, "start")))
      .withScoringMethod(getPbProperty(problem, "scoringmethod"))
      .withValidator(getPbProperty(problem, "validator"))
      .withDifficulty(Integer.valueOf(getPbProperty(problem, "difficulty")))
      .check();
  }

  // TODO: Get rid of the code duplication
  public Problem[] getProblems() throws ServerException {
    try {
      ArrayList<Problem> result = new ArrayList<Problem>();
      String ids = "";
      for (File pf : file("problems").listFiles()) {
        String id = pf.getName(); ids += ":" + id;
        if (!pf.isDirectory() || id.startsWith(".")) continue;

        Problem p = new Problem();
        PbProperties pp = getProblemProperties(id);
        p.start = pp.start();
        p.deadline = pp.deadline();
        p.id = id;
        p.name = pp.name();
        p.totalPoints = pp.points();

        // specific to problems
        p.statement = UtilSrv.readFile(new File(pf, "statement"));
        p.examples = getProblemExamples(p.id);

        result.add(p);
      }
      return result.toArray(new Problem[0]);
    } catch (IOException e) {
      throw UtilSrv.se("Can't parse problems.", e);
    }
  }

  public PbTest[] getProblemExamples(String pbId) throws ServerException {
    try {
      return getTestsHelper(file("problems", pbId, "examples"));
    } catch (IOException e) {
      throw UtilSrv.se("Can't fetch examples for " + pbId, e);
    }
  }

  public PbTest[] getProblemTests(String pbId) throws ServerException {
    try {
      return getTestsHelper(file("problems", pbId, "tests"));
    } catch (IOException e) {
      throw UtilSrv.se("Can't fetch tests for " + pbId, e);
    }
  }

  public String[] getLanguages() throws ServerException {
    try {
      ArrayList<String> result = new ArrayList<String>();
      for (File lf : file("languages").listFiles())
        if (!lf.getName().startsWith(".")) result.add(lf.getName());
      return result.toArray(new String[0]);
    } catch (IOException e) {
      throw UtilSrv.se("Can't read available languages.", e);
    }
  }

  public Language getLanguage(String id) throws ServerException {
    try {
      FileInputStream fis = new FileInputStream(file("languages",id));

      Properties p = new Properties();
      p.load(fis); fis.close();
      Language r = new Language();
      r.saveName = p.getProperty("save");
      r.compileCmd = p.getProperty("compile");
      r.executable = p.getProperty("executable");
      r.runCmd = p.getProperty("run");
      return r;
    } catch (IOException e) {
      throw UtilSrv.se("Can't judge using " + id + ".", e);
    }
  }

  public String getGlobalProperty(String k) throws ServerException {
    try {
      FileInputStream f = new FileInputStream(file("config"));
      Properties p = new Properties();
      p.load(f); f.close();
      return p.getProperty(k);
    } catch (IOException e) {
      throw UtilSrv.se("Can't read " + k + ".", e);
    }
  }

  public double getTotalPoints()
      throws ServerException
  {
    try {
      // TODO(rgrig): Fall back to the sum of all pb points.
      return Double.parseDouble(getGlobalProperty("totalpoints"));
    } catch (Exception e) {
      throw UtilSrv.se("Can't read totalpoints.", e);
    }
  }

  public long getScoreFreeze()
      throws ServerException
  {
    String s = getGlobalProperty("scorefreeze");
    if (s == null) return Long.MAX_VALUE;
    else return parseDate(s);
  }

  public void recordPbSubmission(PbSubmission submission)
      throws ServerException
  {
    PrintWriter pw = null;
    try {
      pw = new PrintWriter(new FileWriter(file("scores", "problems"), true));
      pw.printf("%s %s %f %x\n",
          submission.pseudonym(),
          submission.problem(),
          submission.points(),
          submission.time());
      pw.flush();
      pw.close();
    } catch (Throwable t) {
      throw UtilSrv.se("Cannot record problem submission.", t);
    }
  }

  public List<PbSubmission> getPbSubmissions(PbSubmission query)
      throws ServerException
  {
    // Check that unused fields are unused.
    assert (query.points() < 0.0);
    assert (query.time() < 0);

    BufferedReader br = null;
    List<PbSubmission> result = new ArrayList<PbSubmission>();
    try {
      br = new BufferedReader(new FileReader(file("scores", "problems")));
      String line;
      while ((line = br.readLine()) != null) {
        Scanner scan = new Scanner(line);
        String pseudonym = scan.next();
        String problem = scan.next();
        double points = scan.nextDouble();
        long time = scan.nextLong(16);
        if (query.pseudonym() != null
            && !query.pseudonym().equals(pseudonym)) continue;
        if (query.problem() != null
            && !query.problem().equals(problem)) continue;
        result.add(new PbSubmission(pseudonym, problem, points, time));
      }
      br.close();
    } catch (FileNotFoundException e) {
      // That's fine: We just return an empty list.
    } catch (Throwable t) {
      throw UtilSrv.se("Cannot lookup problem submission.", t);
    }
    return result;
  }

  public boolean checkLogin(String pseudonym, String passwdHash) {
    try {
      Scanner s = new Scanner(file("accounts"));
      while (s.hasNext()) {
        String p = s.next();
        String ph = s.next();
        if (pseudonym.equals(p) && passwdHash.equals(ph)) return true;
      }
    } catch (IOException e) {}
    return false;
  }

  private PbTest[] getTestsHelper(File dir) throws IOException {
    ArrayList<PbTest> result = new ArrayList<PbTest>();
    for (File f : dir.listFiles()) {
      if (f.isDirectory()) continue;
      String name = f.getName();
      if (!name.endsWith(".out")) continue;
      name = name.substring(0, name.length() - 4);
      PbTest t = new PbTest();
      t.in = UtilSrv.readFile(new File(f.getParentFile(), name + ".in"));
      t.out = UtilSrv.readFile(f);
      result.add(t);
    }
    return result.toArray(new PbTest[0]);
  }

  private static long parseDate(String date) {
    int year = 2008, month = 0, day = 1, hour = 0, minute = 0;
    Scanner scan = new Scanner(date).useDelimiter("[^0-9]+");
    try {
      year = scan.nextInt();
      month = scan.nextInt() - 1;
      day = scan.nextInt();
      hour = scan.nextInt();
      minute = scan.nextInt();
    } catch (Exception e) {}
    Calendar c = Calendar.getInstance();
    c.clear();
    c.set(year, month, day, hour, minute);
    return c.getTimeInMillis();
  }

}

