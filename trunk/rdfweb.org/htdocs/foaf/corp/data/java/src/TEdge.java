import java.lang.Math;
import java.util.Vector;

/*

# Copyright 2002 Damian Steer, Libby Miller
#
# 
#    This program is free software; you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation; either version 2 of the License, or
#    (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program; if not, write to the Free Software
#    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA

*/


public class TEdge
{
    TNode node1;
    TNode node2;
    Vector labels;
    int hash;

    public TEdge(TNode node1, TNode node2, String label)
    {
	this.node1 = node1;
	this.node2 = node2;
	labels = new Vector();
	labels.add(label);
	node1.addEdge(this);
	node2.addEdge(this);
    }

    public String toString()
    {
	return labels.toString();
    }

    public void addLabel(String label)
    {
	labels.add(label);
    }

    public String getLabel()
    {
	if (labels.size() == 1)
	    {
		return (String) labels.elementAt(0);
	    }
	else
	    {
		int i = (int) (Math.random() * (double) labels.size());
		return (String) labels.elementAt(i);
	    }
    }

    public TNode getOtherNode(TNode node)
    {
	return node.equals(node1)?node2:node1;
    }

}

