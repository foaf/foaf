/* ErrorWindowController */

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

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.util.HashMap;

public class ErrorWindowController extends NSObject {

    NSTableView errorTable;

    NSPanel errorWindow;
    
    ModelErrorData errorData;
    
    HashMap windowToData;
    
    NSWindow currentWindow = null;
    
    static final String checkModelNotification = "org.rdfweb.RDFAuthor.checkModel";
    
    public ErrorWindowController()
    {
        windowToData = new HashMap();
        
        // Initialise notification stuff
        
        NSSelector itemSelector = new NSSelector("checkModel", 
                new Class[] {NSNotification.class} );
        
        NSSelector windowSelector = new NSSelector("currentWindowChanged", 
                new Class[] {NSNotification.class} );
        
        NSSelector windowClosedSelector = new NSSelector("windowClosed",
                new Class[] {NSNotification.class} );
        
        NSNotificationCenter.defaultCenter().addObserver(
            this, itemSelector, checkModelNotification, null);
        
        NSNotificationCenter.defaultCenter().addObserver(
            this, windowSelector, NSWindow.WindowDidBecomeMainNotification, null);
        
        NSNotificationCenter.defaultCenter().addObserver(
            this, windowClosedSelector, NSWindow.WindowWillCloseNotification, null);
    }
    
    public void awakeFromNib()
    {
        NSSelector doubleSelector = new NSSelector("doubleClick" ,
               new Class[] {} );
        
        errorTable.setTarget(this);
        errorTable.setDoubleAction(doubleSelector);
        
        // Column 'Type' should be an image cell
        
        NSTableColumn col = errorTable.tableColumnWithIdentifier("Type");
        NSImageCell imageCell = new NSImageCell();
        imageCell.setImageAlignment(NSImageCell.ImageAlignTopLeft);
        col.setDataCell(imageCell);
        
        // Columns 'Messages' and 'Item' should wrap...
        
        col = errorTable.tableColumnWithIdentifier("Messages");
        NSCell textCell = col.dataCell();
        textCell.setWraps(true);
        
        col = errorTable.tableColumnWithIdentifier("Item");
        textCell = col.dataCell();
        textCell.setWraps(true);
    }
    
    public void showErrorWindow(Object sender) 
    {
        if (errorWindow.isVisible())
        {
            errorWindow.orderOut(this);
        }
        else
        {
            errorWindow.orderFront(this);
        }
    }
    
    public void currentWindowChanged(NSNotification notification)
    {
        currentWindow = (NSWindow) notification.object();
        
        errorTable.setDataSource(windowToData.get(currentWindow));
        errorTable.setDelegate(windowToData.get(currentWindow));
    }
    
    public void windowClosed(NSNotification notification)
    {
        NSWindow window = (NSWindow) notification.object();
        if (window == currentWindow)
        {
            errorTable.setDataSource(null);
            errorTable.setDelegate(null);
        }
    }
    
    public void checkModel(NSNotification notification)
    {
        currentWindow = (NSWindow) notification.object();
        ModelErrorData newData  = new ModelErrorData();
        windowToData.put(currentWindow, newData);
        ((RDFAuthorDocument) currentWindow.delegate()).checkModel(newData);
        errorTable.setDataSource(newData);
        errorTable.setDelegate(newData);
        
        if (newData.hasErrors())
        {
            RDFAuthorUtilities.ShowError(
                "This Model Contains " + newData.numberOfErrors() + " Errors",
                "Double-click on error entries to display information on the problematic parts of the model." +
                "\nErrors: " + newData.numberOfErrors() + "\nWarnings: " + newData.numberOfWarnings(),
                RDFAuthorUtilities.Critical, (NSWindow) errorWindow);
                errorWindow.orderFront(this);
        }
        else
        {
            RDFAuthorUtilities.ShowError(
                "This Model Has No Errors",
                "Errors: " + newData.numberOfErrors() + "\nWarnings: " + newData.numberOfWarnings(),
                RDFAuthorUtilities.Informational, (NSWindow) errorWindow);
            if (newData.hasWarnings())
            {
                errorWindow.orderFront(this);
            }
        }
    }
    
    public void doubleClick()
    {
        int row = errorTable.selectedRow();
        
        if (row != -1)
        {
            ModelItem item = ((ModelErrorData) windowToData.get(currentWindow)).getObjectAtRow(row);
            ((RDFAuthorDocument) currentWindow.delegate()).setCurrentObject(item);
            NSNotificationCenter.defaultCenter().postNotification(
                new NSNotification(InfoController.showInfoNotification, null) );
        }
    }
    
}
