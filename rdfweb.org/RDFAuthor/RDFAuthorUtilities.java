//
//  RDFAuthorUtilities.java
//  RDFAuthor
//
//  Created by pldms on Wed Nov 07 2001.
//

/*
    Copyright 2001 Damian Steer <dm_steer@hotmail.com>

    This file is part of RDFAuthor.

    RDFAuthor is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    RDFAuthor is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with RDFAuthor; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

*/

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
