/* Decompiled by Mocha from Node.class */
/* Originally compiled from Node.java */

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

import java.util.ListIterator;
import java.util.ArrayList;
import java.util.AbstractList;
import java.io.*;

import com.hp.hpl.mesa.rdf.jena.common.Util;

public class Node extends ModelItem implements Serializable
{
    static final long serialVersionUID = 8496964442985450307L;
    
    String id;
    String typeNamespace;
    String typeName;
    ArrayList arcsFrom;
    ArrayList arcsTo;
    ArcNodeList myList;
    boolean literal;
    boolean showType = false;
    boolean showId = false;
    
    NSPoint position;
    NSColor normalColor = NSColor.colorWithCalibratedRGB(0F, 1F, 0F, 0.5F);
    NSColor literalColor = NSColor.colorWithCalibratedRGB(1F, 1F, 0F, 0.5F);
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
        arcsFrom = new ArrayList();
        arcsTo = new ArrayList();
    }
   
    // Same as before - but split the type into namespace & name
    
    public Node(ArcNodeList myList, String id, String type, NSPoint position)
    {
        this.myList = myList;
        literal = false;
        this.id = id;
        this.position = position;
        
        int sep = Util.splitNamespace(type);
                
        String namespace = type.substring(0, sep);
        String name = type.substring(sep);
        
        setType(namespace, name);
        
        arcsFrom = new ArrayList();
        arcsTo = new ArrayList();
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
        // The following spares some pain when I changed from Vectors to Array Lists
        AbstractList arcsFromTemp = (AbstractList) in.readObject();
        AbstractList arcsToTemp = (AbstractList) in.readObject();
        arcsFrom = new ArrayList(arcsFromTemp);
        arcsTo = new ArrayList(arcsToTemp);
        literal = in.readBoolean();
        showType = in.readBoolean();
        showId = in.readBoolean();
        myList = (ArcNodeList) in.readObject();
    
	normalColor = NSColor.colorWithCalibratedRGB(0F, 1F, 0F, 0.5F);
	hilightColor = NSColor.colorWithCalibratedRGB(1F, 0F, 0F, 0.5F);
        literalColor = NSColor.colorWithCalibratedRGB(1F, 1F, 0F, 0.5F);

	defaultSize = new NSSize(20,20);
        
        calculateSize();
    }
    
    public NSRect rect()
    {
        return myRect;
    }
    
    public boolean isConnected()
    {
        return (!arcsFrom.isEmpty() || !arcsTo.isEmpty());
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
    
    // Version of above for unsplit types
    
    public void setType(String type)
    {
        int sep = Util.splitNamespace(type);
                
        String namespace = type.substring(0, sep);
        String name = type.substring(sep);
        
        setType(namespace, name);
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
        for (ListIterator enumerator = arcsFrom.listIterator(); enumerator.hasNext(); )
        {
            Arc arc = (Arc)enumerator.next();
            arc.deleteFromNode(this);
        }
        for (ListIterator enumerator = arcsTo.listIterator(); enumerator.hasNext(); )
        {
            Arc arc = (Arc)enumerator.next();
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
        for (ListIterator enumerator = arcsFrom.listIterator(); enumerator.hasNext(); )
        {
            Arc arc = (Arc)enumerator.next();
            arc.nodeMoved();
        }
        for (ListIterator enumerator = arcsTo.listIterator(); enumerator.hasNext(); )
        {
            Arc arc = (Arc)enumerator.next();
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
        if (this.isLiteral())
        {
            drawMe(literalColor);
        }
        else
        {
            drawMe(normalColor);
        }
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
