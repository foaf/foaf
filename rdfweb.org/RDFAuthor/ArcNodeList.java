/* Decompiled by Mocha from ArcNodeList.class */
/* Originally compiled from ArcNodeList.java */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.util.Enumeration;
import java.util.Vector;
import java.io.StringWriter;
//import java.io.PrintWriter;
import java.io.*;

import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.mem.*;
import com.hp.hpl.mesa.rdf.jena.common.prettywriter.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.RDF;
import com.hp.hpl.mesa.rdf.jena.vocabulary.RDFS;

public class ArcNodeList implements Serializable
{
    Vector array;
    ModelItem currentObject;
    RDFAuthorDocument controller;

    public ArcNodeList(RDFAuthorDocument controller)
    {
        currentObject = null;
        array = new Vector();
        this.controller = controller;
    }
    
    private void writeObject(java.io.ObjectOutputStream out)
     throws IOException
    {
        System.out.println("Serialising " + this);
        out.writeObject(array);
        System.out.println("Wrote ArcNodeList array");
        out.writeObject(currentObject);
        System.out.println("Wrote currentObject");
    }
    
    private void readObject(java.io.ObjectInputStream in)
     throws IOException, ClassNotFoundException
    {
        array = (Vector) in.readObject();
        currentObject = (ModelItem) in.readObject();
    }
    
    public void add(ModelItem anObject)
    {
        array.add(anObject);
        anObject.setMyList(this);
    }

    public void deleteCurrentObject()
    {
        if (currentObject != null)
        {
            array.removeElement(currentObject);
            currentObject.delete();
            setCurrentObject(null);
            controller.modelChanged();
        }
    }

    public void removeObject(ModelItem anObject)
    {
        array.removeElement(anObject);
    }

    public void drawModel()
    {
        for (Enumeration enumerator = array.elements(); enumerator.hasMoreElements(); )
        {
            ModelItem anObject = (ModelItem)enumerator.nextElement();
            if (anObject == currentObject)
                anObject.drawHilight();
            else
                anObject.drawNormal();
        }
    }

    public ModelItem objectAtPoint(NSPoint point)
    {
        for (Enumeration enumerator = array.elements(); enumerator.hasMoreElements(); )
        {
            ModelItem anObject = (ModelItem)enumerator.nextElement();
            if (anObject.containsPoint(point))
                return anObject;
        }
        return null;
    }

    public void setCurrentObject(ModelItem anObject)
    {
        currentObject = anObject;
        controller.modelChanged();
        controller.currentObjectChanged();
    }

    public ModelItem currentObject()
    {
        return currentObject;
    }
    
    public void itemChanged(ModelItem item)
    {
        controller.modelChanged();
        if (item == currentObject)
        {
            controller.currentObjectChanged(); // for the info wndow
        }
    }
    
    public void showTypes(boolean value)
    {
        for (Enumeration enumerator = array.elements(); enumerator.hasMoreElements(); )
        {
            ModelItem anObject = (ModelItem)enumerator.nextElement();
            if (anObject.isNode())
            {
                ((Node) anObject).setShowType(value);
            }
        }
        
        controller.modelChanged();
    }
    
    public void showIds(boolean value)
    {
        for (Enumeration enumerator = array.elements(); enumerator.hasMoreElements(); )
        {
            ModelItem anObject = (ModelItem)enumerator.nextElement();
            if (anObject.isNode())
            {
                ((Node) anObject).setShowId(value);
            }
        }
        
        controller.modelChanged();
    }
    
    public void showProperties(boolean value)
    {
        for (Enumeration enumerator = array.elements(); enumerator.hasMoreElements(); )
        {
            ModelItem anObject = (ModelItem)enumerator.nextElement();
            if (!anObject.isNode())
            {
                ((Arc) anObject).setShowProperty(value);
            }
        }
        
        controller.modelChanged();
    }
    
    public String exportAsRDF()
    {
        Model memModel=new ModelMem();
        String rdfReturned = null;
        
        // Wrap this in one big try/catch
        
        try {
            // Create each node
            
            for (Enumeration enumerator = array.elements(); enumerator.hasMoreElements(); )
            {
                ModelItem item = (ModelItem) enumerator.nextElement();
                if (item.isNode())
                {
                    Node node = (Node) item;
                    
                    if (node.isLiteral())
                    {
                        String id = (node.id() == null)?"":node.id();
                        node.setJenaNode(memModel.createLiteral(id));
                        System.out.println("Created Literal: " + node.id());
                    }
                    else
                    {
                        if ((node.id() == null) && (node.typeNamespace() == null))
                        {
                            node.setJenaNode( memModel.createResource() );
                            System.out.println("Created anon, untyped node");
                        }
                        else if (node.typeNamespace() == null)
                        {
                            node.setJenaNode(memModel.createResource( node.id() ));
                            System.out.println("Created untyped node: " + node.id());
                        }
                        else if (node.id() == null)
                        {
                            Resource type = memModel.createResource( node.typeNamespace() + node.typeName() );
                            node.setJenaNode( memModel.createResource( type ) );
                            System.out.println("Created anonymous node of type: " + type);
                        }
                        else
                        {
                            Resource type = memModel.createResource( node.typeNamespace() + node.typeName() );
                            node.setJenaNode( memModel.createResource( node.id(), type ) );
                            System.out.println("Created node: " + node.id() + " Type: "+type);
                        }
                    }
                }
            }
            
            // Create arcs
            
            for (Enumeration enumerator = array.elements(); enumerator.hasMoreElements(); )
            {
                ModelItem item = (ModelItem) enumerator.nextElement();
                if (!item.isNode())
                {
                    Arc arc = (Arc) item;
                    Property property = memModel.createProperty( arc.propertyNamespace() +
                            arc.propertyName() );
                    memModel.add((Resource) arc.fromNode().jenaNode(), 
                            property, arc.toNode().jenaNode() );
                    System.out.println("Created statement: " + arc.fromNode().jenaNode() + 
                            property + arc.toNode().jenaNode() );
                }
            }
            
            StringWriter stringOutput = new StringWriter();
            
            memModel.write(stringOutput);
            
            rdfReturned = stringOutput.toString();
        }
        catch (Exception error)
        {
            NSAlertPanel alert = new NSAlertPanel();
            alert.runAlert("RDF/XML Export Failed",
                "Export failed, I'm afraid. Try using 'Check Model' for possible problems.",
                null, null, null);
        }
        
        return rdfReturned;
    }

}
