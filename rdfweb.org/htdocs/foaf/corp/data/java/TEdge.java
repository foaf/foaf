import java.lang.Math;
import java.util.Vector;

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

