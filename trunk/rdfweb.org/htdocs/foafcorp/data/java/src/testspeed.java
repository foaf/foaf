import java.util.Iterator;
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


public class testspeed
{

    public static void main(String[] args)
    {
	int[] ttls = new int[]{4,5,6,7};
	int[] nodes = new int[]{100, 500, 1000, 5000, 10000, 50000, 100000, 500000};
	int[] avCons = new int[]{4, 6, 8, 10, 12, 14};
	for (int i = 0; i < nodes.length; i++)
	{
	    int nodeNum = nodes[i];
	    for (int j = 0; j < avCons.length; j++)
	    {
		int avCon = avCons[j];
		performTest(nodeNum, avCon, false, ttls);
	    }
	}
    }

    public static TGraph createGraph(int nodes, int avconnect, boolean connected)
    {
	TGraph graph = new TGraph();

	// if we are creating a connected graph then each node will
	// have two connections guaranteed. So we decrease avconnect by
	// two (ok - not quite true for first and last)

	if (connected) avconnect-=2;
	if (avconnect < 1)
	{
		System.err.println("Av Connection error");
		return null;
	}
	String[] nodeArray = new String[nodes];

	// create distinct node strings

	for (int i = 0; i < nodes; i++)
	    {
		nodeArray[i] = "node" + i;
	    }

	// connect nodes so that they have avconnect connections on
	// average

	for (int i = 0; i < nodes - 1; i++)
	    {
		for (int j = i + 1; j < nodes; j++)
		    {
			if (random(avconnect,nodes - 1)) 
			    graph.addPath(nodeArray[i], nodeArray[j], "connection");
		    }
	    }

	// If we want a connected graph then create it here

	if (connected)
	    {
		for (int i = 0; i < nodes - 1; i++)
		    {
			graph.addPath(nodeArray[i], nodeArray[i+1], "faux");
		    }
	    }

	return graph;
    }

    // This returns 'true' with probability x / y

    public static boolean random(int x, int y)
    {
	double prob = (double) x / (double) y;
	return (java.lang.Math.random() <= prob);
    }

    public static long time()
    {
	return java.util.Calendar.getInstance().getTime().getTime();
    }

    public static void performTest(int nodes, int avConnect, 
	boolean connected, int[] ttls)
    {
	System.out.println("Creating graph.");

	TGraph graph = createGraph(nodes, avConnect, connected);

	System.out.println(
		"Nodes: " + graph.getNodes().size() +
		" (" + nodes + ")");
	System.out.println(
		"Connections: " + graph.getEdges().size());
	System.out.println(
		"Average connections: " + avConnections(graph) +
		" (" + avConnect + ") ");

	for (int i = 0; i < ttls.length; i++)
	    {
		long time = time();
		int pathsNum = 0;
		int ttl = ttls[i];

		for (Iterator it = graph.getNodes().iterator(); it.hasNext();)
		    {
			TNode node = (TNode) it.next();
			Vector paths = graph.findShortestPathsFromNode(node, ttl);
			pathsNum += paths.size();
		    }

		System.out.println("TTL: " + 
			ttl + " Took: " + 
			((double) (time() - time) / 1000) + "s" +
		    " Paths: " + pathsNum);
	    }
    }

    public static double avConnections(TGraph graph)
    {
	return (((double) graph.getEdges().size() * 2) / (double) graph.getNodes().size());
    } 
}
