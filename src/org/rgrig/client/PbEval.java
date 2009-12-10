package org.rgrig.client;

import com.google.gwt.user.client.rpc.*;

public class PbEval implements IsSerializable {
  public String[] exampleOut;
  public String[] exampleErr;
  public boolean compiled;
  public double score;
}
