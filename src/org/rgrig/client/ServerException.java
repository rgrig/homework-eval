package org.rgrig.client;

//RM import com.google.gwt.core.client.*;
//RM import com.google.gwt.user.client.rpc.*;

public class ServerException extends Exception {
  public ServerException(String reason) {
    super(reason);
  }
  public ServerException() {
    super("Unknown reason.");
  }
}
