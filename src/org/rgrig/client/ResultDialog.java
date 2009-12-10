/** Public domain */

package org.rgrig.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/** @author rgrig */
public class ResultDialog extends DialogBox implements ClickListener {

  public ResultDialog(String result) {
    super(false);
    VerticalPanel vp = new VerticalPanel();

    result = result.trim();
    if (!result.startsWith("<pre>"))
      result = "<pre>" + result + "<pre>";
    
    // Caption
    setText("The results are in...");
    
    //DBG msg += result;
    HTML lbl = new HTML(result);
    vp.add(lbl);

    // The OK button
    String btnTxt = "YES!!!";
    if (result.indexOf("rock") == -1) btnTxt = "ok";
    Button ok = new Button(btnTxt, this);
    vp.add(ok);
    
    setWidget(vp);
  }
  
  public void onClick(Widget sender) {
    hide();
  }
  
  public void show() {
    super.show();
    int left = (Window.getClientWidth() - getOffsetWidth()) / 2;
    setPopupPosition(left, 100); 
  }
}
