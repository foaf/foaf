/* Decompiled by Mocha from Node.class */
/* Originally compiled from Node.java */

/* $Id: Node.java,v 1.22 2002-01-06 22:15:29 pldms Exp $ */

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

import java.util.ListIterator;
import java.util.ArrayList;
import java.util.AbstractList;
import java.io.*;

import com.hp.hpl.mesa.rdf.jena.common.Util;

public class Node implements Serializable, ModelItem
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
    
    float x;
    float y;
    
    GraphicalNode graphicNode;

    public Node(String id, String typeNamespace, String typeName, float x, float y)
    {
        literal = false;
        this.id = id;
        this.x = x;
        this.y = y;
        this.typeNamespace = typeNamespace;
        this.typeName = typeName;
        arcsFrom = new ArrayList();
        arcsTo = new ArrayList();
    }

    // As before - but set literal
    
    public Node(String id, String typeNamespace, String typeName, float x, float y, boolean literal)
    {
        this.literal = literal;
        this.id = id;
        this.x = x;
        this.y = y;
        this.typeNamespace = typeNamespace;
        this.typeName = typeName;
        arcsFrom = new ArrayList();
        arcsTo = new ArrayList();
    }
    
    // Same as before - but split the type into namespace & name
    
    public Node(String id, String type, float x, float y)
    {
        literal = false;
        this.id = id;
        this.x = x;
        this.y = y;
        
        int sep = Util.splitNamespace(type);
                
        String namespace = type.substring(0, sep);
        String name = type.substring(sep);
        
        typeNamespace = namespace;
        typeName = name;
        
        arcsFrom = new ArrayList();
        arcsTo = new ArrayList();
    } 
        
    private void writeObject(java.io.ObjectOutputStream out)
     throws IOException
    {
        out.writeFloat(x);
        out.writeFloat(y);
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
        x = in.readFloat();
        y = in.readFloat();
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
    }
    
    public void setGraphicRep(GraphicalNode graphicNode)
    {
        this.graphicNode = graphicNode;
    }
    
    public GraphicalObject graphicRep()
    {
        return graphicNode;
    }
    
    public boolean isConnected()
    {
        return (!arcsFrom.isEmpty() || !arcsTo.isEmpty());
    }
    
    public void setId(String theString)
    {
        id = theString;
        myList.itemChanged(this);
        if (graphicNode != null) graphicNode.calculateSize();
    }

    public String id()
    {
        return id;
    }

    public void setType(String namespace, String name)
    {
        typeNamespace = namespace;
        typeName = name;
        myList.itemChanged(this);
        if (graphicNode != null) graphicNode.calculateSize();
    }
    
    // Version of above for unsplit types
    
    public void setType(String type)
    {
        if (type == null)
        {
            setType(null, null);
        }
        else
        {
            int sep = Util.splitNamespace(type);
            
            String namespace = type.substring(0, sep);
            String name = type.substring(sep);
            
            setType(namespace, name);
        }
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
        myList.itemChanged(this);
        if (graphicNode != null) graphicNode.calculateSize();
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
        if (graphicNode != null) graphicNode.delete(); // Inform graphic node that we're going away
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
    
    public float x()
    {
        return x;
    }
    
    public float y()
    {
        return y;
    }

    public void setPosition(float x, float y)
    {
        this.x = x;
        this.y = y;
        
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
        
        myList.itemChanged(this);
        if (graphicNode != null) graphicNode.calculateRectangle();
    }
    
    public void setPositionDumb(float x, float y)
    {
        this.x = x;
        this.y = y;
        
        myList.itemChanged(this);
        if (graphicNode != null) graphicNode.calculateRectangle();
    }
    
    public void moveBy(float dx, float dy)
    {
        setPosition(x + dx, y + dy);
    }
    
    public boolean isNode()
    {
        return true;
    }
    
    public void setShowType(boolean value)
    {
        showType = value;
        if (graphicNode != null) graphicNode.calculateSize();
    }
    
    public void setShowId(boolean value)
    {
        showId = value;
        if (graphicNode != null) graphicNode.calculateSize();
    }
    
    public String displayString() // string to display
    {
        if (!showType && !showId)
        {
            return null;
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
            
            //typeToShow = isLiteral()?"-- literal --":typeToShow;
            
            if (showType && showId)
            {
                return typeToShow + "\n" + idToShow;
            }
            else if (showType)
            {
                return typeToShow;
            }
            else
            {
                return idToShow;
            }
        }
    }
    
}
