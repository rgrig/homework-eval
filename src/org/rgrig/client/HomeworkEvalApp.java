package org.rgrig.client;

import com.google.gwt.core.client.*;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.user.client.ui.*;

public class HomeworkEvalApp implements EntryPoint {
  public MainPanel mainPanel;
  public HomeworkEvalSrvAsync srv;

  // these are used for a hack in setupMainArea
  public int gotInfo;
  public Quiz[] quiz;
  public Problem[] problem;
  public String[] languages;

  public final TextBox pseudonym = new TextBox();
  public final PasswordTextBox password = new PasswordTextBox();

  public void onModuleLoad() {
    srv = GWT.create(HomeworkEvalSrv.class);
    // TODO Check that the server is ready (has database, answers our RPCs, ..)
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
    vp.add(new Button("Login", new ClickHandler() {
      public void onClick(ClickEvent event) {
        srv.login(pseudonym.getText(), password.getText(), new Aac() {
          public void onSuccess(Object result) {
            if ((Boolean) result) setupMainArea();
            else failed.setVisible(true);
          }
        });
      }
    }));
    vp.add(failed);
    ma.clear(); ma.add(vp);
    pseudonym.setFocus(true);
  }

  public void setupMainArea() {
    gotInfo = 0;

    srv.getQuizzes(new Aac() {
      public void onSuccess(Object result) {
        quiz = (Quiz[])result; ++gotInfo;
//Window.alert("got quizzes");
        if (gotInfo == 3) setupMainAreaHelper();
      }
    });
    srv.getProblems(new Aac() {
      public void onSuccess(Object result) {
//Window.alert("got problems");
        problem = (Problem[])result; ++gotInfo;
        if (gotInfo == 3) setupMainAreaHelper();
      }
    });
    srv.getLanguages(new Aac() {
      public void onSuccess(Object result) {
//Window.alert("got languages");
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
    vp.add(new Button("Logout", new ClickHandler() {
      public void onClick(ClickEvent event) {
        srv.logout(new Aac() {
          public void onSuccess(Object result) { setupLogin(); }
        });
      }
    }));
    mainPanel.selectTab(0);
  }
}

