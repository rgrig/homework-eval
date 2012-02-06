package org.rgrig.server;

import java.util.HashSet;

import org.rgrig.client.ServerException;

public class PbProperties {
  private static HashSet<String> knownScoringMethods;
  static {
    knownScoringMethods = new HashSet<String>();
    knownScoringMethods.add("proportional");
    knownScoringMethods.add("binary");
  }

  private String name;
  private double penalty;
  private double points;
  private int memoryLimit;
  private int timeLimit;
  private long deadline;
  private long start;
  private String scoringMethod;

  public String name() { return name; }
  public double penalty() { return penalty; }
  public double points() { return points; }
  public int memoryLimit() { return memoryLimit; }
  public int timeLimit() { return timeLimit; }
  public long deadline() { return deadline; }
  public long start() { return start; }
  public String scoringMethod() { return scoringMethod; }

  private PbProperties(
      String name,
      double penalty,
      double points,
      int memoryLimit,
      int timeLimit,
      long deadline,
      long start,
      String scoringMethod
  ) {
    this.name = name;
    this.penalty = penalty;
    this.points = points;
    this.memoryLimit = memoryLimit;
    this.timeLimit = timeLimit;
    this.deadline = deadline;
    this.start = start;
    this.scoringMethod = scoringMethod;
  }

  public static PbProperties empty() {
    return new PbProperties(null, -1.0, -1.0, -1, -1, -1l, -1l, "");
  }

  public PbProperties check() throws ServerException {
    if (name == null)
      throw new ServerException("wrong PbProperties.name");
    if (penalty < 0.0)
      throw new ServerException("wrong PbProperties.penalty");
    if (points <= 0.0)
      throw new ServerException("wrong PbProperties.points");
    if (memoryLimit <= 0)
      throw new ServerException("wrong PbProperties.memoryLimit");
    if (timeLimit <= 0)
      throw new ServerException("wrong PbProperties.timeLimit");
    if (deadline <= start)
      throw new ServerException("wrong PbProperties.deadline");
    if (start <= 0)
      throw new ServerException("wrong PbProperties.start");
    if (!knownScoringMethods.contains(scoringMethod))
      throw new ServerException("wrong PbProperties.scoringMethod");
    return this;
  }

  public PbProperties withName(String name) { return new PbProperties(name, penalty, points, memoryLimit, timeLimit, deadline, start, scoringMethod); }
  public PbProperties withPenalty(double penalty) { return new PbProperties(name, penalty, points, memoryLimit, timeLimit, deadline, start, scoringMethod); }
  public PbProperties withPoints(double points) { return new PbProperties(name, penalty, points, memoryLimit, timeLimit, deadline, start, scoringMethod); }
  public PbProperties withMemoryLimit(int memoryLimit) { return new PbProperties(name, penalty, points, memoryLimit, timeLimit, deadline, start, scoringMethod); }
  public PbProperties withTimeLimit(int timeLimit) { return new PbProperties(name, penalty, points, memoryLimit, timeLimit, deadline, start, scoringMethod); }
  public PbProperties withDeadline(long deadline) { return new PbProperties(name, penalty, points, memoryLimit, timeLimit, deadline, start, scoringMethod); }
  public PbProperties withStart(long start) { return new PbProperties(name, penalty, points, memoryLimit, timeLimit, deadline, start, scoringMethod); }
  public PbProperties withScoringMethod(String scoringMethod) { return new PbProperties(name, penalty, points, memoryLimit, timeLimit, deadline, start, scoringMethod); }
}
