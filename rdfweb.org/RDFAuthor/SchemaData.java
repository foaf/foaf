//
//  SchemaData.java
//  RDFAuthor
//
//  Created by pldms on Tue Oct 09 2001.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.mem.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.RDF;
import com.hp.hpl.mesa.rdf.jena.vocabulary.RDFS;
import com.hp.hpl.mesa.rdf.jena.common.*;

public class SchemaData {
    
    SchemaItem classesList;
    SchemaItem propertiesList;
    
    static String ClassPboardType = "org.rdfweb.RDFAuthor.class";
    static String PropertyPboardType = "org.rdfweb.RDFAuthor.property";
    
    public SchemaData()
    {
        classesList = new SchemaItem("Node Types", null, null, null, null);
        propertiesList = new SchemaItem( "Connection Properties", null, null, null, null);
    }

    public void importSchema(String url, NSOutlineView outlineView, NSPanel panel)  // need panel for errors
    {
        try
        {
            Model memModel = new ModelMem();
            memModel = memModel.read(url);
            boolean hasClasses = false;
            boolean hasProperties = false;
            
            if (memModel.size() == 0)
            {
                System.out.println("Oh dear - nothing read");
                RDFAuthorUtilities.ShowError("Nothing Imported",
                    "No classes or properties were found. Are you sure this is a schema?",
                    RDFAuthorUtilities.Normal, (NSWindow) panel);
                return;
            }
        
            ResIterator classes =
                memModel.listSubjectsWithProperty(RDF.type, RDFS.Class);

            ResIterator properties =
                memModel.listSubjectsWithProperty(RDF.type, RDF.Property);
        
            // Read in the classes
        
            while (classes.hasNext())
            {
                hasClasses = true;
                String className = classes.next().toString();
                
                int sep = Util.splitNamespace(className);
            
                String namespace = className.substring(0, sep);
                String name = className.substring(sep);
                
                addToTree(classesList, ClassPboardType, url, namespace, name);
            }
            while (properties.hasNext())
            {
                hasProperties = true;
                String propertyName = properties.next().toString();
                
                int sep = Util.splitNamespace(propertyName);
                
                String namespace = propertyName.substring(0, sep);
                String name = propertyName.substring(sep);
                
                addToTree(propertiesList, PropertyPboardType, url, namespace, name);
            }
            
            if (hasClasses)
            {
                outlineView.reloadItemAndChildren( classesList, true); // This isn't very efficient
                outlineView.expandItem( classesList );
            }
            
            if (hasProperties)
            {
                outlineView.reloadItemAndChildren( propertiesList, true); // This isn't very efficient
                outlineView.expandItem( propertiesList );
            }
        }
        catch (RDFException exception)
        {
            System.err.println("Import failed: " + exception);
            RDFAuthorUtilities.ShowError(
                "Import Failed",
                "Try checking the URL. You are online, aren't you?",
                RDFAuthorUtilities.Normal, (NSWindow) panel);
        }
    }
    
    public void addToTree(SchemaItem list, String type, String url, String namespace, String name)
    {
        SchemaItem namespaceList = list.childWithDisplayName(url);
        
        if (namespaceList == null)
        {
            namespaceList = new SchemaItem( url, null, null, null, list);
            list.add(namespaceList);
        }
        
        SchemaItem nameItem = new SchemaItem(name, type, namespace, name, namespaceList);
        namespaceList.add(nameItem);
    }
    
    public void removeItem(SchemaItem item, NSOutlineView outlineView)
    {
        SchemaItem parent = item.parent();
        if (parent == null)
        {
            outlineView.setNeedsDisplay(true);
            return;
        }
        
        parent.remove(item);
        outlineView.reloadItemAndChildren( parent, true);
        
        if (!parent.hasChildren()) // Parent has no children - how sad - so remove it (unless it's one of the root things)
        {
            removeItem(parent, outlineView);
        }
    }
            
    
    // This is the stuff NSOutlineViews want
    
    public SchemaItem outlineViewChildOfItem( NSOutlineView outlineView, int index, SchemaItem item)
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
            return item.get(index);
        }
    }
    
    public boolean outlineViewIsItemExpandable( NSOutlineView outlineView, SchemaItem item)
    {
        return item.hasChildren();
    }
    
    public int outlineViewNumberOfChildrenOfItem( NSOutlineView outlineView, SchemaItem item)
    {
        if (item == null)
        {
            return 2;
        }
        else
        {
            return item.numberOfChildren();
        }
    }
    
    public String outlineViewObjectValueForItem( NSOutlineView outlineView,
            NSTableColumn tableColumn, SchemaItem item)
    {
        return item.displayName();
    }
    
    public boolean outlineViewWriteItemsToPasteboard( NSOutlineView outlineView, 
            NSArray items, NSPasteboard pboard)
    {
        SchemaItem item = (SchemaItem) items.lastObject();
        
        if (!item.draggable())
        {
            return false;
        }
        else
        {
            NSMutableDictionary dataToDrag = new NSMutableDictionary();
            dataToDrag.setObjectForKey(item.namespace() , "Namespace");
            dataToDrag.setObjectForKey(item.name() , "Name");
            
            NSArray types;
            
            String type = item.type();
            
            types = new NSArray(type);
            
            pboard.declareTypes(types, null);
            pboard.setPropertyListForType(dataToDrag, type);
            
            return true;
        }
    }
    
}
