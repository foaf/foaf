/* ErrorWindowController */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.util.HashMap;

public class ErrorWindowController extends NSObject {

    NSTableView errorTable;

    NSPanel errorWindow;
    
    ModelErrorData errorData;
    
    HashMap windowToData;
    
    NSWindow currentWindow = null;
    
    static String checkModelNotification = "org.rdfweb.RDFAuthor.checkModel";
    
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
        
        // Yuk - all this to have a column with images
        NSTableColumn imageCol = new NSTableColumn();
        imageCol.setIdentifier("Type");
        NSImageCell imageCell = new NSImageCell();
        imageCell.setImageAlignment(NSImageCell.ImageAlignTop);
        imageCol.setDataCell(imageCell);
        //imageCol.setMaxWidth(30);
        //imageCol.setMinWidth(30);
        imageCol.setWidth(30);
        imageCol.setResizable(false);
        imageCol.setHeaderCell(new NSTableHeaderCell(""));
        
        errorTable.addTableColumn(imageCol);
        errorTable.moveColumnToColumn(2, 0); // put the new column at the front
        errorTable.sizeLastColumnToFit();
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
            NSAlertPanel alert = new NSAlertPanel();
            alert.runCriticalAlert("This Model Contains " + newData.numberOfErrors() + " Errors",
                "Double-click on error entries to display information on the problematic parts of the model." +
                "\nErrors: " + newData.numberOfErrors() + "\nWarnings: " + newData.numberOfWarnings(),
                null, null, null);
                errorWindow.orderFront(this);
        }
        else
        {
            NSAlertPanel alert = new NSAlertPanel();
            alert.runInformationalAlert("This Model Has No Errors",
                "Errors: " + newData.numberOfErrors() + "\nWarnings: " + newData.numberOfWarnings(),
                null, null, null);
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
