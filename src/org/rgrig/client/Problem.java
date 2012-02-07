package org.rgrig.client;

import com.google.gwt.user.client.rpc.*;

// TODO(rgrig): Why |Problem| *and* |PbProperties|? Document or fix.
public class Problem implements IsSerializable {
  public String id; // no spaces
  public String name; // may have spaces
  public double points;  // < 0.0 means not tried
  public double totalPoints;
  public long start; /* year, month, day, hour, minute */
  public long deadline; /* year, month, day, hour, minute */
  public String statement;
  public PbTest[] examples;
  public double penaltyPerAttempt;
  public int attempts;
}
