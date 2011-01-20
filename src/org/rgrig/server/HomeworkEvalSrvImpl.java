package org.rgrig.server;

import com.google.gwt.user.server.rpc.*;
import org.rgrig.client.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.concurrent.locks.*;
import javax.servlet.http.*;

/**
 * Evaluates quiz and problem solutions.
 *
 * This delegates to {@code Judge} the compile/run/judge cycle.
 * This delegates to {@code Database} all interactions with a
 * database.
 */
public class HomeworkEvalSrvImpl 
  extends RemoteServiceServlet implements HomeworkEvalSrv {

  public static Logger log = Logger.getLogger("org.rgrig.server");
  public static String DATABASE = "/home/web-eval";

  public Database db;
  public Judge judge;

  public HomeworkEvalSrvImpl() {
    try {
      FileHandler logh = new FileHandler(
        DATABASE+"/log/main"+String.format("%x", System.currentTimeMillis()));
      logh.setFormatter(new SimpleFormatter());
      log.addHandler(logh);
      log.setUseParentHandlers(true);
      //log.setLevel(Level.WARNING); // for release
      log.setLevel(Level.ALL); // for debug
      PrintWriter pw = new PrintWriter(DATABASE+"/log/system");
      System.getProperties().list(pw);
      pw.flush();

      db = new FileDatabase(DATABASE);
      judge = new Judge("jailrun", "/home/jailrun");
    } catch (IOException a) {
      db = null;
      judge = null;
    }
  }

  private void fillInScores(List<Task> ts) throws ServerException {
    String p = getPseudonym();
    for (Task t : ts) {
      t.score = db.getScore(p, t.id);
      if (t.score >= 0.0) t.tried = true;
    }
  }

  /* TODO: the duplicated code in the following two functions is ugly */
  private ArrayList<Task> keepActive(Task[] ts) {
    ArrayList<Task> r = new ArrayList<Task>();
    Calendar now = Calendar.getInstance();
    Calendar start = Calendar.getInstance();
    Calendar deadline = Calendar.getInstance();
    for (Task t : ts) {
      parseDate(start, t.start);
      parseDate(deadline, t.deadline);
      if (start.after(now) || now.after(deadline)) continue;
      t.secondsToDeadline = (int)
        ((deadline.getTimeInMillis() - now.getTimeInMillis()) / 1000l);
      r.add(t);
    }
    return r;
  }

  private ArrayList<Task> keepSeen(Task[] ts) {
    ArrayList<Task> r = new ArrayList<Task>();
    Calendar now = Calendar.getInstance();
    Calendar start = Calendar.getInstance();
    for (Task t : ts) {
      parseDate(start, t.start);
      if (start.after(now)) continue;
      r.add(t);
    }
    return r;
  }

  public Problem[] getProblems() throws ServerException {
    ArrayList<Task> pbs = keepActive(db.getProblems());
    fillInScores(pbs);
    return pbs.toArray(new Problem[0]);
  }

  public Quiz[] getQuizzes() throws ServerException {
    ArrayList<Task> qs = keepActive(db.getQuizzes());
    fillInScores(qs);
    return qs.toArray(new Quiz[0]);
  }

  public String[] getLanguages() throws ServerException {
    return db.getLanguages();
  }

  public String login(String pseudonym, String passwd) 
  throws ServerException {
    try {
      String id = db.checkLogin(pseudonym, UtilSrv.sha(passwd));
      if (id == null) return null;
      HttpSession s = getThreadLocalRequest().getSession();
      s.setAttribute("pseudonym", pseudonym);
      log.info("login " + pseudonym);
      return id;
    } catch (java.security.NoSuchAlgorithmException e) {
      throw UtilSrv.se("Can't verify your login (A)", e);
    } catch (java.io.UnsupportedEncodingException e) {
      throw UtilSrv.se("Can't verify your login (B)", e);
    }
  }

  public void logout() {
    HttpSession s = getThreadLocalRequest().getSession();
    s.setAttribute("pseudonym", "guest");
  }

  public PbEval judgeProblem(
    String problem,
    String langId,
    String solution
  ) throws ServerException {
    try {
      lock();
      PbEval r = new PbEval();
      String pseudonym = getPseudonym();
      if (pseudonym == null) {
        r.score = -1.0;
        return r;
      }
      String solhash = String.format("%x", System.currentTimeMillis());
      try {
        File sollog = new File(DATABASE+"/log/"+solhash);
        FileWriter solwriter = new FileWriter(sollog);
        solwriter.write(solution);
        solwriter.close();
      } catch (IOException e) {}
      log.info("judge:" 
        + " pseudonym=" + pseudonym
        + " problem=" + problem
        + " lang=" + langId
        + " solFile=" + solhash);

      Language lang = db.getLanguage(langId);
      judge.prepare(problem, lang);
      if (judge.compile(solution)) {
        r.compiled = true;
        PbTest[] examples = db.getProblemExamples(problem);
        PbTest[] tests = db.getProblemTests(problem);
        PbLimit pl = db.getProblemLimits(problem);

        int okExamples=0, okTests=0;
        okExamples = judge.run(examples, pl.timelimit, pl.memlimit);
        r.exampleOut = judge.getOut();
        r.exampleErr = judge.getErr();
        if (okExamples != examples.length) r.score = 0.0;
        else {
          okTests = judge.run(tests, pl.timelimit, pl.memlimit);
          r.score = (double) okTests * db.getScore(problem) / tests.length;
        }
      }
      db.setScore(pseudonym, problem, r.score);
      return r;
    } finally {
      unlock();
    }
  }

  public double judgeQuiz(
    String quiz,
    String solution
  ) throws ServerException {
    try {
      lock();
      int correct = 0;
      String pseudonym = getPseudonym();
      if (pseudonym == null) return -1.0;
      log.info("judge:"
        + " pseudonym=" + pseudonym
        + " quiz=" + quiz
        + " solution=" + solution);
      if (db.getScore(pseudonym, quiz) >= 0) return -1.0;
      String reference = db.getQuizAnswer(quiz);
      if (reference == null || reference.length() != solution.length())
        throw new ServerException("Quiz judging failed: " + reference.length() + ":" + solution.length());
      for (int i = 0; i < reference.length(); ++i)
        if (reference.charAt(i) == solution.charAt(i)) ++correct;
      double score = (double) correct * db.getScore(quiz) / reference.length();
      db.setScore(pseudonym, quiz, score);
      return score;
    } finally {
      unlock();
    }
  }

  public User[] getScores() throws ServerException {
    return db.getScores();
  }

  public double scoreScale() throws ServerException {
    ArrayList<Task> ts = keepSeen(db.getQuizzes());
    ts.addAll(keepSeen(db.getProblems()));
    double soFar = 0.0;
    for (Task t : ts) soFar += t.totalScore;
    return db.getTotalScore() / soFar;
  }

  private String getPseudonym() {
    HttpSession s = getThreadLocalRequest().getSession();
    String p = (String)s.getAttribute("pseudonym");
    return p;
  }

  private static volatile ReentrantLock L = new ReentrantLock();

  private void lock() throws ServerException {
    if (L.getQueueLength() >= 3)
      throw new ServerException("Server busy. Try later.");
    L.lock();
    // TODO: perhaps acquire a file lock here
  }

  private void unlock() {
    // TODO: release the file lock here, if it gets acquired
    L.unlock();
  }

  private static void parseDate(Calendar c, String date) {
    int year = 2008, month = 0, day = 1, hour = 0, minute = 0;
    Scanner s = new Scanner(date).useDelimiter("[^0-9]+");
    try {
      year = s.nextInt();
      month = s.nextInt() - 1;
      day = s.nextInt();
      hour = s.nextInt();
      minute = s.nextInt();
    } catch (Exception e) {}
    c.clear();
    c.set(year, month, day, hour, minute);
  }
}

