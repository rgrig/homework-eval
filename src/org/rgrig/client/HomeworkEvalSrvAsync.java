package org.rgrig.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface HomeworkEvalSrvAsync {
  public void getProblems(AsyncCallback callback);

  public void getLanguages(AsyncCallback callback);

  public void login(String pseudonym, String passwd, AsyncCallback callback);
  public void logout(AsyncCallback callback);

  public void judgeProblem(
    String problem,
    String language,
    String solution,
    AsyncCallback callback);

  public void getScores(AsyncCallback callback);
  public void pointsScale(AsyncCallback callback);
}

