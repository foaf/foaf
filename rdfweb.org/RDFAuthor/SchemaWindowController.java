/* SchemaWindowController */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

public class SchemaWindowController extends NSObject {

    NSMenuItem schemaMenuItem;

    NSOutlineView schemaOutlineView;

    NSTextField schemaUrlField;

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

    public void showSchemaWindow(Object sender)
    {
        if (schemaWindow.isVisible())
        {
            schemaWindow.orderOut(this);
        }
        else
        {
            schemaWindow.makeKeyAndOrderFront(this);
        }
    }

}
