/* Decompiled by Mocha from ArcNodeList.class */
/* Originally compiled from ArcNodeList.java */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.util.Enumeration;
import java.util.Vector;
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
    Vector array;
    ModelItem currentObject;
    RDFAuthorDocument controller;

    public ArcNodeList(RDFAuthorDocument controller)
    {
        currentObject = null;
        array = new Vector();
        this.controller = controller;
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
        array = (Vector) in.readObject();
        currentObject = (ModelItem) in.readObject();
    }
    
    public void add(ModelItem anObject)
    {
        array.add(anObject);
        anObject.setMyList(this);
    }

    public void deleteObject(ModelItem item)
    {
        array.removeElement(item);
        item.delete();
        if (item == currentObject)
        {
            setCurrentObject(null);
        }
        controller.modelChanged();
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
            nextItem = (ModelItem) array.firstElement();
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
            nextItem = (ModelItem) array.lastElement();
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
                    }
                    else
                    {
                        if ((node.id() == null) && (node.typeNamespace() == null))
                        {
                            node.setJenaNode( memModel.createResource() );
                        }
                        else if (node.typeNamespace() == null)
                        {
                            node.setJenaNode(memModel.createResource( node.id() ));
                        }
                        else if (node.id() == null)
                        {
                            Resource type = memModel.createResource( node.typeNamespace() + node.typeName() );
                            node.setJenaNode( memModel.createResource( type ) );
                        }
                        else
                        {
                            Resource type = memModel.createResource( node.typeNamespace() + node.typeName() );
                            node.setJenaNode( memModel.createResource( node.id(), type ) );
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
                }
            }
            
            StringWriter stringOutput = new StringWriter();
            
            memModel.write(stringOutput, "RDF/XML-ABBREV");
            
            rdfReturned = stringOutput.toString();
        }
        catch (Exception error)
        {
            return null;
        }
        
        return rdfReturned;
    }
    
    public void checkModel(ModelErrorData errorData)
    {
        for (Enumeration enumerator = array.elements(); enumerator.hasMoreElements(); )
        {
            ModelItem item = (ModelItem) enumerator.nextElement();
            
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
                    if ((node.id() != null) && !isValidUrl(node.id()))
                    {
                        errorData.addError(item, 
                            "Id does not have a valid URI. Ids need to be either empty (anonymous), or URIs");
                    }
                    
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
