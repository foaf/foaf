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
    
    // This isn't correct currently - I need to check if arraylists have reverse iterators
    
    public ModelItem objectAtPoint(ArcNodeList model, NSPoint point)
    {
        for (Iterator iterator = model.getObjects(); iterator.hasNext();)
        {
            ModelItem item = (ModelItem) iterator.next();
            if (item.graphicRep().containsPoint(point))
            {
                return item;
            }
        }

        return null;
    }

}
