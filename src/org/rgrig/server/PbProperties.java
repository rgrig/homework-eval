package org.rgrig.server;

import org.rgrig.client.ServerException;

public class PbProperties {
  private String name;
  private double penalty;
  private double score; // points allocated to this pb; TODO rename points
  private int memoryLimit;
  private int timeLimit;
  private long deadline;
  private long start;

  public String name() { return name; }
  public double penalty() { return penalty; }
  public double score() { return score; }
  public int memoryLimit() { return memoryLimit; }
  public int timeLimit() { return timeLimit; }
  public long deadline() { return deadline; }
  public long start() { return start; }

  private PbProperties(
      String name,
      double penalty,
      double score,
      int memoryLimit,
      int timeLimit,
      long deadline,
      long start
  ) {
    this.name = name;
    this.penalty = penalty;
    this.score = score;
    this.memoryLimit = memoryLimit;
    this.timeLimit = timeLimit;
    this.deadline = deadline;
    this.start = start;
  }

  public static PbProperties empty() {
    return new PbProperties(null, -1.0, -1.0, -1, -1, -1l, -1l);
  }

  public PbProperties check() throws ServerException {
    if (name == null) 
      throw new ServerException("wrong PbProperties.name");
    if (penalty < 0.0) 
      throw new ServerException("wrong PbProperties.penalty");
    if (score <= 0.0) 
      throw new ServerException("wrong PbProperties.score");
    if (memoryLimit <= 0) 
      throw new ServerException("wrong PbProperties.memoryLimit");
    if (timeLimit <= 0)
      throw new ServerException("wrong PbProperties.timeLimit");
    if (deadline <= start)
      throw new ServerException("wrong PbProperties.deadline");
    if (start <= 0)
      throw new ServerException("wrong PbProperties.start");
    return this;
  }

  public PbProperties withName(String name) { return new PbProperties(name, penalty, score, memoryLimit, timeLimit, deadline, start); }
  public PbProperties withPenalty(double penalty) { return new PbProperties(name, penalty, score, memoryLimit, timeLimit, deadline, start); }
  public PbProperties withScore(double score) { return new PbProperties(name, penalty, score, memoryLimit, timeLimit, deadline, start); }
  public PbProperties withMemoryLimit(int memoryLimit) { return new PbProperties(name, penalty, score, memoryLimit, timeLimit, deadline, start); }
  public PbProperties withTimeLimit(int timeLimit) { return new PbProperties(name, penalty, score, memoryLimit, timeLimit, deadline, start); }
  public PbProperties withDeadline(long deadline) { return new PbProperties(name, penalty, score, memoryLimit, timeLimit, deadline, start); }
  public PbProperties withStart(long start) { return new PbProperties(name, penalty, score, memoryLimit, timeLimit, deadline, start); }
}
