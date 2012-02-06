package org.rgrig.server;

import java.util.*;
import org.rgrig.client.*;

/**
 * Knows how to parse the database info.
 */
public interface Database {
  public Problem[] getProblems() throws ServerException;
  public PbTest[] getProblemExamples(String pbId) throws ServerException;
  public PbTest[] getProblemTests(String pbId) throws ServerException;
  public PbProperties getProblemProperties(String pbId) throws ServerException;

  public String[] getLanguages() throws ServerException;
  public Language getLanguage(String id) throws ServerException;

  public double getTotalPoints() throws ServerException;
  public long getScoreFreeze() throws ServerException;

  public void recordPbSubmission(PbSubmission submission)
    throws ServerException;
  public List<PbSubmission> getPbSubmissions(PbSubmission query)
    throws ServerException;

  public boolean checkLogin(String pseudonym, String passwdHash)
    throws ServerException;
}
