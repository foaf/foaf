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


import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.util.HashSet;
import java.util.Iterator;

public class ArcNodeSelection {
    
    static final int IsEmpty = 0;
    static final int IsSingle = 1;
    static final int IsMultiple = 2;
    
    HashSet selection;
    HashSet nodes; // this is useful since I often want complete graphs
    
    public ArcNodeSelection()
    {
        selection = new HashSet();
        nodes = new HashSet();
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
    
    public void set(ModelItem object)
    {
        for (Iterator iterator = selection.iterator(); iterator.hasNext();)
        {
            ModelItem objectGone = (ModelItem) iterator.next();
            objectGone.graphicRep().changed();
        }
        selection.clear();
        if (object != null)
        {
            selection.add(object);
            object.graphicRep().changed();
        }
        findNodes();
    }
    
    public void remove(ModelItem object)
    {
        selection.remove(object);
        object.graphicRep().changed();
        findNodes();
    }
    
    // Find nodes is very useful - it contains all nodes
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
}
