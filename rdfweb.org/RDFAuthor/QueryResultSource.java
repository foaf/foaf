//
//  QueryResultSource.java
//  RDFAuthor
//
//  Created by pldms on Tue Nov 06 2001.

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
