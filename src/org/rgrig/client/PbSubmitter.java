package org.rgrig.client;

import com.google.gwt.user.client.*;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.user.client.ui.*;

public class PbSubmitter implements ClickListener {
  public WebEvalSrvAsync srv;
  public Problem problem;
  public TextArea solution;
  public ListBox lang;
  public Label score;

  public PbSubmitter(
    WebEvalSrvAsync srv,
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

  public void onClick(Widget sender) {
    WaitPopup.Show();
    String l = lang.getItemText(lang.getSelectedIndex());
    if (solution.getText().length() > 20000) {
      Window.alert("Sorry, that solution is too big!");
      return;
    }
    srv.judgeProblem(problem.id, l, solution.getText(), new Aac() {
      public void onSuccess(Object result) {
        PbEval eval = (PbEval)result;
        WaitPopup.Hide();
        problem.score = eval.score;
        score.setText(Util.pointsStr(problem.score, problem.totalScore));
        if (!eval.compiled) {
          Window.alert("The code does not compile.\n" + 
            "Please try to compile it locally before submitting.");
          return;
        }
        if (problem.score >= 0.0)
          ExamplesDialog.Show(eval.exampleOut, eval.exampleErr);
      }
      public void onFailure(Throwable caught) {
        super.onFailure(caught);
        WaitPopup.Hide();
      }
    });
  }
}
