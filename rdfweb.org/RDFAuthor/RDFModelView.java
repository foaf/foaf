/* RDFModelView */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

public class RDFModelView extends NSView {

    MyDocument myDocument;
    boolean addingConnection = false;
    boolean draggingConnection = false;
    boolean addingNode = false;
    NSPoint startPoint;
    NSPoint endPoint;

    NSTextField textDescriptionField;

    public RDFModelView(NSRect frame) {
        super(frame);
        // Initialization code here.        
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
            NSColor.blueColor().set();
            NSBezierPath.strokeLineFromPoint(startPoint, endPoint);
        }
        
        myDocument.drawModel();
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
            myDocument.addNodeAtPoint(point);
            setNeedsDisplay(true);
        }
        else
        {
            myDocument.setCurrentObjectAtPoint(point);
            setNeedsDisplay(true);
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
            myDocument.moveCurrentObjectToPoint(point);
            setNeedsDisplay(true);
        }
    }
    
    public void mouseUp(NSEvent theEvent)
    {
        NSPoint point = convertPointFromView(theEvent.locationInWindow(), null);
        if (draggingConnection)
        {
            draggingConnection = false;
            myDocument.addConnectionFromPoint(startPoint, point);
            setNeedsDisplay(true);
        }
        else
        {
            myDocument.moveCurrentObjectToPoint(point);
            setNeedsDisplay(true);
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
        
}
