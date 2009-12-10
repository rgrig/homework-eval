package org.rgrig.client;

import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

public class DeadlineTimer extends Timer {
  public int minutes;
  public Label lbl;
  public Panel panel;
  
  public DeadlineTimer(int minutes, Label lbl, Panel panel) {
    this.minutes = minutes;
    this.lbl = lbl;
    this.panel = panel;
    write();
  }

  public void run() {
    if (minutes <= 0)  {
      panel.clear();
      panel.add(new Label("Deadline passed. Sorry."));
    } else {
      write();
      --minutes;
      schedule(59950); // intentionally a bit fast
    }
  }

  public void write() {
    lbl.setText(Util.deadlineStr(minutes));
  }
}
