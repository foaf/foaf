
 anNS="http://rdf.desire.org/vocab/recommend.rdf#"
 rdfNS="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
 ImgNS="http://jibbering.com/2002/3/svg/#"
 DCNS="http://purl.org/dc/elements/1.1/"
 foafNS="http://xmlns.com/foaf/0.1/"
 wordnetNS="http://xmlns.com/wordnet/1.6/"
 svgrNS="http://www.w3.org/2001/svgRdf/axsvg-schema.rdf#"
 svgNS="http://www.w3.org/2000/svg"
 travNS='http://www.w3.org/2000/10/swap/pim/travelTerms#'
 airportNS='http://www.daml.org/2001/10/html/airport-ont#'
	posNS='http://www.w3.org/2003/01/geo/wgs84_pos#'
 dctermsNS='http://purl.org/dc/terms/'
 cycNS='http://opencyc.sourceforge.net/daml/cyc.daml#'
 flightNS='http://jibbering.com/2002/8/flight/#'
 contactNS="http://www.w3.org/2000/10/swap/pim/contact#" 
 xlinkNS="http://www.w3.org/1999/xlink"

 myRDF=new RDF()
 myRDF2=new RDF()
 SVGDoc=null
 Airports=[]
 Persons=[]
	Circles=[]
 output=[]

	function parse(str) {
  var o=new Object()
  o['name']=''
   var arr=str.split('&')
		for (i=0;i<arr.length;i++) {
   try {
    arr1=arr[i].split('=')
    if (arr1 && arr1[1]) o[arr1[0]]=unescape(arr1[1]).replace(/\+/gim,' ')
			} catch (E) {}
		}
  return o
 }

	function Airport(ref,lat,lon,nam,loc) {
  this.ref=ref
  this.lat=+lat
  this.lon=+lon
  this.nam=nam
  this.loc=loc
	}

 function init(evt) {
  SVGDoc=evt.getTarget().getOwnerDocument()
		var str=decodeURIComponent(getSrc()+'?')
  output=parse(str.split('?')[1])
  myRDF2.getRDFURL('nearestAirport.1',fn2)
 }
 function fn() {
  airp=myRDF.Match(null,null,null,airportNS+"Airport")
		for (var i=0;i<airp.length;i++) {
   lat=myRDF.getSingleObject(null,airp[i].subject,posNS+'lat',null)
   lon=myRDF.getSingleObject(null,airp[i].subject,posNS+'long',null)
   nam=myRDF.getSingleObject(null,airp[i].subject,airportNS+'name',null)
   loc=myRDF.getSingleObject(null,airp[i].subject,airportNS+'location',null)
   Airports[airp[i].subject]=new Airport(airp[i].subject,lat,lon,nam,loc)
		}
 for (i=Persons.length-1;i>=0;i--) {
  if (Airports[Persons[i].airport] && !Circles[Persons[i].airport]) {
   Circles[Circles.length]=drawPerson(Airports[Persons[i].airport],Persons[i].mbox,Persons[i].sha1)
   Circles[Persons[i].airport]=Circles[Circles.length-1]
			Circles[Persons[i].airport].setAttributeNS(airportNS,'r',Circles[Persons[i].airport].getAttribute('r'))
		} else {
   Circles[Persons[i].airport].setAttribute('r',Number(Circles[Persons[i].airport].getAttribute('r'))+0.1)
			Circles[Persons[i].airport].setAttributeNS(airportNS,'mbox',Circles[Persons[i].airport].getAttributeNS(airportNS,'mbox')+', '+Persons[i].mbox)
  	Circles[Persons[i].airport].setAttributeNS(airportNS,'r',Circles[Persons[i].airport].getAttribute('r'))
		}
	}
		if (output['name']!='') setInterval('flash()',300)
 }
