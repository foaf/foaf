/* QueryController */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

public class QueryController extends NSObject {

    NSDrawer queryDrawer;

    NSTableView resultTable;

    NSComboBox serviceComboButton;

    RDFAuthorDocument rdfAuthorDocument;
    
    NSSize size = new NSSize(10,10);

    ArrayList queryObjects = new ArrayList();
    
    HashMap varToObject;
    HashMap objectToVar;
    
    public void performQuery(Object sender) {
        String query = constructQuery(rdfAuthorDocument.rdfModel);
        System.out.println("Query to perform is:");
        System.out.println(query);
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
        String queryString = "";
        int varNum = 1;
        
        // First create mappings between variables and objects
        
        varToObject = new HashMap();
        objectToVar = new HashMap();
        HashMap nodeToString = new HashMap();
        ArrayList triples = new ArrayList();
        ListIterator iterator = queryObjects.listIterator();
        
        while (iterator.hasNext())
        {
            ModelItem item = (ModelItem) iterator.next();
            if (((Node) item).isConnected()) // no point using unconnected nodes
            {
                String var = "?var_" + varNum;
                varToObject.put( var, item );
                objectToVar.put( item, var );
                varNum++;
            }
        }
        
        // Now go through the nodes

        for (iterator = model.getObjects(); iterator.hasNext();)
        {
            ModelItem item = (ModelItem) iterator.next();
            
            if (item.isNode())
            {
                Node theNode = (Node) item;
                
                if (!theNode.isConnected()) // don't waste my time
                {
                    continue;
                }
                
                if (theNode.isLiteral())
                {
                    if (queryObjects.contains(theNode))
                    {
                        nodeToString.put(theNode, (String) objectToVar.get(theNode));
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
                        nodeToString.put(theNode, (String) objectToVar.get(theNode));
                    }
                    else
                    {
                        if (theNode.id() == null)  // anonymous
                        {
                            String var = "?var_" + varNum;
                            varToObject.put( var, theNode );
                            objectToVar.put( theNode, var );
                            varNum++;
                            nodeToString.put(theNode, var);
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
        }
    
        // Now go through the arcs
    
        for (iterator = model.getObjects(); iterator.hasNext();)
        {
            ModelItem item = (ModelItem) iterator.next();
            
            if (!item.isNode())
            {
                Arc theArc = (Arc) item;
                
                Node startNode = theArc.fromNode();
                Node endNode = theArc.toNode();
                
                // Uh-oh - check for existence of arc property here
                
                triples.add( "(" + 
                    theArc.propertyNamespace + theArc.propertyName + " " +
                    (String) nodeToString.get(startNode) + " " +
                    (String) nodeToString.get(endNode) + ")" );
            }
        }
        
        // Ok - this is the actual query construction
        
        queryString += "SELECT ";
        
        for (int i = 1; i < varNum; i++)
        {
            if (i != 1)
            {
                queryString +=", ";
            }
            
            queryString += "?var_" + i;
        }
        
        queryString += "\nWHERE\n";
        
        for (iterator = triples.listIterator(); iterator.hasNext();)
        {
            queryString += "\t" + (String) iterator.next() + "\n";
        }
        
        return queryString;
    }
}
