import java.util.*;

public class TNode{

    Vector edges;
    String text="nothing";
    int hash;
    boolean visited;

    public TNode(){
	this.setText("nothing");
	edges = new Vector();
    }

    public TNode(String text){
	this.setText(text);
	edges = new Vector();
    }

    public void setVisited(boolean visited)
    {
	this.visited = visited;
    }

    public boolean notVisited()
    {
	return !visited;
    }

    public void addEdge(TEdge edge)
    {
	edges.addElement(edge);
    }

    public Vector getEdges()
    {
	return edges;
    }

    public String getText(){
	return text;
    }
    
    public String toString()
    {
	return text;
    }
    
    public void setText(String text){
	this.text=text;
    }
    /*
    public boolean equals(Object obj)
    {
	if (obj instanceof TNode)
	    {
		return (text.equals(((TNode) obj).getText());
	    }
	return false;
    }
    */

    // Note: we add 'from' here to reduce the cloning of Vectors
    public void getPaths(Vector paths, Vector pathToMe, TEdge from, int ttl)
    {
	if (pathToMe.contains(this)) return;
	if (ttl-- == 0) return; // Kill it off if the path is too long
	
	Vector pathIncludingMe = (Vector) pathToMe.clone();
	
	// if this is the start node 'from' will be null
	if (from != null) pathIncludingMe.add(from);
	pathIncludingMe.addElement(this); // new path with me in it

	paths.add(pathIncludingMe); // add it to the other paths

	for (Enumeration e = edges.elements(); e.hasMoreElements();)
	    {
		TEdge edge = (TEdge) e.nextElement();

		if (edge == from) continue;

		TNode otherEnd = edge.getOtherNode(this);

		// add paths for edges
		otherEnd.getPaths(paths, pathIncludingMe, edge, ttl);
	    }
    }
    
    public Vector pathsFromWithTTL(int ttl)
    {
	Vector paths = new Vector();
	Vector pathToMe = new Vector();
	this.getPaths(paths, pathToMe, null, ttl);
	
	// We aren't interested in the first path found
	// (it is just this node)
	paths.removeElementAt(0);
	return paths;
    }

}
