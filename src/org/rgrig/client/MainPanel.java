package org.rgrig.client;

import com.google.gwt.user.client.*;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.user.client.ui.*;
import java.util.Arrays;

public class MainPanel extends TabPanel {
  public WebEvalApp app;
  public WebEvalSrvAsync srv;

  public Quiz[] quiz;
  public Problem[] problem;
  public String[] languages;
  
  public VerticalPanel[] quizPanel;
  public VerticalPanel[] pbPanel;
  public Panel scoresPanel;

  public Label[] quizDeadline;
  public Label[] pbDeadline;
  public Label[] pbScore;

  public RadioButton[][][] quizAnswer;
  public TextArea[] pbSolution;
  public ListBox[] pbLang;

  public MainPanel(
    Quiz[] quiz, 
    Problem[] problem, 
    String[] languages,
    WebEvalApp app
  ) {
    this.quiz = quiz;
    this.problem = problem;
    this.languages = languages;
    this.app = app;
    srv = app.srv;
    setWidth("600px");
    Arrays.sort(languages);

    quizPanel = new VerticalPanel[quiz.length];
    quizDeadline = new Label[quiz.length];
    quizAnswer = new RadioButton[quiz.length][][];

    pbPanel = new VerticalPanel[problem.length];
    pbDeadline = new Label[problem.length];
    pbScore = new Label[problem.length];
    pbSolution = new TextArea[problem.length];
    pbLang = new ListBox[problem.length];

    scoresPanel = new VerticalPanel();

    for (int i = 0; i < quiz.length; ++i) addQuiz(i);
    for (int i = 0; i < problem.length; ++i) addPb(i);
    addScores();
  }

  private void addQuiz(int idx) {
    Quiz q = quiz[idx];
    Panel p = quizPanel[idx] = new VerticalPanel();
    if (!q.tried) {
      p.add(new Label(Util.pointsStr(0.0, q.totalScore)));
      Label d = quizDeadline[idx] = new Label();
      DeadlineTimer dt = new DeadlineTimer(q.secondsToDeadline / 60, d, p);
      dt.schedule((q.secondsToDeadline % 60 * 1000)|1);
      p.add(d);
      p.add(new HTML("<hr/>"));

      RadioButton[][] allAnswers = 
        quizAnswer[idx] = new RadioButton[q.questions.length][];
      for (int i = 0; i < q.questions.length; ++i) {
        QuizQuestion qq = q.questions[i];
        p.add(new HTML(qq.question));
        RadioButton[] answers = 
          allAnswers[i] = new RadioButton[qq.answers.length];
        for (int j = 0; j < qq.answers.length; ++j) {
          answers[j] = new RadioButton("q_"+q.id+"_"+i, qq.answers[j], true);
          p.add(answers[j]);
        }
        p.add(new HTML("&nbsp;"));
      }

      p.add(new Button("Submit", 
        new QuizSubmitter(srv, q, p, allAnswers, app.pseudonym)));
    } else {
      p.add(new Label(Util.pointsStr(q.score, q.totalScore)));
    }
    add(p, q.name);
  }

