//
//  SchemaItem.java
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

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.util.ArrayList;
import java.util.ListIterator;

public class SchemaItem {
    
    ArrayList children;
    
    String namespace;
    
    String name;
    
    String type;
    
    String displayName;
    
    String description;
    
    SchemaItem parent;
    
    public SchemaItem(String displayName, String type, String nameSpace, 
        String name, String description, SchemaItem parent)
    {
        children = new ArrayList();
        
        this.displayName = displayName;
        this.type = type;
        this.namespace = nameSpace;
        this.name = name;
        this.description = description;
        this.parent = parent;
    }
    
    public SchemaItem childWithDisplayName(String name)
    {
        ListIterator iterator = children.listIterator();
        
        while (iterator.hasNext())
        {
            SchemaItem child = (SchemaItem) iterator.next();
            if (child.displayName().equals(name))
            {
                return child;
            }
        }
        
        return null;
    }
    
    public boolean draggable()
    {
        if (type == null) return false;
        return (type.equals(SchemaData.ClassPboardType) || type.equals(SchemaData.PropertyPboardType));
    }
    
    public boolean selectable()
    {
        return (parent != null);
    }
    
    public void add(SchemaItem item)
    {
        children.add(item);
    }
    
    public void remove(SchemaItem item)
    {
        children.remove(item);
    }
    
    public boolean hasChildren()
    {
        return (this.numberOfChildren() != 0);
    }
    
    public int numberOfChildren()
    {
        return children.size();
    }
    
    public SchemaItem get(int index)
    {
        return (SchemaItem) children.get(index);
    }
    
    public String type()
    {
        return type;
    }
    
    public String namespace()
    {
        return namespace;
    }
    
    public String name()
    {
        return name;
    }
    
    public String displayName()
    {
        return displayName;
    }
    
    public String description()
    {
        return description;
    }
    
    public SchemaItem parent()
    {
        return parent;
    }
}
