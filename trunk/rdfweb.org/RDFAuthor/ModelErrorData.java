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
    int numberOfErrors;
    int numberOfWarnings;
    
    public ModelErrorData()
    {
        errorRecords = new Vector();
    }
    
    public boolean hasErrors()
    {
        return (numberOfErrors != 0);
    }
    
    public boolean hasWarnings()
    {
        return (numberOfWarnings != 0);
    }
    
    public int numberOfErrors()
    {
        return numberOfErrors;
    }
    
    public int numberOfWarnings()
    {
        return numberOfWarnings;
    }
    
    public void addError(ModelItem item, String errorText)
    {
        Vector errorRecord = new Vector();
        
        errorRecord.add("Error");
        errorRecord.add(nameForItem(item));
        errorRecord.add(errorText);
        errorRecord.add(item);
        
        errorRecords.add(errorRecord);
        numberOfErrors ++;
    }
    
    public void addWarning(ModelItem item, String warningText)
    {
        Vector warningRecord = new Vector();
        
        warningRecord.add("Warning");
        warningRecord.add(nameForItem(item));
        warningRecord.add(warningText);
        warningRecord.add(item);
        
        errorRecords.add(warningRecord);
        numberOfWarnings ++;
    }
    
    public String nameForItem(ModelItem item)
    {
        String name = "";
        
        if (item.isNode())
        {
            Node node = (Node) item;
            if (node.isLiteral())
            {
                name += "Literal: \n";
                name += (node.id() == null)?"\"\"":node.id();
            }
            else
            {
                name += (node.typeName() == null)?"Resource: \n":node.typeName() + ": \n";
                name += (node.id() == null)?"<anonymous>":node.id();
            }
        }
        else
        {
            Arc arc = (Arc) item;
            name += "Arc: \n";
            name += (arc.propertyName() == null)?"<no property>":arc.propertyName();
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
            return null;
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
            
    public void tableViewWillDisplayCell( NSTableView aTableView, NSTextFieldCell aCell, 
                    NSTableColumn aTableColumn, int rowIndex)
    {
        String identifier = (String) aTableColumn.identifier();
        if (identifier.equals("Type"))
        {
            Vector row = (Vector) errorRecords.get(rowIndex);
            if (((String) row.get(0)).equals("Error"))
            {
                aCell.setImage(NSImage.imageNamed("error"));
            }
            else
            {
                aCell.setImage(NSImage.imageNamed("warning"));
            }
        }
        else
        {
            aCell.setWraps(true);
        }
    }

}
