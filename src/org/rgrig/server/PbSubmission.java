package org.rgrig.server;

public class PbSubmission {
  private String pseudonym;
  private String problem;
  private double points;
  private long time;

  public PbSubmission(
      String pseudonym,
      String problem,
      double points,
      long time
  ) {
    this.pseudonym = pseudonym;
    this.problem = problem;
    this.points = points;
    this.time = time;
  }

  public static PbSubmission query(String pseudonym, String problem) {
    return new PbSubmission(pseudonym, problem, -1.0, -1l);
  }

  public String pseudonym() { return pseudonym; }
  public String problem() { return problem; }
  public double points() { return points; }
  public long time() { return time; }
}
