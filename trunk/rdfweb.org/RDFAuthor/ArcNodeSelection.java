//
//  ArcNodeSelection.java
//  RDFAuthor
//

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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;

public class ArcNodeSelection {
    
    static final int Empty = 0;
    static final int Single = 1;
    static final int Multiple = 2;
    
    HashSet selection;
    HashSet nodes; // this is useful since I often want complete graphs
    
    public ArcNodeSelection()
    {
        selection = new HashSet();
        nodes = new HashSet();
    }
    
    public int kind()
    {
        if (selection.isEmpty()) return Empty;
        else if (selection.size() == 1) return Single;
        else return Multiple;
    }
    
    // This is for the useful case where only one thing is selected
    
    public ModelItem selectedObject()
    {
        if (selection.size() == 1) // nuts - why did I use a set!!!
        {
            Iterator iterator = selection.iterator();
            return (ModelItem) iterator.next();
        }
        else return null;
    }
    
    public boolean contains(ModelItem object)
    {
        return selection.contains(object);
    }
    
    public void add(ModelItem object)
    {
        selection.add(object);
        object.graphicRep().changed();
        findNodes();
    }
    
    public void add(Collection objects)
    {
        for (Iterator iterator = objects.iterator(); iterator.hasNext();)
        {
            ModelItem object = (ModelItem) iterator.next();
            selection.add(object);
            object.graphicRep().changed();
        }
        findNodes();
    }
    
    public void set(ModelItem object)
    {
        clear();
        
        if (object != null) // not setting to nothing then...
        {
            selection.add(object);
            object.graphicRep().changed();
            findNodes();
        }
    }
    
    public void remove(ModelItem object)
    {
        selection.remove(object);
        object.graphicRep().changed();
        findNodes();
    }
    
    public void clear()
    {
        for (Iterator iterator = selection.iterator(); iterator.hasNext();)
        {
            ModelItem objectGone = (ModelItem) iterator.next();
            objectGone.graphicRep().changed();
        }
        selection.clear();
    }
    
    public void delete()
    {
        // ModelItem.delete()s modify selection (which iterators hate), so copy it first
        HashSet itemsToGo = new HashSet(selection);
        for (Iterator iterator = itemsToGo.iterator(); iterator.hasNext();)
        {
            ModelItem objectGone = (ModelItem) iterator.next();
            objectGone.delete();
        }
    }
    
    // nodes is very useful - it contains all nodes
    // necessary for the selection to be a complete subgraph
    
    public void findNodes()
    {
        nodes.clear();
        for (Iterator iterator = selection.iterator(); iterator.hasNext();)
        {
            ModelItem object = (ModelItem) iterator.next();
            if (object.isNode())
            {
                nodes.add(object);
            }
            else
            {
                nodes.add(((Arc) object).fromNode());
                nodes.add(((Arc) object).toNode());
            }
        }
    }
    
    public void moveBy(float dx, float dy)
    {
        // Move all elements by (dx,dy)
        // Which is equivalent to moving all nodes in 'nodes'
        
        for (Iterator iterator = nodes.iterator(); iterator.hasNext();)
        {
            Node object = (Node) iterator.next();
            object.moveBy(dx, dy);
        }
    }
    
    // This is hard :-(
    // return an array of copies of all the items. The positions are relativised
    /*
    public Object[] copy()
    {
        HashMap nodeToNewNode = new HashMap();
        
        float minX = 1000000;
        float minY = 1000000;
        
        // first find the minimum x and y values
        
        for (Iterator iterator = nodes.iterator(); iterator.hasNext();)
        {
            Node object = (Node) iterator.next();
            if (minX > object.x()) minX = object.x();
            if (minY > object.y()) minY = object.y();
        }
        
        // Next create copies of the nodes
        
        for (Iterator iterator = nodes.iterator(); iterator.hasNext();)
        {
            Node oldNode = (Node) iterator.next();
            Node newNode = new Node( null, oldNode.id(), oldNode.typeNamespace(),
                    oldNode.typeName, 
                    */
}
