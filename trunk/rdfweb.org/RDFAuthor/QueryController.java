/* QueryController */

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

import java.util.*;

public class QueryController extends NSObject {

    NSDrawer queryDrawer;

    NSTableView resultTable;

    NSComboBox serviceComboButton;
    
    NSButton queryButton;

    RDFAuthorDocument rdfAuthorDocument;
    
    NSTextField infoTextField;
    
    NSSize size = new NSSize(10,10);

    ArrayList queryObjects = new ArrayList();
    
    QueryResultSource resultSource;
    QueryThread queryThread = null;
    
    public void performQuery(NSButton sender) 
    {
        if (queryThread != null) // Query already running
        {
            NSSelector killSelector = new NSSelector("killQuery", 
                new Class[] {Object.class, int.class} );
            NSAlertPanel.beginAlertSheet(
                "Kill Current Query?", "OK", "Cancel", null,
                queryDrawer.parentWindow(), this, killSelector, null, queryDrawer.parentWindow(), 
                "Do you really want to kill the current query?");
            return;
        }
        
        if (queryObjects.isEmpty())
        {
            RDFAuthorUtilities.ShowError(
                "No Query Objects Specified",
                "You need to specify which objects are 'unknown' to run a query.", 
                RDFAuthorUtilities.Normal, 
                queryDrawer.parentWindow());
            return;
        }
        
        HashMap varToObject = new HashMap();
        
        String query = constructQuery(rdfAuthorDocument.rdfModel, varToObject);
        String database = serviceComboButton.stringValue().trim();
        
        if (database.equals(""))
        {
            RDFAuthorUtilities.ShowError(
                "No Database Specified", "You need to specify a data source to query.", 
                RDFAuthorUtilities.Normal, 
                queryDrawer.parentWindow());
            return;
        }
        
        System.out.println("Query to perform is:");
        System.out.println(query);
        
        infoTextField.setStringValue("Performing query...");
        
        queryThread = new QueryThread(query, database, varToObject, this);
        
        queryThread.start();
        
        queryButton.setTitle("Kill");
    }
    
    public void killQuery(Object context, int returnCode)
    {
        if (returnCode == NSAlertPanel.DefaultReturn)
        {
            if (queryThread != null) // check that it didn't finish before response
            {
                System.out.println("Killing current thread...");
                // currently there is no way to stop the thread safely
                // or (indeed) tell the server to give up. We just forget about it :-(
                // queryThread.stop();
                queryThread = null;
                infoTextField.setStringValue("");
                queryButton.setTitle("Query");
            }
        }
    }
    
    public void queryDied(QueryThread sender, Exception e)
    {
        // Since I can't kill threads safely better make sure this is the query I want
        
        if (sender != queryThread) return;
        
        RDFAuthorUtilities.ShowError(
            "Error Making Query", "There was an error making the query. The service might be unavailable", 
            RDFAuthorUtilities.Critical, 
            queryDrawer.parentWindow());
        infoTextField.setStringValue("Last query failed.");
        
        NSApplication.beep(); // Since these can take a while I'll beep
        
        queryThread = null;
        
        queryButton.setTitle("Query");
    }
        
    public void queryCompleted(QueryThread sender, ArrayList rows, HashMap varToObject, double duration)
    {
        // Since I can't kill threads safely better make sure this is the query I want
        
        if (sender != queryThread) return;
        
        //ArrayList rows = queryThread.result();
        //HashMap varToObject = queryThread.variableToObjectMapping();
        
        NSApplication.beep(); // Since these can take a while I'll beep
        
        if (rows.isEmpty())
        {
            RDFAuthorUtilities.ShowError(
                "Nothing Found", "No results found for this query", RDFAuthorUtilities.Informational, 
                queryDrawer.parentWindow());
            infoTextField.setStringValue("Last query returned nothing.");
            queryThread = null;
            queryButton.setTitle("Query");
            
            return;
        }
        
        System.out.println("Here [1]");
        
        infoTextField.setStringValue("Query took " 
            + duration + " seconds. " + rows.size() + 
            " results returned.");
        
        System.out.println("Here [2]");
        
        createResultsTable(rows, varToObject);
        
        System.out.println("Here [3]");
        
        queryThread = null;
        
        queryButton.setTitle("Query");
        
        System.out.println("Here [4]");
    }
    
    public void addQueryItem(ModelItem item)
    {
        if (item == null)
        {
            return;
        }
        
        // Currently just do nodes
        
        if (item.isNode())
        {
            if (queryObjects.contains(item))
            {
                queryObjects.remove(item);
            }
            else
            {
                queryObjects.add(item);
            }
        }
    }
    
    // This method is needed to check whether the queryObjects contains deleted objects
    // This is made more complicated since removing a node may remove many arcs
    
    public void checkForDeletedItems(ArcNodeList model)
    {
        for (Iterator iterator = queryObjects.listIterator(); iterator.hasNext();)
        {
            if (!model.contains((ModelItem) iterator.next()))
            {
                iterator.remove();
            }
        }
    }
    
    public void drawQueryItems()
    {
        if (queryObjects.isEmpty())
        {
            return;
        }
        
        for (ListIterator iterator = queryObjects.listIterator(); iterator.hasNext();)
        {
            ModelItem item = (ModelItem) iterator.next();
            
            NSPoint point = item.rect().origin();
            
            NSRect rect = new NSRect( point, size );
            
            NSColor.yellowColor().set();
            
            NSBezierPath.fillRect(rect);
            
            NSColor.blackColor().set();
            
            NSBezierPath.strokeRect(rect);
        }
    }
    
