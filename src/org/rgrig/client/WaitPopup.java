package org.rgrig.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;

public class WaitPopup extends PopupPanel {
  private static final WaitPopup inst = new WaitPopup();
  private WaitPopup() {
    super(false);
    add(new Label("Please wait while your submission is processed."));
    addStyleName("gwt-WaitPopup");
  }

  public static void Show() { inst.center(); }
  public static void Hide() { inst.hide(); }
}
