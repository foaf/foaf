/* SchemaWindowController */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.util.ArrayList;
import java.util.HashMap;

public class SchemaWindowController extends NSObject {

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
        if (url.trim().equals(""))
        {
            RDFAuthorUtilities.ShowError(
                "No URL Entered", "You need to specify a schema URL.",
                RDFAuthorUtilities.Normal, (NSWindow) schemaWindow);
            return;
        }
        schemaData.importSchema(url, schemaOutlineView, schemaWindow);
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

}