    public void toggleShow()
    {
        queryDrawer.toggle(this);
    }
    
    public String constructQuery(ArcNodeList model, HashMap varToObject)
    {
        int varNum = 1;
        
        HashMap objectToVar = new HashMap();
        ArrayList variableList = new ArrayList();
        HashMap nodeToString = new HashMap();
        ArrayList triples = new ArrayList();
        Iterator iterator = queryObjects.listIterator();
        
        while (iterator.hasNext())
        {
            ModelItem item = (ModelItem) iterator.next();
            if (((Node) item).isConnected()) // no point using unconnected nodes
            {
                String var = "var_" + varNum;
                varToObject.put( var, item );
                objectToVar.put( item, var );
                variableList.add(var); // This is a list of the variables we are interested in
                varNum++;
            }
        }
        
        // Now go through the nodes

        for (iterator = model.getNodes(); iterator.hasNext();)
        {
            Node theNode = (Node) iterator.next();
            
            if (!theNode.isConnected()) // don't waste my time
            {
                continue;
            }
                
            if (theNode.isLiteral())
            {
                if (queryObjects.contains(theNode))
                {
                    nodeToString.put(theNode, "?" + (String) objectToVar.get(theNode));
                }
                else
                {
                    nodeToString.put(theNode, theNode.id());
                }
            }
            else
            {
                if (queryObjects.contains(theNode))
                {
                    nodeToString.put(theNode, "?" + (String) objectToVar.get(theNode));
                }
                else
                {
                    if (theNode.id() == null)  // anonymous
                    {
                        nodeToString.put(theNode, "?var_" + varNum);
                        varNum++;
                    }
                    else
                    {
                        nodeToString.put(theNode, theNode.id());
                    }
                }
                    
                if (theNode.typeNamespace() != null)
                {
                    triples.add( "(http://www.w3.org/1999/02/22-rdf-syntax-ns#type " + 
                        (String) nodeToString.get(theNode) + " " + 
                        theNode.typeNamespace() + theNode.typeName() + ")" );
                }
            }
        }
    
        // Now go through the arcs
    
        for (iterator = model.getArcs(); iterator.hasNext();)
        {
            Arc theArc = (Arc) iterator.next();
                
            Node startNode = theArc.fromNode();
            Node endNode = theArc.toNode();
            
            String property;
            
            if (theArc.propertyNamespace() == null) // An 'anonymous' property
            {
                property = "?var_" + varNum;
                varNum ++;
            }
            else
            {
                property = theArc.propertyNamespace() + theArc.propertyName();
            }
            
            triples.add( "(" + 
                property + " " +
                (String) nodeToString.get(startNode) + " " +
                (String) nodeToString.get(endNode) + ")" );
        }
        
        // Ok - this is the actual query construction
        
        String queryString = " SELECT ";
        
        for (iterator = variableList.listIterator(); iterator.hasNext();)
        {
            String variable = (String) iterator.next();
            
            if (!variable.equals((String) variableList.get(0))) // is this the first element?
            {
                queryString +=", ";
            }
            
            queryString += "?" + variable;
        }
        
        queryString += " \n WHERE \n";
        
        for (iterator = triples.listIterator(); iterator.hasNext();)
        {
            queryString += "      " + (String) iterator.next() + " \n";
        }
        
        return queryString;
    }
    
    public void createResultsTable(ArrayList rows, HashMap varToObject)
    {
        System.out.println("Here [6]");
        ArrayList tableArray = new ArrayList();
        
        for (Enumeration enumerator = resultTable.tableColumns().objectEnumerator(); enumerator.hasMoreElements(); )
        {
            //resultTable.removeTableColumn((NSTableColumn) enumerator.nextElement());
            tableArray.add(enumerator.nextElement());
        }
        
        for (Iterator iterator = tableArray.listIterator(); iterator.hasNext(); )
        {
            resultTable.removeTableColumn((NSTableColumn) iterator.next());
        }
        
        System.out.println("Here [7]");
        
        // Next - create new columns and add them to the table
        for (Iterator iterator = varToObject.keySet().iterator(); iterator.hasNext();)
        {
            String identifier = (String) iterator.next();
            NSTableColumn col = new NSTableColumn(identifier);
            col.headerCell().setStringValue(identifier);
            resultTable.addTableColumn(col);
            col.setWidth(400F);
        }
        
        System.out.println("Here [8]");
        
        // Now create the result source for the table
        
        resultSource = new QueryResultSource(rows, varToObject);
        
        System.out.println("Here [9]");
        
        resultTable.setDataSource(resultSource);
        
        System.out.println("Here [10]");
        
        resultTable.reloadData();
    }
    
    // Delegate methods are here

    public void tableViewSelectionDidChange(NSNotification aNotification)
    {
        int selectedRow = resultTable.selectedRow();
        
        if (selectedRow == -1) return; // nothing selected
        
        HashMap row = resultSource.getRow(selectedRow);
        HashMap varToObject = resultSource.variableToObjectMapping();
        
        for (Iterator iterator = row.keySet().iterator(); iterator.hasNext(); )
        {
            String key = (String) iterator.next();
            ModelItem item = (ModelItem) varToObject.get(key);
            String value = (String) row.get(key);
            if (item.isNode()) // Ok - always true currently
            {
                ((Node) item).setId(value);
            }
        }
    }
}
