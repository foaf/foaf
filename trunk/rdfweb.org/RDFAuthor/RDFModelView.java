/* RDFModelView */

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

import java.util.HashMap;

public class RDFModelView extends NSView {

    RDFAuthorDocument rdfAuthorDocument;
    
    static final int AddConnectionMode = 1;
    static final int AddNodeMode = 2;
    static final int AddQueryItemMode = 3;
    static final int MoveSelectMode = 4;
    static final int DeleteItemsMode =5;
    
    boolean draggingConnection = false;
    boolean draggingSelection = false;
    boolean addingRectToSelection = false;
    
    int currentEditingMode = MoveSelectMode;
    
    String saveDescription;
    
    float currentScale = 1;
    
    NSMutableArray dragTypesArray = new NSMutableArray();
    HashMap dragInformation = new HashMap();
    
    BookmarkController bookmarkController;
    
    NSPoint startPoint;
    NSPoint endPoint;
    
    NSRect selectionRect;

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
        NSBezierPath.fillRect(rect);
        
        if (draggingConnection)
        {
            NSColor.lightGrayColor().set();
            NSBezierPath.strokeLineFromPoint(startPoint, endPoint);
        }
        
        if (selectionRect != null)
        {
            NSColor.lightGrayColor().set();
            NSBezierPath.fillRect(selectionRect);
        }
        
