//
//  ArcNodeListIterator.java
//  RDFAuthor
//

/* $Id: ArcNodeListIterator.java,v 1.3 2002-01-06 22:15:28 pldms Exp $ */

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

import java.util.Iterator;
import java.util.ArrayList;

public class ArcNodeListIterator implements Iterator {
    
    boolean getNodes;
    ArrayList list;
    int nextObjectIndex = -1;
    
    public ArcNodeListIterator(ArrayList list, boolean getNodes)
    {
        this.list = list;
        this.getNodes = getNodes;
        
        getNextObject();
    }
    
    private void getNextObject()
    {
        if ((nextObjectIndex + 1) == list.size()) // At the end of the array
        {
            nextObjectIndex = -1;
            return;
        }
        
        int found = -1;
        
        for (int i = nextObjectIndex + 1; i < list.size(); i ++)
        {
            ModelItem item = (ModelItem) list.get(i);
            if (getNodes)
            {
                if (item.isNode())
                {
                    found = i;
                    break;
                }
            }
            else
            {
                if (!item.isNode())
                {
                    found = i;
                    break;
                }
            }
        }
            
        nextObjectIndex = found;
    }
            
    
    public boolean hasNext()
    {
        return (nextObjectIndex != -1);
    }
    
    public Object next()
    {
        if (nextObjectIndex == -1)
        {
            return null;
        }
        
        Object item = list.get(nextObjectIndex);
        getNextObject();
        return item;
    }
    
    public void remove() // don't bother
    {
    }
    
}
