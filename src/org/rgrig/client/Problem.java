package org.rgrig.client;

import com.google.gwt.user.client.rpc.*;

public class Problem extends Task {
  public String statement;
  public PbTest[] examples;
  public double penaltyPerAttempt;
  public int attempts;
}
