package org.rgrig.server;

import java.util.*;
import org.rgrig.client.*;

/**
 * Knows how to parse the database info.
 */
public interface Database {
  public Quiz[] getQuizzes() throws ServerException;
  public Problem[] getProblems() throws ServerException;
  public String getQuizAnswer(String quizId) throws ServerException;
  public PbTest[] getProblemExamples(String pbId) throws ServerException;
  public PbTest[] getProblemTests(String pbId) throws ServerException;
  public PbLimit getProblemLimits(String pbId) throws ServerException;

  public String[] getLanguages() throws ServerException;
  public Language getLanguage(String id) throws ServerException;

  public double getTotalScore()
    throws ServerException;
  public double getScore(String task) 
    throws ServerException;
  public double getScore(String pseudo, String task)
    throws ServerException;
  public User[] getScores()
    throws ServerException;
  public void setScore(String pseudo, String task, double score)
    throws ServerException;

  /** Returns the student ID, or null if the check fails. */
  public String checkLogin(String pseudonym, String passwdHash) 
    throws ServerException;
}
