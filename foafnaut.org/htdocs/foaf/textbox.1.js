/*
Snagged (with permission) from 
  http://www.kevlindev.com/gui/widgets/textbox/index.htm 
*/

/*****
*
*	Textbox.js
*
*****/
Textbox.VERSION = "JL-0.2";
Textbox.textboxes = new Array();


/*****
*
*	contructor
*
*****/
function Textbox(x, y, width, height, parent) {
	this.width  = width;
	this.height = height;
	this.parent = parent;

	var serial = "_textbox_" + Textbox.textboxes.length;
	var SVGDoc = parent.getOwnerDocument();
	var trans  = "translate(" + x + "," + y + ")";
	var g = new Node_Builder( "g", { id: serial, transform: trans } );
	var rect1 = new Node_Builder(
		"rect",
		{ x: -1, y: -1, width: width+1, height: height+1, fill: "#ff9999" }
	);
	var rect2 = new Node_Builder(
		"rect",
		{ x: 0, y: 0, width: width+1, height: height+1, fill: "#ffcccc" }
	);
	var rect3 = new Node_Builder(
		"rect",
		{ width: width, height: height, fill: "white"  }
	);
	var clip_path = new Node_Builder( "clipPath", { id: serial+"_cp" } );
	var rect4 = new Node_Builder(
		"rect",
		{ width: width, height: height, fill: "white" }
	);
	
	var textbox = new Node_Builder(
		"text",
		{ y: "1em", fill: "black", clip_path: "url(#"+serial+"_cp)" }
	);
	g.appendTo(parent);
	clip_path.appendTo(g.node);
	rect4.appendTo(clip_path.node);
	rect1.appendTo(g.node);
	rect2.appendTo(g.node);
	rect3.appendTo(g.node);
	textbox.appendTo(g.node);

	this.textbox = textbox.node;
 g.node.addEventListener("keypress", TextboxKeypress, false);
 textbox.node.addEventListener("keypress", TextboxKeypress, false);
    rect3.node.addEventListener("mouseover", TextboxFocus, false);
    rect3.node.addEventListener("mouseout", TextboxBlur, false);

	Textbox.textboxes[Textbox.textboxes.length] = this;
}
function TextboxNB(x, y, width, height, parent) {
	this.width  = width;
	this.height = height;
	this.parent = parent;

	var serial = "_textbox_" + Textbox.textboxes.length;
	var SVGDoc = parent.getOwnerDocument();
	var trans  = "translate(" + x + "," + y + ")";
	var g = new Node_Builder( "g", { id: serial, transform: trans } );
	var textbox = new Node_Builder(
		"text",
		{ y: "1em", fill: "black", clip_path: "url(#"+serial+"_cp)" }
	);
	g.appendTo(parent);
	textbox.appendTo(g.node);

	this.textbox = textbox.node;
 g.node.addEventListener("keypress", TextboxKeypress, false);
 textbox.node.addEventListener("keypress", TextboxKeypress, false);

	Textbox.textboxes[Textbox.textboxes.length] = this;
}

function TextboxOutput(x, y, width, height, parent) {
	this.width  = width;
	this.height = height;
	this.parent = parent;

	var serial = "_textbox_" + Textbox.textboxes.length;
	var SVGDoc = parent.getOwnerDocument();
	var trans  = "translate(" + x + "," + y + ")";
	var g = new Node_Builder( "g", { id: serial, transform: trans } );
	var rect3 = new Node_Builder(
		"rect",
		{ width: width, height: height, fill: "white"  }
	);
	var clip_path = new Node_Builder( "clipPath", { id: serial+"_cp" } );
	var rect4 = new Node_Builder(
		"rect",
		{ width: width, height: height, fill: "white" }
	);
	
	var textbox = new Node_Builder(
		"text",
		{ y: "1em", fill: "black", clip_path: "url(#"+serial+"_cp)" }
	);
	g.appendTo(parent);
	clip_path.appendTo(g.node);
	rect4.appendTo(clip_path.node);
	rect3.appendTo(g.node);
	textbox.appendTo(g.node);

	this.textbox = textbox.node;
	Textbox.textboxes[Textbox.textboxes.length] = this;
}

TextboxFocus=function(evt) {
 e=evt.getTarget()
 e.setAttribute('fill','#ffff00')
}

