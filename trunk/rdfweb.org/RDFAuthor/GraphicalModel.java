//
//  GraphicalModel.java
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
    This is the Cocoa Version of Graphical Model. Change this for other platforms
*/

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.util.Iterator;
import java.util.ListIterator;

public class GraphicalModel {
    
    public GraphicalModel(ArcNodeList model, RDFModelView rdfModelView)
    {
        // initialise from a model
        
        for (Iterator iterator = model.getNodes(); iterator.hasNext();)
        {
            Node node = (Node) iterator.next();
            
            GraphicalNode graphicNode = new GraphicalNode(node, rdfModelView);
        }
        
        for (Iterator iterator = model.getArcs(); iterator.hasNext();)
        {
            Arc arc = (Arc) iterator.next();
            
            GraphicalArc graphicArc = new GraphicalArc(arc, rdfModelView);
        }
    }
    
    public void drawModel(ArcNodeList model, NSRect rect) // draw model visible in rect
    {
        ModelItem currentObject = model.currentObject();
        
        // Draw Arcs then Nodes - looks better
        
        for (Iterator iterator = model.getArcs(); iterator.hasNext();)
        {
            Arc arc = (Arc) iterator.next();
            
            // In the following not typing the GraphicalObjects makes things really slow
            
            if (arc == currentObject)
            {
                ((GraphicalArc) arc.graphicRep()).drawHilight(rect);
            }
            else
            {
                ((GraphicalArc) arc.graphicRep()).drawNormal(rect);
            }
        }
        
        for (Iterator iterator = model.getNodes(); iterator.hasNext();)
        {
            Node node = (Node) iterator.next();
            
            if (node == currentObject)
            {
                ((GraphicalNode) node.graphicRep()).drawHilight(rect);
            }
            else
            {
                ((GraphicalNode) node.graphicRep()).drawNormal(rect);
            }
        }
    }
    
    public ModelItem objectAtPoint(ArcNodeList model, NSPoint point)
    {
        // getObjects(true) means start at end
        for (ListIterator iterator = model.getObjects(true); iterator.hasPrevious();)
        {
            ModelItem item = (ModelItem) iterator.previous();
            if (item.graphicRep().containsPoint(point))
            {
                return item;
            }
        }

        return null;
    }
    
    // Get the smallest rectangle which contains all the items.
    
    public NSRect bounds(ArcNodeList model)
    {
        NSRect boundsRect = NSRect.ZeroRect;
        
        for (ListIterator iterator = model.getObjects(); iterator.hasNext();)
        {
            ModelItem item = (ModelItem) iterator.next();
            
            NSRect itemBounds = item.graphicRep().bounds();
            
            boundsRect = boundsRect.rectByUnioningRect( itemBounds );
        }
        
        return boundsRect;
    }
    
    public String svgRepresentation(RDFAuthorDocument document, ArcNodeList model, RDFModelView rdfModelView)
    {
        String svg = "";
        
        svg += "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";
        svg += "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 20010904//EN\"\n";
        svg += "	\"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">\n";
        
        NSSize docSize = rdfModelView.frame().size();
        
        svg += "\n<svg width=\"" + docSize.width() + "px\" ";
        svg += "height=\"" + docSize.height() + "px\" xmlns=\"http://www.w3.org/2000/svg\">\n\n";
        
        svg += "<title>" + document.displayName() + "</title>\n";
        
        //String dateStamp = java.util.Calendar.getInstance().toString();
        
        NSGregorianDate date = new NSGregorianDate(); // Stuff java.util.Calendar
        String dateStamp = date.toString();
        
        System.out.println("Date stamp: " + dateStamp);
        
        svg += "<desc>RDF model produced by RDFAuthor (http://rdfweb.org/people/damian/RDFAuthor) at " +
            dateStamp + "</desc>\n";
        
        svg += GraphicalArc.svgArrowHead();
        
        svg += "<rect x=\"0px\" y=\"0px\" width=\"" + docSize.width() + "px\" ";
        svg += "height=\"" + docSize.height() + "px\" fill=\"white\" />\n\n";
        
        ModelItem currentObject = model.currentObject();
        
        // Draw Arcs then Nodes - looks better
        
        for (Iterator iterator = model.getArcs(); iterator.hasNext();)
        {
            Arc arc = (Arc) iterator.next();
            
            // In the following not typing the GraphicalObjects makes things really slow
            
            if (arc == currentObject)
            {
                svg += ((GraphicalArc) arc.graphicRep()).drawSvgHilight();
            }
            else
            {
                svg += ((GraphicalArc) arc.graphicRep()).drawSvgNormal();
            }
        }
        
        for (Iterator iterator = model.getNodes(); iterator.hasNext();)
        {
            Node node = (Node) iterator.next();
            
            if (node == currentObject)
            {
                svg += ((GraphicalNode) node.graphicRep()).drawSvgHilight();
            }
            else
            {
                svg += ((GraphicalNode) node.graphicRep()).drawSvgNormal();
            }
        }
        
        svg += "\n\n</svg>";
        
        return svg;
    }
        
        
}
