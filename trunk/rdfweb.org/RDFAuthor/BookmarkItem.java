//
//  BookmarkItem.java
//  RDFAuthor
//

/* $Id: BookmarkItem.java,v 1.4 2002-02-06 00:36:23 pldms Exp $ */

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

// This class is initialised from a dragged item. It tries to keep as much of 
// this info as possible to create a drag object later.

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;

public class BookmarkItem implements Serializable {
    
    static final String StringPboardType = "org.rdfweb.RDFAuthor.String";
    static final String URLPboardType = "org.rdfweb.RDFAuthor.URL";
    
    Object data;
    String type;
    String displayName;
    
    public BookmarkItem(NSPasteboard pboard, String type)
    {
        this.type = type;
        
        if (type.equals(NSPasteboard.URLPboardType)) {
            NSArray URLs = (NSArray) pboard.propertyListForType(NSPasteboard.URLPboardType);
            
            String id = (String) URLs.objectAtIndex(0);
            
            displayName = id;
            data = URLs;
        }
        else if (type.equals(NSPasteboard.StringPboardType)) {
            
            String id = (String) pboard.stringForType(NSPasteboard.StringPboardType);
            
            displayName = id;
            
            // Here I'm going to be sneaky. IE (and other Carbon apps) don't seem to
            // set the drag type for URLs - they are just strings. So I use the URI
            // checker. This is better since all URIs will be detected, but OTOH
            // URIs can match unintentionally.
            // If the string is a URI fix the data.
            
            if (RDFAuthorUtilities.isValidURI(id))
            {
                data = new NSArray( new String[]{ id, "" } );
                this.type = NSPasteboard.URLPboardType;
            }
            else
            {
                data = id;
            }
        }
        else if (type.equals(SchemaData.ClassPboardType))
        {
            NSDictionary info = (NSDictionary) pboard.propertyListForType(
                SchemaData.ClassPboardType);
            
            displayName = (String) info.objectForKey("Name");
            data = info;
        }
        else if (type.equals(SchemaData.PropertyPboardType))
        {
            NSDictionary info = (NSDictionary) pboard.propertyListForType(
                SchemaData.PropertyPboardType);
            
            displayName = (String) info.objectForKey("Name");
            data = info;
        }
    }

    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException
    {
        out.writeObject(mapTypes(type));
        out.writeObject( ns2java(data, type) );
        out.writeObject(displayName);
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        type = mapTypes( (String) in.readObject() );
        data = java2ns(in.readObject(), type);
        displayName = (String) in.readObject();
    }

    /*
        The following gets hairy. The problem is that we don't want to save
        NS (Cocoa) specific data. There are two issues:
        
        1) Cocoa objects need to be changed to java-serialisable things
        2) We don't want the pboard type to be Cocoa specific
        
        So the following converts between the two.
    */
    
    private String mapTypes(String type)
    {
        // Convert NS to cross platform
        if (type.equals(NSPasteboard.StringPboardType)) return StringPboardType;
        if (type.equals(NSPasteboard.URLPboardType)) return URLPboardType;
            
        // Next two are the inverses of the previous
        if (type.equals(StringPboardType)) return NSPasteboard.StringPboardType;
        if (type.equals(URLPboardType)) return NSPasteboard.URLPboardType;
        
        // SchemaTypes - no problem
        return type;
    }
    
    private Object ns2java(Object object, String nsType)
    {
        if (nsType.equals(NSPasteboard.StringPboardType)) return object;
            
        if ( (nsType.equals(SchemaData.ClassPboardType)) ||
                    (nsType.equals(SchemaData.PropertyPboardType)) )
        {
            HashMap map = new HashMap();
            map.put("Namespace", ((NSDictionary) object).objectForKey("Namespace"));
            map.put("Name", ((NSDictionary) object).objectForKey("Name"));
            return map;
        }
        
        if (nsType.equals(NSPasteboard.URLPboardType))
        {
                String url = (String) ((NSArray) object).objectAtIndex(0);
                return url;
        }
        
        return null; // Shouldn't get here
    }

    private Object java2ns(Object object, String crossType)
    {
        if (crossType.equals(StringPboardType)) return object;
            
        if ( (crossType.equals(SchemaData.ClassPboardType)) ||
                    (crossType.equals(SchemaData.PropertyPboardType)) )
        {
            NSMutableDictionary map = new NSMutableDictionary();
            map.setObjectForKey(((HashMap) object).get("Namespace"), "Namespace");
            map.setObjectForKey(((HashMap) object).get("Name"), "Name");
            return map;
        }
        
        if (crossType.equals(URLPboardType))
        {
                NSArray array = new NSArray(new String[]{ (String) object, "" });
                return array;
        }
        
        return null; // Bad thing if we find ourselves here
    }
    
    public boolean equals(Object item)
    {
        BookmarkItem theItem = (BookmarkItem) item; // don't worry - item always BookmarkItem
        
        if (!type.equals(theItem.type())) return false;
        if (!displayName.equals(theItem.displayName())) return false;
        if (!data.equals(theItem.data)) return false;
        
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
    
    public Object data()
    {
        return data;
    }
    
    public void createDragItem(NSPasteboard pboard)
    {
        NSArray types = new NSArray(type);
            
        pboard.declareTypes(types, null);
        if (!(data instanceof String)) // Only occasion we don't create a plist
        {
            pboard.setPropertyListForType(data, type);
        }
        else
        {
            pboard.setStringForType((String) data, type);
        }
    }
}
