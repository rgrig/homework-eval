package org.rgrig.client;

import com.google.gwt.user.client.*;
import com.google.gwt.user.client.rpc.*;

public abstract class Aac implements AsyncCallback {
  public void onFailure(Throwable caught) {
    String msg = caught.getMessage();
    while ((caught = caught.getCause()) != null)
      msg += " > " + caught.getMessage();
    Window.alert("Server Error: " + msg);
  }
}
