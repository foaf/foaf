/* RDFAuthorDocument */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.io.*;
import java.util.HashMap;

public class RDFAuthorDocument extends NSDocument {
    
    String FileFormatVersion = "RDFAuthor File Format Version 0.1";
    
    NSTextField textDescriptionField;

    RDFModelView rdfModelView;
    
    NSScrollView rdfModelScrollView;
    
    RDFToolbar rdfToolbar;
    
    NSWindow window;
    
    NSTextView previewTextView;
    NSScrollView previewView;
    
    QueryController queryController;
    
    ArcNodeList rdfModel;
    
    HashMap exportMappings;
    
    boolean addingNode;
    boolean addingConnection;
    boolean deleting;
    boolean markQueryObjects =  false;
    boolean showTypes;
    boolean showIds;
    boolean showProperties;
    
    String defaultPropertyNamespace = null;
    String defaultPropertyName = null;
    String defaultClassNamespace = null;
    String defaultClassName = null;
    
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
        else if (exportMappings.get(aType) != null)
        {
            String outputType = (String) exportMappings.get(aType);
            String rdfData = rdfModel.exportAsRDF(outputType);
            
            if (rdfData == null)
            {
                RDFAuthorUtilities.ShowError(
                    "RDF Export Failed",
                    "Export failed, I'm afraid. Try using 'Check Model' for possible problems.",
                    RDFAuthorUtilities.Critical, window);
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
            System.out.println("Unknown save type: " + aType);
            return null;
        }
    }

