//
//  GraphicalArc.java
//  RDFAuthor
//

/* $Id: GraphicalArc.java,v 1.6 2002-02-05 16:02:57 pldms Exp $ */

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

import java.io.Writer;

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
        arrowHead.lineToPoint(new NSPoint(-20.0F, 4.0F));
        arrowHead.lineToPoint(new NSPoint(-20.0F, -4.0F));
        arrowHead.closePath();
    }

    public boolean containsPoint(NSPoint point)
    {
        return handleRect.containsPoint(point, true); // always flipped
    }
    
    public boolean intersectsRect(NSRect rect)
    {
        return handleRect.intersectsRect(rect);
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
            float angle = (float)Math.atan2(dy, dx);
            
            myColor.set();
            
            NSBezierPath.fillRect(handleRect);
            
            NSBezierPath.strokeLineFromPoint(fromNodePos, toNodePos);
            
            NSAffineTransform transformArrow = NSAffineTransform.transform();
            NSAffineTransform translateArrow = NSAffineTransform.transform();
            
            translateArrow.translateXYBy(toNodePos.x(), toNodePos.y());
            
            NSAffineTransform rotateTransform = NSAffineTransform.transform();
        
            rotateTransform.rotateByRadians(angle);
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
    
    public void drawSvgNormal(Writer writer) throws java.io.IOException
    {
        drawSvg(writer, normalColor);
    }
    
    public void drawSvgHilight(Writer writer) throws java.io.IOException
    {
        drawSvg(writer, hilightColor);
    }
    
    public void drawSvg(Writer writer, NSColor colour) throws java.io.IOException
    {
        String svgColour = "rgb(" + colour.redComponent() * 100 + "%," +
            colour.greenComponent() * 100 + "%," + colour.blueComponent() * 100 + "%)";
        
        String fontSpec = "font-family=\"Helvetica\" font-size=\"12\"";
        writer.write("<g " + fontSpec + " fill=\"" + svgColour +"\" fill-opacity=\"" + colour.alphaComponent() +
            "\">\n");
        
        writer.write("<rect x=\"" + handleRect.x() + "px\" y=\"" + handleRect.y() + "px\" width=\"" + mySize.width() +
            "px\" height=\"" + mySize.height() + "px\"/>\n"); 
        
        writer.write("<line x1=\"" + arc.fromNode().x() + "px\" y1=\"" + arc.fromNode().y() +
            "px\" x2=\"" + arc.toNode().x() + "px\" y2=\"" + arc.toNode().y() + "px\" stroke=\""
            + svgColour + "\" stroke-opacity=\"" + colour.alphaComponent() + "\"/>\n");
        
        double dx = arc.toNode().x() - arc.fromNode().x();
        double dy = arc.toNode().y() - arc.fromNode().y();
        double angle = Math.toDegrees(Math.atan2(dy,dx));
        
        writer.write("<g transform=\"translate(" + arc.toNode().x() + "," + arc.toNode().y() + ")\">\n");
        writer.write("<g transform=\"rotate(" + angle + ")\">\n");
        
        writer.write("<use xlink:href=\"#ArrowHead\"/>\n");
        
        writer.write("</g>\n");
        
        writer.write("</g>\n");
        
        String stringToDraw = arc.displayString();
        if (stringToDraw != null)
        {
            // This will never contain a line break (well, here's hoping :-)
            writer.write("<text x=\"" + (handleRect.x()) +"px\" y=\"" + 
                (handleRect.y()+mySize.height()-4) +"px\" fill=\"black\" fill-opacity=\"1.0\">");
            writer.write(stringToDraw + "</text>\n");
        }
        
        writer.write("</g>\n\n");
    }
    
    public static void svgArrowHead(Writer writer) throws java.io.IOException
    {
        writer.write("<defs>\n");
        
        writer.write("<path id=\"ArrowHead\" d=\"M 0 0 L -20 4 L -20 -4 Z\"/>\n");
        
        writer.write("</defs>\n");
    }
}
