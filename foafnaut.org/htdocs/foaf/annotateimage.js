includesPerson=false
lx=[80,20,60,40,120,30]
ly=[20,80,40,60,30,120]
LZ=new Function("n","n=n+''; return (n.length==1?'0'+n:n)")
 function DrawNodes(str,x,y) {
  subj=CreateSubject(str,x,y)
		for (var i=0;i<Triples.length;i++) {
   if (str==Triples[i].subject) {
   if (Triples[i].type=='literal') obj=CreateObject(Triples[i].object,(+x)+lx[i%lx.length],(+y)+ly[i%ly.length])
    else {
     obj=CreateSubject(Triples[i].object,(+x)+lx[i%lx.length],(+y)+ly[i%ly.length])
				}
    CreatePredicate(subj,obj,Triples[i].predicate)
			}
  }
 }

	function makeNS(str) {
		if (str.indexOf('#')==-1) {
   predns=str.substring(0,str.lastIndexOf('/')+1)
   if (namespaces[predns]) str=namespaces[predns]+':'+str.substr(str.lastIndexOf('/')+1)
		} else {
   predns=str.substring(0,str.lastIndexOf('#')+1)
   if (namespaces[predns]) str=namespaces[predns]+':'+str.substr(str.lastIndexOf('#')+1)
		}
  return str
	}

 anNS="http://rdf.desire.org/vocab/recommend.rdf#"
 rdfNS="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
 ImgNS="http://jibbering.com/2002/3/svg/#"
 DCNS="http://purl.org/dc/elements/1.1/"
 foafNS="http://xmlns.com/foaf/0.1/"
 wordnetNS="http://xmlns.com/wordnet/1.6/"
 svgrNS="http://www.w3.org/2001/svgRdf/axsvg-schema.rdf#"

 namespaces=new Array()
 namespaces['rdf']='http://www.w3.org/1999/02/22-rdf-syntax-ns#'
 namespaces['http://www.w3.org/1999/02/22-rdf-syntax-ns#']='rdf'
 namespaces['foaf']='http://xmlns.com/foaf/0.1/'
 namespaces['http://xmlns.com/foaf/0.1/']='foaf'
 namespaces['wn']='http://xmlns.com/wordnet/1.6/'
 namespaces['http://xmlns.com/wordnet/1.6/']='wn'
 namespaces['dc']='http://purl.org/dc/elements/1.1/'
 namespaces['http://purl.org/dc/elements/1.1/']='dc'
 namespaces['img']='http://jibbering.com/2002/3/svg/#'
 namespaces['http://jibbering.com/2002/3/svg/#']='img'
 namespaces['http://jibbering.com/2002/6/terms#']='terms'


 boxMode=true
 var textbox_url;
 var textbox_desc;
 var textbox_title;
 SVGDoc=null
 startX=10
 startY=40
 ellHeight=0
 ellWidth=0
 Paths=new Array()
 Path=null
 output=null
 mooNode=null
 d=new Date()
 today=d.getFullYear()+'-'+LZ(d.getMonth()+1)+'-'+d.getDate()
 xmlstr='<?xml version="1.0" ?><rdf:RDF  xmlns:an="http://rdf.desire.org/vocab/recommend.rdf#" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:dc="http://purl.org/dc/elements/1.1/"  xmlns:foaf="http://xmlns.com/foaf/0.1/"  xmlns:wn="http://xmlns.com/wordnet/1.6/"  xmlns:svgr="http://www.w3.org/2001/svgRdf/axsvg-schema.rdf#" xmlns:img="http://jibbering.com/2002/3/svg/#" xmlns:terms="http://jibbering.com/2002/6/terms#"><rdf:Description rdf:about=""><an:annotator><foaf:Person/></an:annotator><dc:date>'+today+'</dc:date><an:annotates rdf:resource=""/></rdf:Description><rdf:Description rdf:about=""><dc:title/><dc:description/><img:height/><img:width/></rdf:Description></rdf:RDF>'
 RDFDoc=null
 Triples=new Array()

 function init(evt) {
  SVGDoc = evt.getTarget().getOwnerDocument();
  var str=decodeURIComponent(getSrc()+'?')
  output=parse(str.split('?')[1])
  InitMenu(output['img'],output['h'],output['w'],output['title'],output['desc'])
 }

	function parse(str) {
  var o=new Object()
  o['img']='http://'
  o['th']=''
  o['name']=''
  o['email']='mailto:'
  o['h']='480'
  o['w']='640'
  o['title']=''
  o['desc']=''
  o['nt']=''
  var arr=str.split('&')
		for (i=0;i<arr.length;i++) {
   try {
    arr1=arr[i].split('=')
    if (arr1 && arr1[1]) o[arr1[0]]=unescape(arr1[1]).replace(/\+/gim,' ').Trim()
			} catch (E) {}
		}
  return o
 }

