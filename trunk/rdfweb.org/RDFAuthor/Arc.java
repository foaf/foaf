/* Decompiled by Mocha from Arc.class */
/* Originally compiled from Arc.java */

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

public class Arc extends ModelItem
{
    Node fromNode;
    Node toNode;
    ArcNodeList myList;
    String typeName;
    String typeNamespace;
    boolean showType = false;
    
    NSColor normalColor = NSColor.blueColor();
    NSColor hilightColor = NSColor.redColor();
    NSBezierPath arrowHead;
    NSSize mySize;
    NSSize defaultSize = new NSSize(15,15);
    NSRect myRect;
    NSAttributedString displayString = null;

    public Arc(Node fromNode, Node toNode, String namespace, String name)
    {
        this.fromNode = fromNode;
        this.toNode = toNode;
        fromNode.addFromArc(this);
        toNode.addToArc(this);
        setType(name, namespace);
        arrowHead = NSBezierPath.bezierPath();
        arrowHead.moveToPoint(new NSPoint(0.0F, 0.0F));
        arrowHead.lineToPoint(new NSPoint(10.0F, -20.0F));
        arrowHead.lineToPoint(new NSPoint(-10.0F, -20.0F));
        arrowHead.closePath();
    }

    public void setMyList(ArcNodeList list)
    {
        myList = list;
    }

    public void setType(String namespace, String name)
    {
        typeName = name;
        typeNamespace = namespace;
    }

    public String typeName()
    {
        return typeName;
    }
    
    public String typeNamespace()
    {
        return typeNamespace;
    }
    
    public void delete()
    {
        toNode.removeToArc(this);
        fromNode.removeFromArc(this);
        myList.removeObject(this);
    }
    
    public void deleteFromNode(Node node)
    {
        if (toNode == node)
        {
            fromNode.removeFromArc(this);
        }
        else
        {
            toNode.removeToArc(this);
        }
        myList.removeObject(this);
    }
    
    public boolean isNode()
    {
        return false;
    }

    public void drawNormal()
    {
        drawMe(normalColor);
    }

    public void drawHilight()
    {
        drawMe(hilightColor);
    }

    public void drawMe(NSColor myColor)
    {
        double dx = toNode.position().x() - fromNode.position().x();
        double dy = toNode.position().y() - fromNode.position().y();
        float angle = (float)Math.atan2(dx, dy);
        
        myColor.set();
        
        NSBezierPath.fillRect(myRect);
        
        if (displayString != null)
        {
            NSGraphics.drawAttributedString(displayString, myRect);
        }
        
        NSBezierPath.strokeLineFromPoint(fromNode.position(), toNode.position());
        
        NSAffineTransform transformArrow = NSAffineTransform.transform();
        NSAffineTransform translateArrow = NSAffineTransform.transform();
        
        translateArrow.translateXYBy(toNode.position.x(), toNode.position().y());
        
        NSAffineTransform rotateTransform = NSAffineTransform.transform();
        
        rotateTransform.rotateByRadians(-angle);
        transformArrow.appendTransform(rotateTransform);
        transformArrow.appendTransform(translateArrow);
        
        NSBezierPath arrowToDraw = transformArrow.transformBezierPath(arrowHead);
        
        arrowToDraw.fill();
    }

    public boolean containsPoint(NSPoint point)
    {
        return myRect.containsPoint(point, true); // always flipped
    }
    
    public void calculateSize()
    {
        if (!showType)
        {
            mySize = defaultSize;
            displayString = null;
        }
        else
        {
            String typeToShow = (typeName == null)?"-- None --":typeName;
            
            displayString = new NSAttributedString(typeName);
            
            mySize = NSGraphics.sizeOfAttributedString(displayString);
        }
        
        calculateRectangle();
    }
    
    public void calculateRectangle()
    {
        float x = (toNode.position().x() + fromNode.position().x()) / 2.0F;
        float y = (toNode.position().y() + fromNode.position().y()) / 2.0F;
        myRect = new NSRect(x - mySize.width()/2F,
                            y - mySize.height()/2F,
                            mySize.width(),
                            mySize.height() );
    }
        
}
