//
//  GraphicalArc.java
//  RDFAuthor
//
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

/*
    This is the Cocoa Version of Graphical Arc. Change this for other platforms
*/

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

public class GraphicalArc implements GraphicalObject
{

    Arc arc;
    
    RDFModelView rdfModelView;
    
    NSRect bounds = NSRect.ZeroRect;
    
    NSColor normalColor = NSColor.colorWithCalibratedRGB(0F, 0F, 1F, 0.5F);
    NSColor hilightColor = NSColor.colorWithCalibratedRGB(1F, 0F, 0F, 0.5F);
    NSBezierPath arrowHead;
    NSSize mySize;
    NSSize defaultSize = new NSSize(15,15);
    NSRect handleRect = NSRect.ZeroRect;
    NSAttributedString displayString = null;
    
    public GraphicalArc(Arc arc, RDFModelView rdfModelView)
    {
        this.arc = arc;
        this.rdfModelView = rdfModelView;
        arc.setGraphicRep(this);
        
        initArrowHead();
        
        calculateSize();
    }
    
    public ModelItem modelItem()
    {
        return arc;
    }
    
    public NSRect bounds()
    {
        return bounds;
    }
    
    public void delete()
    {
        // We're going to be deleted, so redraw the space this node occupies
        rdfModelView.setNeedsDisplay(bounds);
    }
    
    public void changed() // something changed - needs redisplaying
    {
        rdfModelView.setNeedsDisplay(bounds);
    }
    
    public void initArrowHead()
    {
        arrowHead = NSBezierPath.bezierPath();
        arrowHead.moveToPoint(new NSPoint(0.0F, 0.0F));
        arrowHead.lineToPoint(new NSPoint(3.0F, -20.0F));
        arrowHead.lineToPoint(new NSPoint(-3.0F, -20.0F));
        arrowHead.closePath();
    }

    public boolean containsPoint(NSPoint point)
    {
        return handleRect.containsPoint(point, true); // always flipped
    }

    public void drawNormal(NSRect rect)
    {
        drawMe(normalColor, rect);
    }

    public void drawHilight(NSRect rect)
    {
        drawMe(hilightColor, rect);
    }

    public void drawMe(NSColor myColor, NSRect rect)
    {
        if (bounds.intersectsRect(rect))
        {
            NSPoint toNodePos = new NSPoint( arc.toNode().x(), arc.toNode().y() );
            NSPoint fromNodePos = new NSPoint( arc.fromNode().x(), arc.fromNode().y() );
            
            double dx = toNodePos.x() - fromNodePos.x();
            double dy = toNodePos.y() - fromNodePos.y();
            float angle = (float)Math.atan2(dx, dy);
            
            myColor.set();
            
            NSBezierPath.fillRect(handleRect);
            
            NSBezierPath.strokeLineFromPoint(fromNodePos, toNodePos);
            
            NSAffineTransform transformArrow = NSAffineTransform.transform();
            NSAffineTransform translateArrow = NSAffineTransform.transform();
            
            translateArrow.translateXYBy(toNodePos.x(), toNodePos.y());
            
            NSAffineTransform rotateTransform = NSAffineTransform.transform();
        
            rotateTransform.rotateByRadians(-angle);
            transformArrow.appendTransform(rotateTransform);
            transformArrow.appendTransform(translateArrow);
            
            NSBezierPath arrowToDraw = transformArrow.transformBezierPath(arrowHead);
            
            arrowToDraw.fill();
            
            if (displayString != null)
            {
                NSGraphics.drawAttributedString(displayString, handleRect);
            }
        }
    }
    
    public void calculateSize()
    {
        String stringToDraw = arc.displayString();
        if (stringToDraw == null)
        {
            mySize = defaultSize;
            displayString = null;
        }
        else
        {
            displayString = new NSAttributedString( stringToDraw );
            mySize = NSGraphics.sizeOfAttributedString(displayString);
        }
        
        calculateRectangle();
    }
    
    public void calculateRectangle()
    {
        //NSRect changedRect = bounds; // this rect will be oldbounds U newbounds - the changed region
        
        rdfModelView.setNeedsDisplay(bounds); // mark old bounds as dirty
        
        NSPoint toNodePos = new NSPoint( arc.toNode().x(), arc.toNode().y() );
        NSPoint fromNodePos = new NSPoint( arc.fromNode().x(), arc.fromNode().y() );
        
        float x = (toNodePos.x() + fromNodePos.x()) / 2.0F;
        float y = (toNodePos.y() + fromNodePos.y()) / 2.0F;
        
        // handleRect is the rectangle on the arc which is clickable and contains the text
        handleRect = new NSRect(x - mySize.width()/2F,
                            y - mySize.height()/2F,
                            mySize.width(),
                            mySize.height() );
        
        bounds = new NSRect(toNodePos, fromNodePos);
        bounds = bounds.rectByUnioningRect(handleRect);
        
        //changedRect = changedRect.rectByUnioningRect(bounds);
        
        //rdfModelView.setNeedsDisplay(changedRect);
        
        rdfModelView.setNeedsDisplay(bounds); // mark new bounds as dirty
    }

}
