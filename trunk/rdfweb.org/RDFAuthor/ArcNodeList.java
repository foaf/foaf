/* Decompiled by Mocha from ArcNodeList.class */
/* Originally compiled from ArcNodeList.java */

/*
    Copyright 2001 Damian Steer <dm_steer@hotmail.com>, Libby Miller <libby.miller@bristol.ac.uk>

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

import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.AbstractList;
import java.util.HashMap;
import java.io.StringWriter;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;

import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.mem.*;
import com.hp.hpl.mesa.rdf.jena.common.prettywriter.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.RDF;
import com.hp.hpl.mesa.rdf.jena.vocabulary.RDFS;

public class ArcNodeList extends java.lang.Object implements Serializable
{
    // End problems with serialisation - yes!
    
    static final long serialVersionUID = -830262328941904810L;
    
    ArrayList array;
    ModelItem currentObject;
    RDFAuthorDocument controller;

    public ArcNodeList(RDFAuthorDocument controller)
    {
        currentObject = null;
        array = new ArrayList();
        this.controller = controller;
    }
    
    public ArcNodeList(RDFAuthorDocument controller, Reader reader, String type)
                throws RDFException
    {
        currentObject = null;
        array = new ArrayList();
        this.controller = controller;
        
        // Import from contents of reader
        
        Model memModel = new ModelMem();
        
        memModel.read(reader, ""); // Hmm - what's base?
        
        HashMap jenaNodeToNode = new HashMap(); 
        HashMap nodeToX = new HashMap();
        HashMap nodeToY = new HashMap();
        
        float x = 70; // nasty out layout. Improve me!
        float y = 70;
        
        float minX = 100000; // these are for calculating the extent of the nodes
        float minY = 100000;
        float maxX = -100000;
        float maxY = -100000;
        
        for (StmtIterator iterator = memModel.listStatements(); iterator.hasNext(); )
        {
            Statement statement = iterator.next();
            RDFNode object = statement.getObject();
            Resource subject = statement.getSubject();
            Property property = statement.getPredicate();
            
            Node subjectNode = (Node) jenaNodeToNode.get(subject);
            Node objectNode = (Node) jenaNodeToNode.get(object);
            
            // If this statement sets the type of the subject node then
            // set the type of the node
            
            if (property.equals(RDF.type))
            {
                if (subjectNode == null) // not added yet
                {
                    String id = subject.getURI();
                    if (id != null) id = (id.equals(""))?null:id;
                    // This is dodgy - object might be a literal (I guess)
                    subjectNode = new Node(this, id, object.toString(),
                        new NSPoint( x, y));
                    x += 70; y += 10;
                    if (x > 400F) x = 70;
                    jenaNodeToNode.put(subject, subjectNode);
                    array.add(subjectNode);
                }
                else
                {
                    // This is dodgy - see above
                    subjectNode.setType(object.toString());
                }
            }
            // Ok - this is a special hack for embedded positioning info
            else if (property.toString().equals("http://rdfweb.org/people/damian/2001/10/RDFAuthor/schema/x")) // x coord
            {
                if (subjectNode == null) // not added yet
                {
                    String id = subject.getURI();
                    if (id != null) id = (id.equals(""))?null:id;
                    subjectNode = new Node(this, id, object.toString(),
                        new NSPoint( x, y));
                    x += 70; y += 10;
                    if (x > 400F) x = 70;
                    jenaNodeToNode.put(subject, subjectNode);
                    array.add(subjectNode);
                }
                
                Float xVal = new Float(object.toString());
                
                nodeToX.put(subjectNode, xVal);
                
                float xpos = xVal.floatValue();
                
                if ((maxX == -100000) && (minX == 100000)) // neither set yet
                {
                    maxX = xpos;
                    minX = xpos;
                }
                else
                {
                    if (xpos > maxX) maxX = xpos;
                    if (xpos < minX) minX = xpos;
                }
            }
            else if (property.toString().equals("http://rdfweb.org/people/damian/2001/10/RDFAuthor/schema/y")) // y coord
            {
                if (subjectNode == null) // not added yet
                {
                    String id = subject.getURI();
                    if (id != null) id = (id.equals(""))?null:id;
                    subjectNode = new Node(this, id, object.toString(),
                        new NSPoint( x, y));
                    x += 70; y += 10;
                    if (x > 400F) x = 70;
                    jenaNodeToNode.put(subject, subjectNode);
                    array.add(subjectNode);
                }
                
                Float yVal = new Float(object.toString());
                
                nodeToY.put(subjectNode, yVal);
                
                float ypos = yVal.floatValue();
                
                if ((maxY == -100000) && (minY == 100000)) // neither set yet
                {
                    maxY = ypos;
                    minY = ypos;
                }
                else
                {
                    if (ypos > maxY) maxY = ypos;
                    if (ypos < minY) minY = ypos;
                }
            }
            else
            {
                // First create nodes (if needed)
                if (subjectNode == null) // never a literal, which makes life easy
                {
                    String id = subject.getURI();
                    if (id != null) id = (id.equals(""))?null:id;
                    subjectNode = new Node(this, id, null, null, new NSPoint( x, y));
                    x += 70; y += 10;
                    if (x > 400F) x = 70;
                    jenaNodeToNode.put(subject, subjectNode);
                    array.add(subjectNode);
                }
                if (objectNode == null)
                {
                    // Is this a resource?
                    if (object instanceof Resource)
                    {
                        String id = ((Resource) object).getURI();
                        if (id != null) id = (id.equals(""))?null:id;
                        objectNode = new Node(this, id, null, null, new NSPoint( x, y));
                        x += 70; y += 10;
                        if (x > 400F) x = 70;
                        jenaNodeToNode.put(object, objectNode);
                        array.add(objectNode);
                    }
                    else // Must be a literal
                    {
                        String content = ((Literal) object).getString();
                        content = (content.equals(""))?null:content;
                        objectNode = new Node(this, content, null, null, new NSPoint( x, y));
                        x += 70; y += 10;
                        if (x > 400F) x = 70;
                        objectNode.setIsLiteral(true);
                        jenaNodeToNode.put(object, objectNode);
                        array.add(objectNode);
                    }
                }
                
                // Now create the arc
                
                Arc arc = new Arc( this, subjectNode, objectNode, property.getNameSpace(), property.getLocalName() );
                array.add(arc);
            }
        }
        
        // Deal with the extent
        
        System.out.println("Extent is: (" + minX + "," + minY + "," + maxX + "," + maxY +")");
        
        
        
        // Ok - now look at coord info (if any)
        
        for (Iterator iterator = this.getNodes(); iterator.hasNext();)
        {
            Node node = (Node) iterator.next();
            if ((nodeToX.get(node) != null) && (nodeToY.get(node) != node)) // if we have both coords
            {
                float xval = ((Float) nodeToX.get(node)).floatValue() - minX + 20;
                float yval = ((Float) nodeToY.get(node)).floatValue() - minY + 20;
                
                node.setPosition(new NSPoint(xval, yval));
            }
        }
    }
    
    public void setController(RDFAuthorDocument controller)
    {
        this.controller = controller;
    }
    
    private void writeObject(java.io.ObjectOutputStream out)
     throws IOException
    {
        out.writeObject(array);
        out.writeObject(currentObject);
    }
    
    private void readObject(java.io.ObjectInputStream in)
     throws IOException, ClassNotFoundException
    {
        // Like node - I changed to ArrayLists from vectors. This gets round an annoying problem
        AbstractList arrayTemp = (AbstractList) in.readObject();
        array = new ArrayList(arrayTemp);
        currentObject = (ModelItem) in.readObject();
    }
    
    public void add(ModelItem anObject)
    {
        array.add(anObject);
        anObject.setMyList(this);
    }

    public void deleteObject(ModelItem item)
    {
        if (item == currentObject)
        {
            selectPreviousObject();
        }
        
        array.remove(item);
        item.delete();
        controller.modelChanged();
    }

    public void removeObject(ModelItem anObject)
    {
        array.remove(anObject);
    }
    
    public boolean contains(ModelItem anObject)
    {
        return array.contains(anObject);
    }
    
    public ListIterator getObjects()
    {
        return array.listIterator();
    }
    
    public ArcNodeListIterator getNodes()
    {
        return new ArcNodeListIterator(array, true);
    }
        
    public ArcNodeListIterator getArcs()
    {
        return new ArcNodeListIterator(array, false);
    }
        
    public void drawModel(NSRect rect)
    {
        /*
        for (ListIterator enumerator = array.listIterator(); enumerator.hasNext(); )
        {
            ModelItem anObject = (ModelItem)enumerator.next();
            if (anObject == currentObject)
                anObject.drawHilight();
            else
                anObject.drawNormal();
        }
        */
        for (Iterator iterator = this.getArcs(); iterator.hasNext();)
        {
            ModelItem anObject = (ModelItem) iterator.next();
            if (anObject == currentObject)
                anObject.drawHilight(rect);
            else
                anObject.drawNormal(rect);
        }
        for (Iterator iterator = this.getNodes(); iterator.hasNext();)
        {
            ModelItem anObject = (ModelItem) iterator.next();
            if (anObject == currentObject)
                anObject.drawHilight(rect);
            else
                anObject.drawNormal(rect);
        }
    }

    public ModelItem objectAtPoint(NSPoint point)
    {
        // This has to go backwards, since they are displayed in the opposite way
        for (int index = array.size() - 1; index >= 0; index --)
        {
            ModelItem item = (ModelItem) array.get(index);
            if (item.containsPoint(point))
            {
                return item;
            }
        }

        return null;
    }

    public void setCurrentObject(ModelItem anObject)
    {
        currentObject = anObject;
        controller.modelChanged();
        controller.currentObjectChanged();
    }
    
    public void selectNextObject()
    {
        ModelItem nextItem;
        
        if (array.size() == 0)
        {
            return;
        }
        else if (currentObject == null)
        {
            nextItem = (ModelItem) array.get(0);
        }
        else
        {
            int indexNext = (array.indexOf(currentObject) + 1) % array.size();
            nextItem = (ModelItem) array.get( indexNext );
        }
        
        setCurrentObject(nextItem);
    }
    
    public void selectPreviousObject()
    {
        ModelItem nextItem;
        
        if (array.size() == 0)
        {
            return;
        }
        else if (currentObject == null)
        {
            nextItem = (ModelItem) array.get(array.size() - 1);
        }
        else
        {
            // Hmm - the '+ array.size()' counters the posibility that the numerator is negative
            int indexPrevious = (array.indexOf(currentObject) + array.size() - 1) % array.size();
            nextItem = (ModelItem) array.get( indexPrevious );
        }
        
        setCurrentObject(nextItem);
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
    
    public void itemChanged(ModelItem item, NSRect rect) // variation of above where a rect needs redrawing
    {
        controller.modelChanged(rect);
        if (item == currentObject)
        {
            controller.currentObjectChanged(); // for the info wndow
        }
    }
    
    public void showTypes(boolean value)
    {
        for (ListIterator enumerator = array.listIterator(); enumerator.hasNext(); )
        {
            ModelItem anObject = (ModelItem)enumerator.next();
            if (anObject.isNode())
            {
                ((Node) anObject).setShowType(value);
            }
        }
        
        controller.modelChanged();
    }
    
    public void showIds(boolean value)
    {
        for (ListIterator enumerator = array.listIterator(); enumerator.hasNext(); )
        {
            ModelItem anObject = (ModelItem)enumerator.next();
            if (anObject.isNode())
            {
                ((Node) anObject).setShowId(value);
            }
        }
        
        controller.modelChanged();
    }
    
    public void showProperties(boolean value)
    {
        for (ListIterator enumerator = array.listIterator(); enumerator.hasNext(); )
        {
            ModelItem anObject = (ModelItem)enumerator.next();
            if (!anObject.isNode())
            {
                ((Arc) anObject).setShowProperty(value);
            }
        }
        
        controller.modelChanged();
    }
    
    public String exportAsRDF(String outputType)
    {
        Model memModel=new ModelMem();
        String rdfReturned = null;
        HashMap nodeToJenaNode = new HashMap();
        
        // Wrap this in one big try/catch
        
        try {
            // Create each node
            
            for (Iterator enumerator = this.getNodes(); enumerator.hasNext(); )
            {
                Node node = (Node) enumerator.next();
                    
                if (node.isLiteral())
                {
                    String id = (node.id() == null)?"":node.id();
                    nodeToJenaNode.put(node, memModel.createLiteral(id));
                }
                else
                {
                    if ((node.id() == null) && (node.typeNamespace() == null))
                    {
                        nodeToJenaNode.put(node, memModel.createResource() );
                    }
                    else if (node.typeNamespace() == null)
                    {
                        nodeToJenaNode.put(node, memModel.createResource( node.id() ));
                    }
                    else if (node.id() == null)
                    {
                        Resource type = memModel.createResource( node.typeNamespace() + node.typeName() );
                        nodeToJenaNode.put(node, memModel.createResource( type ) );
                    }
                    else
                    {
                        Resource type = memModel.createResource( node.typeNamespace() + node.typeName() );
                        nodeToJenaNode.put(node, memModel.createResource( node.id(), type ) );
                    }
                }
            }
            
            // Create arcs
            
            for (Iterator enumerator = this.getArcs(); enumerator.hasNext(); )
            {
                Arc arc = (Arc) enumerator.next();
                
                Property property = memModel.createProperty( arc.propertyNamespace() +
                        arc.propertyName() );
                memModel.add( (Resource) nodeToJenaNode.get( arc.fromNode() ), 
                        property, (RDFNode) nodeToJenaNode.get( arc.toNode() ) );
            }
            
            StringWriter stringOutput = new StringWriter();
            
            memModel.write(stringOutput, outputType);
            
            rdfReturned = stringOutput.toString();
        }
        catch (Exception error)
        {
            System.err.println("Error serialising: " + error);
            return null;
        }
        
        return rdfReturned;
    }
    
    public void checkModel(ModelErrorData errorData)
    {
        for (ListIterator enumerator = array.listIterator(); enumerator.hasNext(); )
        {
            ModelItem item = (ModelItem) enumerator.next();
            
            if (item.isNode())
            {
                Node node = (Node) item;
                
                if (node.isLiteral())
                {
                    if (node.id() == null)
                    {
                        errorData.addWarning(item, "This literal has no content. Is this what you wanted?");
                    }
                }
                else
                {
                    // Removed this check - let in everything
                    //if ((node.id() != null) && !isValidUrl(node.id()))
                    //{
                    //    errorData.addError(item, 
                    //        "Id does not have a valid URI. Ids need to be either empty (anonymous), or URIs");
                    //}
                    
                    if ((node.typeNamespace() != null) && !isValidUrl(node.typeNamespace() + node.typeName()))
                    {
                        errorData.addError(item,
                            "Type does not have a valid URI. Types need to be either empty (generic resource), or URIs");
                    }
                }
            }
            else
            {
                Arc arc = (Arc) item;
                
                if (arc.fromNode().isLiteral())
                {
                    errorData.addError(item,
                        "This connection starts from a literal. This isn't allowed.");
                }
                
                if (arc.propertyNamespace() == null)
                {
                    errorData.addError(item,
                        "This connection has no property. This isn't allowed. Properties must be valid URIs only.");
                }
                else if (!isValidUrl(arc.propertyNamespace() + arc.propertyName))
                {
                    errorData.addError(item,
                        "This connection's property is not a valid URI.");
                }
            }
        }
    }
            
    public boolean isValidUrl(String url)
    {
        try
        {
            URL temp = new URL(url);
            return true;
        }
        catch (MalformedURLException error)
        {
            return false;
        }
    }
}
