package org.rgrig.server;

public class QuizSubmission {
  private String pseudonym;
  private String quiz;
  private double score;

  public QuizSubmission(String pseudonym, String quiz, double score) {
    this.pseudonym = pseudonym;
    this.quiz = quiz;
    this.score = score;
  }

  public static QuizSubmission query(String pseudonym, String quiz) {
    return new QuizSubmission(pseudonym, quiz, -1.0);
  }

  public String pseudonym() { return pseudonym; }
  public String quiz() { return quiz; }
  public double score() { return score; }
}
