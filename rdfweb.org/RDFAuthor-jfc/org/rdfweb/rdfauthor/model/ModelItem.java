/* ModelItem.java */

/* $Id: ModelItem.java,v 1.2 2002-04-11 12:32:05 pldms Exp $ */

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

//package org.rdfweb.rdfauthor.model;

import org.rdfweb.rdfauthor.view.GraphicalObject;

public interface ModelItem
{
    public boolean isNode();

    public void setMyList(ArcNodeList list);

    public void delete();
    
    public GraphicalObject graphicRep();
    
    public boolean matches(String text);
}
