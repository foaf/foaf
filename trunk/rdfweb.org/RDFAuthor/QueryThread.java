//
//  QueryThread.java
//  RDFAuthor
//
//  Created by pldms on Thu Nov 08 2001.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

// This does the actual query. It's a thread because these things can be - er - slow.
// Axis doesn't do async yet

import org.apache.axis.client.ServiceClient;
import java.util.ArrayList;
import java.lang.Thread;
import java.util.HashMap;

public class QueryThread extends Thread {
    
    QueryController owner;
    ArrayList returned = null;
    String endpoint="http://swordfish.rdfweb.org:8080/axis/servlet/AxisServlet";
    String query = null;
    String database = null;
    HashMap varToObject;
    long duration;
    
    public QueryThread(String query, String database, HashMap varToObject, 
            QueryController owner)
    {
        this.query = query;
        this.database = database;
        this.varToObject = varToObject;
        this.owner = owner;
    }
    
    public void run()
    {
        try
        {
            // I'll time this (which is probably useful)
            long startTime = java.util.Calendar.getInstance().getTime().getTime();
            
            ServiceClient client = new ServiceClient(endpoint);
            
            returned = (ArrayList) client.invoke(
                                            "http://rdfweb.org/RDF/RDFWeb/SOAPDemo",
                                            "squish",
                                            new Object [] { query,database,"" });
            duration = java.util.Calendar.getInstance().getTime().getTime() - startTime;
        }
        catch (Exception e)
        {
            owner.queryDied(this, e);
        }
        
        owner.queryCompleted(this);
    }

    public ArrayList result()
    {
        return returned;
    }
    
    public double duration()
    {
        return (double) duration/1000d;
    }
    
    public HashMap variableToObjectMapping()
    {
        return varToObject;
    }
    
}
