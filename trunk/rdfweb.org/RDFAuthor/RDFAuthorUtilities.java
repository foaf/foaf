//
//  RDFAuthorUtilities.java
//  RDFAuthor
//
//  Created by pldms on Wed Nov 07 2001.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

// This class is just a bunch of useful methods

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;


public class RDFAuthorUtilities {
    
    static final int Normal = 1;
    static final int Critical = 2;
    static final int Informational = 3;
    
    // The following is for error panels so that I can have cool sheets
    // (the advantage being that they don't interupt the user too much)
    // If there is no window (eg when loading fails) or the window is not visible
    // I use a modal panel.
    
    public static void ShowError(String errorTitle, String errorText, int type, NSWindow window)
    {
        if ((window == null) || (!window.isVisible()))
        {
            switch (type)
            {
                case Normal:		NSAlertPanel.runAlert(errorTitle, errorText, null, null, null);
                                        break;
                case Critical:		NSAlertPanel.runCriticalAlert(errorTitle, errorText, null, null, null);
                                        break;
                case Informational: 	NSAlertPanel.runInformationalAlert(errorTitle, errorText, null, null, null);
            }
        }
        else
        {
            switch (type)
            {
                case Normal:
                    NSAlertPanel.beginAlertSheet(errorTitle, null, null, null,
                                                    window, null, null, null, window, errorText);
                    break;
                case Critical:
                    NSAlertPanel.beginCriticalAlertSheet(errorTitle, null, null, null,
                                                    window, null, null, null, window, errorText);
                    break;
                case Informational:
                    NSAlertPanel.beginInformationalAlertSheet(errorTitle, null, null, null,
                                                    window, null, null, null, window, errorText);
            }
        }
    }
}
