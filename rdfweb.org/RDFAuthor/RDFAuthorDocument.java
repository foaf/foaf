/* RDFAuthorDocument */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

public class RDFAuthorDocument extends NSDocument {
    
    NSButton addArcButton;

    NSButton addNodeButton;

    NSTextField descriptionTextField;

    RDFModelView rdfModelView;
    
    ArcNodeList rdfModel;
    boolean addingNode;
    boolean addingConnection;
    boolean showTypes = false;
    boolean showIds = false;
    boolean showProperties = false;
    
    public RDFAuthorDocument() {
        super();
        rdfModel = new ArcNodeList(this);
    }
    
    public RDFAuthorDocument(String fileName, String fileType) {
        super(fileName, fileType);
    }
        
    public NSData dataRepresentationOfType(String aType) {
        // Insert code here to create and return the data for your document.
        System.out.println("Wants to save as " + aType);
        
        if (aType.equals("RDFAuthor Document"))
        {
            return NSArchiver.archivedDataWithRootObject(rdfModel);
        }
        else if (aType.equals("RDF Document"))
        {
            String rdfData = rdfModel.exportAsRDF();
            
            if (rdfData == null)
            {
                return null;
            }
            else
            {
                // Ugh 
                System.out.println(rdfData);
                NSMutableStringReference rdfString = new NSMutableStringReference();
                rdfString.setString(rdfData);
                return rdfString.dataUsingEncoding(
                    NSStringReference.UTF8StringEncoding, false);
            }
        }
        else
        {
            System.out.println("Unknown save type");
            return null;
        }
    }

    public boolean loadDataRepresentation(NSData data, String aType) {
        // Insert code here to read your document from the given data.
        System.out.println("Wants to load something of type " + aType);
        
        if (aType.equals("RDFAuthor Document"))
        {
            Object got = NSUnarchiver.unarchiveObjectWithData(data);
            System.out.println("Got: " + got);
            //rdfModelView.setNeedsDisplay(true);
            return true;
        }
        else
        {
            System.out.println("Don't know this type");
            return false;
        }
    }
    
    public String windowNibName() {
        return "RDFAuthorDocument";
    }

    public void windowControllerDidLoadNib(NSWindowController  aController) {
        super.windowControllerDidLoadNib(aController);
        // Add any code here that need to be executed once the windowController has loaded the document's window.
    }
    
    public void modelChanged()
    {
        updateChangeCount(1);
        rdfModelView.setNeedsDisplay(true);
        // Tell info window that something changed
        NSNotificationCenter.defaultCenter().postNotification(
            new NSNotification(InfoController.itemChangedNotification, this) );
    }
    
    public ModelItem currentObject()
    {
        return rdfModel.currentObject();
    }
    
    public void currentObjectChanged()
    {
        // Tell info window current item changed
        NSNotificationCenter.defaultCenter().postNotification(
            new NSNotification(InfoController.itemChangedNotification, this) );
    }
    
    public void showTypes(NSButton sender) 
    {
        showTypes = (sender.state() == NSCell.OnState);
        rdfModel.showTypes(showTypes);
    }

    public void showName(NSButton sender) 
    {
        showIds = (sender.state() == NSCell.OnState);
        rdfModel.showIds(showIds);
    }

    public void showProperties(NSButton sender) 
    {
        showProperties = (sender.state() == NSCell.OnState);
        rdfModel.showProperties(showProperties);
    }

    public void addNode(Object sender) 
    {
        if (addingNode)
        {
            addingNode = false;
            descriptionTextField.setStringValue("");
            rdfModelView.addNode(false);
        }
        else
        {
            if (addArcButton.state() == NSCell.OnState)
            {
                addArcButton.setState(NSCell.OffState);
                addingConnection = false;
                rdfModelView.addConnection(false);
            }
            descriptionTextField.setStringValue("Click to place a new node");
            addingNode = true;
            rdfModelView.addNode(true);
        }
    }

    public void addArc(Object sender) 
    {
        if (addingConnection)
        {
            addingConnection = false;
            descriptionTextField.setStringValue("");
            rdfModelView.addConnection(false);
        }
        else
        {
            rdfModelView.addConnection(true);
            addingConnection = true;
            rdfModelView.addConnection(true);
            if (addNodeButton.state() == NSCell.OnState)
            {
                addNodeButton.setState(NSCell.OffState);
                addingNode = false;
                rdfModelView.addNode(false);
            }
            descriptionTextField.setStringValue("Drag between two nodes to connect");
        }
    }

    public void deleteCurrentItem(Object sender)
    {
        rdfModel.deleteCurrentObject();
    }
    
    public void addNodeAtPoint(String id, String typeName, String typeNamespace, NSPoint point)
    {
        Node newNode = new Node(rdfModel, id, typeName, typeNamespace, point);
        rdfModel.add(newNode);
        newNode.setShowId(showIds);
        newNode.setShowType(showTypes);
        rdfModel.setCurrentObject(newNode);
    }
    
    public void addConnectionFromPoint(NSPoint fromPoint, NSPoint toPoint)
    {
        ModelItem startNode = rdfModel.objectAtPoint(fromPoint);
        ModelItem endNode = rdfModel.objectAtPoint(toPoint);
        if (startNode != null && endNode != null && startNode.isNode() && endNode.isNode())
        {
            Arc newArc = new Arc(rdfModel, (Node)startNode, (Node)endNode, null, null);
            newArc.setShowProperty(showProperties);
            rdfModel.add(newArc);
            rdfModel.setCurrentObject(newArc);
        }
    }
    
    public void setCurrentObjectAtPoint(NSPoint point)
    {
        ModelItem item = rdfModel.objectAtPoint(point);
        rdfModel.setCurrentObject(item);
    }
    
    public void moveCurrentObjectToPoint(NSPoint point)
    {
        ModelItem item = rdfModel.currentObject();
        if ((item !=null) && item.isNode())
        {
            ((Node) item).setPosition(point);
        }
    }
    
    public void setIdForNodeAtPoint(String id, NSPoint point)
    {
        ModelItem item = rdfModel.objectAtPoint(point);
        if ((item !=null) && item.isNode())
        {
            ((Node) item).setId(id);
        }
        else
        {
            addNodeAtPoint(id, null, null, point);
        }
    }
    
    public void setTypeForNodeAtPoint(String namespace, String name, NSPoint point)
    {
        ModelItem item = rdfModel.objectAtPoint(point);
        if ((item !=null) && item.isNode())
        {
            ((Node) item).setType(namespace, name);
        }
        else
        {
            addNodeAtPoint(null, namespace, name, point);
        }
    }
    
    public void setTypeForArcAtPoint(String namespace, String name, NSPoint point)
    {
        ModelItem item = rdfModel.objectAtPoint(point);
        if ((item !=null) && !item.isNode())
        {
            ((Arc) item).setProperty(namespace, name);
        }
    }
    
    public void drawModel()
    {
        rdfModel.drawModel();
    }
}