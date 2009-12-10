package org.rgrig.client;

import com.google.gwt.user.client.rpc.*;

public class User implements IsSerializable, Comparable {
  public String pseudonym;
  public double score;
  public int compareTo(Object o) {
    User ou = (User) o;
    if (score > ou.score) return -1;
    if (score < ou.score) return +1;
    return pseudonym.compareTo(ou.pseudonym);
  }
}
