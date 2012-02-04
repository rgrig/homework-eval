package org.rgrig.server;

import java.io.*;
import java.util.*;
import java.security.*;
import org.rgrig.client.ServerException;

public class UtilSrv {

  public static String sha(String s)
  throws NoSuchAlgorithmException, UnsupportedEncodingException {
    MessageDigest md = MessageDigest.getInstance("sha-1");
    md.update(s.getBytes("UTF-8"));
    StringBuilder sb = new StringBuilder();
    for (byte b : md.digest()) {
      sb.append(hex((b>>4)&15));
      sb.append(hex(b&15));
    }
    return sb.toString();
  }

  private static char hex(int d) {
    if (d < 10) return (char)(d + (int)'0');
    return (char)(d - 10 + (int) 'a');
  }

  public static String readFile(File f) throws IOException {
    StringBuilder sb = new StringBuilder();
    FileInputStream fis = new FileInputStream(f);
    InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
    int c;
    while ((c = isr.read()) != -1)
      sb.append((char)c);
    isr.close();
    return sb.toString();
  }

  public static ServerException se(String reason, Throwable t) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    while (t != null) {
      pw.print("\n*** ");
      t.printStackTrace(pw);
      t = t.getCause();
    }
    pw.flush();
    return new ServerException(sw.toString());
  }
}

