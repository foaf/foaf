//
//  QueryThread.java
//  RDFAuthor
//
//  Created by pldms on Thu Nov 08 2001.

/* $Id: QueryThread.java,v 1.7 2002-04-10 15:22:20 pldms Exp $ */

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

// This does the actual query. It's a thread because these things can be - er - slow.
// Axis doesn't do async yet

import org.apache.axis.client.ServiceClient;
import java.util.ArrayList;
import java.lang.Thread;
import java.util.HashMap;

public class QueryThread extends Thread {
    
    ArrayList returned = null;
    String endpoint="http://swordfish.rdfweb.org:8080/axis/servlet/AxisServlet";
    String query = null;
    String database = null;
    HashMap varToObject;
    long duration;
    boolean finished;
    boolean died;
    
    public QueryThread(String query, String database, HashMap varToObject, 
            QueryController owner)
    {
        this.query = query;
        this.database = database;
        this.varToObject = varToObject;
        this.finished = false;
        this.died = false;
    }
    
    public void run()
    {
        try
        {
            // I'll time this (which is probably useful)
            long startTime = System.currentTimeMillis();
            
            ServiceClient client = new ServiceClient(endpoint);
            
            returned = (ArrayList) client.invoke(
                                            "http://rdfweb.org/RDF/RDFWeb/SOAPDemo",
                                            "squish",
                                            new Object [] { query,database,"" });
            duration = System.currentTimeMillis() - startTime;
        }
        catch (Exception e)
        {
            died = true;
            System.out.println("QueryThread died: " + e);
        }
        
        finished = true;
    }
    
    public boolean finished()
    {
        return finished;
    }
    
    public boolean died()
    {
        return died;
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