        rdfAuthorDocument.drawModel(rect);
    }
    
    public NSData TIFFRepresentation() // I have half an idea what's going on here :-)
    {
        NSRect bounds = this.bounds();
        NSImage image;
        NSData tiffData;
        NSGraphicsContext currentContext;
        
        if (bounds.isEmpty()) {
            return null;
        }
        image = new NSImage(bounds.size());
        image.setFlipped(true);
        image.lockFocus();
        currentContext = NSGraphicsContext.currentContext();
        currentContext.saveGraphicsState();
        this.drawRect(bounds);
        currentContext.restoreGraphicsState();
        image.unlockFocus();
        tiffData = image.TIFFRepresentation();
        return tiffData;
    }

    
    public void setSizeFromPrintInfo(NSPrintInfo printInfo)
    {
        // This would be simple, but for the margins
        
        float width = printInfo.paperSize().width() - printInfo.leftMargin() - printInfo.rightMargin();
        float height = printInfo.paperSize().height() - printInfo.topMargin() - printInfo.bottomMargin();
        
        this.setFrameSize(new NSSize( width, height ));
    }
    
    public void sliderChanged(NSSlider slider)
    {
        // This is pretty sneaky - though maybe standard (I don't know)
        // We set the scale for the clip view of the scroll view - the
        // rdf model view is unchanged.
        
        NSClipView clipView = this.enclosingScrollView().contentView();
        
        float newScale = slider.floatValue() / 100f;
        
        // Scaling is cumulative for NSViews, so this sets the absolute scale
        
        float scaleValue = newScale / currentScale;
        
        // Remember the center so that we can move to it afterwards
        
        float midX = this.visibleRect().midX();
        float midY = this.visibleRect().midY();
        
        currentScale = newScale;
        
        clipView.scaleUnitSquareToSize(new NSSize(scaleValue, scaleValue));
        
        // Now scaling has occured try to center on the previous center
        // (visible rect has now changed due to the scaling)
        
        float x = midX - this.visibleRect().width()/2F;
        float y = midY - this.visibleRect().height()/2F;
        
        this.scrollPoint(new NSPoint(x,y));
    }
    
    public void moveBy(float x, float y)
    {
        this.scrollPoint(new NSPoint(this.visibleRect().x() - x,
                this.visibleRect().y() - y));
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
            rdfAuthorDocument.deleteSelection();
        }
        else
        {
            super.keyDown(theEvent);
        }
    }
    
    public void mouseDown(NSEvent theEvent)
    {
        NSPoint point = convertPointFromView(theEvent.locationInWindow(), null);
                
        switch (currentEditingMode)
        {
            case AddConnectionMode:	startPoint = point; endPoint = point; break;
            case DeleteItemsMode:	rdfAuthorDocument.deleteObjectAtPoint(point); break;
            case AddNodeMode:		rdfAuthorDocument.addNodeAtPoint(null, null, null, point, false);
                                        startPoint = point;
                                        break; // false above - default to resource
            case AddQueryItemMode:	rdfAuthorDocument.addQueryItemAtPoint(point); break;
            case MoveSelectMode:	startPoint = point;
                                        if (theEvent.modifierFlags() == NSEvent.AlternateKeyMask)
                                        {
                                        }
                                        else if (theEvent.modifierFlags() == NSEvent.ShiftKeyMask)
                                        {
                                            draggingSelection = 
                                                rdfAuthorDocument.addObjectAtPointToSelection(point);
                                            if (!draggingSelection)
                                            {
                                                selectionRect = NSRect.ZeroRect;
                                                addingRectToSelection = true;
                                            }
                                        }
                                        else
                                        {
                                            draggingSelection =
                                                rdfAuthorDocument.setSelectionToObjectAtPoint(point);
                                            if (!draggingSelection)
                                            {
                                                selectionRect = NSRect.ZeroRect;
                                                addingRectToSelection = false;
                                            }
                                        }
        }
    }
    
    public void mouseDragged(NSEvent theEvent)
    {
        NSPoint point = convertPointFromView(theEvent.locationInWindow(), null);
        
        switch (currentEditingMode)
        {
            // The following mess is due to a problem with anti-aliasing.
            // The anti-aliasing can affect points just outside the line, so I need to counter this.
            // This effect is much less visible for other graphical items.
            case AddConnectionMode:	NSRect changedRect = new NSRect(startPoint, endPoint);
                                        changedRect = changedRect.rectByUnioningRect(new NSRect(startPoint, point));
                                        changedRect = changedRect.rectByInsettingRect(-1.0f, -1.0f);
                                        setNeedsDisplay(changedRect);
                                        endPoint = point;
                                        draggingConnection = true;
                                        break;
            case MoveSelectMode:
            case AddNodeMode:		if (draggingSelection)
                                        {
                                            rdfAuthorDocument.moveSelectionBy(
                                                point.x() - startPoint.x(), point.y() - startPoint.y());
                                            startPoint = point;
                                        }
                                        else if (theEvent.modifierFlags() == NSEvent.AlternateKeyMask)
                                        {
                                            this.moveBy(
                                                point.x() - startPoint.x(), point.y() - startPoint.y());
                                        }
                                        else if (selectionRect != null)
                                        {
                                            this.setNeedsDisplay(selectionRect);
                                            selectionRect = new NSRect(startPoint, point);
                                            this.setNeedsDisplay(selectionRect);
                                        }
        }
    }
    
    public void mouseUp(NSEvent theEvent)
    {
        NSPoint point = convertPointFromView(theEvent.locationInWindow(), null);
        
        switch (currentEditingMode)
        {
            case AddConnectionMode:	draggingConnection = false;
                                        rdfAuthorDocument.addConnectionFromPoint(startPoint, point);
                                        // See above for explanation of this nonsense
                                        NSRect changedRect = new NSRect(startPoint, point);
                                        changedRect = changedRect.rectByInsettingRect(-1.0f, -1.0f);
                                        setNeedsDisplay(changedRect); // clean up drag line
                                        break;
            case MoveSelectMode:	if (theEvent.clickCount() == 2) // double click
                                        {
                                            // user held down command and double clicked - open url
                                            if (theEvent.modifierFlags() == NSEvent.CommandKeyMask)
                                            {
                                                rdfAuthorDocument.openUrlForObjectAtPoint(point);
                                            }
                                            else // show info for object
                                            {
                                                rdfAuthorDocument.showInfoForObjectAtPoint(point);
                                            }
                                        }
                                        else if (selectionRect != null)
                                        {
                                            this.setNeedsDisplay(selectionRect);
                                            rdfAuthorDocument.setSelectionFromRect(selectionRect,
                                                addingRectToSelection);
                                            selectionRect = null;
                                            addingRectToSelection = false;
                                        }
        }
        draggingSelection = false;
    }
    
    public void setEditingMode(int mode)
    {
        currentEditingMode = mode;
        
        switch (currentEditingMode)
        {
            case MoveSelectMode:	textDescriptionField.setStringValue(""); break;
            case AddNodeMode:		textDescriptionField.setStringValue("Click to place a new node");
                                        break;
            case AddConnectionMode:	textDescriptionField.setStringValue("Drag between two nodes to connect");
                                        break;
            case DeleteItemsMode:	
                    textDescriptionField.setStringValue("Click on items to remove them from the model");
                    break;
            case AddQueryItemMode:	
                    textDescriptionField.setStringValue("Click on items to mark them as unknown objects for query");
                    break;
        }
    }
    
    public void copy(Object sender)
    {
    }
    
    public void cut(Object sender)
    {
    }
    
    public void paste(Object sender)
    {
    }
    
    public void selectAll(Object sender)
    {
    }
    
    public void clear(Object sender)
    {
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
        
        if (type.equals(NSPasteboard.URLPboardType)) {
        
            NSArray URLs = (NSArray) pboard.propertyListForType(NSPasteboard.URLPboardType);

            String id = (String) URLs.objectAtIndex(0);
            
            rdfAuthorDocument.setIdForNodeAtPoint(id, point, false); // false - if new node don't want a literal
        }
        else if (type.equals(NSPasteboard.StringPboardType)) {
            
            String id = (String) pboard.stringForType(NSPasteboard.StringPboardType);
            
            rdfAuthorDocument.setIdForNodeAtPoint(id, point, true); // true - if new node make it a literal
        }
        else if (type.equals(SchemaData.ClassPboardType))
        {
            NSDictionary info = (NSDictionary) pboard.propertyListForType(
                SchemaData.ClassPboardType);
            String name = (String) info.objectForKey("Name");
            String namespace = (String) info.objectForKey("Namespace");
            
            rdfAuthorDocument.setTypeForNodeAtPoint(namespace, name, point);
        }
        else if (type.equals(SchemaData.PropertyPboardType))
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
