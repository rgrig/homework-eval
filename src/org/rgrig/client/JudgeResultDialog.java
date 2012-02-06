package org.rgrig.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

public class JudgeResultDialog extends DialogBox {
  private static JudgeResultDialog inst = new JudgeResultDialog();
  private static StringBuffer buffer = new StringBuffer();
  private Label result = new Label();
  private Label hint = new Label();
  private HTMLTable.CellFormatter cf;
  private HTMLTable.RowFormatter rf;
  private FlexTable tbl;


  private JudgeResultDialog() {
    super(false);
    setText("Dude Judge says ...");
    VerticalPanel p = new VerticalPanel();
    tbl = new FlexTable();
    tbl.addStyleName("gwt-FlexTable");
    cf = tbl.getCellFormatter();
    rf = tbl.getRowFormatter();
    p.add(result);
    p.add(hint);
    p.add(tbl);
    p.add(new Button("OK", new ClickHandler() {
      public void onClick(ClickEvent event) { JudgeResultDialog.this.hide(); }
    }));
    setWidget(p);
  }

  public static void show(PbEval eval) {
    int td = 0;
    if (!eval.compiled) {
      inst.result.setText("Your program does not compile.");
      inst.hint.setText("Hints: Compile locally, check selected language.");
    } else {
      inst.result.setText("Your program gets " + eval.points
          + (eval.points == 1.0? " point." : " points."));
      td = Math.min(eval.exampleOut.length, eval.exampleErr.length);
      if (td > 0) {
        inst.hint.setText("Here is what it does on the provided examples:");
        inst.tbl.setText(0,0,"stdout"); inst.tbl.setText(0,1,"stderr");
        inst.cf.setStyleName(0,0,"gwt-FlexTable-cell-header");
        inst.cf.setStyleName(0,1,"gwt-FlexTable-cell-header");

        for (int i = 0; i < td; ++i) {
          inst.tbl.setWidget(i+1,0,pre(eval.exampleOut[i]));
          inst.tbl.setWidget(i+1,1,pre(eval.exampleErr[i]));
          inst.cf.setStyleName(i+1,0,"gwt-FlexTable-cell");
          inst.cf.setStyleName(i+1,1,"gwt-FlexTable-cell");
          inst.rf.setStyleName(i+1,"gwt-FlexTable-row-normal");
        }
      } else {
        inst.hint.setText("Hints: try on tricky inputs, try on BIG inputs.");
      }
    }
    while (inst.tbl.getRowCount()>td+1)
      inst.tbl.removeRow(td+1);
    inst.center();
  }

  private static final int LIMIT = 512;
  private static HTML pre(String s) {
    buffer.setLength(0);
    for (int i = 0; i < Math.min(s.length(), LIMIT); ++i) {
      if (s.charAt(i) == '\n') buffer.append("<br/>");
      else if (s.charAt(i) == '<') buffer.append("&lt;");
      else if (s.charAt(i) == '>') buffer.append("&gt;");
      else if (s.charAt(i) == ' ') buffer.append("&nbsp;");
      else buffer.append(s.charAt(i));
    }
    if (s.length() > LIMIT) buffer.append("...");
    HTML html = new HTML(buffer.toString());
    html.addStyleName("code");
    return html;
  }
}

