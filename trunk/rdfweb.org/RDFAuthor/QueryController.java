/* QueryController */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.axis.client.ServiceClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Enumeration;
import java.util.Iterator;

public class QueryController extends NSObject {

    NSDrawer queryDrawer;

    NSTableView resultTable;

    NSComboBox serviceComboButton;

    RDFAuthorDocument rdfAuthorDocument;
    
    NSSize size = new NSSize(10,10);

    ArrayList queryObjects = new ArrayList();
    
    HashMap varToObject;
    HashMap objectToVar;
    ArrayList variableList;
    
    QueryResultSource resultSource;
    
    public void performQuery(Object sender) {
        if (queryObjects.isEmpty())
        {
            RDFAuthorUtilities.ShowError(
                "No Query Objects Specified",
                "You need to specify which objects are 'unknown' to run a query.", 
                RDFAuthorUtilities.Normal, 
                queryDrawer.parentWindow());
            return;
        }
        
        String query = constructQuery(rdfAuthorDocument.rdfModel);
        System.out.println("Query to perform is:");
        System.out.println(query);
        
        String database = serviceComboButton.stringValue().trim();
        
        if (database.equals(""))
        {
            RDFAuthorUtilities.ShowError(
                "No Database Specified", "You need to specify a data source to query.", 
                RDFAuthorUtilities.Normal, 
                queryDrawer.parentWindow());
            return;
        }
        
        String endpoint="http://swordfish.rdfweb.org:8080/axis/servlet/AxisServlet";
        ArrayList rows;
        try
        {
            ServiceClient client = new ServiceClient(endpoint);
        
            rows = (ArrayList) client.invoke(
                                            "http://rdfweb.org/RDF/RDFWeb/SOAPDemo",
                                            "squish",
                                            new Object [] { query,database,"" });
        }
        catch (Exception e)
        {
            RDFAuthorUtilities.ShowError(
                "Error Making Query", "There was an error making the query.\nUseful error text:\n" + e, 
                RDFAuthorUtilities.Critical, 
                queryDrawer.parentWindow());
            return;
        }
        if (rows.isEmpty())
        {
            RDFAuthorUtilities.ShowError(
                "Nothing Found", "No results found for this query", RDFAuthorUtilities.Informational, 
                queryDrawer.parentWindow());
            return;
        }
        
        createResultsTable(rows);
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
    
    public String constructQuery(ArcNodeList model)
    {
        int varNum = 1;
        
        // First create mappings between variables and objects
        
        varToObject = new HashMap();
        objectToVar = new HashMap();
        variableList = new ArrayList();
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
                        String var = "var_" + varNum;
                        varNum++;
                        nodeToString.put(theNode, "?" + var);
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
            
            // Uh-oh - check for existence of arc property here
            
            triples.add( "(" + 
                theArc.propertyNamespace + theArc.propertyName + " " +
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
    
    public void createResultsTable(ArrayList rows)
    {
        // First - remove all columns
        
        Enumeration enumerator = resultTable.tableColumns().objectEnumerator();
        
        while (enumerator.hasMoreElements())
        {
            resultTable.removeTableColumn((NSTableColumn) enumerator.nextElement());
        }
        
        // Next - create new columns and add them to the table
        for (ListIterator iterator = variableList.listIterator(); iterator.hasNext();)
        {
            String identifier = (String) iterator.next();
            NSTableColumn col = new NSTableColumn(identifier);
            col.headerCell().setStringValue(identifier);
            resultTable.addTableColumn(col);
            col.setWidth(400F);
        }
        
        // Now create the result source for the table
        
        resultSource = new QueryResultSource(rows);
        
        resultTable.setDataSource(resultSource);
        resultTable.reloadData();
    }
    
    // Delegate methods are here
    
    public boolean tableViewShouldSelectTableColumn( NSTableView aTableView, NSTableColumn aTableColumn)
    {
        return false; // Don't allow table column selection
    }

    public void tableViewSelectionDidChange(NSNotification aNotification)
    {
        int selectedRow = resultTable.selectedRow();
        
        if (selectedRow == -1) return; // nothing selected
        
        HashMap row = resultSource.getRow(selectedRow);
        
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
