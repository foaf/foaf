//
//  GraphicalNode.java
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
    This is the Cocoa Version of Graphical Node. Change this for other platforms
*/

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

public class GraphicalNode implements GraphicalObject
{
    
    Node node;
    
    RDFModelView rdfModelView;
    
    NSRect bounds = NSRect.ZeroRect;
    
    NSColor normalColor = NSColor.colorWithCalibratedRGB(0F, 1F, 0F, 0.5F);
    NSColor literalColor = NSColor.colorWithCalibratedRGB(1F, 1F, 0F, 0.5F);
    NSColor hilightColor = NSColor.colorWithCalibratedRGB(1F, 0F, 0F, 0.5F);
    NSSize mySize;
    NSSize defaultSize = new NSSize(20,20);
    NSAttributedString displayString = null;
    
    public GraphicalNode(Node node, RDFModelView rdfModelView)
    {
        this.node = node;
        this.rdfModelView = rdfModelView;
        node.setGraphicRep(this);
        
        calculateSize();
    }
    
    public ModelItem modelItem()
    {
        return node;
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

    
    public void drawNormal(NSRect rect)
    {
        if (node.isLiteral())
        {
            drawMe(literalColor, rect);
        }
        else
        {
            drawMe(normalColor, rect);
        }
    }

    public void drawHilight(NSRect rect)
    {
        drawMe(hilightColor, rect);
    }

    public void drawMe(NSColor myColor, NSRect rect)
    {
        if (bounds.intersectsRect(rect)) // do I need to be drawn?
        {
            myColor.set();
            
            NSBezierPath.bezierPathWithOvalInRect(bounds).fill();
            if (displayString != null)
            {
                NSGraphics.drawAttributedString(displayString, bounds);
            }
        }
    }

    public boolean containsPoint(NSPoint point)
    {
        return bounds.containsPoint(point, true); // RDFModelView always flipped
    }

    public void calculateSize()
    {
        String stringToDraw = node.displayString();
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
        //NSRect changedRect = bounds;
        
        rdfModelView.setNeedsDisplay(bounds); // mark old bounds as dirty
        
        bounds = new NSRect(node.x() - mySize.width()/2F,
                            node.y() - mySize.height()/2F,
                            mySize.width(),
                            mySize.height() );
        
        //changedRect = changedRect.rectByUnioningRect(bounds); // rectangle affected by changed
        
        //rdfModelView.setNeedsDisplay(changedRect);
        
        rdfModelView.setNeedsDisplay(bounds); // mark new bounds as dirty
    }
    
    public String drawSvgNormal()
    {
        if (node.isLiteral())
        {
            return drawSvg(literalColor);
        }
        else
        {
            return drawSvg(normalColor);
        }
    }
    
    public String drawSvgHilight()
    {
        return drawSvg(hilightColor);
    }
    
    public String drawSvg(NSColor colour)
    {
        String svgColour = "rgb(" + colour.redComponent() * 100 + "%," +
            colour.greenComponent() * 100 + "%," + colour.blueComponent() * 100 + "%)";
        
        String svg = "";
        
        String fontSpec = "font-family=\"Helvetica\" font-size=\"12\"";
        svg += "<g " + fontSpec + ">\n";
        
        svg += "<ellipse cx=\"" + node.x() + "px\" cy=\"" + node.y() + "px\" rx=\"" + mySize.width() / 2F +
            "px\" ry=\"" + mySize.height() / 2F + "px\" fill=\"" + svgColour +
            "\" fill-opacity=\"" + colour.alphaComponent() + "\" />\n";
        
        String stringToDraw = node.displayString();
        if (stringToDraw != null)
        {
            if (stringToDraw.indexOf("\n") > -1)
            {
                // This will break for some literals with many line breaks
                String top = stringToDraw.substring(0,stringToDraw.indexOf("\n"));
                String bottom = stringToDraw.substring(stringToDraw.indexOf("\n") + 1);
                
                svg += "<text x=\"" + (bounds.x()) +"px\" y=\"" 
                    + (bounds.y()+mySize.height()/2 - 4) +"px\" fill=\"black\">";
                svg += top + "</text>\n";
                
                svg += "<text x=\"" + (bounds.x()) +"px\" y=\"" + 
                    (bounds.y()+mySize.height() - 4) +"px\" fill=\"black\">";
                svg += bottom + "</text>\n";
            }
            else
            {
                svg += "<text x=\"" + (bounds.x()) +"px\" y=\"" +
                    (bounds.y()+mySize.height() - 4) +"px\" fill=\"black\">";
                svg += stringToDraw + "</text>\n";
            }
        }
        
        svg += "</g>\n\n";
        
        return svg;
    }

}
