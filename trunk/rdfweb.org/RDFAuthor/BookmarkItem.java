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

import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;

public class BookmarkItem implements Serializable {
    
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

    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException
    {
        out.writeObject(ns2java(data));
        out.writeObject(type);
        out.writeObject(displayName);
        out.writeObject(namespace);
        out.writeObject(name);
        out.writeBoolean(dataIsPropertyList);
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        data = java2ns(in.readObject());
        type = (String) in.readObject();
        displayName = (String) in.readObject();
        namespace = (String) in.readObject();
        name = (String) in.readObject();
        dataIsPropertyList = in.readBoolean();
    }

    // This is a kludge. The Cocoa (NS*) classes won't serialise, so the next two methods
    // convert between the two
    
    private Object ns2java(Object object)
    {
        if (object instanceof String) // Simple case
        {
            return object;
        }
        else if (object instanceof NSDictionary)
        {
            HashMap map = new HashMap();
            map.put("Namespace", ((NSDictionary) object).objectForKey("Namespace"));
            map.put("Name", ((NSDictionary) object).objectForKey("Name"));
            return map;
        }
        else if (object instanceof NSArray) // this is for URL types - this is odd. Why an array?
        {
            ArrayList array = new ArrayList();
            array.add(((NSArray) object).objectAtIndex(0));
            array.add(((NSArray) object).objectAtIndex(1));

            return array;
        }
        else
        {
            System.out.println("Oh dear..");
            return null;
        }
    }

    private Object java2ns(Object object)
    {
        if (object instanceof String) // Simple case
        {
            return object;
        }
        else if (object instanceof HashMap)
        {
            NSMutableDictionary map = new NSMutableDictionary();
            map.setObjectForKey(((HashMap) object).get("Namespace"), "Namespace");
            map.setObjectForKey(((HashMap) object).get("Name"), "Name");
            return map;
        }
        else if (object instanceof ArrayList)
        {
            NSArray array = new NSArray(((ArrayList) object).toArray());
            return array;
        }
        else
        {
            System.out.println("Oh dear..");
            return null;
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
