//
//  GraphicalArc.java
//  RDFAuthor
//

/* $Id: GraphicalArc.java,v 1.1.1.1 2002-04-09 12:49:40 pldms Exp $ */

/*
    Copyright 2001, 2002 Damian Steer <dm_steer@hotmail.com>

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
    This is the JFC Version of Graphical Arc. Change this for other platforms
*/

package org.rdfweb.rdfauthor.view;

import java.awt.*;
import javax.swing.*;
import java.awt.geom.*;

import java.io.Writer;

import org.rdfweb.rdfauthor.model.*;
import org.rdfweb.rdfauthor.gui.*;

public class GraphicalArc implements GraphicalObject
{

    Arc arc;
    
    RDFModelView rdfModelView;
    
    Rectangle bounds = null;
    
    Color normalColor = new Color(0F, 0F, 1F, 0.5F);
    Color hilightColor = new Color(1F, 0F, 0F, 0.5F);
    GeneralPath arrowHead;
    GeneralPath arrowToDraw; // we precalculate the path, since it involves some overhead
    Dimension mySize;
    Dimension defaultSize = new Dimension(15,15);
    Rectangle handleRect = null;
    String displayString = null;
    
    public GraphicalArc(Arc arc, RDFModelView rdfModelView)
    {
        this.arc = arc;
        this.rdfModelView = rdfModelView;
        arc.setGraphicRep(this);
        
        initArrowHead();
        
        contentChanged();
        rdfModelView.addObject(this);
    }
    
    public ModelItem modelItem()
    {
        return arc;
    }
    
    public Rectangle bounds()
    {
        return bounds;
    }
    
    public void delete()
    {
        // We're going to be deleted, so redraw the space this node occupies
        rdfModelView.repaint(bounds);
        rdfModelView.removeObject(this);
    }
    
    public void changed() // something changed - needs redisplaying
    {
        rdfModelView.repaint(bounds);
    }
    
    public void initArrowHead()
    {
        arrowHead = new GeneralPath();
        arrowHead.moveTo(0.0f, 0.0f);
        arrowHead.lineTo(-20.0f, 4.0f);
        arrowHead.lineTo(-20.0f, -4.0f);
        arrowHead.closePath();
    }

    public boolean containsPoint(Point point)
    {
        return handleRect.contains(point);
    }
    
    public boolean intersects(Shape shape)
    {
        return shape.intersects(bounds);
    }
    
    public void drawNormal(Graphics2D g)
    {
        drawMe(normalColor, g);
    }

    public void drawHilight(Graphics2D g)
    {
        drawMe(hilightColor, g);
    }

    public void drawMe(Color myColor, Graphics2D g)
    {
      //if (bounds.intersectsRect(rect))
      //{
            g.setPaint(myColor);
            
            g.fill(arrowToDraw);
            g.draw(arrowToDraw);

	    /*
            if (displayString != null)
            {
                NSGraphics.drawAttributedString(displayString, handleRect);
            }
	    */
    }
    
    public void contentChanged()
    {
        String stringToDraw = arc.displayString();
        //if (stringToDraw == null)
        //{
            mySize = defaultSize;
            displayString = null;
	    //}
	    //else
	    /*{
            displayString = new NSAttributedString( stringToDraw );
            mySize = NSGraphics.sizeOfAttributedString(displayString);
	    }*/
        
        boundsChanged();
    }
    
    public void boundsChanged()
    {
        rdfModelView.repaint(bounds); // mark old bounds as dirty
        
        Point toNodePos = new Point( (int) arc.toNode().x(),
				     (int) arc.toNode().y() );
        Point fromNodePos = new Point( (int) arc.fromNode().x(),
				       (int) arc.fromNode().y() );
        
        int x = (toNodePos.x + fromNodePos.x) / 2;
        int y = (toNodePos.y + fromNodePos.y) / 2;
        
        // handleRect is the rectangle on the arc which is clickable and contains the text
        handleRect = new Rectangle(x - mySize.width/2,
                            y - mySize.height/2,
                            mySize.width,
                            mySize.height );
        
        createArrow(fromNodePos, toNodePos);
        
        bounds = arrowToDraw.getBounds();
        
        rdfModelView.repaint(bounds); // mark new bounds as dirty
    }
    
    public void createArrow(Point fromNodePos, Point toNodePos)
    {
        double dx = toNodePos.getX() - fromNodePos.getX();
        double dy = toNodePos.getY() - fromNodePos.getY();
        float angle = (float)Math.atan2(dy, dx);
        float distance = (float) fromNodePos.distance(toNodePos);
        
        arrowToDraw = new GeneralPath();
        
        arrowToDraw.append(arrowHead, false);
        
        if (distance > 50)
        {
            arrowToDraw.moveTo(-20, 0);
            arrowToDraw.lineTo(30 - distance, 0);
        }
        else if (!(distance < 20))
        {
            arrowToDraw.moveTo(-20, 0);
            arrowToDraw.lineTo(-distance, 0);
        }
        
        AffineTransform transform = new AffineTransform();
        
	transform.translate(toNodePos.getX(), toNodePos.getY());
        
	transform.rotate(angle);
        
        if (distance > 50)
        {
	  transform.translate( -15, 0);
        }
        
	arrowToDraw = (GeneralPath)
	  transform.createTransformedShape(arrowToDraw);
        
        //arrowToDraw.moveToPoint(fromNodePos);
        //arrowToDraw.lineToPoint(toNodePos);
        
        arrowToDraw.append(handleRect, false);
        //arrowToDraw.appendBezierPath(line);
    }
  /*
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
	}*/
}
