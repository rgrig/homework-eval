package org.rgrig.client;

import com.google.gwt.user.client.rpc.*;

public interface WebEvalSrvAsync {
  public void getProblems(AsyncCallback callback);
  public void getQuizzes(AsyncCallback callback);

  public void getLanguages(AsyncCallback callback);

  public void login(String pseudonym, String passwd, AsyncCallback callback);
  public void logout(AsyncCallback callback);

  public void judgeProblem(
    String problem,
    String language, 
    String solution, 
    AsyncCallback callback);
  public void judgeQuiz(
    String quiz,
    String solution, 
    AsyncCallback callback);

  public void getScores(AsyncCallback callback);
  public void scoreScale(AsyncCallback callback);
}

