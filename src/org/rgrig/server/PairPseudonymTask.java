package org.rgrig.server;

public class PairPseudonymTask {
  private String pseudonym;
  private String task;

  public PairPseudonymTask(String pseudonym, String task) {
    assert pseudonym != null;
    assert task != null;
    this.pseudonym = pseudonym;
    this.task = task;
  }

  public String pseudonym() { return pseudonym; }
  public String task() { return task; }

  @Override public int hashCode() {
    return pseudonym.hashCode() + task.hashCode(); 
  }

  @Override public boolean equals(Object o) {
    if (!(o instanceof PairPseudonymTask)) return false;
    PairPseudonymTask ot = (PairPseudonymTask) o;
    return pseudonym.equals(ot.pseudonym) && task.equals(ot.task);
  }
}
