//
//  QueryResultSource.java
//  RDFAuthor
//
//  Created by pldms on Tue Nov 06 2001.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.util.ArrayList;
import java.util.HashMap;

public class QueryResultSource {
    
    ArrayList resultsArray;
    HashMap varToObject;
    
    public QueryResultSource(ArrayList results, HashMap varToObject)
    {
        resultsArray = results;
        this.varToObject = varToObject;
    }
    
    public int numberOfRowsInTableView(NSTableView aTableView)
    {
        return resultsArray.size();
    }
    
    public Object tableViewObjectValueForLocation( NSTableView aTableView, 
                    NSTableColumn aTableColumn, int rowIndex)
    {
        String key = (String) aTableColumn.identifier();
        return ((HashMap) resultsArray.get( rowIndex )).get(key);
    }
    
    public HashMap getRow(int row)
    {
        return (HashMap) resultsArray.get( row );
    }
    
    public HashMap variableToObjectMapping()
    {
        return varToObject;
    }
}
