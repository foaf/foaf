//
//  GraphicalArc.java
//  RDFAuthor
//

/* $Id: GraphicalArc.java,v 1.2 2002-04-11 12:32:06 pldms Exp $ */

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
import java.awt.font.*;
import javax.swing.*;
import java.awt.geom.*;

import java.io.Writer;

//import org.rdfweb.rdfauthor.model.*;
import Arc;
import Node;
import ModelItem;
import ArcNodeList;
import org.rdfweb.rdfauthor.gui.*;

public class GraphicalArc implements GraphicalObject
{

    Arc arc;
    
    RDFModelView rdfModelView;
    
    Rectangle2D bounds = new Rectangle2D.Double();
    
    Color normalColor = new Color(0F, 0F, 1F, 0.5F);
    Color hilightColor = new Color(1F, 0F, 0F, 0.5F);
    GeneralPath arrowHead;
    GeneralPath arrowToDraw; // we precalculate the path, since it involves some overhead
    Rectangle2D mySize;
    Rectangle2D defaultSize = new Rectangle2D.Double(0, 0, 15,15);
    Rectangle2D handleRect;
    MultiText displayString;
  
  Font font = new Font("Helvetica", Font.PLAIN, 12);

  boolean graphicsReady;
  
    public GraphicalArc(Arc arc, RDFModelView rdfModelView)
    {
        this.arc = arc;
        this.rdfModelView = rdfModelView;
        arc.setGraphicRep(this);
        
        initArrowHead();

	// We need the graphics context to work out the shape
	if (rdfModelView.getGraphics() != null)
	  {
	    graphicsReady = true;
	    contentChanged();
	  }

        rdfModelView.addObject(this);
    }
    
    public ModelItem modelItem()
    {
        return arc;
    }
    
    public Rectangle2D bounds()
    {
        return bounds;
    }
    
    public void delete()
    {
        // We're going to be deleted, so redraw the space this node occupies
        rdfModelView.repaint(bounds.getBounds());
        rdfModelView.removeObject(this);
    }
    
    public void changed() // something changed - needs redisplaying
    {
        rdfModelView.repaint(bounds.getBounds());
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
      if (!graphicsReady) return true;
      
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
      if (!graphicsReady)
	{
	  graphicsReady = true;
	  contentChanged();
	}
      
      g.setPaint(myColor);
      
      g.fill(arrowToDraw);
      g.draw(arrowToDraw);
      
      if (displayString != null)
	{
	  g.setPaint(Color.black);
	  
	  displayString.draw(g, handleRect.getX(),
			   handleRect.getY());
	  
	}
    }
    
    public void contentChanged()
    {
      String stringToDraw = arc.displayString();
        if (stringToDraw == null)
        {
	  mySize = defaultSize;
	  displayString = null;
	}
	else
	  {
	    displayString = new MultiText(
					  (Graphics2D)
					  rdfModelView.getGraphics(),
					  stringToDraw, font);

	    mySize = displayString.getBounds();
	    
	  }
        boundsChanged();
    }
    
    public void boundsChanged()
    {
        rdfModelView.repaint(bounds.getBounds()); // mark old bounds as dirty
        
        Point2D toNodePos = new Point2D.Float( arc.toNode().x(),
				     arc.toNode().y() );
        Point2D fromNodePos = new Point2D.Float( arc.fromNode().x(),
				       arc.fromNode().y() );
        
        double x = (toNodePos.getX() + fromNodePos.getX()) / 2;
        double y = (toNodePos.getY() + fromNodePos.getY()) / 2;
        
        // handleRect is the rectangle on the arc which is clickable
	// and contains the text

        handleRect = new Rectangle2D.Double(
					    x - mySize.getWidth() / 2,
					    y - mySize.getHeight() / 2,
					    mySize.getWidth(),
					    mySize.getHeight() );
        
        createArrow(fromNodePos, toNodePos);
        
        bounds = arrowToDraw.getBounds();
	
        rdfModelView.repaint(bounds.getBounds()); // mark new bounds as dirty
    }
    
    public void createArrow(Point2D fromNodePos, Point2D toNodePos)
    {
        double dx = toNodePos.getX() - fromNodePos.getX();
        double dy = toNodePos.getY() - fromNodePos.getY();
        double angle = Math.atan2(dy, dx);
        double distance = fromNodePos.distance(toNodePos);
        
        arrowToDraw = new GeneralPath();
        
        arrowToDraw.append(arrowHead, false);
        
        if (distance > 50)
        {
            arrowToDraw.moveTo(-20, 0);
            arrowToDraw.lineTo(30 - (float) distance, 0);
        }
        else if (!(distance < 20))
        {
            arrowToDraw.moveTo(-20, 0);
            arrowToDraw.lineTo((float) -distance, 0);
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
        
        arrowToDraw.append(handleRect, false);
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
