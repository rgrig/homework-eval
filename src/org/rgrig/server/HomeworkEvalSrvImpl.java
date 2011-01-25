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

  // config stuff {{{
  public static String DATABASE = "/home/homework-eval-db";
  public static String JUDGE_USER = "jailrun";
  public static String JUDGE_HOME = "/home/" + JUDGE_USER;
  // }}}

  public static Logger log = Logger.getLogger("org.rgrig.server");

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
      judge = new Judge(JUDGE_USER, JUDGE_HOME);
    } catch (IOException a) {
      db = null;
      judge = null;
    }
  }

  /* TODO: the duplicated code in the following two functions is ugly */
  private ArrayList<Task> keepActive(Task[] ts) {
    ArrayList<Task> r = new ArrayList<Task>();
    long now = System.currentTimeMillis();
    for (Task t : ts) {
      if (t.start > now || now > t.deadline) continue;
      r.add(t);
    }
    return r;
  }

  private ArrayList<Task> keepSeen(Task[] ts) {
    ArrayList<Task> r = new ArrayList<Task>();
    long now = System.currentTimeMillis();
    for (Task t : ts) {
      if (t.start > now) continue;
      r.add(t);
    }
    return r;
  }

  public Problem[] getProblems() throws ServerException {
    ArrayList<Task> pbs = keepActive(db.getProblems());
    for (Task t : pbs) {
      Problem p = (Problem) t;
      List<PbSubmission> submissions = 
          db.getPbSubmissions(PbSubmission.query(getPseudonym(), p.id));
      p.score = -1.0;
      p.attempts = 0;
      int attempts = 0;
      for (PbSubmission s : submissions) {
        if (s.points() > p.score) {
          p.score = s.points();
          p.attempts = attempts;
        }
        ++attempts;
      }
    }
    return pbs.toArray(new Problem[0]);
  }

  public Quiz[] getQuizzes() throws ServerException {
    ArrayList<Task> qs = keepActive(db.getQuizzes());
    // TODO: fill in scores
    return qs.toArray(new Quiz[0]);
  }

  public String[] getLanguages() throws ServerException {
    return db.getLanguages();
  }

  public boolean login(String pseudonym, String passwd) 
  throws ServerException {
    try {
      if (db == null) throw new ServerException("No database.");
      boolean ok = db.checkLogin(pseudonym, UtilSrv.sha(passwd));
      if (ok) {
        HttpSession s = getThreadLocalRequest().getSession();
        s.setAttribute("pseudonym", pseudonym);
        s.setMaxInactiveInterval(-1);
        log.info("login " + pseudonym);
      }
      return ok;
    } catch (java.security.NoSuchAlgorithmException e) {
      throw UtilSrv.se("Can't verify your login (A)", e);
    } catch (java.io.UnsupportedEncodingException e) {
      throw UtilSrv.se("Can't verify your login (B)", e);
    } catch (NullPointerException e) {
      throw UtilSrv.se("Can't verify your login (C)", e);
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
      long now = System.currentTimeMillis();
      String solhash = String.format("%x", now);
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
        PbProperties pp = db.getProblemProperties(problem);

        int okExamples=0, okTests=0;
        okExamples = judge.run(examples, pp.timeLimit(), pp.memoryLimit());
        r.exampleOut = judge.getOut();
        r.exampleErr = judge.getErr();
        if (okExamples != examples.length) r.score = 0.0;
        else {
          okTests = judge.run(tests, pp.timeLimit(), pp.memoryLimit());
          r.score = (double) okTests * pp.score() / tests.length;
        }
      }
      db.recordPbSubmission(new PbSubmission(pseudonym, problem, r.score, now));
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
      List<QuizSubmission> previousSubmissions = 
        db.getQuizSubmissions(QuizSubmission.query(getPseudonym(), quiz));
      if (!previousSubmissions.isEmpty())
        throw new ServerException("You can submit quizzes only once.");
      String reference = db.getQuizAnswer(quiz);
      if (reference == null || reference.length() != solution.length())
        throw new ServerException("Quiz judging failed: " + reference.length() + ":" + solution.length());
      for (int i = 0; i < reference.length(); ++i)
        if (reference.charAt(i) == solution.charAt(i)) ++correct;
      double score = (double) correct * db.getScore(quiz) / reference.length();
      db.recordQuizSubmission(new QuizSubmission(
            getPseudonym(), quiz, score));
      return score;
    } finally {
      unlock();
    }
  }

  private static class ProblemAttemptData {
    public int count;
    public int attempts;
    public double points;
    public long time;
  }

  // TODO This code is HORRIBLE!
  public User[] getScores() throws ServerException {
    try {
      List<PbSubmission> problemSubmissions = 
          db.getPbSubmissions(PbSubmission.query(null, null));
      HashMap<PairPseudonymTask, ProblemAttemptData> acc =
          new HashMap<PairPseudonymTask, ProblemAttemptData>();
      for (PbSubmission submission : problemSubmissions) {
        PairPseudonymTask key = new PairPseudonymTask(
            submission.pseudonym(), submission.problem());
        ProblemAttemptData data = acc.get(key);
        if (data == null) {
          data = new ProblemAttemptData();
          data.points = -1.0;
          acc.put(key, data);
        }
        if (submission.points() > data.points) {
          data.points = submission.points();
          data.attempts = data.count;
          data.time = submission.time();
        }
        ++data.count;
      }
      HashMap<String, User> byPseudonym = new HashMap<String, User>();
      HashMap<String, PbProperties> pbProperties = 
          new HashMap<String, PbProperties>();
      for (Map.Entry<PairPseudonymTask, ProblemAttemptData> e : acc.entrySet()) {
        String pb = e.getKey().task();
        if (!pbProperties.containsKey(pb))
          pbProperties.put(pb, db.getProblemProperties(pb));
      }
      for (Map.Entry<PairPseudonymTask, ProblemAttemptData> e : acc.entrySet()) {
        User u = byPseudonym.get(e.getKey().pseudonym());
        if (u == null) {
          u = new User();
          u.pseudonym = e.getKey().pseudonym();
          byPseudonym.put(u.pseudonym, u);
        }
        if (e.getValue().points > 0.0) {
          u.score += e.getValue().points;
          u.penalty += 
              (e.getValue().time - pbProperties.get(e.getKey().task()).start())
              / 1000.0 / 60.0;
          u.penalty +=
            e.getValue().attempts 
            * pbProperties.get(e.getKey().task()).penalty();
        }
      }
      return byPseudonym.values().toArray(new User[0]);
    } catch (Throwable t) {
      throw UtilSrv.se("Failed to get scoreboard.", t);
    }
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
}

