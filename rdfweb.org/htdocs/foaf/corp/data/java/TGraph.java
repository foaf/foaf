import java.util.*;
//import org.desire.rudolf.rdf.*;
//import org.w3c.rdf.*;

public class TGraph
{
    Vector edges;
    Vector nodes;
    Hashtable nodeLookup;
    Hashtable edgeLookup;

    public static void main(String[] args)
    {
	TGraph graph = new TGraph();

	//graph.buildGraph(graph);
	
	String topPerson = null;
	float avLen = 0;

System.out.println("<?xml version=\"1.0\" ?>\n"+
"<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""+
" xmlns:test=\"http://example.com/test/\">");


	for (Iterator i = graph.edges.iterator(); i.hasNext();)
	    {
		TEdge edge = (TEdge) i.next();
		TNode node1 = edge.node1;
		TNode node2 = edge.node2;

System.out.println("<rdf:Description rdf:about=\""+node1.getText()+"\">\n"+
//"<"+edge.getLabel()+" rdf:resource=\""+node2.getText()+"\" />\n"+
"<test:label rdf:resource=\""+node2.getText()+"\" />\n"+
"</rdf:Description>");

 	    }

System.out.println("</rdf:RDF>");

/*
	for (Iterator i = graph.nodes.iterator(); i.hasNext();)
	    {
		TNode node = (TNode) i.next();
		Vector paths =
		    graph.findShortestPathsFromNode(node);
		//System.out.println(graph.paths(paths));
		System.out.println("Node: " + node.getText());
		float len = graph.averagePathLength(paths);
		System.out.println("Av. path length: " +
				   len +
				   " connected to: " + (paths.size()+1) +
				   "/" + graph.nodes.size() + " people.");
		if ((topPerson == null) || avLen > len)
		    {
			topPerson = node.getText();
			avLen = len;
		    }
	    }
	
	System.out.println("\n" + topPerson + " wins with " +
			   avLen + " average path length.");

*/
    }

    public TGraph()
    {
	edges = new Vector();
	nodes = new Vector();
	nodeLookup = new Hashtable();
	edgeLookup = new Hashtable();
    }

    public void addPath(String node1, String node2, String edgeLabel)
    {
	node1 = node1.toLowerCase();
	node2 = node2.toLowerCase();

	if (node1.equals(node2))
	    {
		//System.err.println("WARNING: '" + node1 +
		//		   "' codepicted with themself in image: '" + 
		//		   edgeLabel + "'. Ignoring.");
		return;
	    }
	TEdge edgeObj = (TEdge) edgeLookup.get(node1 + node2);
	if (edgeObj == null) edgeObj = (TEdge) edgeLookup.get(node2 + node1);
	TNode nodeObj1 = (TNode) nodeLookup.get(node1);
	TNode nodeObj2 = (TNode) nodeLookup.get(node2);

	if (nodeObj1 == null)
	    {
		nodeObj1 = new TNode(node1);
		nodes.addElement(nodeObj1);
		nodeLookup.put(node1, nodeObj1);
	    }

	if (nodeObj2 == null)
	    {
		nodeObj2 = new TNode(node2);
		nodes.addElement(nodeObj2);
		nodeLookup.put(node2, nodeObj2);
	    }
	
	if (edgeObj == null)
	    {
		edgeObj = new TEdge(nodeObj1, nodeObj2, edgeLabel);
		edges.addElement(edgeObj);
		edgeLookup.put(node1 + node2, edgeObj);
	    }
	else
	    {
		edgeObj.addLabel(edgeLabel);
	    }
    }

    public String toString()
    {
	String string = "[";
	for (Enumeration e = edges.elements(); e.hasMoreElements();)
	    {
		TEdge edge = (TEdge) e.nextElement();
		TNode node1 = edge.node1;
		TNode node2 = edge.node2;
		string += "["+node1+","+edge+","+node2+"]";
	    }
	string += "]";
	return string;
    }

    public Vector getEdges()
    {
	return edges;
    }

    public Vector getNodes()
    {
	return nodes;
    }

    /*
     * This method finds the shortest paths between a node in the graph
     * and all other (reachable) nodes. It returns a Vector of tree
     * elements. To find each path start with each element of the
     * vector, find its parent, and repeat until parent == null. That
     * gives you all the shortest paths. Easy, huh? And hopefully
     * fast
     */

