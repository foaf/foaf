/* InfoController */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

public class InfoController extends NSObject {

    NSPanel infoWindow;

    NSButton literalButton;

    NSTextField literalTextField;

    NSTextField propertyTextField;

    NSTextField resourceIdField;

    NSTextField resourceTypeField;

    public void literalButtonChanged(Object sender) {
    }

    public void literalTextChanged(Object sender) {
    }

    public void propertyTextChanged(Object sender) {
    }

    public void resourceIdTextChanged(Object sender) {
    }

    public void resourceTypeTextChanged(Object sender) {
    }

    public void showInfoWindow(Object sender) {
        if (infoWindow.isVisible())
        {
            infoWindow.orderOut(this);
        }
        else
        {
            infoWindow.makeKeyAndOrderFront(this);
        }
    }
}
