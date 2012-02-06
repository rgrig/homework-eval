package org.rgrig.client;

import java.io.*;

public class Util {
  // TODO: Add information about the number of attempts.
  public static String pointsStr(double points, double totalPoints) {
    if (points < 0.0)
      return "You did not try to solve this problem yet.";
    if (Math.abs(points - totalPoints) < 1e-9)
      return "You solved this problem. Congratulations!"; 
    String r = "You have " + points + " point";
    if (points != 1.0) r += "s";
    r += " out of " + totalPoints + ".";
    return r;
  }

  public static String deadlineStr(int minutes) {
    int days, hours;
    days = minutes / 60 / 24; minutes %= 60 * 24;
    hours = minutes / 60; minutes %= 60;

    String r = "You have";
    if (days > 0) {
      r += " " + days + " day";
      if (days != 1) r += "s";
    }
    if (hours > 0) {
      if (days > 0) r += " and";
      r += " " + hours + " hour";
      if (hours != 1) r += "s";
    }
    if (minutes > 0) {
      if (days + hours > 0) r += " and";
      r += " " + minutes + " minute";
      if (minutes != 1) r += "s";
    }
    r += " until the deadline.";
    return r;
  }
}
