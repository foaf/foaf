import java.util.Iterator;
import java.util.Vector;
import java.io.*;

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