function fn2() {
 Pers=myRDF2.Match(null,null,null,foafNS+'Person')
 Aps=[]
	for (var i=0;i<Pers.length;i++) {
  mbox=myRDF2.getSingleObject(null,Pers[i].subject,foafNS+'name',null)
  sha1=myRDF2.getSingleObject(null,Pers[i].subject,foafNS+'mbox_sha1sum',null)

  airp=myRDF2.getSingleObject(null,Pers[i].subject,contactNS+'nearestAirport',null)
		airpl=airp.replace('http://www.daml.org/cgi-bin/airport?','')
		if (Aps[airpl]==null) {
   Aps[Aps.length]=airpl
   Aps[airpl]=Aps[Aps.length-1]
		}
  Persons.push({mbox:mbox,airport:airp,sha1:sha1})
 }
 myRDF=new RDF()
 myRDF.getRDFURL('airports.1?'+Aps.join(';'),fn)

}

 function drawPerson(r,mbox,sha1) {
   lat=r.lat;lon=r.lon;nam=r.nam;
   gel=SVGDoc.getElementById('gel')
   linkEl=SVGDoc.createElementNS(svgNS,'a')
   linkEl.setAttributeNS(xlinkNS,'href','/foaf/foafnaut.svg?sha1='+sha1)
   linkEl.setAttribute('target','foafnaut')
   cEl=SVGDoc.createElementNS(svgNS,'circle')
   cEl.setAttribute('cx',lon)
   cEl.setAttribute('cy',lat)
   cEl.setAttribute('r',1.2)
   cEl.setAttribute('stroke','red')
   cEl.setAttribute('stroke-width','0.15')
   cEl.setAttribute('fill','yellow')
   cEl.setAttribute('opacity','1.5')
   cEl.setAttributeNS(airportNS,'name',nam)
   cEl.setAttributeNS(airportNS,'mbox',mbox)
			if (mbox==output['name']) {
    cEl.id='flash'
    cEl.setAttribute('r',1)
			} 
   cEl.addEventListener('mouseover',mm,false)
   cEl.addEventListener('mouseout',mo,false)
			linkEl.appendChild(cEl)
   gel.appendChild(linkEl)
			return cEl
 }

function mm(e) {
 var el=e.getTarget()
 popup=SVGDoc.getElementById('popup') 
 x=e.getClientX()
 y=e.getClientY()
 scale = SVGDoc.documentElement.getCurrentScale();
 trans = SVGDoc.documentElement.getCurrentTranslate();  
 var x = x/scale + (( 0.0 - trans.x ) / scale);
 var y = y/scale + (( 0.0 - trans.y ) / scale);

try {
 popup.firstChild.removeChild(popup.firstChild.firstChild)
 popup.lastChild.removeChild(popup.lastChild.firstChild)
} catch (e) {}
 popup.firstChild.appendChild(SVGDoc.createTextNode(el.getAttributeNS(airportNS,'name')))
 popup.lastChild.appendChild(SVGDoc.createTextNode(el.getAttributeNS(airportNS,'mbox')))
 y=y*1.75
 popup.setAttribute('transform','scale(2,1) translate('+(x+(0.5*1/document.documentElement.currentScale))+','+(y+(0.5*1/document.documentElement.currentScale))+') scale('+1/document.documentElement.currentScale+')');
}

function flash() {
try {
 el=SVGDoc.getElementById('flash')
	if (el.getAttribute('fill')=='yellow') {
  el.setAttribute('fill','red')
	} else {
  el.setAttribute('fill','yellow')
	}
} catch (E) {}
}

function mo(evt) {
try {
 popup=SVGDoc.getElementById('popup') 
 popup.firstChild.removeChild(popup.firstChild.firstChild)
 popup.lastChild.removeChild(popup.lastChild.firstChild)
} catch (e) {}
}

function zoom(evt) {
 for (var i=0;i<Circles.length;i++) {
  Circles[i].setAttribute('r',Number(Circles[i].getAttributeNS(airportNS,'r'))*2/document.documentElement.currentScale)
		Circles[i].setAttribute('stroke-width',0.15*1/document.documentElement.currentScale)
	}
 try {
  var str=popup.getAttribute('transform')
  str=str.substr(0,str.lastIndexOf('scale(')+6)
  str=str+1/document.documentElement.currentScale+')'
  popup.setAttribute('transform',str);
 } catch (e) {} 
}
