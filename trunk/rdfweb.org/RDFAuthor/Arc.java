/* Decompiled by Mocha from Arc.class */
/* Originally compiled from Arc.java */

/* $Id: Arc.java,v 1.17 2002-03-22 17:02:00 pldms Exp $ */

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

import java.io.*;

import com.hp.hpl.mesa.rdf.jena.common.Util;

public class Arc implements Serializable, ModelItem
{
    static final long serialVersionUID = 2402533035356176862L;
    
    Node fromNode;
    Node toNode;
    ArcNodeList myList;
    String propertyName;
    String propertyNamespace;
    boolean showProperty = false;
    
    GraphicalArc graphicArc;

    public Arc(Node fromNode, Node toNode, String namespace, String name)
    {
        this.fromNode = fromNode;
        this.toNode = toNode;
        fromNode.addFromArc(this);
        toNode.addToArc(this);
        this.propertyNamespace = namespace;
        this.propertyName = name;
    }
    
    // Same as before - but split the property into namespace & name
    
    public Arc(Node fromNode, Node toNode, String property)
    {
        this.fromNode = fromNode;
        this.toNode = toNode;
        fromNode.addFromArc(this);
        toNode.addToArc(this);
        
        int sep = Util.splitNamespace(property);
                
        String namespace = property.substring(0, sep);
        String name = property.substring(sep);
        
        this.propertyNamespace = namespace;
        this.propertyName = name;
    }
    
    private void writeObject(java.io.ObjectOutputStream out)
     throws IOException
    {
        out.writeObject(fromNode);
        out.writeObject(toNode);
        out.writeObject(propertyNamespace);
        out.writeObject(propertyName);
        out.writeBoolean(showProperty);
        out.writeObject(myList);
    }
    
    private void readObject(java.io.ObjectInputStream in)
     throws IOException, ClassNotFoundException
    {
        fromNode = (Node) in.readObject();
        toNode = (Node) in.readObject();
        propertyNamespace = (String) in.readObject();
        propertyName = (String) in.readObject();
        showProperty = in.readBoolean();
        myList = (ArcNodeList) in.readObject();
    }
    
    public void setMyList(ArcNodeList list)
    {
        myList = list;
    }
    
    public void setGraphicRep(GraphicalArc graphicArc)
    {
        this.graphicArc = graphicArc;
    }
    
    public GraphicalObject graphicRep()
    {
        return graphicArc;
    }
    
    public void setShowProperty(boolean value)
    {
        showProperty = value;
        if (graphicArc != null) graphicArc.contentChanged();
    }

    public void setProperty(String namespace, String name)
    {
        propertyName = name;
        propertyNamespace = namespace;
        myList.itemChanged(this);
        
        if (graphicArc != null) graphicArc.contentChanged();
    }
    
    // Version of above but property not split
    
    public void setProperty(String property)
    {
        if (property == null)
        {
            setProperty(null, null);
        }
        else
        {
            int sep = Util.splitNamespace(property);
            
            String namespace = property.substring(0, sep);
            String name = property.substring(sep);
            
            setProperty(namespace, name);
        }
    }
    
    public String propertyName()
    {
        return propertyName;
    }
    
    public String propertyNamespace()
    {
        return propertyNamespace;
    }
    
    public Node fromNode()
    {
        return fromNode;
    }
    
    public Node toNode()
    {
        return toNode;
    }
    
    public void delete()
    {
        toNode.removeToArc(this);
        fromNode.removeFromArc(this);
        if (graphicArc != null) graphicArc.delete(); // tell graphical rep that we're dying :-(
        myList.removeObject(this);
    }
    
    public void deleteFromNode(Node node) // Node dies - we die
    {
        if (toNode == node)
        {
            fromNode.removeFromArc(this);
        }
        else
        {
            toNode.removeToArc(this);
        }
        if (graphicArc != null) graphicArc.delete(); // tell graphical rep that we're dying :-(
        myList.removeObject(this);
    }
    
    public void nodeMoved()
    {
        myList.itemChanged(this);
        if (graphicArc != null) graphicArc.boundsChanged();
    }
    
    public boolean isNode()
    {
        return false;
    }
    
    public String displayString() // returns string to draw
    {
        if (!showProperty)
        {
            return null;
        }
        else
        {
            return (propertyName == null)?"-- None --":propertyName;
        }
    }
        
}
