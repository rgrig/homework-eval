package org.rgrig.client;

import com.google.gwt.user.client.rpc.*;

/**
 * The interface between the client and the server.
 *
 * The client can ask for problems, can ask for the available languages, can log
 * in and out, can ask for a problem to be judged, and can ask for the current
 * scores (points and penalties).
 */
@RemoteServiceRelativePath("HomeworkEvalSrv")
public interface HomeworkEvalSrv extends RemoteService {
  /* Ask for (active) problems. */
  public Problem[] getProblems() throws ServerException;

  /* Returns the list of available languages. */
  public String[] getLanguages() throws ServerException;

  /* Functions for logging in and out. */
  public boolean login(String pseudonym, String passwd) throws ServerException;
  public void logout() throws ServerException;

  /* Judging functions. */
  public PbEval judgeProblem(
    String problem,
    String language,
    String solution) throws ServerException;

  /* Returns the user scores now. */
  public User[] getScores() throws ServerException;

  /* Returns by how much points should be mutliplied to get an estimate
   * of the final. */
  public double pointsScale() throws ServerException;
}
