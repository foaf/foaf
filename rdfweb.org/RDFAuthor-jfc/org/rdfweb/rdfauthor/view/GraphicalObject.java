//
//  GraphicalObject.java
//  RDFAuthor
//

/* $Id: GraphicalObject.java,v 1.2 2002-04-11 12:32:06 pldms Exp $ */

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

package org.rdfweb.rdfauthor.view;

import java.awt.*;
import java.awt.geom.*;
import java.io.Writer;

import ModelItem;

public interface GraphicalObject {
    
    public void drawHilight(Graphics2D g);
    
    public void drawNormal(Graphics2D g);
    
    public ModelItem modelItem();
    
    public Rectangle2D bounds();
    
    public void delete();
    
    public void changed();
    
    public boolean containsPoint(Point point);
    
    public boolean intersects(Shape shape);
    
    public void contentChanged();
    
    public void boundsChanged();
    
  //public void drawSvgNormal(Writer writer) throws java.io.IOException;
    
  //public void drawSvgHilight(Writer writer) throws java.io.IOException;
}
