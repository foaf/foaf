/* SchemaWindowController */

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