  // TODO: Eliminate code duplication
  private void addPb(int idx) {
    Problem pb = problem[idx];
    Panel p = pbPanel[idx] = new VerticalPanel();

    final Label scoreL = new Label(Util.pointsStr(pb.score, pb.totalScore));
    p.add(scoreL);
    Label d = pbDeadline[idx] = new Label();
    DeadlineTimer dt = new DeadlineTimer(pb.secondsToDeadline / 60, d, p);
    dt.schedule((pb.secondsToDeadline % 60 * 1000)|1);
    p.add(d);

    TextArea ta = pbSolution[idx] = new TextArea();
    ta.setCharacterWidth(80);
    ta.setVisibleLines(20);
    p.add(ta);

    HorizontalPanel submitP = new HorizontalPanel();
    submitP.add(new Label("Language:"));
    ListBox pl = pbLang[idx] = new ListBox();
    pl.setVisibleItemCount(1);
    for (int i = 0; i < languages.length; ++i) pl.addItem(languages[i]);
    submitP.add(pl);
    submitP.add(new Button("Submit",
      new PbSubmitter(srv, pb, ta, pl, scoreL)));
    p.add(submitP);

    p.add(new HTML(pb.statement));
    p.add(new HTML("<br/><b>Examples</b>"));

    FlexTable examples = new FlexTable();
    HTMLTable.RowFormatter rf = examples.getRowFormatter();
    HTMLTable.CellFormatter cf = examples.getCellFormatter();
    examples.setText(0,0,"Input"); examples.setText(0,1,"Output");
    cf.setStyleName(0, 0, "gwt-FlexTable-cell-header");
    cf.setStyleName(0, 1, "gwt-FlexTable-cell-header");
    for (int i = 0; i < pb.examples.length; ++i) {
      PbTest ex = pb.examples[i];
      examples.setWidget(i+1, 0, new HTML("<pre>"+ex.in+"</pre>"));
      examples.setWidget(i+1, 1, new HTML("<pre>"+ex.out+"</pre>"));
      cf.setStyleName(i+1,0,"gwt-FlexTable-cell");
      cf.setStyleName(i+1,1,"gwt-FlexTable-cell");
      rf.setStyleName(i+1,"gwt-FlexTable-row-normal");
    }
    FlowPanel fp = new FlowPanel(); fp.add(examples);
    p.add(fp);

    add(p, pb.name);
  }

  private void addScores() {
    add(scoresPanel, "Scores");
    scoresPanel.clear();
    final FlexTable table = new FlexTable();
    table.addStyleName("gwt-FlexTable");
    updateScores(table);

    scoresPanel.add(table);
    scoresPanel.add(new Button("Update", new ClickListener() {
      public void onClick(Widget sender) { updateScores(table); }
    }));

  }

  private void updateScores(final FlexTable t) {
    final HTMLTable.RowFormatter rf = t.getRowFormatter();
    final HTMLTable.CellFormatter cf = t.getCellFormatter();
    t.setText(0,0,"Pseudonym"); 
    t.setText(0,1,"Score"); 
    t.setText(0,2,"Final (est.)");
    cf.setStyleName(0, 0, "gwt-FlexTable-cell-header");
    cf.setStyleName(0, 1, "gwt-FlexTable-cell-header");
    cf.setStyleName(0, 2, "gwt-FlexTable-cell-header");

    srv.scoreScale(new Aac() {
      public void onSuccess(Object result) {
        final double scale = ((Double)result).doubleValue();
        srv.getScores(new Aac() {
          public void onSuccess(Object result) {
            User[] users = (User[]) result;
            Arrays.sort(users);
            for (int i = 0; i < users.length; ++i) {
              t.setText(i+1,0,
                users[i].pseudonym);
              t.setText(i+1,1,format(1,users[i].score));
              t.setText(i+1,2,format(0,scale*users[i].score));
              cf.setStyleName(i+1,0,"gwt-FlexTable-cell");
              cf.setStyleName(i+1,1,"gwt-FlexTable-cell");
              cf.setStyleName(i+1,2,"gwt-FlexTable-cell");
              if (app.pseudonym.getText().equals(users[i].pseudonym))
                rf.setStyleName(i+1, "gwt-FlexTable-row-selected");
              else
                rf.setStyleName(i+1, "gwt-FlexTable-row-normal");
            }
            while (t.getRowCount() > users.length + 1)
              t.removeRow(users.length + 1);
          }
        });
      }
    });
  }

  /* TODO this is completely brain-dead, fix */
  private static String format(int precision, double number) {
    int i;
    for (i = 0; i < precision; ++i) number *= 10.0;
    if (number > 1000000 || number < -1000000) return "OOPS";
    String s = "" + (int)(number+0.5);
    if (precision <= 0) return s;
    int missing = precision + 1 - s.length();
    for (i = 0; i < missing; ++i) s = "0" + s;
    return s.substring(0, s.length() - precision) + "." + 
      s.substring(s.length() - precision);
  }
}

