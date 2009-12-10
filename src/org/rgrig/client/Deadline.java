/** Public domain */

package org.rgrig.client;

import com.google.gwt.user.client.rpc.RemoteService;

/** @author rgrig */
public interface Deadline extends RemoteService {
  public long get();
}
