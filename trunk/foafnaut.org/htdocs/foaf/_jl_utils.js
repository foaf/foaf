function setXYsib(nde,x,y) {
	if (nde) {
  var ellwidth=+nde.nextSibling.getBBox().width
 	if (nde.nodeName=='ellipse') {
   nde.setAttribute('cx',x)
   nde.setAttribute('cy',y)
  } 
 	if (nde.nodeName=='rect') {
   var ellHeight=18
   nde.setAttribute('x',x-ellwidth/2*1.5)
   nde.setAttribute('y',y-ellHeight/2*1.5)
  } 
  nde.nextSibling.setAttribute('x',x-ellwidth/2)
  nde.nextSibling.setAttribute('y',y+2) 
 }
}
function setXY(nde,x,y) {
	if (nde) {
 	if (nde.nodeName=='ellipse') {
   nde.setAttribute('cx',x)
   nde.setAttribute('cy',y)
   return true
  } 
 	if (nde.nodeName=='rect') {
   nde.setAttribute('x',x)
   nde.setAttribute('y',y)
   return true
  } 
  nde.setAttribute('x',x)
  nde.setAttribute('y',y) 
	}
}

function getXY(nde) {
	if (nde.nodeName=='ellipse') {
  var x=nde.getAttribute('cx')
  var y=nde.getAttribute('cy')
  return { x:parseInt(x),y:parseInt(y) }
 } 
 if (nde.nodeName=='rect') {
  var x=+nde.getAttribute('x')
  var y=+nde.getAttribute('y')  
  x=x+nde.getAttribute('width')/2
  y=y+nde.getAttribute('height')/2
  return { x:parseInt(x),y:parseInt(y) }
	}
 var x=nde.getAttribute('x')
 var y=nde.getAttribute('y') 
 return {x:x,y:y }
}

function _jl_createButton(txt,fn,x,y) {
 var ta=SVGDoc.createElement('a')
 var tb=SVGDoc.createElement('text')
 var rect=SVGDoc.createElement('rect')
 tb.appendChild(SVGDoc.createTextNode(txt))
 ta.setAttributeNS(null,"class","button")
 setXY(rect,x-3,y-13)
 rect.setAttributeNS(null,"fill","yellow")
 rect.setAttributeNS(null,"stroke","black")
 rect.setAttributeNS(null,"height",Math.floor(tb.getBBox().height/20+1)*20)
 rect.setAttributeNS(null,"width",tb.getBBox().width*1.5)
 ta.addEventListener("click",fn,false)
 ta.appendChild(rect)
 ta.appendChild(tb)
 setXY(tb,x,y)
 return ta
}
