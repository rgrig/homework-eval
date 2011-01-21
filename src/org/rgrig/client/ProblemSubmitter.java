package org.rgrig.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

public class ProblemSubmitter implements ClickHandler {
  public ProblemSubmitter(
    String pbId,
    TextArea solution,
    ListBox language,
    HomeworkEvalApp application
  ) {
    this.pbId = pbId;
    this.solution = solution;
    this.language = language;
    this.application = application;
  }

  private String pbId;
  private TextArea solution;
  private ListBox language;
  private HomeworkEvalApp application;

  public void onClick(ClickEvent event) {
    // TODO
    Window.alert("Trying to submit solution for " + pbId);
  }
}
