package org.rgrig.client;

import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

public class DeadlineTimer extends Timer {
  public long deadline;
  public Label lbl;
  public Panel panel;
  
  public DeadlineTimer(long deadline, Label lbl, Panel panel) {
    this.deadline = deadline;
    this.lbl = lbl;
    this.panel = panel;
    scheduleRepeating(1000);
  }

  public void run() {
    if (System.currentTimeMillis() >= deadline)  {
      panel.clear();
      panel.add(new Label("Deadline passed. Sorry."));
    } else {
      int minutes = 
        (int) ((deadline - System.currentTimeMillis()) / 1000l / 60l);
      lbl.setText(Util.deadlineStr(minutes));
    }
  }
}
