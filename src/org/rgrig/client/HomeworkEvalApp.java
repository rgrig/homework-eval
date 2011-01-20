package org.rgrig.client;

import com.google.gwt.core.client.*;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.rpc.*;

public class HomeworkEvalApp implements EntryPoint {
  public MainPanel mainPanel;
  public HomeworkEvalSrvAsync srv;

  // these are used for a hack in setupMainArea
  public int gotInfo;
  public Quiz[] quiz;
  public Problem[] problem;
  public String[] languages;

  public String studentId;
  public final TextBox pseudonym = new TextBox();
  public final PasswordTextBox password = new PasswordTextBox();

  public void onModuleLoad() {
    // Prepare for RPC calls 
    srv = (HomeworkEvalSrvAsync)GWT.create(HomeworkEvalSrv.class);
    ServiceDefTarget endpoint = (ServiceDefTarget)srv;
    String moduleRelativeUrl = GWT.getModuleBaseURL() + "HomeworkEvalSrv";
    endpoint.setServiceEntryPoint(moduleRelativeUrl);

    setupLogin();
  }

  public void setupLogin() {
    pseudonym.setText("");
    password.setText("");

    VerticalPanel vp = new VerticalPanel();
    Panel ma = RootPanel.get("mainarea");
    HorizontalPanel hp1 = new HorizontalPanel();
    hp1.add(new Label("Pseudonym:"));
    hp1.add(pseudonym);
    vp.add(hp1);
    HorizontalPanel hp2 = new HorizontalPanel();
    hp2.add(new Label("Password:"));
    hp2.add(password);
    vp.add(hp2);
    final Label failed = new Label("Login failed!");
    failed.setVisible(false);
    vp.add(new Button("Login", new ClickListener() {
      public void onClick(Widget sender) {
        srv.login(pseudonym.getText(), password.getText(), new Aac() {
          public void onSuccess(Object result) {
            studentId = (String)result;
            if (studentId != null) setupMainArea();
            else failed.setVisible(true);
          }
        });
      }
    }));
    vp.add(failed);
    ma.clear(); ma.add(vp);
  }

  public void setupMainArea() {
    gotInfo = 0;

    srv.getQuizzes(new Aac() {
      public void onSuccess(Object result) {
        quiz = (Quiz[])result; ++gotInfo;
        if (gotInfo == 3) setupMainAreaHelper();
      }
    });
    srv.getProblems(new Aac() {
      public void onSuccess(Object result) {
        problem = (Problem[])result; ++gotInfo;
        if (gotInfo == 3) setupMainAreaHelper();
      }
    });
    srv.getLanguages(new Aac() {
      public void onSuccess(Object result) {
        languages = (String[])result; ++gotInfo;
        if (gotInfo == 3) setupMainAreaHelper();
      }
    });
  }

  private void setupMainAreaHelper() {
    Panel ma = RootPanel.get("mainarea");
    VerticalPanel vp = new VerticalPanel();
    ma.clear(); ma.add(vp);

    mainPanel = new MainPanel(quiz, problem, languages, this);
    vp.add(mainPanel);
    vp.add(new HTML("<hr />"));
    vp.add(new Button("Logout", new ClickListener() {
      public void onClick(Widget sender) {
        srv.logout(new Aac() {
          public void onSuccess(Object result) { setupLogin(); }
        });
      }
    }));
    mainPanel.selectTab(0);
  }
}

