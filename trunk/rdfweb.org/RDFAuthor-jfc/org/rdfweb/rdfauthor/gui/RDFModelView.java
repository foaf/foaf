/* RDFModelView */

/* $Id*/

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

package org.rdfweb.rdfauthor.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.*;

import org.rdfweb.rdfauthor.*;
import org.rdfweb.rdfauthor.model.*;
import org.rdfweb.rdfauthor.view.*;

public class RDFModelView extends JComponent
  implements MouseListener, MouseMotionListener
{

  RDFAuthorDocument rdfAuthorDocument;
  ArrayList graphicObjects;
  
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
    
  HashMap dragInformation = new HashMap();
  
  //BookmarkController bookmarkController;
  
  Point startPoint;
  Point endPoint;
  
  Rectangle selectionRect;


  public RDFModelView()
  {
        super();
        // Initialization code here.

	this.addMouseListener(this);
	this.addMouseMotionListener(this);
	
	
        graphicObjects = new ArrayList();
	/*      
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
	*/
    }
  /**
   * Dear god, why doesn't Rectangle have this constructor?
   * See 'JFC list of annoyances - vol XIII'
   */
  
  public Rectangle newRectangle(Point point1, Point point2)
  {
    int x;
    int y;
    int width;
    int height;

    width = (point1.x - point2.x);
    height = (point1.y - point2.y);
    
    if (point1.x > point2.x) x = point2.x;
    else
      {
	x = point1.x; width = -width; 
      }
    

    if (point1.y > point2.y) y = point2.y;
    else
      {
	y = point1.y;
	height = - height;
      }
    
    return new Rectangle(x, y, width, height);
  }
  
  public boolean isOpaque()
  {
    return true;
  }
    
  public void paint(Graphics g)
  {
    Graphics2D g2 = (Graphics2D) g;
    
    // Drawing code here.
        
    g2.setPaint(Color.white);
    
    g2.fill(g2.getClip());
        
    if (draggingConnection)
      {
	g2.setPaint(Color.lightGray);
	g2.drawLine(startPoint.x, startPoint.y,
		    endPoint.x, endPoint.y);
      }
    
    if (selectionRect != null)
      {
	g2.setPaint(Color.lightGray);
	g2.fill(selectionRect);
      }
    
    drawModel(g2);
    //rdfAuthorDocument.drawModel(g2);
  }

  /*
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
  */
  /*    
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
  */
  
    public void moveBy(float x, float y)
    {
      //this.scrollPoint(new NSPoint(this.visibleRect().x() - x,
      //        this.visibleRect().y() - y));
    }
  
  /* 
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
  */

  public void mouseReleased(MouseEvent theEvent)
  {
    Point point = theEvent.getPoint();
    
    switch (currentEditingMode)
      {
      case AddConnectionMode:
	startPoint = point; endPoint = point; break;
      case DeleteItemsMode:
	rdfAuthorDocument.deleteObject(objectAtPoint(point)); break;
      case AddNodeMode:
	rdfAuthorDocument.addNodeAtPoint(null, null, null, point, false);
	startPoint = point;
	break; // false above - default to resource
      case AddQueryItemMode:
	rdfAuthorDocument.addQueryItem(objectAtPoint(point)); break;
      case MoveSelectMode:
	startPoint = point;
	ModelItem item = objectAtPoint(point);
	if (theEvent.getModifiers() == InputEvent.SHIFT_MASK)
	  {
	    if (item != null)
	      {
		rdfAuthorDocument.addObjectToSelection(item);
		draggingSelection=true;
	      }
	    else
	      {
		draggingSelection = false;
		selectionRect = new Rectangle(0, 0);
		addingRectToSelection = true;
	      }
	  }
	else
	  {
	    if (item != null)
	      {
		rdfAuthorDocument.setSelectionToObject(item);
		draggingSelection=true;
	      }
	    else
	      {
		draggingSelection = false;
		selectionRect = new Rectangle(0, 0);
		addingRectToSelection = false;
	      }
	  }
      }
  }
    
  public void mouseDragged(MouseEvent theEvent)
  {
    Point point = theEvent.getPoint();
    
    switch (currentEditingMode)
      {
      case AddConnectionMode:
	repaint(newRectangle(startPoint, endPoint));
	repaint(newRectangle(startPoint, point));
	endPoint = point;
	draggingConnection = true;
	break;
      case MoveSelectMode:
      case AddNodeMode:
	if (draggingSelection)
	  {
	    rdfAuthorDocument.moveSelectionBy(
					      point.x -
					      startPoint.x,
					      point.y -
					      startPoint.y);
	    startPoint = point;
	  }
	else if (theEvent.getModifiers() == InputEvent.ALT_MASK)
	  {
	    this.moveBy(
			point.x - startPoint.x,
			point.y - startPoint.y);
	  }
	else if (selectionRect != null)
	  {
	    this.repaint(selectionRect);
	    selectionRect = newRectangle(startPoint, point);
	    this.repaint(selectionRect);
	  }
      }
  }

  public void mouseMoved(MouseEvent theEvent)
  {
  }
  
  public void mouseClicked(MouseEvent theEvent)
  {
  }

  public void mouseEntered(MouseEvent theEvent)
  {
  }

  public void mouseExited(MouseEvent theEvent)
  {
  }

  public void mousePressed(MouseEvent theEvent)
  {
      Point point = theEvent.getPoint();
              
      switch (currentEditingMode)
        {
	case AddConnectionMode:
	  draggingConnection = false;
	  rdfAuthorDocument.addConnectionFrom(objectAtPoint(startPoint),
					      objectAtPoint(point));
	  
	  repaint(newRectangle(startPoint, point));
	  break;
	case MoveSelectMode:
	  if (theEvent.getClickCount() == 2) // double click
	  {
	    // user held down command and double clicked - open url
	    if (theEvent.getModifiers() == InputEvent.META_MASK)
	      {
		rdfAuthorDocument.openUrlForObject(objectAtPoint(point));
	      }
	    else // show info for object
	      {
		rdfAuthorDocument.showInfoForObject(objectAtPoint(point));
	      }
	  }
	  else if (selectionRect != null)
	    {
	      repaint(selectionRect);
	      rdfAuthorDocument.setSelection(objectsInRect(selectionRect),
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
    /*  
    switch (currentEditingMode)
      {
      case MoveSelectMode:
	textDescriptionField.setStringValue(""); break;
      case AddNodeMode:
	textDescriptionField.setStringValue("Click to place a new node");
	break;
      case AddConnectionMode:
	textDescriptionField.setStringValue("Drag between two nodes to connect");
	break;
      case DeleteItemsMode:	
	textDescriptionField.setStringValue("Click on items to remove them from the model");
	break;
      case AddQueryItemMode:	
	textDescriptionField.setStringValue("Click on items to mark them as unknown objects for query");
	break;
      }
    */
  }
    
  public int editingMode()
  {
    return currentEditingMode;
  }

  /*
    public void copy(Object sender)
    {
        ArcNodeSelection selection = rdfAuthorDocument.selection();

        // No point copying if there is nothing to copy
        if (selection.kind() == ArcNodeSelection.Empty)
        {
            return;
        }

        System.out.println("Copying...");
        
        NSPasteboard pboard = NSPasteboard.generalPasteboard();
        ArrayList objectsToCopy = selection.copy();
        pboard.declareTypes(new NSArray(ArcNodeSelection.GraphPboardType), null);

        // Begin serialisation to clipboard
        
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream s = new ObjectOutputStream(out);
            s.writeObject(objectsToCopy);
            s.flush();
            out.flush();

            NSData copiedData = new NSData(out.toByteArray());

            s.close();
            out.close();
            
            pboard.setDataForType(copiedData, ArcNodeSelection.GraphPboardType);
        }
        catch (Exception e)
        {
            System.out.println("Got error: "+e);
        }
    }

    public void cut(Object sender)
    {
        copy(null);
        rdfAuthorDocument.deleteSelection();
    }
    
    public void paste(Object sender)
    {
        System.out.println("Pasting...");
        NSPasteboard pboard = NSPasteboard.generalPasteboard();

        // Can we paste this?
        if (pboard.types().containsObject(ArcNodeSelection.GraphPboardType))
        {
            System.out.println("Got something to paste...");
            NSData data = pboard.dataForType(ArcNodeSelection.GraphPboardType);
            ArrayList graph;

            // Begin deserialisation from clipboard
            
            try
            {
                ByteArrayInputStream in =
                new ByteArrayInputStream( data.bytes(0, data.length()) );
                ObjectInputStream s = new ObjectInputStream(in);

                graph = (ArrayList) s.readObject();

                s.close();
                in.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return;
            }
            
            System.out.println("Objects: " + graph);

            rdfAuthorDocument.selection().clear();
            
            for (Iterator iterator = graph.iterator(); iterator.hasNext();)
            {
                ModelItem object = (ModelItem) iterator.next();
                rdfAuthorDocument.addObject(object);
            }
        }
    }
  
    public void selectAll(Object sender)
    {
        rdfAuthorDocument.selectAll();
    }
    
    // This is so inefficient, but easy :-)
    
    public void clear(Object sender)
    {
        selectAll(null);
        rdfAuthorDocument.deleteSelection();
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
        
        // Inform the bookmark controller that the user added something
        // (for auto add)
        
        bookmarkController.addItem(pboard, type);
        
        if (type.equals(NSPasteboard.URLPboardType)) {
        
            NSArray URLs = (NSArray) pboard.propertyListForType(NSPasteboard.URLPboardType);

            String id = (String) URLs.objectAtIndex(0);
            
            // false - if new node don't want a literal
            rdfAuthorDocument.setIdForNode(id, objectAtPoint(point), false); 
        }
        else if (type.equals(NSPasteboard.StringPboardType)) {
            
            // Here I'm going to be sneaky. IE (and other Carbon apps) don't seem to
            // set the drag type for URLs - they are just strings. So I use the URI
            // checker. This is better since all URIs will be detected, but OTOH
            // URIs can match unintentionally.
            
            String id = (String) pboard.stringForType(NSPasteboard.StringPboardType);
            
            if (RDFAuthorUtilities.isValidURI(id))
            {
                // false - if new node don't want literal
                rdfAuthorDocument.setIdForNode(id, objectAtPoint(point), false);
            }
            else
            {
                // true - if new node want literal
                rdfAuthorDocument.setIdForNode(id, objectAtPoint(point), true);
            }
        }
        else if (type.equals(SchemaData.ClassPboardType))
        {
            NSDictionary info = (NSDictionary) pboard.propertyListForType(
                SchemaData.ClassPboardType);
            String name = (String) info.objectForKey("Name");
            String namespace = (String) info.objectForKey("Namespace");
            
            rdfAuthorDocument.setTypeForNode(namespace, name, objectAtPoint(point));
        }
        else if (type.equals(SchemaData.PropertyPboardType))
        {
            NSDictionary info = (NSDictionary) pboard.propertyListForType(
                SchemaData.PropertyPboardType);
            String name = (String) info.objectForKey("Name");
            String namespace = (String) info.objectForKey("Namespace");
            
            rdfAuthorDocument.setTypeForArc(namespace, name, objectAtPoint(point));
        }
        else {
            System.err.println("The view has not registered for drag type: " + type);
        }
        
        // Restore description to previous value
        textDescriptionField.setStringValue(saveDescription);
    }
  */
  
    /**
     * The following methods deal with the graphical representation of
     * the model - i.e. objects implementing the GraphicalObject interface
     **/
     
    public void addObject(GraphicalObject object)
    {
        graphicObjects.add(object);
        this.repaint(object.bounds());
    }
    
    public void addObject(ArcNodeList model)
    {
        // initialise from a model
        
        for (Iterator iterator = model.getNodes(); iterator.hasNext();)
        {
            Node node = (Node) iterator.next();
            
            GraphicalNode graphicNode = new GraphicalNode(node, this);
        }
        
        for (Iterator iterator = model.getArcs(); iterator.hasNext();)
        {
            Arc arc = (Arc) iterator.next();
            
            GraphicalArc graphicArc = new GraphicalArc(arc, this);
        }
    }
    
    public void removeObject(GraphicalObject object)
    {
        graphicObjects.remove(object);
    }
    
    public ModelItem objectAtPoint(Point point)
    {
        int index = graphicObjects.size();
        while (index-- > 0)
        {
            GraphicalObject item = (GraphicalObject) graphicObjects.get(index);
            if (item.containsPoint(point))
            {
                return item.modelItem();
            }
        }

        return null;
    }
    
    public ArrayList objectsInRect(Rectangle rect)
    {
        ArrayList hits = new ArrayList();
        
        int index = graphicObjects.size();
        while (index-- > 0)
        {
            GraphicalObject item = (GraphicalObject) graphicObjects.get(index);
            if (item.intersects(rect))
            {
                hits.add(item.modelItem());
            }
        }
        
        return hits;
    }
    
    public void drawModel(Graphics2D g) // draw model visible in rect
    {
        ArcNodeSelection selection = rdfAuthorDocument.selection();
        
        for (Iterator i = graphicObjects.iterator(); i.hasNext();)
        {
            GraphicalObject item = (GraphicalObject) i.next();
        
            if (item.intersects(g.getClip()))
            {
                if (selection.contains(item.modelItem()))
                {
                    item.drawHilight(g);
                }
                else
                {
                    item.drawNormal(g);
                }
	    }
        }
        
    }
    
  /*
    public void svgRepresentation(Writer writer)
                throws java.io.IOException
    {
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
        writer.write("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.0//EN\"\n");
        writer.write("	\"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">\n");
        
        NSSize docSize = frame().size();
        
        writer.write("\n<svg width=\"" + docSize.width() + "px\" ");
        writer.write("height=\"" + docSize.height() + "px\" xmlns=\"http://www.w3.org/2000/svg\">\n\n");
        
        writer.write("<title>" + rdfAuthorDocument.displayName() + "</title>\n");
        
        NSGregorianDate date = new NSGregorianDate(); // Stuff java.util.Calendar!
        
        writer.write("<desc>RDF model produced by RDFAuthor (http://rdfweb.org/people/damian/RDFAuthor) at " +
            date.toString() + "</desc>\n");
        
        GraphicalArc.svgArrowHead(writer);
        
        writer.write("<rect x=\"0px\" y=\"0px\" width=\"" + docSize.width() + "px\" ");
        writer.write("height=\"" + docSize.height() + "px\" fill=\"white\" />\n\n");
        
        ArcNodeSelection selection = rdfAuthorDocument.selection();
        
        for (Iterator i = graphicObjects.iterator(); i.hasNext();)
        {
            GraphicalObject item = (GraphicalObject) i.next();
            
            if (selection.contains(item.modelItem()))
            {
                item.drawSvgHilight(writer);
            }
            else
            {
                item.drawSvgNormal(writer);
            }
        }
        
        writer.write("\n\n</svg>");
    }
  */
}
