/* RDFModelView */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.util.HashMap;

public class RDFModelView extends NSView {

    RDFAuthorDocument rdfAuthorDocument;
    boolean addingConnection = false;
    boolean draggingConnection = false;
    boolean addingNode = false;
    boolean deleting = false;
    String saveDescription;
    
    NSMutableArray dragTypesArray = new NSMutableArray();
    HashMap dragInformation = new HashMap();
    
    BookmarkController bookmarkController;
    
    NSPoint startPoint;
    NSPoint endPoint;

    NSTextField textDescriptionField;

    public RDFModelView(NSRect frame) {
        super(frame);
        // Initialization code here.
        
        // Register for dragging types
        dragTypesArray.addObject(NSPasteboard.URLPboardType);
        dragTypesArray.addObject(NSPasteboard.StringPboardType);
        dragTypesArray.addObject(SchemaData.ClassPboardType);
        dragTypesArray.addObject(SchemaData.PropertyPboardType);
        
        registerForDraggedTypes((NSArray) dragTypesArray);
        
        // Put info for each drag type into hash for info field
        
        dragInformation.put(NSPasteboard.URLPboardType,
            "Drop URL on node to set its id. Otherwise creates a new node with this id.");
        dragInformation.put(NSPasteboard.StringPboardType,
            "Drop String on node to set its id. Otherwise creates a new node with this id.");
        dragInformation.put(SchemaData.ClassPboardType,
            "Drop class on node to set its type. Otherwise creates a new node of this type.");
        dragInformation.put(SchemaData.PropertyPboardType,
            "Drop property on arc to set the arc's property");
        
    }

    public boolean isOpaque()
    {
        return true;
    }
    
    public boolean isFlipped()
    {
        return true;
    }
    
    public void drawRect(NSRect rect) {
        // Drawing code here.
        
        NSColor.whiteColor().set();
        NSBezierPath.fillRect(bounds());
        
        if (draggingConnection)
        {
            NSColor.blueColor().set();
            NSBezierPath.strokeLineFromPoint(startPoint, endPoint);
        }
        
        rdfAuthorDocument.drawModel();
    }
    
    public boolean acceptsFirstResponder()
    {
        return true;
    }
    
    public void keyDown(NSEvent theEvent)
    {
        String chars = theEvent.characters();
        if (chars.equals("\t"))
        {
            rdfAuthorDocument.selectNextObject();
        }
        else if (chars.equals("\u0019")) // shift-tab (always, I hope)
        {
            rdfAuthorDocument.selectPreviousObject();
        }
        else if (chars.equals("\u007F")) // backspace
        {
            rdfAuthorDocument.deleteCurrentObject();
        }
        else
        {
            super.keyDown(theEvent);
        }
    }
    
    public void mouseDown(NSEvent theEvent)
    {
        NSPoint point = convertPointFromView(theEvent.locationInWindow(), null);
        if (addingConnection)
        {
            startPoint = point;
        }
        else if (addingNode)
        {
            rdfAuthorDocument.addNodeAtPoint(null, null, null, point, false); // false - default to resource
        }
        else if (deleting)
        {
            rdfAuthorDocument.deleteObjectAtPoint(point);
        }
        else
        {
            rdfAuthorDocument.setCurrentObjectAtPoint(point);
        }
    }
    
    public void mouseDragged(NSEvent theEvent)
    {
        NSPoint point = convertPointFromView(theEvent.locationInWindow(), null);
        if (addingConnection)
        {
            endPoint = point;
            draggingConnection = true;
            setNeedsDisplay(true);
        }
        else
        {
            rdfAuthorDocument.moveCurrentObjectToPoint(point);
        }
    }
    
    public void mouseUp(NSEvent theEvent)
    {
        NSPoint point = convertPointFromView(theEvent.locationInWindow(), null);
        if (draggingConnection)
        {
            draggingConnection = false;
            rdfAuthorDocument.addConnectionFromPoint(startPoint, point);
        }
        else if (!addingNode)
        {
            if (theEvent.clickCount() == 2) // double click - show Info for item
            {
                rdfAuthorDocument.showInfoForObjectAtPoint(point);
            }
        }
    }
    
    public void addConnection(boolean value)
    {
        addingConnection = value;
    }
    
