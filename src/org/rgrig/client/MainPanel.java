package org.rgrig.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.user.client.ui.*;
import java.util.Arrays;

public class MainPanel extends TabPanel {
  public HomeworkEvalApp app;
  public HomeworkEvalSrvAsync srv;

  public Problem[] problem;
  public String[] languages;

  public VerticalPanel[] pbPanel;
  public Panel scoresPanel;

  public Label[] pbDeadline;
  public Label[] pbScore;

  public TextArea[] pbSolution;
  public ListBox[] pbLang;

  public MainPanel(
    Problem[] problem,
    String[] languages,
    HomeworkEvalApp app
  ) {
    this.problem = problem;
    this.languages = languages;
    this.app = app;
    srv = app.srv;
    setWidth("600px"); // TODO Move in CSS.
    Arrays.sort(languages);

    pbPanel = new VerticalPanel[problem.length];
    pbDeadline = new Label[problem.length];
    pbScore = new Label[problem.length];
    pbSolution = new TextArea[problem.length];
    pbLang = new ListBox[problem.length];

    scoresPanel = new VerticalPanel();

    for (int i = 0; i < problem.length; ++i) addPb(i);
    addScores();
  }

  // TODO: Eliminate code duplication
  private void addPb(int idx) {
//Window.alert("start MainPanel.addPb");
    Problem pb = problem[idx];
    Panel p = pbPanel[idx] = new VerticalPanel();

    final Label scoreL = new Label(Util.pointsStr(pb.score, pb.totalScore));
    p.add(scoreL);
    Label d = pbDeadline[idx] = new Label();
    DeadlineTimer dt = new DeadlineTimer(pb.deadline, d, p);
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
//Window.alert("stop MainPanel.addPb, pb.name=" + pb.name);
  }

  private void addScores() {
//Window.alert("MainPanel.addScores");
    add(scoresPanel, "Scores");
    scoresPanel.clear();
    final FlexTable table = new FlexTable();
    table.addStyleName("gwt-FlexTable");
    updateScores(table);

    scoresPanel.add(table);
    scoresPanel.add(new Button("Update", new ClickHandler() {
      public void onClick(ClickEvent event) { updateScores(table); }
    }));

  }

  private void updateScores(final FlexTable t) {
    final HTMLTable.RowFormatter rf = t.getRowFormatter();
    final HTMLTable.CellFormatter cf = t.getCellFormatter();
    t.setText(0,0,"Pseudonym");
    t.setText(0,1,"Score");
    t.setText(0,2,"Penalty");
    t.setText(0,3,"Final (est.)");
    cf.setStyleName(0, 0, "gwt-FlexTable-cell-header");
    cf.setStyleName(0, 1, "gwt-FlexTable-cell-header");
    cf.setStyleName(0, 2, "gwt-FlexTable-cell-header");
    cf.setStyleName(0, 3, "gwt-FlexTable-cell-header");

    srv.scoreScale(new Aac() {
      public void onSuccess(Object result) {
        final double scale = ((Double)result).doubleValue();
        srv.getScores(new Aac() {
          public void onSuccess(Object result) {
            User[] users = (User[]) result;
            Arrays.sort(users);
            for (int i = 0; i < users.length; ++i) {
              t.setText(i+1,0,users[i].pseudonym);
              t.setText(i+1,1,"" + Math.round(users[i].score));
              t.setText(i+1,2,"" + Math.round(users[i].penalty));
              t.setText(i+1,3,"" + Math.round(scale*users[i].score));
              cf.setStyleName(i+1,0,"gwt-FlexTable-cell");
              cf.setStyleName(i+1,1,"gwt-FlexTable-cell");
              cf.setStyleName(i+1,2,"gwt-FlexTable-cell");
              cf.setStyleName(i+1,3,"gwt-FlexTable-cell");
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
}

