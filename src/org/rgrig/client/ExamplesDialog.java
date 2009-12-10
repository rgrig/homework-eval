package org.rgrig.client;

import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

public class ExamplesDialog extends DialogBox {
  private static ExamplesDialog inst = new ExamplesDialog();
  private static StringBuffer buffer = new StringBuffer();
  private HTMLTable.CellFormatter cf;
  private HTMLTable.RowFormatter rf;
  private FlexTable tbl;


  private ExamplesDialog() {
    super(false);
    setText("Output on examples");
    VerticalPanel p = new VerticalPanel();
    tbl = new FlexTable();
    tbl.addStyleName("gwt-FlexTable");
    cf = tbl.getCellFormatter();
    rf = tbl.getRowFormatter();
    p.add(new HTML("<br/>"));
    p.add(tbl);
    p.add(new HTML("<br/>"));
    p.add(new Button("OK", new ClickListener() {
      public void onClick(Widget sender) { ExamplesDialog.this.hide(); }
    }));
    setWidget(p);
  }

  public static void Show(String[] out, String[] err) {
    inst.hide();
    inst.tbl.setText(0,0,"stdout"); inst.tbl.setText(0,1,"stderr");
    inst.cf.setStyleName(0,0,"gwt-FlexTable-cell-header");
    inst.cf.setStyleName(0,1,"gwt-FlexTable-cell-header");

    int td = Math.min(out.length, err.length);
    for (int i = 0; i < td; ++i) {
      inst.tbl.setWidget(i+1,0,pre(out[i]));
      inst.tbl.setWidget(i+1,1,pre(err[i]));
      inst.cf.setStyleName(i+1,0,"gwt-FlexTable-cell");
      inst.cf.setStyleName(i+1,1,"gwt-FlexTable-cell");
      inst.rf.setStyleName(i+1,"gwt-FlexTable-row-normal");
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

