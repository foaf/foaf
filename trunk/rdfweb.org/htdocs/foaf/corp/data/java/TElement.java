/*
 * This class represents a simple tree element. It is only used for
 * the 'shortest paths' routine as a simple way to store paths in a
 * (hopefully) efficient manner.
 */

public class TElement
{
    TElement parent;
    TNode node;
    TEdge edge;

    public TElement(TNode node, TElement parent)
    {
	this.node = node;
	this.parent = parent;
    }

    public TElement(TNode node, TElement parent, TEdge edge)
    {
	this.node = node;
	this.parent = parent;
	this.edge = edge;
    }

    public TNode node()
    {
	return node;
    }

    public TElement parent()
    {
	return parent;
    }

    public TEdge edge()
    {
	return edge;
    }

}
