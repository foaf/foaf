//
//  ArcNodeListIterator.java
//  RDFAuthor
//
//  Created by pldms on Wed Nov 07 2001.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

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
    
    public void getNextObject()
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
