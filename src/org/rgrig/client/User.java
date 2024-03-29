package org.rgrig.client;

import com.google.gwt.user.client.rpc.*;

public class User implements IsSerializable, Comparable {
  private static double EPSILON = 1e-9;

  public String pseudonym;
  public double points;
  public double penalty;
  public int compareTo(Object o) {
    User ou = (User) o;
    if (Math.abs(points - ou.points) > EPSILON)
      return Double.compare(ou.points, points);
    if (Math.abs(penalty - ou.penalty) > EPSILON)
      return Double.compare(penalty, ou.penalty);
    return pseudonym.compareTo(ou.pseudonym);
  }
}
