/* Decompiled by Mocha from Node.class */
/* Originally compiled from Node.java */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import java.util.Enumeration;
import java.util.Vector;

public class Node extends ModelItem
{
    String id;
    String typeNamespace;
    String typeName;
    float size;
    Vector arcsFrom;
    Vector arcsTo;
    ArcNodeList myList;
    boolean literal;
    boolean showType = false;
    boolean showId = false;
    
    NSPoint position;
    NSColor normalColor = NSColor.blackColor();
    NSColor hilightColor = NSColor.redColor();
    NSSize mySize;
    NSSize defaultSize = new NSSize(20,20);
    NSRect myRect;
    NSAttributedString displayString = null;

    public Node(String id, String typeNamespace, String typeName, NSPoint position)
    {
        size = 10.0F;
        literal = false;
        this.id = id;
        setType(typeNamespace,typeName);
        this.position = position;
        arcsFrom = new Vector();
        arcsTo = new Vector();
    }

    public void setId(String theString)
    {
        id = theString;
        calculateSize();
    }

    public String id()
    {
        return id;
    }

    public void setType(String namespace, String name)
    {
        typeNamespace = namespace;
        typeName = name;
        calculateSize();
    }

    public String typeNamespace()
    {
        return typeNamespace;
    }
    
    public String typeName()
    {
        return typeName;
    }
    
    public boolean isLiteral()
    {
        return literal;
    }

    public void setIsLiteral(boolean literalValue)
    {
        literal = literalValue;
    }

    public void setMyList(ArcNodeList list)
    {
        myList = list;
    }

    public void delete()
    {
        for (Enumeration enumerator = arcsFrom.elements(); enumerator.hasMoreElements(); )
        {
            Arc arc = (Arc)enumerator.nextElement();
            arc.deleteFromNode(this);
        }
        for (Enumeration enumerator = arcsTo.elements(); enumerator.hasMoreElements(); )
        {
            Arc arc = (Arc)enumerator.nextElement();
            arc.deleteFromNode(this);
        }
        myList.removeObject(this);
    }

    public void addToArc(Arc arc)
    {
        arcsTo.add(arc);
    }

    public void removeToArc(Arc arc)
    {
        arcsTo.remove(arc);
    }

    public void addFromArc(Arc arc)
    {
        arcsFrom.add(arc);
    }

    public void removeFromArc(Arc arc)
    {
        arcsFrom.remove(arc);
    }

    public void setPosition(NSPoint position)
    {
        this.position = position;
    }

    public boolean isNode()
    {
        return true;
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
        NSBezierPath.bezierPathWithOvalInRect(myRect).fill();
        if (displayString != null)
        {
            NSGraphics.drawAttributedString(displayString, myRect);
        }
    }

    public boolean containsPoint(NSPoint point)
    {
        return myRect.containsPoint(point, true); // RDFModelView always flipped
    }

    public NSPoint position()
    {
        return position;
    }
    
    public void calculateSize()
    {
        if (!showType && !showId)
        {
            mySize = defaultSize;
            displayString = null;
        }
        else
        {
            String typeToShow = (typeName == null)?"-- None --":typeName;
            String idToShow = (id == null)?"-- None --":id;
            
            if (showType && showId)
            {
                displayString = new NSAttributedString(typeName + "\n" + id);
            }
            else if (showType)
            {
                displayString = new NSAttributedString(typeName);
            }
            else
            {
                displayString = new NSAttributedString(id);
            }
            
            mySize = NSGraphics.sizeOfAttributedString(displayString);
        }
        
        calculateRectangle();
    }
    
    public void calculateRectangle()
    {
        myRect = new NSRect(position.x() - mySize.width()/2F,
                            position.y() - mySize.height()/2F,
                            mySize.width(),
                            mySize.height() );
    }
}