    public boolean loadDataRepresentation(NSData data, String aType) {
        // Insert code here to read your document from the given data.
        System.out.println("Wants to load something of type " + aType);
        
        boolean success;
        
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
                    RDFAuthorUtilities.ShowError(
                        "Incompatible File Format",
                        "This version requires " + FileFormatVersion + ", but this file is in " + formatVersion
                        + ".\nBlame the author." ,
                        RDFAuthorUtilities.Critical, null);
                    success = false;
                }
                else
                {
                    showTypes = s.readBoolean();
                    showIds = s.readBoolean();
                    showProperties = s.readBoolean();                
                    rdfModel = (ArcNodeList) s.readObject();

                    rdfModel.setController(this);
            
                    success = true;
                }
            }
            catch (Exception e)
            {
                RDFAuthorUtilities.ShowError(
                    "File Loading Failed", 
                    "Loading failed. Is this really an RDFAuthor file?\nError:\n"+e,
                    RDFAuthorUtilities.Critical, null);
                success = false;
            }
        }
        else
        {
            System.out.println("Don't know this type");
            success = false;
        }
        
        if (rdfModelView != null) // This is needed for 'revert' - doesn't display otherwise
        {
            rdfModelView.setNeedsDisplay(true);
        }
        
        if (!success)
        {
            System.out.println("Loading failed :-(");
        }
        
        return success;
    }
    
    public String windowNibName() {
        return "RDFAuthorDocument";
    }
    
    // Most of the initialisation happens here
    
    public void windowControllerDidLoadNib(NSWindowController  aController) {
        super.windowControllerDidLoadNib(aController);
        // Add any code here that need to be executed once the windowController has loaded the document's window.
	
        window = aController.window();
        
	// Attach the toolbar to the document window.
	NSToolbar theToolbar = new NSToolbar(RDFToolbar.identifier);
        theToolbar.setAllowsUserCustomization(true);
	theToolbar.setAutosavesConfiguration(true);
	theToolbar.setDisplayMode(NSToolbar.NSToolbarDisplayModeIconOnly);
        
        theToolbar.setDelegate((NSObject) rdfToolbar);
        
        window.setToolbar(theToolbar);
        
        // Set rdfModelView's size to current paper size
        
        rdfModelView.setSizeFromPrintInfo( NSPrintInfo.sharedPrintInfo() );
        
        // There must be a way to do this in interface builder...
        
        rdfModelScrollView.setHasHorizontalScroller(true);
        rdfModelScrollView.setHasVerticalScroller(true);
        rdfModelScrollView.setDocumentView(rdfModelView);
        rdfModelScrollView.setDrawsBackground(true);
        rdfModelScrollView.setBackgroundColor(NSColor.lightGrayColor());
        
        // This is for exporting
        exportMappings = new HashMap();
        
        exportMappings.put("RDF/XML Document", "RDF/XML-ABBREV");
        exportMappings.put("N-Triple Document", "N-TRIPLE");
        exportMappings.put("N3 Document", "N3");
        
        // Get notifications from the Schema Window
        
        NSSelector schemaChanged = new NSSelector("schemaSelectionChanged", 
                new Class[] {NSNotification.class} );
                
        NSNotificationCenter.defaultCenter().addObserver(
            this, schemaChanged, SchemaWindowController.schemaItemChangedNotification , null);
    }
    
    public void setPrintInfo(NSPrintInfo printInfo)
    {
        System.out.println("Setting paper size");
        rdfModelView.setSizeFromPrintInfo(printInfo);
        NSPrintInfo.setSharedPrintInfo(printInfo);
        super.setPrintInfo(printInfo);
    }
    
    public boolean showTextPreview(boolean showPreview, String type)
    {
        if (showPreview)
        {
            boolean success = createPreviewText(type);
            if (!success)
            {
                return false;
            }
            NSRect rect = rdfModelView.frame();
            previewView.setFrame(rect);
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
    
    public boolean createPreviewText(String type)
    {
        String rdfData = rdfModel.exportAsRDF(type);
        if (rdfData == null)
        {
            RDFAuthorUtilities.ShowError(
                "Serialisation Failed",
                "I couldn't convert this to '" + type + 
                "'. Try using 'Check Model' for possible problems.\n(Note: N3 Doesn't work currently)",
                RDFAuthorUtilities.Critical, window);
            previewTextView.setString("");
            return false;
        }
        
        previewTextView.setString(rdfData);
        return true;
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
    
    public void markQueryItems(boolean markThem)
    {
        if (markThem)
        {
            rdfModelView.deleteMode(false);
            rdfModelView.addConnection(false);
            rdfModelView.addNode(false);
            textDescriptionField.setStringValue("Click on items to mark them as unknown objects for query");
            markQueryObjects = true;
        }
        else
        {
            markQueryObjects = false;
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
            queryController.checkForDeletedItems(rdfModel);
        }
    }
    
    public void deleteCurrentObject()
    {
        if (rdfModel.currentObject() != null)
        {
            rdfModel.deleteObject(rdfModel.currentObject());
            queryController.checkForDeletedItems(rdfModel);
        }
    }
    
    public void addNodeAtPoint(String id, String typeNamespace, String typeName, NSPoint point, boolean isLiteral)
    {
        // Do the 'defaults' thing
        
        typeName = (typeName == null)? defaultClassName : typeName ;
        typeNamespace = (typeNamespace == null)? defaultClassNamespace : typeNamespace ;
        
        Node newNode = new Node(rdfModel, id, typeNamespace, typeName, point);
        rdfModel.add(newNode);
        newNode.setShowId(showIds);
        newNode.setShowType(showTypes);
        newNode.setIsLiteral(isLiteral);
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
            Arc newArc = new Arc(rdfModel, (Node)startNode, (Node)endNode, defaultPropertyNamespace, defaultPropertyName);
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
        
        if (markQueryObjects)
        {
            queryController.addQueryItem(item);
            rdfModelView.setNeedsDisplay(true);
        }
        else
        {
            rdfModel.setCurrentObject(item);
        }
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
    
    public void setIdForNodeAtPoint(String id, NSPoint point, boolean isLiteral)
    {
        ModelItem item = rdfModel.objectAtPoint(point);
        if ((item !=null) && item.isNode())
        {
            ((Node) item).setId(id);
        }
        else
        {
            addNodeAtPoint(id, null, null, point, isLiteral);
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
            addNodeAtPoint(null, namespace, name, point, false);  // false - not a literal
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
        queryController.drawQueryItems();
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
    
    // Schema window selection changed
    
    public void schemaSelectionChanged(NSNotification notification)
    {
        SchemaItem item = (SchemaItem) notification.object();
        
        if (item.type() == null)
        {
            defaultPropertyNamespace = null;
            defaultPropertyName = null;
            defaultClassNamespace = null;
            defaultClassName = null;
        }
        else if (item.type().equals(SchemaData.ClassPboardType)) // it's a class
        {
            defaultPropertyNamespace = null;
            defaultPropertyName = null;
            defaultClassNamespace = item.namespace();
            defaultClassName = item.name();
        }
        else if (item.type().equals(SchemaData.PropertyPboardType)) // it's a property
        {
            defaultPropertyNamespace = item.namespace();
            defaultPropertyName = item.name();
            defaultClassNamespace = null;
            defaultClassName = null;
        }
    }
    
}