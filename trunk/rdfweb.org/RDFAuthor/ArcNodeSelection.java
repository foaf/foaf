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
    
    public ArcNodeSelection()
    {
        selection = new HashSet();
    }
    
    public boolean contains(ModelItem object)
    {
        return selection.contains(object);
    }
    
    public void add(ModelItem object)
    {
        selection.add(object);
    }
    
    public void set(ModelItem object)
    {
        selection.clear();
        selection.add(object);
    }
    
    public void remove(ModelItem object)
    {
        selection.remove(object);
    }
    
    public void moveBy(float dx, float dy)
    {
        // Move all elements by (dx,dy)
        
        for (Iterator iterator = selection.iterator(); iterator.hasNext();)
        {
            ModelItem object = (ModelItem) iterator.next();
            
            if (object.isNode())
            {
                ((Node) object).moveBy(dx, dy);
            }
            else
            {
                Arc arc = (Arc) object;
                Node toNode = arc.toNode();
                Node fromNode = arc.fromNode();
                
                // We don't want to move nodes twice, so check to see if is part of the selection
                if (!selection.contains(toNode))
                {
                    toNode.moveBy(dx, dy);
                }
                
                if (!selection.contains(fromNode))
                {
                    fromNode.moveBy(dx, dy);
                }
            }
        }
    }
}
