//
//  SchemaItem.java
//  RDFAuthor
//
//  Created by pldms on Sun Nov 04 2001.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

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
    
    SchemaItem parent;
    
    public SchemaItem(String displayName, String type, String nameSpace, String name, SchemaItem parent)
    {
        children = new ArrayList();
        
        this.displayName = displayName;
        this.type = type;
        this.namespace = nameSpace;
        this.name = name;
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
    
    public SchemaItem parent()
    {
        return parent;
    }
}
