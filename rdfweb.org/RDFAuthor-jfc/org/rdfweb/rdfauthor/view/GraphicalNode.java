//
//  GraphicalNode.java
//  RDFAuthor
//

/* $Id: GraphicalNode.java,v 1.2 2002-04-11 12:32:06 pldms Exp $ */

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
    This is the jfc Version of Graphical Node. Change this for other platforms
*/

package org.rdfweb.rdfauthor.view;

import java.awt.*;
import java.awt.geom.*;
import java.io.Writer;

import ModelItem;
import Node;
import org.rdfweb.rdfauthor.gui.*;

public class GraphicalNode implements GraphicalObject
{
    
    Node node;
    
    RDFModelView rdfModelView;
    
    Rectangle2D bounds = new Rectangle2D.Double();
    
    Color normalColor = new Color(0F, 1F, 0F, 0.5F);
    Color literalColor = new Color(1F, 1F, 0F, 0.5F);
    Color hilightColor = new Color(1F, 0F, 0F, 0.5F);
    Rectangle2D mySize;
    Rectangle2D defaultSize = new Rectangle(20,20);

  Font font = new Font("Helvetica", Font.PLAIN, 12);
  
    MultiText displayString;

  boolean graphicsReady;
  
    public GraphicalNode(Node node, RDFModelView rdfModelView)
    {
        this.node = node;
        this.rdfModelView = rdfModelView;
        node.setGraphicRep(this);

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
        return node;
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

    
    public void drawNormal(Graphics2D g)
    {
        if (node.isLiteral())
        {
            drawMe(literalColor, g);
        }
        else
        {
            drawMe(normalColor, g);
        }
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
            
      if (node.isLiteral())
	{
	  g.fill(bounds);
	}
      else
	{
	  g.fill(new Ellipse2D.Double(bounds.getX(), bounds.getY(),
		     bounds.getWidth(), bounds.getHeight()));
	}
      if (displayString != null)
	{
	  g.setPaint(Color.black);
	  
	  displayString.draw(g, bounds.getX(),
			     bounds.getY());
	}
        
    }

  public boolean containsPoint(Point point)
  {
    return bounds.contains(point);
  }
    
  public boolean intersects(Shape shape)
  {
    if (!graphicsReady) return true;

    return shape.intersects(bounds);
  }
    
  public void contentChanged()
  {
    String stringToDraw = node.displayString();
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
    
    bounds = new Rectangle2D.Double(node.x() - mySize.getWidth()/2,
			   node.y() - mySize.getHeight()/2,
			   mySize.getWidth(),
			   mySize.getHeight() );
    
    rdfModelView.repaint(bounds.getBounds()); // mark new bounds as dirty
  }
/*
    public void drawSvgNormal(Writer writer) throws java.io.IOException
    {
        if (node.isLiteral())
        {
            drawSvg(writer, literalColor);
        }
        else
        {
            drawSvg(writer, normalColor);
        }
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
        
        writer.write("<g " + fontSpec + ">\n");
        
        if (node.isLiteral())
        {
            writer.write("<rect x=\"" + bounds.x() + "px\" y=\"" + bounds.y() + 
                "px\" width=\"" + bounds.width() +
                "px\" height=\"" + bounds.height() + "px\" fill=\"" + svgColour +
                "\" fill-opacity=\"" + colour.alphaComponent() + "\" />\n");
        }
        else
        {
            writer.write("<ellipse cx=\"" + node.x() + "px\" cy=\"" + node.y() + 
                "px\" rx=\"" + mySize.width() / 2F +
                "px\" ry=\"" + mySize.height() / 2F + "px\" fill=\"" + svgColour +
                "\" fill-opacity=\"" + colour.alphaComponent() + "\" />\n");
        }
        
        String stringToDraw = node.displayString();
        
        if (stringToDraw != null)
        {
            if (stringToDraw.indexOf("\n") > -1)
            {
                // Display string has been fixed, so it will only ever contain
                // one new line - thus the following always works
                
                String top = stringToDraw.substring(0,stringToDraw.indexOf("\n"));
                String bottom = stringToDraw.substring(stringToDraw.indexOf("\n") + 1);
                
                writer.write("<text x=\"" + (bounds.x()) +"px\" y=\"" 
                    + (bounds.y()+mySize.height()/2 - 4) +"px\" fill=\"black\">");
                writer.write(top + "</text>\n");
                
                writer.write("<text x=\"" + (bounds.x()) +"px\" y=\"" + 
                    (bounds.y()+mySize.height() - 4) +"px\" fill=\"black\">");
                writer.write(bottom + "</text>\n");
            }
            else
            {
                writer.write("<text x=\"" + (bounds.x()) +"px\" y=\"" +
                    (bounds.y()+mySize.height() - 4) +"px\" fill=\"black\">");
                writer.write(stringToDraw + "</text>\n");
            }
        }
        
        writer.write("</g>\n\n");
    }
*/
}
