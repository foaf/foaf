/* ErrorWindowController */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.util.Hashtable;

public class ErrorWindowController extends NSObject {

    NSTableView errorTable;

    NSPanel errorWindow;
    
    ModelErrorData errorData;
    
    Hashtable windowToData;
    
    NSWindow currentWindow = null;
    
    static String checkModelNotification = "org.rdfweb.RDFAuthor.checkModel";
    
    public ErrorWindowController()
    {
        windowToData = new Hashtable();
        
        // Initialise notification stuff
        
        NSSelector itemSelector = new NSSelector("checkModel", 
                new Class[] {NSNotification.class} );
        
        NSSelector windowSelector = new NSSelector("currentWindowChanged", 
                new Class[] {NSNotification.class} );
        
        NSNotificationCenter.defaultCenter().addObserver(
            this, itemSelector, checkModelNotification, null);
        
        NSNotificationCenter.defaultCenter().addObserver(
            this, windowSelector, NSWindow.WindowDidBecomeMainNotification, null);
    }
    
    public void awakeFromNib()
    {
        NSSelector doubleSelector = new NSSelector("doubleClick" ,
               new Class[] {} );
        
        errorTable.setTarget(this);
        errorTable.setDoubleAction(doubleSelector);
    }
    
    public void showErrorWindow(Object sender) 
    {
        if (errorWindow.isVisible())
        {
            errorWindow.orderOut(this);
        }
        else
        {
            errorWindow.makeKeyAndOrderFront(this);
        }
    }
    
    public void currentWindowChanged(NSNotification notification)
    {
        currentWindow = (NSWindow) notification.object();
        
        errorTable.setDataSource(windowToData.get(currentWindow));
    }
    
    public void checkModel(NSNotification notification)
    {
        currentWindow = (NSWindow) notification.object();
        ModelErrorData newData  = new ModelErrorData();
        windowToData.put(currentWindow, newData);
        ((RDFAuthorDocument) currentWindow.delegate()).checkModel(newData);
        errorTable.setDataSource(newData);
        errorWindow.makeKeyAndOrderFront(this);
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