String.prototype.Trim=new Function("return this.replace(/^\\s+|\\s+$/g,'')")
function InitMenu(url,h,w,t,d) {
  var txtel=SVGDoc.createElement('text')
  mooNode=SVGDoc.getElementById('moo')
  txtel.appendChild(SVGDoc.createTextNode('Image URL:'))
  setXY(txtel,startX,startY+2)
  mooNode.appendChild(txtel)
  ellWidth=txtel.getBBox().width
  ellHeight=txtel.getBBox().height
  textbox_url = new Textbox(startX+ellWidth+10, startY-ellHeight, Math.floor(ellWidth*6), ellHeight*1.5, mooNode);
  textbox_url.add_char(url);
  var txtel=SVGDoc.createElement('text')
  txtel.appendChild(SVGDoc.createTextNode('Height:'))
  setXY(txtel,startX,startY+ellHeight*2.5+2)
  mooNode.appendChild(txtel)
  textbox_height = new Textbox(startX+ellWidth+10, startY-ellHeight+ellHeight*2.5, Math.floor(ellWidth), ellHeight*1.5, mooNode);
  textbox_height.add_char(h);
  var txtel=SVGDoc.createElement('text')
  txtel.appendChild(SVGDoc.createTextNode('Width:'))
  setXY(txtel,startX,startY-ellHeight+ellHeight*6+2)
  mooNode.appendChild(txtel)
  textbox_width = new Textbox(startX+ellWidth+10, startY-ellHeight+ellHeight*5, Math.floor(ellWidth), ellHeight*1.5, mooNode);
  textbox_width.add_char(w);
  var txtel=SVGDoc.createElement('text')
  txtel.appendChild(SVGDoc.createTextNode('Image Title:'))
  setXY(txtel,startX,startY-ellHeight+ellHeight*8.5+2)
  mooNode.appendChild(txtel)
  textbox_title = new Textbox(startX+ellWidth+10, startY-ellHeight+ellHeight*7.5, Math.floor(ellWidth*6), ellHeight*1.5, mooNode);
  if (t.length>0) textbox_title.add_char(t);

  var txtel=SVGDoc.createElement('text')
  txtel.appendChild(SVGDoc.createTextNode('Description:'))
  setXY(txtel,startX,startY-ellHeight+ellHeight*11+2)
  mooNode.appendChild(txtel)
  descbox_title = new Textbox(startX+ellWidth+10, startY-ellHeight+ellHeight*10, Math.floor(ellWidth*6), ellHeight*1.5, mooNode);
  if (d.length>0) descbox_title.add_char(d);

  mooNode.appendChild(_jl_createButton('Get Image',getImg,startX,startY-ellHeight+ellHeight*13.5))
 }


	function GetRDF() {
  about=new Date()
  RDFDoc=parseXML(xmlstr,null)
  DescElement=RDFDoc.getElementsByTagNameNS(rdfNS,"Description").item(1)
  DescElement.setAttributeNS(rdfNS,"rdf:about",textbox_url.getText())
  AnElement=RDFDoc.getElementsByTagNameNS(anNS,"annotates").item(0)
  AnElement.setAttributeNS(rdfNS,"rdf:resource",textbox_url.getText())
  HeightElement=RDFDoc.getElementsByTagNameNS(ImgNS,"height").item(0)
  HeightElement.appendChild(RDFDoc.createTextNode(textbox_height.getText()))
  WidthElement=RDFDoc.getElementsByTagNameNS(ImgNS,"width").item(0)
  WidthElement.appendChild(RDFDoc.createTextNode(textbox_width.getText()))
  AreaElement=RDFDoc.createElement('img:area')
  TotalArea=Math.round(+textbox_height.getText()*(+textbox_width.getText()))
  AreaElement.appendChild(RDFDoc.createTextNode(TotalArea))
  DescElement.appendChild(AreaElement)
  TitleElement=RDFDoc.getElementsByTagNameNS(DCNS,"title").item(0)
  TitleElement.appendChild(RDFDoc.createTextNode(textbox_title.getText()))
  aDescElement=RDFDoc.getElementsByTagNameNS(DCNS,"description").item(0)
  aDescElement.appendChild(RDFDoc.createTextNode(descbox_title.getText()))
		if (output['th']!='') {
   ThumbElement=RDFDoc.createElement('foaf:thumbnail')
   ThumbElement.setAttributeNS(rdfNS,"rdf:resource",output['th'])
   DescElement.appendChild(ThumbElement)
		}
  if (output['name']!='') {
   PersonElement=RDFDoc.getElementsByTagNameNS(foafNS,"Person").item(0)
   foafName=RDFDoc.createElement('foaf:name')
   foafName.appendChild(RDFDoc.createTextNode(output['name']))
   PersonElement.appendChild(foafName)
   if (output['email'].indexOf('mailto:')!=0) output['email']='mailto:'+output['email']
   if (output['email']!='mailto:') {
   foafEmail=RDFDoc.createElement('foaf:mbox')
   foafEmail.setAttributeNS(rdfNS,"rdf:resource",output['email'])
   PersonElement.appendChild(foafEmail)
   foafsha1=RDFDoc.createElement('foaf:mbox_sha1sum')
   foafsha1.appendChild(RDFDoc.createTextNode(calcSHA1(output['email'])))
   PersonElement.appendChild(foafsha1)
			}
		} else {
   // No Annotator - do Nothing
		}
  for (i=0;i<Paths.length;i++) {
   hasPart=RDFDoc.createElement('img:hasPart')
   Polygon=RDFDoc.createElement('img:Polygon')
   Polygon.setAttributeNS(rdfNS,"rdf:about","http://jibbering.com/rdfsvg/#"+about.valueOf()+'_'+i)
   polypath=RDFDoc.createElement('img:polypath')
   polypath.appendChild(RDFDoc.createTextNode(Paths[i].Path.getAttribute('d')))
   DescElement.appendChild(hasPart)
   hasPart.appendChild(Polygon)
   Polygon.appendChild(polypath)

   var area=RDFDoc.createElement('img:area')
   areaVal=getArea(Paths[i].Path.getAttribute('d'))
   area.appendChild(RDFDoc.createTextNode(areaVal))
   Polygon.appendChild(area)

   var ddd=RDFDoc.createElement('dc:description')
   ddd.appendChild(RDFDoc.createTextNode(Paths[i].descBox.getText()))
   Polygon.appendChild(ddd)


   var inpos=RDFDoc.createElement('svgr:InPosition')
   ip=getCentroid(Paths[i].Path.getAttribute('d'))
   middle={x:+textbox_width.getText()/2,y:+textbox_height.getText()/2}
   inpos.appendChild(RDFDoc.createTextNode(GetPosition(middle,ip)))
   Polygon.appendChild(inpos)


   var frac=RDFDoc.createElement('img:fraction')
   frac.appendChild(RDFDoc.createTextNode((areaVal/TotalArea).toFixed(3)))
   Polygon.appendChild(frac)
   Depicts=RDFDoc.createElement('foaf:regionDepicts')
   res=Paths[i].resourceBox.getText()
   if (res.indexOf(wordnetNS+'Person')==0) res=foafNS+'Person'
   word=RDFDoc.createElement(makeNS(res))
   if (res.indexOf(foafNS+'Person')==0) {
    includesPerson=true
    nm=RDFDoc.createElement('foaf:name')
    nm.appendChild(RDFDoc.createTextNode(Paths[i].nBox.getText()))
    word.appendChild(nm)
    emv=Paths[i].eBox.getText()
    if (emv.indexOf('mailto:')!=0) emv='mailto:'+emv
				if (emv!='' && emv!='mailto:') {
     em=RDFDoc.createElement('foaf:mbox')
     em.setAttributeNS(rdfNS,"rdf:resource",emv)
     word.appendChild(em)
     emsha=RDFDoc.createElement('foaf:mbox_sha1sum')
     emsha.appendChild(RDFDoc.createTextNode(calcSHA1(emv)))
     word.appendChild(emsha)
				}
    id=RDFDoc.createElement('img:depiction')
    id.setAttributeNS(rdfNS,"rdf:resource","http://jibbering.com/rdfsvg/#"+about.valueOf()+'_'+i)
    word.appendChild(id)
    foaf=RDFDoc.createElement('foaf:depiction')
    foaf.setAttributeNS(rdfNS,"rdf:resource",textbox_url.getText())
    word.appendChild(foaf)
   }
   Depicts.appendChild(word)
   Polygon.appendChild(Depicts)
  } 
  str=printNode(RDFDoc)
  tb = new TextboxOutput(0, 0, "100%", "100%", SVGDoc.firstChild);
  strs=str.split('>')
  for (i=0;i<strs.length-1;i++) tb.add_tspan(' '+strs[i]+'>')
  SVGDoc.firstChild.appendChild(_jl_createButton('Store the RDF, and tell Annotea and codepiction about it.',SubmitRDF,400,110))
	}



