/* Decompiled by Mocha from Arc.class */
/* Originally compiled from Arc.java */

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

public class Arc extends ModelItem
{
    Node fromNode;
    Node toNode;
    ArcNodeList myList;
    String propertyName;
    String propertyNamespace;
    boolean showProperty = false;
    
    NSColor normalColor = NSColor.colorWithCalibratedRGB(0F, 0F, 1F, 0.5F);
    NSColor hilightColor = NSColor.colorWithCalibratedRGB(1F, 0F, 0F, 0.5F);
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
        setProperty(name, namespace);
        arrowHead = NSBezierPath.bezierPath();
        arrowHead.moveToPoint(new NSPoint(0.0F, 0.0F));
        arrowHead.lineToPoint(new NSPoint(6.0F, -20.0F));
        arrowHead.lineToPoint(new NSPoint(-6.0F, -20.0F));
        arrowHead.closePath();
    }

    public void setMyList(ArcNodeList list)
    {
        myList = list;
    }

    public void setShowProperty(boolean value)
    {
        showProperty = value;
        calculateSize();
    }

    public void setProperty(String namespace, String name)
    {
        propertyName = name;
        propertyNamespace = namespace;
        calculateSize();
    }

    public String propertyName()
    {
        return propertyName;
    }
    
    public String propertyNamespace()
    {
        return propertyNamespace;
    }
    
    public Node fromNode()
    {
        return fromNode;
    }
    
    public Node toNode()
    {
        return toNode;
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
    
    public void nodeMoved()
    {
        calculateRectangle();
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
        
        if (displayString != null)
        {
            NSGraphics.drawAttributedString(displayString, myRect);
        }
    }

    public boolean containsPoint(NSPoint point)
    {
        return myRect.containsPoint(point, true); // always flipped
    }
    
    public void calculateSize()
    {
        if (!showProperty)
        {
            mySize = defaultSize;
            displayString = null;
        }
        else
        {
            String propertyToShow = (propertyName == null)?"-- None --":propertyName;
            
            displayString = new NSAttributedString(propertyToShow);
            
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
