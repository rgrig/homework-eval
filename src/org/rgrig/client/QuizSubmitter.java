package org.rgrig.client;

import com.google.gwt.user.client.*;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.user.client.ui.*;

public class QuizSubmitter implements ClickListener {
  public HomeworkEvalSrvAsync srv;
  public Quiz quiz;
  public Panel panel;
  public RadioButton[][] answers;
  public TextBox pseudonym;

  public QuizSubmitter(
    HomeworkEvalSrvAsync srv,
    Quiz quiz, 
    Panel panel,
    RadioButton[][] answers, 
    TextBox pseudonym
  ) {
    this.srv = srv;
    this.quiz = quiz;
    this.panel = panel;
    this.answers = answers;
    this.pseudonym = pseudonym;
  }

  public void onClick(Widget sender) {
    WaitPopup.Show();
    String s = "";
    for (int i = 0; i < answers.length; ++i) {
      char n = ' ';
      for (int j = 0; j < answers[i].length; ++j)
        if (answers[i][j].isChecked()) n = (char)((int)'a' + j);
      s += n;
    }
    srv.judgeQuiz(quiz.id, s, new Aac() {
      public void onSuccess(Object result) {
        quiz.score = ((Double)result).doubleValue();
        if (quiz.score >= 0.0) {
          panel.clear();
          panel.add(new Label(Util.pointsStr(quiz.score, quiz.totalScore)));
        } else {
          Window.alert("You (" + pseudonym.getText() + ") are not"
            + " allowed to submit.");
        }
        WaitPopup.Hide();
      }
      public void onFailure(Throwable caught) {
        super.onFailure(caught);
        WaitPopup.Hide();
      }
    });
  }
}
