package org.rgrig.client;

import com.google.gwt.user.client.rpc.*;

public class Task implements IsSerializable {
  public String id; // no spaces
  public String name; // may have spaces
  public double score;  // < 0.0 means not tried
  public double totalScore;
  public long start; /* year, month, day, hour, minute */
  public long deadline; /* year, month, day, hour, minute */
}