    public void addNode(boolean value)
    {
        addingNode = value;
    }
    
    public void deleteMode(boolean value)
    {
        deleting = value;
    }
    
    // Drag and Drop stuff begins here.
    
    public String validateDrag(NSDraggingInfo sender) {
        // if the source of the drag operation does not originate from this view...
        if(sender.draggingSource() != this) {
            // get the pasteboard and get the type of item dropped
            NSPasteboard pb = sender.draggingPasteboard();
            String type = pb.availableTypeFromArray((NSArray) dragTypesArray);
            
            // if type exists return the string for it else return null
            if(type != null) {
                return type;
            }
        }
        return null;
    }

    public int draggingEntered(NSDraggingInfo sender) {
        // if drag operation is valid, let the pasteboard know
        String type = validateDrag(sender);
        if(type != null) {
            saveDescription = textDescriptionField.stringValue();
            textDescriptionField.setStringValue((String) dragInformation.get(type));
            return NSDraggingInfo.DragOperationCopy;
        }
        // else return no operation
        return NSDraggingInfo.DragOperationNone;
    }
    
    public int draggingUpdated(NSDraggingInfo sender) {
        // if drag operation is valid, let the pasteboard know
        // the app is only copying the type (filename in this case)
        if(validateDrag(sender) != null) {
            return NSDraggingInfo.DragOperationCopy;
        }
        // else return no operation
        return NSDraggingInfo.DragOperationNone;
    }

    public void draggingExited(NSDraggingInfo sender) {
        // Restore description to previous value, and redraw
        textDescriptionField.setStringValue(saveDescription);
        this.setNeedsDisplay(true);
    }
    
    public boolean prepareForDragOperation(NSDraggingInfo sender) {
        // if dragging is valid, let the pasteboard know the app is prepared
        if(validateDrag(sender) != null) {
            return true;
        }
        
        return false;
    }
    
    public boolean performDragOperation(NSDraggingInfo sender) {
        // if dragging is valid, let the pasteboard know the app is performing operation
        if(validateDrag(sender) != null) {
            return true;
        }
        return false;
    }
    
    public void concludeDragOperation(NSDraggingInfo sender) {
        
        // create a new NSPasteboard object to confirm type of dropped item
        NSPasteboard pboard = sender.draggingPasteboard();
        
        NSPoint point = convertPointFromView(sender.draggingLocation(), null);
        
        // check type to make sure it conforms with type you registered
        String type = pboard.availableTypeFromArray((NSArray) dragTypesArray);
        
        bookmarkController.addItem(pboard, type);
        
        if (type.equalsIgnoreCase(NSPasteboard.URLPboardType)) {
        
            NSArray URLs = (NSArray) pboard.propertyListForType(NSPasteboard.URLPboardType);

            String id = (String) URLs.objectAtIndex(0);
            
            rdfAuthorDocument.setIdForNodeAtPoint(id, point, false); // false - if new node don't want a literal
        }
        else if (type.equalsIgnoreCase(NSPasteboard.StringPboardType)) {
            
            String id = (String) pboard.stringForType(NSPasteboard.StringPboardType);
            
            rdfAuthorDocument.setIdForNodeAtPoint(id, point, true); // true - if new node make it a literal
        }
        else if (type.equalsIgnoreCase(SchemaData.ClassPboardType))
        {
            NSDictionary info = (NSDictionary) pboard.propertyListForType(
                SchemaData.ClassPboardType);
            String name = (String) info.objectForKey("Name");
            String namespace = (String) info.objectForKey("Namespace");
            
            rdfAuthorDocument.setTypeForNodeAtPoint(namespace, name, point);
        }
        else if (type.equalsIgnoreCase(SchemaData.PropertyPboardType))
        {
            NSDictionary info = (NSDictionary) pboard.propertyListForType(
                SchemaData.PropertyPboardType);
            String name = (String) info.objectForKey("Name");
            String namespace = (String) info.objectForKey("Namespace");
            
            rdfAuthorDocument.setTypeForArcAtPoint(namespace, name, point);
        }
        else {
            System.err.println("The view has not registered for drag type: " + type);
        }
        
        // Restore description to previous value, and redraw
        textDescriptionField.setStringValue(saveDescription);
        setNeedsDisplay(true);
    }

}
