/* RDFAuthorDocument */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.io.*;

public class RDFAuthorDocument extends NSDocument {
    
    String FileFormatVersion = "RDFAuthor File Format Version 0.1";
    
    NSTextField textDescriptionField;

    RDFModelView rdfModelView;
    
    RDFToolbar rdfToolbar;
    
    NSWindow window;
    
    NSTextView previewTextView;
    NSScrollView previewView;
    
    
    ArcNodeList rdfModel;
    boolean addingNode;
    boolean addingConnection;
    boolean deleting;
    boolean showTypes;
    boolean showIds;
    boolean showProperties;
    
    public RDFAuthorDocument() {
        super();
        rdfModel = new ArcNodeList(this);
        showTypes = false;
        showIds = false;
        showProperties = false;
    }
    
    public RDFAuthorDocument(String fileName, String fileType) {
        super(fileName, fileType);
    }
    
    public void printDocumentUsingPrintPanel(boolean flag)
    {
        NSPrintOperation printOperation = 
            NSPrintOperation.printOperationWithView((NSView) rdfModelView);
        printOperation.runModalOperation(window, null, null, null);
    }
    
    public void printDocument(Object sender) {
        printDocumentUsingPrintPanel(true);
    }
    
    public NSData dataRepresentationOfType(String aType) {
        // Insert code here to create and return the data for your document.
        System.out.println("Wants to save as " + aType);
        
        if (aType.equals("RDFAuthor Document"))
        {
            try
            {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ObjectOutputStream s = new ObjectOutputStream(out);
                s.writeObject(FileFormatVersion);
                s.writeBoolean(showTypes);
                s.writeBoolean(showIds);
                s.writeBoolean(showProperties);
                s.writeObject(rdfModel);
                
                NSData savedData = new NSData(out.toByteArray());

                return savedData;
            }
            catch (Exception e)
            {
                System.out.println("Got error: "+e);
                return null;
            }
        }
        else if (aType.equals("RDF/XML Document"))
        {
            String rdfData = rdfModel.exportAsRDF();
            
            if (rdfData == null)
            {
                NSAlertPanel alert = new NSAlertPanel();
                alert.runCriticalAlert("RDF/XML Export Failed",
                    "Export failed, I'm afraid. Try using 'Check Model' for possible problems.",
                    null, null, null);
                return null;
            }
            else
            {
                // Ugh 
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
            try
            {
                ByteArrayInputStream in = 
                    new ByteArrayInputStream( data.bytes(0, data.length()) );
                ObjectInputStream s = new ObjectInputStream(in);
                String formatVersion = (String) s.readObject();
                if (!formatVersion.equals(FileFormatVersion))
                {
                    NSAlertPanel alert = new NSAlertPanel();
                    alert.runAlert("Incompatible File Format",
                        "This version requires " + FileFormatVersion + ", but this file is in " + formatVersion
                        + ".\nBlame the author." ,
                        null, null, null);
                    return false;
                }
                showTypes = s.readBoolean();
                showIds = s.readBoolean();
                showProperties = s.readBoolean();                
                rdfModel = (ArcNodeList) s.readObject();

                rdfModel.setController(this);
            
                return true;
            }
            catch (Exception e)
            {
                System.out.println("Input died with: " +e);
                return false;
            }
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
	
        window = aController.window();
        
	// Attach the toolbar to the document window.
	window.setToolbar(rdfToolbar);
    }
    
    public boolean showTextPreview(boolean showPreview)
    {
        if (showPreview)
        {
            String rdfData = rdfModel.exportAsRDF();
            if (rdfData == null)
            {
                NSAlertPanel alert = new NSAlertPanel();
                alert.runCriticalAlert("RDF/XML Serialisation Failed",
                    "I couldn't convert this to RDF/XML. Try using 'Check Model' for possible problems.",
                    null, null, null);
                return false;
            }
            
            NSRect rect = rdfModelView.frame();
            previewView.setFrame(rect);
            previewTextView.setString(rdfData);

            window.contentView().replaceSubview(rdfModelView, previewView);
            return true;
        }
        else
        {
            NSRect rect = previewView.frame();
            rdfModelView.setFrame(rect);
            window.contentView().replaceSubview(previewView, rdfModelView);
            return true;
        }
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
    
    public void showTypes(boolean value)
    {
        showTypes = value;
        rdfModel.showTypes(value);
    }

    public void showIds(boolean value) 
    {
        showIds = value;
        rdfModel.showIds(value);
    }

    public void showProperties(boolean value) 
    {
        showProperties = value;
        rdfModel.showProperties(value);
    }

    public void addNodes(boolean addThem) 
    {
        if (addThem)
        {
            addingConnection = false;
            rdfModelView.addConnection(false);
            rdfModelView.deleteMode(false);
            textDescriptionField.setStringValue("Click to place a new node");
            addingNode = true;
            rdfModelView.addNode(true);
        }
        else
        {
            addingNode = false;
            rdfModelView.addNode(false);
            textDescriptionField.setStringValue("");
        }
    }

    public void addArcs(boolean addThem)
    {
        if (addThem)
        {
            rdfModelView.addConnection(true);
            rdfModelView.addNode(false);
            rdfModelView.deleteMode(false);
            addingConnection = true;
            textDescriptionField.setStringValue("Drag between two nodes to connect");
        }
        else
        {
            addingConnection = false;
            rdfModelView.addConnection(false);
            textDescriptionField.setStringValue("");
        }
    }
    
    public void deleteItems(boolean delete)
    {
        if (delete)
        {
            rdfModelView.deleteMode(true);
            rdfModelView.addConnection(false);
            rdfModelView.addNode(false);
            textDescriptionField.setStringValue("Click on items to remove them from the model");
        }
        else
        {
            deleting = false;
            rdfModelView.deleteMode(false);
            textDescriptionField.setStringValue("");
        }
    }
    
    public void showInfoForObjectAtPoint(NSPoint point)
    {
        ModelItem item = rdfModel.objectAtPoint(point);
        if (item != null)
        {
            setCurrentObject(item);
            NSNotificationCenter.defaultCenter().postNotification(
                new NSNotification(InfoController.showInfoNotification, null) );
        }
    }

    
    public void deleteObjectAtPoint(NSPoint point)
    {
        ModelItem item = rdfModel.objectAtPoint(point);
        if (item != null)
        {
            rdfModel.deleteObject(item);
        }
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
        if (startNode != null && endNode != null
            && startNode.isNode() && endNode.isNode()
            && (startNode != endNode) )
        {
            Arc newArc = new Arc(rdfModel, (Node)startNode, (Node)endNode, null, null);
            newArc.setShowProperty(showProperties);
            rdfModel.add(newArc);
            rdfModel.setCurrentObject(newArc);
        }
        else
        {
            rdfModelView.setNeedsDisplay(true); // need this to get rid of drag line
        }
    }
    
    public void setCurrentObject(ModelItem item)
    {
        rdfModel.setCurrentObject(item);
    }
    
    public void setCurrentObjectAtPoint(NSPoint point)
    {
        ModelItem item = rdfModel.objectAtPoint(point);
        rdfModel.setCurrentObject(item);
    }
    
    public void selectNextObject()
    {
        rdfModel.selectNextObject();
    }
    
    public void selectPreviousObject()
    {
        rdfModel.selectPreviousObject();
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
    
    public void doCheckModel()
    {
        NSNotificationCenter.defaultCenter().postNotification(
            new NSNotification(ErrorWindowController.checkModelNotification, window) );
    }
    
    public void checkModel(ModelErrorData errorData)
    {
        rdfModel.checkModel(errorData);
    }
}