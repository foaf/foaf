/* Decompiled by Mocha from ArcNodeList.class */
/* Originally compiled from ArcNodeList.java */

/* $Id: ArcNodeList.java,v 1.33 2002-02-19 14:10:04 pldms Exp $ */

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

public class ArcNodeList implements Serializable
{
    // End problems with serialisation - yes!
    
    static final long serialVersionUID = -830262328941904810L;
    
    ArrayList array;
    RDFAuthorDocument controller;
    ArcNodeSelection selection;

    public ArcNodeList(RDFAuthorDocument controller)
    {
        array = new ArrayList();
        this.controller = controller;
        selection = new ArcNodeSelection();
    }
    
    public ArcNodeList(RDFAuthorDocument controller, Reader reader, String type)
                throws RDFException
    {
        array = new ArrayList();
        this.controller = controller;
        selection = new ArcNodeSelection();
        
        // Import from contents of reader
        
        Model memModel = new ModelMem();
        
        memModel.read(reader, ""); // Hmm - what's the base?
        
        HashMap jenaNodeToNode = new HashMap(); 
        
        float x;
        float y;
        
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
                    x = (float) java.lang.Math.random() * 100 + 100;
                    y = (float) java.lang.Math.random() * 100 + 100;
                    // This is dodgy - object might be a literal (I guess)
                    subjectNode = new Node(id, object.toString(), x, y);
                    subjectNode.setMyList(this);
                    jenaNodeToNode.put(subject, subjectNode);
                    array.add(subjectNode);
                }
                else
                {
                    // This is dodgy - see above
                    subjectNode.setType(object.toString());
                }
            }
            else
            {
                // First create nodes (if needed)
                if (subjectNode == null) // never a literal, which makes life easy
                {
                    String id = subject.getURI();
                    if (id != null) id = (id.equals(""))?null:id;
                    x = (float) java.lang.Math.random() * 100 + 100;
                    y = (float) java.lang.Math.random() * 100 + 100;
                    subjectNode = new Node(id, null, null, x, y);
                    subjectNode.setMyList(this);
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
                        x = (float) java.lang.Math.random() * 100 + 100;
                        y = (float) java.lang.Math.random() * 100 + 100;
                        objectNode = new Node(id, null, null, x, y);
                        objectNode.setMyList(this);
                        jenaNodeToNode.put(object, objectNode);
                        array.add(objectNode);
                    }
                    else // Must be a literal
                    {
                        String content = ((Literal) object).getString();
                        content = (content.equals(""))?null:content;
                        x = (float) java.lang.Math.random() * 100 + 100;
                        y = (float) java.lang.Math.random() * 100 + 100;
                        objectNode = new Node(content, null, null, x, y, true); // true - is literal
                        objectNode.setMyList(this);
                        objectNode.literal = true;
                        jenaNodeToNode.put(object, objectNode);
                        array.add(objectNode);
                    }
                }
                
                // Now create the arc
                
                Arc arc = new Arc(subjectNode, objectNode, property.getNameSpace(), property.getLocalName() );
                arc.setMyList(this);
                array.add(arc);
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
        out.writeObject(selection);
    }
    
    private void readObject(java.io.ObjectInputStream in)
     throws IOException, ClassNotFoundException
    {
        // Like node - I changed to ArrayLists from vectors. This gets round an annoying problem
        AbstractList arrayTemp = (AbstractList) in.readObject();
        array = new ArrayList(arrayTemp);

        // I moved from a single selection (ModelItem) to using multiple selections
        // (ArcNodeSelection)
        // This keeps things backwards compatible
        Object selectionObject = in.readObject();
        // FIX: old docs can have 'null' - there was no selection -
        // which was a 'ModelItem' but we get it back untyped.
        if (selectionObject == null)
        {
            selection = new ArcNodeSelection();
        }
        else if (selectionObject instanceof ModelItem) // this is the old way
        {
            selection = new ArcNodeSelection();
            selection.add((ModelItem) selectionObject);
        }
        else
        {
            selection = (ArcNodeSelection) selectionObject;
        }
    }

    public int size()
    {
        return array.size();
    }
    
    // get number of nodes (if true) of arcs (if false)
    
    public int size(boolean getNodes)
    {
        int number = 0;
        for (ListIterator iterator = this.getObjects(); iterator.hasNext(); )
        {
            ModelItem object = (ModelItem) iterator.next();
            if (object.isNode() == getNodes) number++;
        }
        
        return number;
    }
    
    public void add(ModelItem anObject)
    {
        array.add(anObject);
        anObject.setMyList(this);
    }

    public void deleteObject(ModelItem item)
    {
        if (item == selection.selectedObject())
        {
            selectPreviousObject();
        }
        
        removeObject(item);
        item.delete();
        controller.modelChanged();
    }
    
    // modelitems call this themselves - above is for actual deleting
    public void removeObject(ModelItem anObject)
    {
        array.remove(anObject);
        selection.remove(anObject);
    }
    
    public boolean contains(ModelItem anObject)
    {
        return array.contains(anObject);
    }
    
    public void setSelection(ModelItem object)
    {
        selection.set(object);
    }
    
    public void addToSelection(ModelItem object)
    {
        selection.add(object);
    }

    public void selectAll()
    {
        selection.add(array);
    }
    
    public ArcNodeSelection selection()
    {
        return selection;
    }
    
    // Called when user hits backspace
    public void deleteSelection()
    {
        if (selection.kind() == ArcNodeSelection.Empty)
        {
            return;
        }
        
        ModelItem currentObject = selection.selectedObject(); // this might be null, meaning multiple selections
        
        selection.delete();
        
        // if the selection (just deleted) was single then now select the previous object,
        // unless the model is now empty.
        if ((currentObject != null) && !array.isEmpty())
        {
            selection.set(currentObject);
            selectPreviousObject();
        }
    }
    
    public void moveSelectionBy(float dx, float dy)
    {
        selection.moveBy(dx, dy);
    }
    
    public ListIterator getObjects()
    {
        return array.listIterator();
    }
    
    public ListIterator getObjects(boolean reverse) // version of above, but indicate order
    {
        if (reverse)
        {
            return array.listIterator(array.size()); // start at end (if that makes sense ;-)
        }
        else
        {
            return array.listIterator();
        }
    }
    
    public ArcNodeListIterator getNodes()
    {
        return new ArcNodeListIterator(array, true);
    }
        
    public ArcNodeListIterator getArcs()
    {
        return new ArcNodeListIterator(array, false);
    }

    public void selectNextObject()
    {
        ModelItem nextItem;
        
        if (array.size() == 0) return;
        else
        {
            // This only really makes sense for single selections really
            if (selection.kind() == ArcNodeSelection.Single)
            {
                int indexNext = (array.indexOf(selection.selectedObject()) + 1) % array.size();
                nextItem = (ModelItem) array.get( indexNext );
            }
            else nextItem = (ModelItem) array.get(0);
            
            selection.set(nextItem);
        }
    }
    
    public void selectPreviousObject()
    {
        ModelItem nextItem;
        
        if (array.size() == 0) return;
        else
        {
            if (selection.kind() == ArcNodeSelection.Single)
            {
                // The '+ array.size()' counters the posibility that the numerator is negative
                int indexPrevious = (array.indexOf(selection.selectedObject()) + array.size() - 1) % array.size();
                nextItem = (ModelItem) array.get( indexPrevious );
            }
            else nextItem = (ModelItem) array.get(array.size() - 1);
            
            selection.set(nextItem);
        }
    }

    public void itemChanged(ModelItem item)
    {
        controller.modelChanged();
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
    
    public void exportAsRDF(Writer writer, String outputType) throws RDFException
    {
        Model memModel=new ModelMem();
        HashMap nodeToJenaNode = new HashMap();
        
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

        memModel.write(writer, outputType);
        
        //RDFWriter rdfWriter = memModel.getWriter(outputType);
        
        /*for (Iterator iterator = System.getProperties().keySet().iterator(); iterator.hasNext();)
        {
            String key = (String) iterator.next();
            
            if (key.startsWith(RDFWriter.NSPREFIXPROPBASE))
            {
                String value = System.getProperty(key);
                key = key.substring(RDFWriter.NSPREFIXPROPBASE.length(), key.length());
                System.out.println("Found: " + key + " => " + value);
                rdfWriter.setNsPrefix(key, value);
            }
        }*/
        
        //rdfWriter.write( memModel, writer, "" );
        
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
                    if ((node.id() != null) && !RDFAuthorUtilities.isValidURI(node.id()))
                    {
                        errorData.addError(item, 
                            "Id does not have a valid URI. " +
                            "Ids need to be either empty (anonymous), or URIs");
                    }
                    
                    if ((node.typeNamespace() != null) && 
                        !RDFAuthorUtilities.isValidURI(node.typeNamespace() + node.typeName()))
                    {
                        errorData.addError(item,
                            "Type does not have a valid URI. " +
                            "Types need to be either empty (generic resource), or URIs");
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
                        "This connection has no property. This isn't allowed. " +
                        "Properties must be valid URIs only.");
                }
                else if (!RDFAuthorUtilities.isValidURI(arc.propertyNamespace() 
                                                                + arc.propertyName))
                {
                    errorData.addError(item,
                        "This connection's property is not a valid URI.");
                }
            }
        }
    }

}
