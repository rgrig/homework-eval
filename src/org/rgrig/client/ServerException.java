package org.rgrig.client;

import com.google.gwt.core.client.*;
import com.google.gwt.user.client.rpc.*;

public class ServerException extends SerializableException {
  public ServerException(String reason) {
    super(reason);
  }
  public ServerException() {
    super("Unknown reason.");
  }
}