function SubmitRDF() {
 str=printNode(RDFDoc)
 postURL('submit-annotation.1',str,ReturnRDF)
 tb = new TextboxOutput(0, 0, "100%", "100%", SVGDoc.firstChild);
 tb.add_tspan('Please Wait....')
}
function ReturnRDF(obj) {
 tb = new TextboxOutput(0, 0, "100%", "100%", SVGDoc.firstChild);
 strs=obj.content.split('>')
 for (i=0;i<strs.length-1;i++) tb.add_tspan(' '+strs[i]+'>')
}

function ReturnRDFBlank(obj) {}



 function getImg() {
  img=SVGDoc.createElement('image')
  setXY(img,0,0)
  img.setAttribute('height',textbox_height.getText())
  img.setAttribute('width',textbox_width.getText())
  img.addEventListener("click",drawLine,false)
  img.setAttributeNS('http://www.w3.org/1999/xlink','href',textbox_url.getText())
  SVGDoc.firstChild.appendChild(img)
  txtbut2=SVGDoc.createElement('text')

  mooNode.appendChild(_jl_createButton('View RDF',GetRDF,10,Number(textbox_height.getText())+20)) 
  mooNode.appendChild(_jl_createButton('Region',function() { boxMode=true },150,Number(textbox_height.getText())+20))
  mooNode.appendChild(_jl_createButton('Outline',function() { boxMode=false },210,Number(textbox_height.getText())+20))
  SVGDoc.setTitle(textbox_title.getText())
 }




 function drawLine(e) {
	if (!e.shiftKey && !e.altKey && !e.ctrlKey) {
   x=e.getClientX()
   y=e.getClientY()
   scale = SVGDoc.getDocumentElement().getCurrentScale();
   trans = SVGDoc.getDocumentElement().getCurrentTranslate();  
   var x = x/scale + (( 0.0 - trans.x ) / scale);
   var y = y/scale + (( 0.0 - trans.y ) / scale);
   x=Math.floor(x*100)/100
   y=Math.floor(y*100)/100   
  if (Path==null) {
   var lineel=SVGDoc.createElement('g')
   lineel.setAttribute("style","stroke-width:1px;stroke:#FFFF00;fill:#ff0000;opacity:1;")
   lineel.setAttribute("fill-opacity","0.1")
   lineel.setAttribute("stroke-opacity","1")
   pathel=SVGDoc.createElement('path')
   str="M"+x+" "+y+" L"+x+" "+y
   pathel.setAttribute("d",str)
   Path=pathel
   Path.addEventListener("click",drawLine,false)
   lineel.appendChild(pathel)
   SVGDoc.firstChild.appendChild(lineel)
  } else {
   str=Path.getAttribute("d")
   if (boxMode) {
    str=str.replace(/z$/,'')
    str+="L"+x+" "+y+" z"
   } else {
    str+="L"+x+" "+y+" M"+x+" "+y+" "
			}
   Path.setAttribute("d",str)
		}
 } 
 if (e.shiftKey && Path) {
  str=Path.getAttribute("d")
  str=str.replace(/z$/,'')
  str+=" L"+str.substring(1,str.indexOf('L'))+" z"
  Path.setAttribute("d",str)
  Path.setAttribute("opacity","0.6")
  Path.setAttribute("fill-opacity","0.6")

		if (Paths.length==0) {
   var txtel=SVGDoc.createElement('text')
   txtel.appendChild(SVGDoc.createTextNode('Area'))
   var Y=Math.floor(ellHeight*(Paths.length*1.5))+Number(textbox_height.getText())+30
   setXY(txtel,startX,Y+2+ellHeight)
   mooNode.appendChild(txtel) 
   var txtel=SVGDoc.createElement('text')
   txtel.appendChild(SVGDoc.createTextNode('Resource'))
   setXY(txtel,ellWidth-10,Y+2+ellHeight)
   mooNode.appendChild(txtel)
   var txtel=SVGDoc.createElement('text')
   txtel.appendChild(SVGDoc.createTextNode('name (if Person)'))
   setXY(txtel,Math.floor(ellWidth*3.5)+ellWidth+10,Y+2+ellHeight)
   mooNode.appendChild(txtel)
   var txtel=SVGDoc.createElement('text')
   txtel.appendChild(SVGDoc.createTextNode('email (if Person)'))
   setXY(txtel,Math.floor(ellWidth*7)+ellWidth+10,Y+2+ellHeight)
   mooNode.appendChild(txtel)
		}
  var txtel=SVGDoc.createElement('text')
  txtel.appendChild(SVGDoc.createTextNode(''+(Paths.length+1)))
  var Y=Math.floor(ellHeight*(1+Paths.length*3.4))+Number(textbox_height.getText())+35
  setXY(txtel,startX,Y+2+ellHeight)
  mooNode.appendChild(txtel)

  var txtel=SVGDoc.createElement('text')
  txtel.appendChild(SVGDoc.createTextNode('Desc.'))
  setXY(txtel,startX,Y+2+ellHeight+ellHeight*1.7)

  mooNode.appendChild(txtel)
  var tw = new Textbox(ellWidth-10, Y, Math.floor(ellWidth*3), ellHeight*1.5, mooNode);
  r=''
  try {
   r=output['w'+(Paths.length+1)]
   tw.add_char(r);
		} catch (e) {}

		if (r==foafNS+'Person' || r==wordnetNS+'Person') {
   var tn = new Textbox(Math.floor(ellWidth*3.5)+ellWidth+10, Y, Math.floor(ellWidth*3), ellHeight*1.5, mooNode);
  try {
   r=output['n'+(Paths.length+1)]
   tn.add_char(r);
		} catch (e) {}
   var te = new Textbox(Math.floor(ellWidth*7)+ellWidth+10, Y, Math.floor(ellWidth*3), ellHeight*1.5, mooNode);
  try {
   r=output['e'+(Paths.length+1)]
   te.add_char(r);
		} catch (e) {}
		}
  var td = new Textbox(ellWidth-10, Y+ellHeight*1.7, Math.floor(ellWidth*9), ellHeight*1.5, mooNode);
  r=''
  try {
   r=output['d'+(Paths.length+1)]
   tw.add_char(r);
		} catch (e) {}
  Paths.push({Path:Path, resourceBox:tw, nBox:tn, eBox:te, descBox:td })
  Path=null
 }
}



