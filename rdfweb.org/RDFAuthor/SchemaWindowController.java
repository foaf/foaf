/* SchemaWindowController */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.util.ArrayList;
import java.util.HashMap;

public class SchemaWindowController extends NSObject {

    static String schemaItemChangedNotification = "org.rdfweb.RDFAuthor.schemaItemChanged";

    NSMenuItem schemaMenuItem;

    NSOutlineView schemaOutlineView;

    NSComboBox schemaUrlField;

    NSPanel schemaWindow;

    SchemaData schemaData = new SchemaData();
        
    public void awakeFromNib()
    {
        schemaOutlineView.setDataSource(schemaData);
    }

    public void addSchema(Object sender) 
    {
        String url = schemaUrlField.stringValue();
        schemaData.importSchema(url, schemaOutlineView);
    }
    
    public void deleteSelectedItem(Object sender)
    {
        // First - get row of selected item
        
        int row = schemaOutlineView.selectedRow();
        if (row == -1) // No selected row
        {
            return;
        }
        
        schemaData.removeItem((SchemaItem) schemaOutlineView.itemAtRow(row), schemaOutlineView);
    }
    
    public void showSchemaWindow(Object sender)
    {
        if (schemaWindow.isVisible())
        {
            schemaWindow.orderOut(this);
        }
        else
        {
            schemaWindow.orderFront(this);
        }
    }
    
    
    // Delegate methods begin here
    
    public boolean outlineViewShouldSelectItem( NSOutlineView outlineView, Object item)
    {
        return ((SchemaItem) item).selectable();
    }
    
    public boolean outlineViewShouldEditTableColumn( NSOutlineView outlineView, NSTableColumn tableColumn, Object item)
    {
        return false;
    }
    
    public void outlineViewSelectionDidChange(NSNotification notification)
    {
        SchemaItem newItem;
        
        // First - get row of selected item
        
        int row = schemaOutlineView.selectedRow();
        if (row == -1) // No selected row
        {
            newItem = new SchemaItem(null, null, null, null, null); // Post empty object
        }
        else
        {
            newItem = (SchemaItem) schemaOutlineView.itemAtRow(row);
        }
        
        NSNotificationCenter.defaultCenter().postNotification(new NSNotification 
            ( schemaItemChangedNotification , newItem) );
    }
}
