import java.util.Iterator;
import java.util.Vector;
import java.io.*;

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


public class foaftest
{
    public static void main(String[] args)
    {
	//String foaf = "linkdata.dat";
//	String foaf = "knows.dat"; 
	String foaf = "_corplinks-simple.dat";
//	String query = "mailto:libby.miller@bristol.ac.uk";
	String query = "Exxon-Mobil";
	if (args.length != 0) {
	  query = args[0];
	}
	TGraph db2 = loadDataFile( foaf );
	queryGraph(db2, query);

    }

    public static TGraph loadData (  )
    {
	System.out.println("Creating graph from random test data.");
	String link = "connection"; // we can store per-link information here
	TGraph graph = new TGraph();
        String kevin = "kevin";
        String erdos = "erdos";
        String janet = "janet";
        String john = "john";
     
        graph.addPath(kevin, erdos, link);
	graph.addPath(erdos, janet, link);
	graph.addPath(janet, john, link);
	return (graph);
    }


    public static TGraph loadDataFile ( String fname ) 
    {

	TGraph graph = new TGraph();
        String mb1;
        String mb2;
	String img;

        Vector urls = new Vector();
        BufferedReader br = null;
        try
        {
            br = new BufferedReader(
                new FileReader( fname ) );
            String line;
            while ( ( line = br.readLine() ) != null )
            {
                if ( !line.equals( "" ) )
                {
                    int i = line.indexOf( " " );
                    int j = line.lastIndexOf( " " );

			mb1=line.substring( 0, i ).trim();
			mb2=line.substring( i, j ).trim();
			img=line.substring( j ).trim();
 			System.out.println("Adding nodes: "+mb1+" -- "+img+" -=> "+ mb2 );
			graph.addPath(mb1, mb2, img);
                }
            }
        }
        catch ( Exception ee )
        {
            System.err.println( "\nerror " + ee );
        }
        try
        {
            br.close();
        }
        catch ( Exception ene )
        {
            System.out.println( "cannot close " + ene );
        }
	return graph;
    }



	public static void queryGraph (TGraph graph, String query) {
        int ttl = 6; // time to live
	System.out.println(
	    "Nodes: " + graph.getNodes().size() +"\n");
	System.out.println(
		"Connections: " + graph.getEdges().size());
	 Vector paths = graph.findShortestPathsFromNodeNamed( query, ttl );
	 System.out.println( "Paths are: "+graph.paths(paths) );
    }
}







