//
//  ModelErrorData.java
//  RDFAuthor
//
//  Created by pldms on Mon Oct 15 2001.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.util.Vector;

public class ModelErrorData extends Object {
    
    Vector errorRecords;
    
    public ModelErrorData()
    {
        errorRecords = new Vector();
    }
    
    public void addError(ModelItem item, String errorText)
    {
        Vector errorRecord = new Vector();
        
        errorRecord.add("Error");
        errorRecord.add(nameForItem(item));
        errorRecord.add(errorText);
        errorRecord.add(item);
        
        errorRecords.add(errorRecord);
    }
    
    public void addWarning(ModelItem item, String warningText)
    {
        Vector warningRecord = new Vector();
        
        warningRecord.add("Warning");
        warningRecord.add(nameForItem(item));
        warningRecord.add(warningText);
        warningRecord.add(item);
        
        errorRecords.add(warningRecord);
    }
    
    public String nameForItem(ModelItem item)
    {
        String name = "";
        
        if (item.isNode())
        {
            Node node = (Node) item;
            if (node.isLiteral())
            {
                name += "Literal (" + node + "): ";
                name += (node.id() == null)?"\"\"":node.id();
            }
            else
            {
                name += (node.typeName() == null)?"Resource (":node.typeName() + " (";
                name += node + "): ";
                name += (node.id() == null)?"anonymous":node.id();
            }
        }
        else
        {
            Arc arc = (Arc) item;
            name += "Arc (" + arc + "): ";
            name += (arc.propertyName() == null)?"none":arc.propertyName();
        }
        
        return name;
    }
    
    public ModelItem getObjectAtRow(int row)
    {
        return (ModelItem) ((Vector) errorRecords.get(row)).get(3);
    }
    
    // NSTableDataSource stuff
    
    public int numberOfRowsInTableView(NSTableView aTableView)
    {
        return errorRecords.size();
    }
    
    public Object tableViewObjectValueForLocation( 
        NSTableView aTableView, NSTableColumn aTableColumn, int rowIndex)
    {
        Vector row = (Vector) errorRecords.get(rowIndex);
        String identifier = (String) aTableColumn.identifier();
        if (identifier == null)
        {
            return "What?";
        }
        if (identifier.equals("Type"))
        {
            return row.get(0);
        }
        else if (identifier.equals("Item"))
        {
            return row.get(1);
        }
        else if (identifier.equals("Messages"))
        {
            return row.get(2);
        }
        else
        {
            return null;
        }
    }

}
