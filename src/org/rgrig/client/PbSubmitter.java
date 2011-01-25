package org.rgrig.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.user.client.ui.*;

public class PbSubmitter implements ClickHandler {
  public HomeworkEvalSrvAsync srv;
  public Problem problem;
  public TextArea solution;
  public ListBox lang;
  public Label score;

  public PbSubmitter(
    HomeworkEvalSrvAsync srv,
    Problem problem,
    TextArea solution,
    ListBox lang,
    Label score
  ) {
    this.srv = srv;
    this.problem = problem;
    this.solution = solution;
    this.lang = lang;
    this.score = score;
  }

  public void onClick(ClickEvent event) {
    WaitPopup.Show();
    String l = lang.getItemText(lang.getSelectedIndex());
    if (solution.getText().length() > 20000) {
      Window.alert("Sorry, that solution is too big!");
      return;
    }
    srv.judgeProblem(problem.id, l, solution.getText(), new Aac() {
      public void onSuccess(Object result) {
        if (result == null) return; // TODO: logout
        PbEval eval = (PbEval)result;
        WaitPopup.Hide();
        problem.score = Math.max(eval.score, problem.score);
        score.setText(Util.pointsStr(problem.score, problem.totalScore));
        JudgeResultDialog.show(eval);
      }
      public void onFailure(Throwable caught) {
        super.onFailure(caught);
        WaitPopup.Hide();
      }
    });
  }
}
