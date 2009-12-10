package org.rgrig.client;

import com.google.gwt.user.client.rpc.*;

public class PbTest implements IsSerializable {
  public String in;
  public String out;
  public PbTest() {
    this("no in", "no out");
  }
  public PbTest(String in, String out) {
    this.in = in;
    this.out = out;
  }
}