function getArea(mypath) {
 mypath=mypath.replace(/M/gim,'L')
 mypath=mypath.substring(0,mypath.indexOf('z'))
 Points=mypath.split("L")
 oPoints=[]
	for (var i=1;i<Points.length;i++) {
  Points[i].match(/([\d]*?) ([\d]*).*/gim)
  oPoints[i-1]={x:+RegExp.$1, y:+RegExp.$2 }
	}
 return Math.abs(Math.round(Area(oPoints)))
}
function getCentroid(mypath) {
 mypath=mypath.replace(/M([\d]*?) ([\d]*)/gim,'')
 mypath=mypath.substring(0,mypath.indexOf('z'))
 Points=mypath.split("L")
 oPoints=[]
	for (var i=1;i<Points.length;i++) {
  Points[i].match(/([\d]*?) ([\d]*).*/gim)
  oPoints[i-1]={x:+RegExp.$1, y:+RegExp.$2 }
	}
 return Centroid(oPoints)
}

function Area(arr) {
 var sum=0
	for (var i=0;i<arr.length-1;i++) {
  sum+=arr[i].x*arr[i+1].y
  sum-=arr[i].y*arr[i+1].x
 }
 sum/=2
 return sum
}

function Centroid(arr) {
 var A=Area(arr)
 var sumcx=0
 var sumcy=0
 for (var i=0;i<arr.length-1;i++) {
  sumcx+=(arr[i].x+arr[i+1].x)*(arr[i].x*arr[i+1].y-arr[i+1].x*arr[i].y)
  sumcy+=(arr[i].y+arr[i+1].y)*(arr[i].x*arr[i+1].y-arr[i+1].x*arr[i].y)
 }
 var cx=(sumcx)/(6*A)
 var cy=(sumcy)/(6*A)
 circ=SVGDoc.createElement('circle')
 circ.setAttributeNS(null,'cx',cx)
 circ.setAttributeNS(null,'cy',cy)
 circ.setAttributeNS(null,'r',6)
 circ.setAttributeNS(null,'stroke-width','2')
 circ.setAttributeNS(null,'fill','red')
 SVGDoc.documentElement.appendChild(circ)
 return {x:cx,y:cy}
}

