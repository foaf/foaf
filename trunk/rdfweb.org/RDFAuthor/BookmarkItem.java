//
//  BookmarkItem.java
//  RDFAuthor
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

// This class is initialised from a dragged item. It tries to keep as much of this info as possible
// to create a drag object later.

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;


public class BookmarkItem extends Object {
    
    Object data;
    String type;
    String displayName;
    String namespace = ""; // these two make life easier
    String name = "";
    boolean dataIsPropertyList;
    
    public BookmarkItem(NSPasteboard pboard, String type)
    {
        this.type = type;
        
        if (type.equals(NSPasteboard.URLPboardType)) {
            NSArray URLs = (NSArray) pboard.propertyListForType(NSPasteboard.URLPboardType);
            
            String id = (String) URLs.objectAtIndex(0);
            
            displayName = id;
            data = URLs;
            dataIsPropertyList = true;
        }
        else if (type.equals(NSPasteboard.StringPboardType)) {
            
            String id = (String) pboard.stringForType(NSPasteboard.StringPboardType);
            
            displayName = id;
            data = id;
            dataIsPropertyList = false;
        }
        else if (type.equals(SchemaData.ClassPboardType))
        {
            NSDictionary info = (NSDictionary) pboard.propertyListForType(
                SchemaData.ClassPboardType);
            name = (String) info.objectForKey("Name");
            namespace = (String) info.objectForKey("Namespace");
            
            displayName = name;
            data = info;
            dataIsPropertyList = true;
        }
        else if (type.equals(SchemaData.PropertyPboardType))
        {
            NSDictionary info = (NSDictionary) pboard.propertyListForType(
                SchemaData.PropertyPboardType);
            name = (String) info.objectForKey("Name");
            namespace = (String) info.objectForKey("Namespace");
            
            displayName = name;
            data = info;
            dataIsPropertyList = true;
        }
    }
    
    public boolean equals(Object item)
    {
        BookmarkItem theItem = (BookmarkItem) item; // don't worry - item always BookmarkItem
        
        if (!type.equals(theItem.type())) return false;
        if (!displayName.equals(theItem.displayName())) return false;
        if (!namespace.equals(theItem.namespace())) return false;
        if (!name.equals(theItem.name())) return false;
        
        return true;
    }
    
    public String displayName()
    {
        return displayName;
    }
    
    public String type()
    {
        return type;
    }
    
    public String name()
    {
        return name;
    }
    
    public String namespace()
    {
        return namespace;
    }
    
    public void createDragItem(NSPasteboard pboard)
    {
        NSArray types = new NSArray(type);
            
        pboard.declareTypes(types, null);
        if (dataIsPropertyList)
        {
            pboard.setPropertyListForType(data, type);
        }
        else
        {
            pboard.setStringForType((String) data, type);
        }
    }
}
