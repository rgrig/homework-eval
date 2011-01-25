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

  /* TODO: Perhaps add some caching here once the debugging is done. */
  private String getProperty(String a, String b, String k) {
    try {
      FileInputStream fis1 = new FileInputStream(file("config"));
      FileInputStream fis2 = new FileInputStream(file(a,b,"config"));

      Properties defaults = new Properties();
      defaults.load(fis1);
      Properties p = new Properties(defaults);
      p.load(fis2);
      fis1.close(); fis2.close();

      return p.getProperty(k);
    } catch (IOException e) {
      return null;
    }
  }

  private String getPbProperty(String problem, String key) {
    return getProperty("problems", problem, key);
  }

  private String getQuizProperty(String quiz, String key) {
    return getProperty("quizzes", quiz, key);
  }

  public Quiz[] getQuizzes() throws ServerException {
    try {
      ArrayList<Quiz> result = new ArrayList<Quiz>();
      File[] quizzes = file("quizzes").listFiles();
      for (File qf : quizzes) {
        String id = qf.getName();
        if (!qf.isDirectory() || id.startsWith(".")) continue;
        Quiz q = new Quiz();
        q.start = getQuizProperty(id, "start");
        q.deadline = getQuizProperty(id, "deadline");
        q.id = id;
        q.name = getQuizProperty(id, "name");
        q.totalScore = Double.parseDouble(getQuizProperty(id, "score"));
        q.questions = parseQuizQuestions(qf);
        result.add(q);
      }
      return result.toArray(new Quiz[0]);
    } catch (IOException e) {
      throw UtilSrv.se("Can't parse quizzes.", e);
    }
  }

  // TODO: Get rid of the code duplication
  public Problem[] getProblems() throws ServerException {
    try {
      ArrayList<Problem> result = new ArrayList<Problem>();
      String ids = "";
      for (File pf : file("problems").listFiles()) {
        String id = pf.getName(); ids += ":" + id;
        if (!pf.isDirectory() || id.startsWith(".")) continue;

        // task stuff
        Problem p = new Problem();
        p.start = getPbProperty(id, "start");
        p.deadline = getPbProperty(id, "deadline");
        p.id = pf.getName();
        p.name = getPbProperty(p.id, "name");
        p.totalScore = Double.parseDouble(getPbProperty(p.id, "score"));
       
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

  public String getQuizAnswer(String quiz) throws ServerException {
    try {
      StringBuilder sb = new StringBuilder();
      File qq = file("quizzes", quiz, "questions");
      BufferedReader br = new BufferedReader(new FileReader(qq));
      String line = br.readLine();
      while (line != null) {
        while (line != null && !isQuizAnswer(line))
          line = br.readLine();
        char c = 'a';
        do {
          if (isQuizCorrectAnswer(line)) sb.append(c);
          line = br.readLine(); ++c;
        } while (isQuizAnswer(line));
        line = br.readLine();
      }
      return sb.toString();
    } catch (IOException e) {
      throw new ServerException("Can't read reference quiz answer for " + quiz);
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

  public PbLimit getProblemLimits(String pbId) throws ServerException {
    PbLimit l = new PbLimit();
    l.timelimit = Integer.parseInt(getPbProperty(pbId, "timelimit"));
    l.memlimit = Integer.parseInt(getPbProperty(pbId, "memlimit"));
    return l;
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

  public double getTotalScore() 
  throws ServerException {
    try {
      FileInputStream fis = new FileInputStream(file("config"));
      Properties p = new Properties();
      p.load(fis); fis.close();
      return Double.parseDouble(p.getProperty("totalscore"));
    } catch (Exception e) {
      throw UtilSrv.se("Can't read totalscore.", e);
    }
  }

  public double getScore(String task) 
  throws ServerException {
    String s = getQuizProperty(task, "score");
    if (s == null) s = getPbProperty(task, "score");
    try { return Double.parseDouble(s); }
    catch (Exception e) {
      throw new ServerException("Can't read total score for task " + task);
    }
  }

  public double getScore(String pseudo, String task) 
  throws ServerException {
    try {
      File pf = file("scores", pseudo);
      pf.createNewFile();
      Properties p = new Properties();
      FileInputStream fis = new FileInputStream(pf);
      p.load(fis); fis.close();
      String ss = p.getProperty(task);
      if (ss == null) return -1.0;
      return Double.parseDouble(ss);
    } catch (IOException e) {
      throw new ServerException("Can't read your score for task " + task);
    }
  }

  public User[] getScores() 
  throws ServerException {
    try {
      ArrayList<User> result = new ArrayList<User>();
      ArrayList<String> tasks = new ArrayList<String>();
      for (File q : file("quizzes").listFiles()) 
        if (q.isDirectory() && !q.getName().startsWith("."))
          tasks.add(q.getName());
      for (File p : file("problems").listFiles()) 
        if (p.isDirectory() && !p.getName().startsWith("."))
          tasks.add(p.getName());
      for (File u : file("scores").listFiles()) {
        if (u.isDirectory() || u.getName().startsWith(".")) continue;
        User uu = new User();
        uu.pseudonym = u.getName();
        Properties up = new Properties();
        up.load(new FileInputStream(u));
        for (String t : tasks) {
          String points = up.getProperty(t, "0.0");
          uu.score += Double.parseDouble(points);
        }
        result.add(uu);
      }
      return result.toArray(new User[0]);
    } catch (IOException e) {
      throw UtilSrv.se("Can't retrieve scores.", e);
    }
  }

  public void setScore(String pseudo, String task, double score) 
  throws ServerException {
    try {
      File pf = file("scores", pseudo);
      pf.createNewFile();
      Properties p = new Properties();
      FileInputStream fis = new FileInputStream(pf);
      p.load(fis); fis.close();
      p.setProperty(task, ""+score);
      FileOutputStream fos = new FileOutputStream(pf);
      p.store(fos, "no comment"); fos.close();
    } catch (IOException e) {
      throw new ServerException("Can't save your score!");
    }
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

  private QuizQuestion[] parseQuizQuestions(File qd) 
  throws ServerException {
    try {
      File q = new File(qd, "questions");
      BufferedReader br = new BufferedReader(new FileReader(q));
      ArrayList<QuizQuestion> result = new ArrayList<QuizQuestion>();
      ArrayList<String> answers = new ArrayList<String>();
      String line = br.readLine();
      while (line != null) {
        QuizQuestion lq = new QuizQuestion();
        lq.question = "";
        while (line != null && !isQuizAnswer(line)) {
          lq.question += "\n" + line;
          line = br.readLine();
        }
        answers.clear();
        while (line != null && isQuizAnswer(line)) {
          answers.add(line.substring(3));
          line = br.readLine();
        }
        lq.answers = answers.toArray(new String[0]);
        result.add(lq);
        while (line != null && "".equals(line)) line = br.readLine();
      }
      return result.toArray(new QuizQuestion[0]);
    } catch (IOException e) {
      throw new ServerException("Cannot read quiz " + qd.getName() + ".");
    }
  }

  private boolean isQuizAnswer(String line) {
    return line != null &&
      (line.startsWith(" * ") || line.startsWith(" - "));
  }

  private boolean isQuizCorrectAnswer(String line) {
    return line != null && line.startsWith(" * ");
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
}

