//
//  GraphicalObject.java
//  RDFAuthor
//

/* $Id: GraphicalObject.java,v 1.3 2002-01-06 22:15:28 pldms Exp $ */

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

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

public interface GraphicalObject {
    
    public void drawHilight(NSRect rect);
    
    public void drawNormal(NSRect rect);
    
    public ModelItem modelItem();
    
    public NSRect bounds();
    
    public void delete();
    
    public void changed();
    
    public boolean containsPoint(NSPoint point);
    
    public boolean intersectsRect(NSRect rect);
    
    public void calculateSize(); // Give this (and the following) a better name!!!!
    
    public void calculateRectangle();
}
