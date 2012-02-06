package org.rgrig.server;

import org.rgrig.client.*;
import java.util.*;
import java.util.logging.*;
import java.io.*;

public class Judge {
  private static class StreamReader extends Thread {
    public String result;
    public IOException exception;
    private InputStreamReader source;

    public StreamReader(InputStream source) throws IOException {
      this.source = new InputStreamReader(source, "UTF-8");
    }

    public void run() {
      try {
        int c;
        StringBuilder sb = new StringBuilder();
        while ((c = source.read()) != -1) sb.append((char) c);
        result = sb.toString();
        source.close();
      } catch (IOException e) {
        exception = e;
      }
    }
  }


  public static Logger log = Logger.getLogger("org.rgrig.server");

  public String user;
  public File workDir;
  public List<String> cmdPrefix;

  /* for saving program output, line by line */
  private String stdout;
  private String stderr;
  private ArrayList<String> allStdout;
  private ArrayList<String> allStderr;

  private String srcFileName;
  private String compileCmd;
  private String executableName;
  private String runCmd;

  public Judge(String user, String pathPrefix) {
    this.user = user;
    cmdPrefix = new ArrayList<String>();
    cmdPrefix.add("sudo");
    cmdPrefix.add("-u");
    cmdPrefix.add(user);
    cmdPrefix.add("limit");
    workDir = new File(pathPrefix);
  }

  public void prepare(String pb, Language lang)
  throws ServerException {
    try {
    srcFileName = lang.saveName.replaceAll("PB", pb);
    compileCmd = lang.compileCmd.replaceAll("PB", pb);
    executableName = lang.executable.replaceAll("PB", pb);
    runCmd = lang.runCmd.replaceAll("PB", pb);
    exec("rm -rf *", "", 1, 10);
    } catch (Exception e) {
      UtilSrv.se("Can't cleanup for testing.", e);
    }
  }

  public boolean compile(String sol) throws ServerException {
    try {
      createSrcFile();
      File f = new File(workDir, srcFileName);
      FileWriter fw = new FileWriter(f);
      fw.write(sol);
      fw.close();
      exec(compileCmd, "", 2, 2048);
      File ef = new File(workDir, executableName);
      return ef.exists();
    } catch (Exception e) {
      throw UtilSrv.se("Can't compile.", e);
    }
  }

  public int run(PbTest[] tests, int timelimit, int memlimit)
  throws ServerException {
    allStdout = new ArrayList<String>();
    allStderr = new ArrayList<String>();
    int correct = 0;
    for (int i = 0; i < tests.length; ++i) {
      try {
        int rc = exec(runCmd, tests[i].in, timelimit, memlimit);
        allStdout.add(stdout);
        allStderr.add(stderr);
        if (!stderr.isEmpty()) {
log.fine("NOK, stderr: " + stderr);
          continue;
        }
        if (diff(stdout, tests[i].out)) continue;
log.fine("OK");
        ++correct;
      } catch (Exception e) {
        /* assume incorrect */
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();
        log.finer("exc stack: " + sw);
        Throwable t = e;
        while (t != null) {
          log.finer("exc: " + t);
          t = t.getCause();
        }
      }
    }
    return correct;
  }

  public String[] getOut() {
    return allStdout.toArray(new String[0]);
  }

  public String[] getErr() {
    return allStderr.toArray(new String[0]);
  }

  /**
   * Executes a command as {@code user}. Saves the stdout/stderr
   * in the corresponding fields and returns the return code of
   * the child process.
   *
   * @param cmd The command to execute
   * @param data The data to send to the stdin of the process
   * @param timelimt How many seconds the command can run
   * @param memlimit How many megabytes the command can use
   */
  private int exec(String cmdS, String data, int timelimit, int memlimit)
  throws Exception {
    ArrayList<String> cmd = new ArrayList<String>(cmdPrefix);
    cmd.add("-m"); cmd.add(""+memlimit);
    cmd.add("-c"); cmd.add(""+timelimit);
    cmd.add("-w"); cmd.add(""+(3*timelimit+1));
    cmd.add("-x"); cmd.add(cmdS);
    String tmp = "";
    for (String cs : cmd) tmp += " \"" + cs + "\"";
    log.fine("exec " + tmp);
    ProcessBuilder pb = new ProcessBuilder(cmd);
    pb.directory(workDir);
    Process p = pb.start();
    OutputStreamWriter writer =
      new OutputStreamWriter(p.getOutputStream());
    StreamReader rOut = new StreamReader(p.getInputStream());
    StreamReader rErr = new StreamReader(p.getErrorStream());
    rOut.start(); rErr.start();

    // TODO I think this may block for big tests. Check and Fix.
    // send and receive data "in parallel"
    writer.write(data);
    writer.flush(); writer.close();
    rOut.join(); rErr.join();
    stdout = rOut.result;
    stderr = rErr.result;
    if (rOut.exception != null) throw rOut.exception;
    if (rErr.exception != null) throw rErr.exception;

//log.finer("out: " + stdout);
//log.finer("err: " + stderr);
//log.finer("done exec");
    return p.waitFor();
  }

  private static final double EPS = 1e-6;
  private boolean diff(String a, String b)
  throws ServerException {
//log.fine("out1: " + a);
//log.fine("out2: " + b);
    Scanner sa = new Scanner(a);
    Scanner sb = new Scanner(b);
    while (sa.hasNext() && sb.hasNext()) {
      if (sa.hasNextDouble() || sb.hasNextDouble()) {
        if (!sa.hasNextDouble() || !sb.hasNextDouble()) return true;
        double da = sa.nextDouble();
        double db = sb.nextDouble();
        double d_abs = Math.abs(da-db);
        double d_rel = d_abs / Math.abs(db);
        if (!(d_abs < EPS || d_rel < EPS)) {
log.fine("NOK, " + da + " too far from " + db);
          return true;
        }
      } else {
        String xa = sa.next();
        String xb = sb.next();
        if (!xa.equals(xb)) {
log.fine("NOK, " + xa + " != " + xb);
          return true;
        }
      }
    }
    if (sa.hasNext() || sb.hasNext()) {
log.fine("NOK: different number of tokens.");
      return true;
    }
    return false;
  }

  private void createSrcFile() throws ServerException {
    try {
      exec("touch " + srcFileName, "", 1, 10);
      exec("chmod 666 " + srcFileName, "", 1, 10);
    } catch (Exception e) {
      UtilSrv.se("Can't save submission.", e);
    }
  }
}