    public Vector findShortestPathsFromNode(TNode root, int ttl)
    {
	// Clear graph for search

	for (Iterator nodeIterator = nodes.iterator();
	     nodeIterator.hasNext();)
	    {
		TNode node = (TNode) nodeIterator.next();
		node.setVisited(false);
	    }

	// But (of course) we start with a node, so let's say it's
	// been visited

	root.setVisited(true);

	// Set up the root of the tree

	TElement treeRoot = new TElement(root, null);

	// Set up parents vector (just treeRoot) and leaves (empty)

	Vector parents = new Vector();
	parents.add(treeRoot);

	Vector leaves = new Vector();

	// indicates whether to keep going
	boolean continuing = true;

	while (continuing)
	    {
		Vector children = new Vector();
		continuing = false;

		for (Iterator parentIterator = parents.iterator();
		     parentIterator.hasNext();)
		    {
			TElement parent = (TElement)
			    parentIterator.next();
			TNode parentNode = parent.node();

			for (Iterator edgeIterator =
				 parentNode.getEdges().iterator();
			     edgeIterator.hasNext();)
			    {
				TEdge edge = (TEdge)
				    edgeIterator.next();
				TNode childNode =
				    edge.getOtherNode(parentNode);
				if (childNode.notVisited())
				    {
					childNode.setVisited(true);
					TElement child = new
					    TElement(childNode,
						     parent, edge);
					leaves.add(child);
					children.add(child);
					continuing = true;
				    }
			    }
		    }
		parents = children;
		ttl --;
		if (ttl == 0) continuing = false;
	    }

	return leaves;
    }

    public Vector findShortestPathsFromNodeNamed(String name, int ttl)
    {
	name = name.toLowerCase();
	TNode node = (TNode) nodeLookup.get(name);
	if (node == null)
	    {
		System.err.println("No node named '" + name + "'");
		return null;
	    }

	return findShortestPathsFromNode(node, ttl);
    }

    public String paths(Vector leaves)
    {
	String pathString = "";
	for (Iterator leafIterator = leaves.iterator();
	     leafIterator.hasNext();)
	    {
		TElement leaf = (TElement) leafIterator.next();

		while (leaf.parent() != null)
		    {
			pathString += leaf.node() + " , ";
			leaf = leaf.parent();
		    }

		pathString += leaf.node() + "\n";
	    }
	return pathString;
    }

    public int pathLength(TElement leaf)
    {
	int length = 1;
	while (leaf.parent() != null)
	    {
		leaf = leaf.parent();
		length ++;
	    }
	return length;
    }

    public float averagePathLength(Vector paths)
    {
	int length = 0;
	for (Iterator i = paths.iterator(); i.hasNext();)
	    {
		length += pathLength((TElement) i.next());
	    }

	return (float) length / (float) paths.size();
    }

    /*    public void buildGraph(TGraph graph){
	
	//find the people linked to this mbox
	String query1="SELECT ?mbox1, ?mbox2, ?uri "+
	    "WHERE "+
	    "   (foaf::depiction ?x ?img) "+
	    "   (foaf::depiction ?z ?img) "+
	    "   (foaf::thumbnail ?img ?uri) "+
	    //"   (http://purl.org/dc/elements/1.1/hasVersion ?img ?uri) "+
	    "   (foaf::mbox ?x ?mbox1) "+
	    "   (foaf::mbox ?z ?mbox2) "+
	    "USING foaf for http://xmlns.com/foaf/0.1/ ";
	
	//System.out.println(query1+"\n\n");
	
	java.sql.ResultSet Recordset1 =null;
	java.sql.Driver DriverRecordset1=null;
	java.sql.Connection Conn=null;
	
	try{
	    
	    DriverRecordset1=(java.sql.Driver)Class.forName("org.desire.rudolf.query.modelcore.ModelCoreDriver").newInstance();
	    Conn=java.sql.DriverManager.getConnection("rdf:jdbc:postgresql://localhost:5432/codepict?auth=password&user=postgres&password=notneeded");
	    java.sql.Statement StatementRecordset1 =Conn.createStatement();
	    Recordset1 =StatementRecordset1.executeQuery(query1);
	    StatementRecordset1.close();
	    Conn.close();
	}catch(Exception e){
	    System.err.println("oops"+e);
	    e.printStackTrace();
	}
	
	try{
	    while (Recordset1.next()) {
		
		String mb1=(String)Recordset1.getString("mbox1");
		String mb2=(String)Recordset1.getString("mbox2");
		String uri=(String)Recordset1.getString("uri");

		graph.addPath(mb1, mb2, uri);
	    }

	}catch(Exception e2){
	    System.err.println("oops"+e2);
	}

    }//end method
    */

}





