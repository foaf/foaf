/*****
*
*	Node_Builder.js
*	Copyright 2000, 2001, Kevin Lindsey
*
*****/

Node_Builder.VERSION = 1.2;

/*****
*
*	constructor
*
*****/
function Node_Builder(type, attributes, text) {
	this.type       = type;
	this.attributes = attributes;
	this.text       = text;

	this.node       = null;
	this.parent     = null;
	this.data       = null;
}


/*****
*
*	appendTo
*
*****/
Node_Builder.prototype.appendTo = function(parent) {
	var SVGDoc     = parent.getOwnerDocument();
	var node       = SVGDoc.createElement(this.type);

	this.node   = node;
	this.parent = parent;

	for (var a in this.attributes) {
		node.setAttribute(a, this.attributes[a]);
	}

	if (this.text) {
		var text = SVGDoc.createTextNode(this.text);
		node.appendChild(text);
		this.data = text;
	}

	if (parent) {
		parent.appendChild(this.node);
	}
};


/*****
*
*	remove
*
*****/
Node_Builder.prototype.remove = function() {
	if (this.node && this.parent) {
		this.parent.removeChild(this.node);
	}

	this.type       = "";
	this.attributes = null;
	this.text       = null;
	this.node       = null;
	this.parent     = null;
	this.data       = null;
};
