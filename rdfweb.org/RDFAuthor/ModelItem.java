/* Decompiled by Mocha from ModelItem.class */
/* Originally compiled from ModelItem.java */

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

import com.apple.cocoa.foundation.NSPoint;
import com.apple.cocoa.foundation.NSRect;

public class ModelItem extends java.lang.Object
{
    public ModelItem()
    {
    }

    public boolean isNode()
    {
        return false;
    }

    public boolean isLiteral()
    {
        return false;
    }

    public void setIsLiteral(boolean value)
    {
    }

    public void setIdStringValue(String string)
    {
    }

    public String idStringValue()
    {
        return null;
    }

    public void setTypeStringValue(String string)
    {
    }

    public String typeStringValue()
    {
        return null;
    }

    public void drawNormal(NSRect rect)
    {
    }

    public void drawHilight(NSRect rect)
    {
    }

    public boolean containsPoint(NSPoint point)
    {
        return false;
    }

    public NSRect rect()
    {
        return null;
    }

    public void setMyList(ArcNodeList list)
    {
    }

    public void delete()
    {
    }
}