TextboxBlur=function(evt) {
 e=evt.getTarget()
 e.setAttribute('fill','white')
}

/*****
*
*	TextboxKeypress
*
*	Process a keypress event
*
*****/
TextboxKeypress = function(event) {
try {
	var textbox = Textbox.Find_Textbox(event.getTarget().getParentNode());
	var key = event.getCharCode();
	if ( key >= 32 && key <= 127 ) {
 try {
		textbox.add_char(String.fromCharCode(key));
 } catch (e) { }
	} else if ( key == 8 ) {
		textbox.delete_char();
	} else if ( key == 13 ) {
		textbox.add_tspan("");
	} else {
		//alert(key);
	}
} catch (e) {}
}

/*****
*
*	add_char
*
*	Add a character to end of the current line
*	If the current line exceeds the width of the
*	textbox, then create a new line
*
*****/
Textbox.prototype.add_char = addChar
TextboxNB.prototype.add_char = addChar
TextboxOutput.prototype.add_char = addChar

function addChar(new_char) {
	var textbox = this.textbox;
 if ( !textbox.hasChildNodes() ) { this.add_tspan("", 0) }
 	var tspan = textbox.getLastChild();
  var data  = tspan.getFirstChild();
 	if (new_char.length!=0) {
   data.appendData(new_char);
   if ( tspan.getComputedTextLength && tspan.getComputedTextLength() > this.width ) {
    this.width=tspan.getComputedTextLength()
    nde=textbox.parentNode.firstChild
    do {
     nde.setAttributeNS(null,'width',tspan.getComputedTextLength()+5)
     nde=nde.nextSibling
    } while (nde)
    this.add_tspan(" ");
   }
		}
}

/*****
*
*	delete_char
*
*	Delete the last character of the last line
*	If a line is empty as a result, then remove
*	that line from the <text> element
*
*****/
Textbox.prototype.delete_char =delChar
TextboxNB.prototype.delete_char =delChar

function delChar() {
	var textbox = this.textbox;

	if ( textbox.hasChildNodes() ) {
	    var tspan  = textbox.getLastChild();
	    var data   = tspan.getFirstChild();
	    var length = data.getLength();

	    if ( length > 1 ) {
	        data.deleteData(length-1, 1);
	    } else {
	        textbox.removeChild(tspan);
	    }
	}
}

/*****
*
*	add_tspan
*
*	Used to add a new line to the textbox
*	Offset is an optional parameter which designates
*	the vertical offset of the new <tspan> element.
*	This was needed to handle the first <tspan> added
*	to the <text> element
*
*****/
Textbox.prototype.add_tspan = function(new_char, offset) {
	var SVGDoc = this.parent.getOwnerDocument();
	var tspan  = SVGDoc.createElement("tspan");
	var data   = SVGDoc.createTextNode(new_char);

	if ( offset == null ) { offset = "1em" }
	tspan.setAttribute("x", 0);
	tspan.setAttribute("dy", offset);
	tspan.appendChild(data);
	this.textbox.appendChild(tspan);
}
TextboxOutput.prototype.add_tspan = function(new_char, offset) {
	var SVGDoc = this.parent.getOwnerDocument();
	var tspan  = SVGDoc.createElement("tspan");
	var data   = SVGDoc.createTextNode(new_char);

	if ( offset == null ) { offset = "1em" }
	tspan.setAttribute("x", 0);
	tspan.setAttribute("dy", offset);
	tspan.appendChild(data);
	this.textbox.appendChild(tspan);
}

/*****	Class Methods	*****/

/*****
*
*	Find_Textbox
*
*****/
Textbox.Find_Textbox = function(textbox) {
	var result = null;
	var id     = textbox.getAttribute("id") + "";
	var match  = id.match(/(\d+)$/);

	if (match != null) {
		var index = match[1];
		result = Textbox.textboxes[index];
	}

	return result;
};

Textbox.prototype.getText=function() {
	var textbox = this.textbox;
 if ( !textbox.hasChildNodes() ) { this.add_tspan("", 0) }
 var nde=textbox.firstChild
 str=''
 do {
  str+=nde.firstChild.nodeValue
  nde=nde.nextSibling
	} while (nde)
  return str
}
