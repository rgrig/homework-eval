package org.rgrig.client;

import com.google.gwt.user.client.rpc.*;

public class Task implements IsSerializable {
  public String id;
  public String name;
  public double score;
  public double totalScore;
  public String start; /* year, month, day, hour, minute */
  public String deadline; /* year, month, day, hour, minute */
  public int secondsToDeadline;
  public boolean tried;
}