Positions=['N','NNE','NE','ENE','E','ESE','SE','SSE','S','SSW','SW','WSW','W','WNW','NW','NNW']
function GetPosition(mid,ip) {
 var pos
 circ=SVGDoc.createElement('path')
 circ.setAttribute("d","M "+mid.x+" "+mid.y+"L"+(ip.x)+" "+(ip.y))
 circ.setAttribute('stroke-width','2')
 circ.setAttribute('stroke','yellow')
 circ.setAttribute('fill','red')
 SVGDoc.documentElement.appendChild(circ)
 var xl=ip.x-mid.x
 var yl=ip.y-mid.y
 var c=Math.sqrt(xl*xl+yl*yl)
 var a=xl
 ang=Math.asin(a/c)
 if (xl>0 && yl>0) ang=Math.PI-ang
 if (xl<0 && yl>0) ang=-ang+Math.PI
 if (xl<0 && yl<0) ang=ang+Math.PI*2

 if (c/mid.x<0.2) {
   pos="M"
 } else {
   n=Math.floor(((ang+Math.PI/8)/(2*Math.PI))*16)%16
   pos=Positions[n]
		}
 txt=SVGDoc.createElement('text')
 txt.setAttribute("x",ip.x)
 txt.setAttribute("y",ip.y)
 txt.setAttribute('stroke-width','0')
 txt.setAttribute('stroke','yellow')
 txt.setAttribute('fill','yellow')
 txt.appendChild(SVGDoc.createTextNode(" "+((ang/(2*Math.PI))*360).toFixed(1)+"deg"+' '+pos))
 SVGDoc.documentElement.appendChild(txt)
 return pos
}
