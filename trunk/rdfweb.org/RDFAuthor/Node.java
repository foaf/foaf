/* Decompiled by Mocha from Node.class */
/* Originally compiled from Node.java */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.util.Enumeration;
import java.util.Vector;
import java.io.*;

import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.mem.*;
import com.hp.hpl.mesa.rdf.jena.common.prettywriter.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.RDF;
import com.hp.hpl.mesa.rdf.jena.vocabulary.RDFS;

public class Node extends ModelItem implements Serializable
{
    String id;
    String typeNamespace;
    String typeName;
    Vector arcsFrom;
    Vector arcsTo;
    ArcNodeList myList;
    boolean literal;
    boolean showType = false;
    boolean showId = false;
    RDFNode jenaNode;
    
    NSPoint position;
    NSColor normalColor = NSColor.colorWithCalibratedRGB(0F, 1F, 0F, 0.5F);
    NSColor hilightColor = NSColor.colorWithCalibratedRGB(1F, 0F, 0F, 0.5F);
    NSSize mySize;
    NSSize defaultSize = new NSSize(20,20);
    NSRect myRect;
    NSAttributedString displayString = null;

    public Node(ArcNodeList myList, String id, String typeNamespace, String typeName, NSPoint position)
    {
        this.myList = myList;
        literal = false;
        this.id = id;
        this.position = position;
        setType(typeNamespace,typeName);
        arcsFrom = new Vector();
        arcsTo = new Vector();
    }
    
        
    private void writeObject(java.io.ObjectOutputStream out)
     throws IOException
    {
        out.writeFloat(position.x());
        out.writeFloat(position.y());
        out.writeObject(id);
        out.writeObject(typeNamespace);
        out.writeObject(typeName);
        out.writeObject(arcsFrom);
        out.writeObject(arcsTo);
        out.writeBoolean(literal);
        out.writeBoolean(showType);
        out.writeBoolean(showId);
        out.writeObject(myList);
    }
    
    private void readObject(java.io.ObjectInputStream in)
     throws IOException, ClassNotFoundException
    {
        float x = in.readFloat();
        float y = in.readFloat();
        position = new NSPoint(x, y);
        id = (String) in.readObject();
        typeNamespace = (String) in.readObject();
        typeName = (String) in.readObject();
        arcsFrom = (Vector) in.readObject();
        arcsTo = (Vector) in.readObject();
        literal = in.readBoolean();
        showType = in.readBoolean();
        showId = in.readBoolean();
        myList = (ArcNodeList) in.readObject();
    
	normalColor = NSColor.colorWithCalibratedRGB(0F, 1F, 0F, 0.5F);
	hilightColor = NSColor.colorWithCalibratedRGB(1F, 0F, 0F, 0.5F);
	defaultSize = new NSSize(20,20);
        
        calculateSize();
    }
    
    public void setJenaNode(RDFNode theNode)
    {
        jenaNode = theNode;
    }
    
    public RDFNode jenaNode()
    {
        return jenaNode;
    }
    
    public void setId(String theString)
    {
        id = theString;
        calculateSize();
        myList.itemChanged(this);
    }

    public String id()
    {
        return id;
    }

    public void setType(String namespace, String name)
    {
        typeNamespace = namespace;
        typeName = name;
        calculateSize();
        myList.itemChanged(this);
    }

    public String typeNamespace()
    {
        return typeNamespace;
    }
    
    public String typeName()
    {
        return typeName;
    }
    
    public boolean isLiteral()
    {
        return literal;
    }

    public void setIsLiteral(boolean literalValue)
    {
        literal = literalValue;
        calculateSize();
        myList.itemChanged(this);
    }

    public void setMyList(ArcNodeList list)
    {
        myList = list;
    }

    public void delete()
    {
        for (Enumeration enumerator = arcsFrom.elements(); enumerator.hasMoreElements(); )
        {
            Arc arc = (Arc)enumerator.nextElement();
            arc.deleteFromNode(this);
        }
        for (Enumeration enumerator = arcsTo.elements(); enumerator.hasMoreElements(); )
        {
            Arc arc = (Arc)enumerator.nextElement();
            arc.deleteFromNode(this);
        }
        myList.removeObject(this);
    }

    public void addToArc(Arc arc)
    {
        arcsTo.add(arc);
    }

    public void removeToArc(Arc arc)
    {
        arcsTo.remove(arc);
    }

    public void addFromArc(Arc arc)
    {
        arcsFrom.add(arc);
    }

    public void removeFromArc(Arc arc)
    {
        arcsFrom.remove(arc);
    }

    public void setPosition(NSPoint position)
    {
        this.position = position;
        for (Enumeration enumerator = arcsFrom.elements(); enumerator.hasMoreElements(); )
        {
            Arc arc = (Arc)enumerator.nextElement();
            arc.nodeMoved();
        }
        for (Enumeration enumerator = arcsTo.elements(); enumerator.hasMoreElements(); )
        {
            Arc arc = (Arc)enumerator.nextElement();
            arc.nodeMoved();
        }
        calculateRectangle();
        myList.itemChanged(this);
    }

    public boolean isNode()
    {
        return true;
    }
    
    public void setShowType(boolean value)
    {
        showType = value;
        calculateSize();
    }
    
    public void setShowId(boolean value)
    {
        showId = value;
        calculateSize();
    }
    
    public void drawNormal()
    {
        drawMe(normalColor);
    }

    public void drawHilight()
    {
        drawMe(hilightColor);
    }

    public void drawMe(NSColor myColor)
    {
        myColor.set();
        
        NSBezierPath.bezierPathWithOvalInRect(myRect).fill();
        if (displayString != null)
        {
            NSGraphics.drawAttributedString(displayString, myRect);
        }
    }

    public boolean containsPoint(NSPoint point)
    {
        return myRect.containsPoint(point, true); // RDFModelView always flipped
    }

    public NSPoint position()
    {
        return position;
    }
    
    public void calculateSize()
    {
        if (!showType && !showId)
        {
            mySize = defaultSize;
            displayString = null;
        }
        else
        {
            String typeToShow;
            String idToShow;
            
            if (isLiteral())
            {
                typeToShow = "-- literal --";
                idToShow = (id == null)?"-- empty --":"\"" + id + "\"";
            }
            else
            {
                typeToShow = (typeName == null)?"-- resource --":typeName;
                idToShow = (id == null)?"-- anonymous --":id;
            }
            
            typeToShow = isLiteral()?"-- literal --":typeToShow;
            
            if (showType && showId)
            {
                displayString = new NSAttributedString(typeToShow + "\n" + idToShow);
            }
            else if (showType)
            {
                displayString = new NSAttributedString(typeToShow);
            }
            else
            {
                displayString = new NSAttributedString(idToShow);
            }
            
            mySize = NSGraphics.sizeOfAttributedString(displayString);
        }
        
        calculateRectangle();
    }
    
    public void calculateRectangle()
    {
        myRect = new NSRect(position.x() - mySize.width()/2F,
                            position.y() - mySize.height()/2F,
                            mySize.width(),
                            mySize.height() );
    }
}
