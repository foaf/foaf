//
//  SchemaData.java
//  RDFAuthor
//
//  Created by pldms on Tue Oct 09 2001.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.mem.*;
import com.hp.hpl.mesa.rdf.jena.common.prettywriter.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.RDF;
import com.hp.hpl.mesa.rdf.jena.vocabulary.RDFS;

public class SchemaData {
    
    Vector classesList = new Vector();
    Vector propertiesList = new Vector();
    
    public SchemaData()
    {
        classesList.add( headerData( "Classes", null, null, null ) );
        propertiesList.add( headerData( "Properties", null, null, null) );
    }
    
    public Hashtable headerData( String displayName, String type, String nameSpace, String name)
    {
        type = (type == null)?"":type;
        nameSpace = (nameSpace == null)?"":nameSpace;
        name = (name == null)?"":name;
        Hashtable header = new Hashtable();
        header.put("displayName", displayName);
        header.put("type", type);
        header.put("nameSpace", nameSpace);
        header.put("name", name);
        return header;
    }
    
    public void importSchema(String url, NSOutlineView outlineView)
    {
        try
        {
        Model memModel = new ModelMem();
        memModel = memModel.read(url);
        
        if (memModel.size() == 0)
        {
            System.out.println("Oh dear - nothing read");
        }
        
        ResIterator classes =
                memModel.listSubjectsWithProperty(RDF.type, RDFS.Class);

        ResIterator properties =
                memModel.listSubjectsWithProperty(RDF.type, RDF.Property);
        
        // Read in the classes
        
        while (classes.hasNext())
        {
            String className = classes.next().toString();
            
            int sep = className.lastIndexOf("/");
            int hash = className.lastIndexOf("#");
            
            sep = (hash > sep)?hash:sep;
            
            String namespace = className.substring(0, sep+1);
            String name = className.substring(sep+1);
            
            addToTree(classesList, "Class", namespace, name);
        }
        while (properties.hasNext())
        {
            String propertyName = properties.next().toString();
            
            int sep = propertyName.lastIndexOf("/");
            int hash = propertyName.lastIndexOf("#");
            
            sep = (hash > sep)?hash:sep;
            
            String namespace = propertyName.substring(0, sep+1);
            String name = propertyName.substring(sep+1);
            
            addToTree(propertiesList, "Property", namespace, name);
        }
        
        outlineView.reloadItemAndChildren( classesList, true); // This isn't very efficient
        outlineView.reloadItemAndChildren( propertiesList, true); // This isn't very efficient
        
        }
        catch (RDFException exception)
        {
            System.err.println("Import failed: " + exception);
        }
    }
    
    public void addToTree(Vector list, String type, String namespace, String name)
    {
        Vector namespaceVector = null;
        
        Enumeration enumerator = list.elements();
        
        enumerator.nextElement(); // Skip the first element
        
        while (enumerator.hasMoreElements())
        {
            Vector temp = (Vector) enumerator.nextElement();
            String tempNamespace = (String) ((Hashtable) temp.firstElement()).get("displayName");
            if (tempNamespace.equals(namespace))
            {
                namespaceVector = temp;
                break;
            }
        }
        
        if (namespaceVector == null)
        {
            namespaceVector = new Vector();
            namespaceVector.add( headerData(namespace, null, null, null) );
            list.add(namespaceVector);
        }
        
        Vector nameVector = new Vector();
        nameVector.add( headerData(name, type, namespace, name) );
        namespaceVector.add(nameVector);
    }
    
    public Vector outlineViewChildOfItem( NSOutlineView outlineView, int index, Vector item)
    {
        if (item == null)
        {
            if (index == 0)
            {
                return classesList;
            }
            else
            {
                return propertiesList;
            }
        }
        else
        {
            return (Vector) item.elementAt(index+1);
        }
    }
    
    public boolean outlineViewIsItemExpandable( NSOutlineView outlineView, Vector item)
    {
        return (item.size() != 1);
    }
    
    public int outlineViewNumberOfChildrenOfItem( NSOutlineView outlineView, Vector item)
    {
        if (item == null)
        {
            return 2;
        }
        else
        {
            return (item.size() - 1);
        }
    }
    
    public String outlineViewObjectValueForItem( NSOutlineView outlineView,
            NSTableColumn tableColumn, Vector item)
    {
        Hashtable info = (Hashtable) item.firstElement();
        return (String) info.get("displayName");
    }
    
    public boolean outlineViewWriteItemsToPasteboard( NSOutlineView outlineView, 
            NSArray items, NSPasteboard pboard)
    {
        

}
