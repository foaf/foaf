/* BookmarkController */

/* $Id: BookmarkController.java,v 1.4 2002-02-06 00:36:23 pldms Exp $ */

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

public class BookmarkController {

    NSDrawer bookmarkDrawer;

    NSTableView bookmarkTable;

    RDFAuthorDocument rdfAuthorDocument;
    
    NSButton autoAddSwitch;
    
    NSMutableArray dragTypesArray = new NSMutableArray();
    
    ArrayList bookmarkedItems = new ArrayList();
    HashMap typeToImage = new HashMap();
    
    public void awakeFromNib()
    {
        // This is the same as for the RDFModelView: we accept anything it will
        dragTypesArray.addObject(NSPasteboard.URLPboardType);
        dragTypesArray.addObject(NSPasteboard.StringPboardType);
        dragTypesArray.addObject(SchemaData.ClassPboardType);
        dragTypesArray.addObject(SchemaData.PropertyPboardType);
        
        // This maps types to NSImages
        
        typeToImage.put(NSPasteboard.URLPboardType, NSImage.imageNamed("URLType"));
        typeToImage.put(NSPasteboard.StringPboardType, NSImage.imageNamed("literalType"));
        typeToImage.put(SchemaData.ClassPboardType, NSImage.imageNamed("classType"));
        typeToImage.put(SchemaData.PropertyPboardType, NSImage.imageNamed("propertyType"));
        
        bookmarkTable.registerForDraggedTypes((NSArray) dragTypesArray);
        
        // Column 'Type' displays images, so I'll set it so that it can
        
        NSTableColumn col = bookmarkTable.tableColumnWithIdentifier("Type");
        NSImageCell imageCell = new NSImageCell();
        imageCell.setImageAlignment(NSImageCell.ImageAlignTopLeft);
        col.setDataCell(imageCell);
    }
    
    public void toggleShow()
    {
        bookmarkDrawer.toggle(this);
    }

    public ArrayList items()
    {
        return bookmarkedItems;
    }

    public void setItems(ArrayList items)
    {
        bookmarkedItems = items;
        bookmarkTable.reloadData();
    }
    
    public void addItem(NSPasteboard pb, String type)
    {
        if (autoAddSwitch.state() == NSCell.OnState) // does the user want to auto-add?
        {
            BookmarkItem item = new BookmarkItem(pb, type);
            if (!bookmarkedItems.contains(item)) // this could be a pain otherwise
            {
                bookmarkedItems.add(item);
                bookmarkTable.reloadData();
            }
        }
    }
    
    // NSTableView.DataSource methods start here
    
    public int numberOfRowsInTableView(NSTableView aTableView)
    {
        return bookmarkedItems.size();
    }
    
    public Object tableViewObjectValueForLocation( NSTableView aTableView, NSTableColumn aTableColumn, int rowIndex)
    {
        if (aTableColumn.identifier().equals("Type"))
        {
            String type = ((BookmarkItem) bookmarkedItems.get(rowIndex)).type();
            return typeToImage.get(type);
        }
        else
        {
            return ((BookmarkItem) bookmarkedItems.get(rowIndex)).displayName();
        }
    }
    
    public boolean tableViewAcceptDrop( NSTableView tableView, NSDraggingInfo info, int row, int operation)
    {
        NSPasteboard pb = info.draggingPasteboard();
        String type = pb.availableTypeFromArray((NSArray) dragTypesArray);
        
        if(type != null) {
            bookmarkedItems.add(row, new BookmarkItem(pb, type));
            return true;
        }
        return false;
    }
    
    public int tableViewValidateDrop( NSTableView tableView, NSDraggingInfo info, int row, int operation)
    {
        NSPasteboard pb = info.draggingPasteboard();
        String type = pb.availableTypeFromArray((NSArray) dragTypesArray);
        
        if(type != null) {
            return operation;
        }
        return 0;
    }
    
    public boolean tableViewWriteRowsToPasteboard( NSTableView tableView, NSArray rows, NSPasteboard pboard)
    {
        int row = ((java.lang.Integer) rows.objectAtIndex(0)).intValue();
        
        ((BookmarkItem) bookmarkedItems.get(row)).createDragItem(pboard);
        
        return true;
    }
    
    public void tableViewSelectionDidChange(NSNotification aNotification)
    {
        int row = bookmarkTable.selectedRow();
        
        if (row == -1) // no selection
        {
            rdfAuthorDocument.setClassPropertyDefaults(null, null, null, null);
            return;
        }
        
        BookmarkItem item = (BookmarkItem) bookmarkedItems.get(row);
        
        if (item.type().equals( SchemaData.ClassPboardType ))
        {
            rdfAuthorDocument.setClassPropertyDefaults(
                (String) ((NSDictionary) item.data()).objectForKey("Namespace"), 
                (String) ((NSDictionary) item.data()).objectForKey("Name"),
                null, null );
        }
        else if (item.type().equals( SchemaData.PropertyPboardType ))
        {
            rdfAuthorDocument.setClassPropertyDefaults(
                null, null,
                (String) ((NSDictionary) item.data()).objectForKey("Namespace"), 
                (String) ((NSDictionary) item.data()).objectForKey("Name") );
        }
        else
        {
            rdfAuthorDocument.setClassPropertyDefaults(null, null, null, null);
        }
    }
}
