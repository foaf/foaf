/*
rdfparser.release.js -

Version 0.1

Copyright 2002 Jim Ley - http://jibbering.com/
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:
1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


*/


function RDF() {
 RDF_NS="http://www.w3.org/1999/02/22-rdf-syntax-ns#"

 this.Version="0.1"

 rdfNS=""
 GlobalID=0
 genids=[]
 inTriples=new Array()
 Namespaces=new Array()
 xmld=null
 xml=null
 this.Match=Match
 this.getSingleObject=SingleObject
 this.getSingleObjectResource=SingleObjectResource
 this.toNTriples=outputNTriples
 this.getRDFURL=getRDF
 this.getRDFNode=GetTriplesNode
 this.getRDFURLNTriples=getRDF_NT
 this.loadRDFXML=_loadRDFXML
	this.getTriples=function() { return inTriples }
 callbackfn=null
 baseURL=''

	function getRDF(url,func) {
  callbackfn=func
		if (url.indexOf('#')==-1) {
   baseURL=url
 	} else {
   baseURL=url.substr(0,url.indexOf('#'))
  }
  getURL(url,ReturnRDF)
	}

	function getRDF_NT(url,func) {
  callbackfn=func
  getURL(url,ReturnRDF_NT)
	}


 function ReturnRDF(obj) {
  if (typeof parseXML=='undefined') {
   try {
    xml=new ActiveXObject ("Microsoft.XMLDOM");
    xml.async=false
    xml.validateOnParse=false
    xml.resolveExternals=false
    xml.loadXML(obj.content)
			} catch (e) {
    try {
     Document.prototype.loadXML = function (s) {
      var doc2 = (new DOMParser()).parseFromString(s, "text/xml");
      while (this.hasChildNodes()) this.removeChild(this.lastChild);
      for (var i = 0; i < doc2.childNodes.length; i++) {
       this.appendChild(this.importNode(doc2.childNodes[i], true));
      }
     }
     xml=document.implementation.createDocument('', '', null);
     xml.loadXML(obj.content)
				} catch (e) {
     if (window.alert) window.alert("OK, I give up, you're not ASV, Batik, IE or\n a Mozilla build or anything else a bit like them.")
				}
			}
  } else {
   xml=parseXML(obj.content,null)
   if (''+xml=='null') {
    xml=parseXML(obj.content,SVGDoc)
			}
		}
  try {
   xmld=xml.documentElement
   var a=xml.documentElement.childNodes
   gettriples=true
 	} catch (E) {
   try {
    xmld=xml.childNodes.item(0)
    gettriples=true
  	} catch (E) {
    if (window.alert) window.alert("No XML Document Found, or not valid XML, or something\n Basically an error so I'm giving up.")
    gettriples=false
			}
 	} 
 if (gettriples) GetTriples()
  callbackfn()
 }
 function ReturnRDF_NT(obj) {
  str=obj.content.split('.\n')
		for (var i=0;i<str.length;i++) {
   tris=str[i].split(' ')
			if (tris.length>2) {
    var subj=tris[0]
    subj=subj.split('<')[1].split('>')[0]
    var pred=tris[1]
    pred=pred.split('<')[1].split('>')[0]
    obj=str[i].substr(str[i].indexOf(pred+'> ')+pred.length+2,str[i].length)
 			if (obj.indexOf('<')==0) {
     obj=obj.split('<')[1].split('>')[0]
     inTriples.push(new Triple(subj,pred,obj,"resource"))
 			} else {
     obj=obj.split('"')[1].split('"')[0]
     inTriples.push(new Triple(subj,pred,obj,"literal"))
 			}
			}
		}
  callbackfn()
 }

 function _loadRDFXML(xmltxt) {
  if (typeof parseXML=='undefined') {
   try {
    xml=new ActiveXObject ("Microsoft.XMLDOM");
    xml.async=false
    xml.validateOnParse=false
    xml.resolveExternals=false
    xml.loadXML(xmltxt)
			} catch (e) {
    try {
   Document.prototype.loadXML = function (s) {
    // parse the string to a new doc   
    var doc2 = (new DOMParser()).parseFromString(s, "text/xml");
    // remove all initial children
    while (this.hasChildNodes()) this.removeChild(this.lastChild);
    // insert and import nodes
    for (var i = 0; i < doc2.childNodes.length; i++) {
     this.appendChild(this.importNode(doc2.childNodes[i], true));
    }
   }

     xml=document.implementation.createDocument('', '', null);
     xml.loadXML(xmltxt)
				} catch (e) {
     if (window.alert) window.alert("OK, I give up, you're not ASV, Batik, IE or\n a Mozilla build or anything else a bit like them.")
				}
			}
  } else {
   xml=parseXML(xmltxt,null)
		}
  try {
   var a=xml.documentElement.childNodes
   gettriples=true
 	} catch (E) {
   if (window.alert) window.alert("No XML Document Found.")
   gettriples=false
 	} 
if (gettriples) GetTriples()
 }

C=0

function GetTriples() {
  getNamespaces(xmld)
  xmlbase=xmld.getAttribute('xml:base')
  if (xmlbase && xmlbase!='')  baseURL=xmlbase.substr(0,xmlbase.lastIndexOf('/')+1)
  createPredicates(xmld.childNodes)
  for (j=0;j<inTriples.length;j++) {
   it=inTriples[j]
   if (!it.object) { it.object=""; it.type="literal" }
   its=it.subject
   if (its.indexOf('#')==0 || (its.length==0 && it.type=="resource")) it.subject=baseURL+its
   if ((it.object.indexOf(':')==-1 || it.object.length==0) && it.type=="resource") {
  it.object=baseURL+it.object
	}
   if (it.type!="literal" && it.object==RDF_NS+"Description") inTriples.splice(j--,1)
		}

 	for (i=0;i<genids.length;i++) {
			if (genids[i].subject) {
    g=genids[i].id
    for (j=0;j<inTriples.length;j++) {
     if (inTriples[j].subject==g) inTriples[j].subject=genids[i].subject
     if (inTriples[j].object==g) inTriples[j].object=genids[i].subject
  	 }
			}
 	}
  return inTriples
}
function GetTriplesNode(node,baseURL) {
  xml=node.getOwnerDocument()
  getNamespaces(xmld)
  createPredicates(node.childNodes)
 	for (i=0;i<genids.length;i++) {
   g=genids[i].id
   for (j=0;j<inTriples.length;j++) {
    if (inTriples[j].subject==g) inTriples[j].subject=genids[i].subject
    if (inTriples[j].subject==g) inTriples[j].subject=genids[i].subject
    if (inTriples[j].object==g) inTriples[j].object=genids[i].subject
    if (inTriples[j].subject.indexOf('#')==0 || inTriples[j].subject.length==0) inTriples[j].subject=baseURL+inTriples[j].subject
    if (inTriples[j].object.indexOf('#')==0 || inTriples[j].object.length==0) inTriples[j].object=baseURL+inTriples[j].object
 	 }
 	}
  return inTriples
}

function outputNTriples() {
 str=''
 for (i=0;i<inTriples.length;i++) {
   if (inTriples[i].subject.indexOf('genid')==0) str+='_:n'+inTriples[i].subject.substr(6)+' ';
    else str+='<'+inTriples[i].subject+'> ';
   str+='<'+inTriples[i].predicate+'> '
   if (inTriples[i].type=='literal') str+='"'+inTriples[i].object+'"'
   else  if (inTriples[i].object && inTriples[i].object.indexOf('genid')==0) str+='_:n'+inTriples[i].object.substr(6)+''
    else str+='<'+inTriples[i].object+'>'
   str+='.\n'
		}
 return str
}

function createPredicates(els) {
 var el,i,j,attr,nn,nv,attrs,ns
 for (i=0;i<els.length;i++) {
  subject=GenID()
  el=els.item(i)
  while (el && el.nodeType!=1) el=els.item(++i)
		if (el) {
   getNamespaces(el)
   attrs=el.attributes
			if (typeof el.getAttributeNS=='unknown') {
    vl=el.getAttributeNS(RDF_NS,'about')
    if (!vl) {
     vl=el.getAttributeNS(RDF_NS,'ID')
     if (vl) vl='#'+vl
    }
			} else {
    vl=el.getAttribute(rdfNS+':about')
    if (!vl) {
     vl=el.getAttribute(rdfNS+':ID')
     if (vl) vl='#'+vl
    }
			}
 		if (vl && vl!='') {
    subject=vl
			}	
 		for (j=0;j<attrs.length;j++) {
    attr=attrs.item(j)
    nn=String(':'+attr.nodeName+'::').split(':')
    ns=nn[1]
    nn=nn[2]
    nv=attr.nodeValue
				if (ns!=rdfNS && ns!='xmlns') {
     inTriples.push(new Triple(subject,Namespaces['_'+ns]+nn,nv,"literal"))
				}
    if (ns==rdfNS && nn=='about') {
     genids.push({id:subject,subject:nv})
     if (!(Namespaces['_'+rdfNS]+"type"=="http://www.w3.org/1999/02/22-rdf-syntax-ns#type" && Namespaces['_'+ns]+el.nodeName=="http://www.w3.org/1999/02/22-rdf-syntax-ns#rdf:Description")) {
 				}
 			}
			}
  }
		if (el) {
   nn=String(':'+el.nodeName+'::').split(':')
   ns=nn[1]
   nn=nn[2]
 		if (ns!=rdfNS) {
if (el.nodeName.indexOf(':')==-1) ses=['','',el.nodeName]
else {
    var ses=String(':'+el.nodeName+'::').split(':')
			}
    inTriples.push(new Triple(subject,Namespaces['_'+rdfNS]+"type",Namespaces['_'+ses[1]]+ses[2],"resource"))
 		}
		}
  if (el && el.childNodes) AnalyseChildren(subject,el.childNodes)
 }
}
function AnalyseChildren(subject,els) {
 var el,i,n,attr,nn,nv,attr,ns,elsl
	if (els) {
  elsl=els.length
  for (var i=0;i<elsl;i++) {
  el=els.item(i)
  while (el && el.nodeType!=1) el=els.item(++i)
		if (el) {
   getNamespaces(el)
   nn=el.nodeName
   attrs=el.attributes
   if (typeof el.getAttributeNS=='unknown') {
    vl=el.getAttributeNS(RDF_NS,'about')
    if (!vl) {
     vl=el.getAttributeNS(RDF_NS,'ID')
     if (vl) vl='#'+vl
    }
			} else {
    vl=el.getAttribute(rdfNS+':about')
    if (!vl) {
     vl=el.getAttribute(rdfNS+':ID')
     if (vl) vl='#'+vl
    }
			}
 		if (vl && vl!='') {
    subject=vl
			}	
 		for (j=0;j<attrs.length;j++) {
    attr=attrs.item(j)
    nna=String(':'+attr.nodeName+'::').split(':')
    nsa=nna[1]
    nna=nna[2]
    nva=attr.nodeValue
				if (nsa!=rdfNS && nsa!='xmlns') {
     if (Namespaces['_'+nsa]) inTriples.push(new Triple(subject,Namespaces['_'+nsa]+nna,nva,"literal"))
				}
    if (nsa==rdfNS && nna=='about') {
     mysubject=nva
     genids.push({id:subject,subject:nva})
     if (!(Namespaces['_'+rdfNS]+"type"=="http://www.w3.org/1999/02/22-rdf-syntax-ns#type" && Namespaces['_'+ns]+el.nodeName=="http://www.w3.org/1999/02/22-rdf-syntax-ns#rdf:Description")) {
     inTriples.push(new Triple(subject,Namespaces['_'+rdfNS]+"about",mysubject,"resource"))
				}
 			}
			}
   if (nn.indexOf(':')==-1) ns=''
    else {
     ns=nn.split(':')[0]
     nn=nn.split(':')[1]
    }
   nvobj=getNodeValue(el)
   nv=nvobj.val;typ=nvobj.type

   if (ns==rdfNS && nn=='Description') {
    var elf=el.firstChild
				if (elf) {
     try {
      nn1=String(':'+elf.nodeName+'::').split(':')
      ns1=nn1[1]
      nn1=nn1[2]
 					for (var ii=0;ii<elf.attributes.length;ii++) {
       attr1=elf.attributes.item(ii)
       nna1=String(':'+attr1.nodeName+'::').split(':')
       nsa1=nna1[1]
       nna1=nna1[2] 
       nva1=attr1.nodeValue
   				if (nsa1!=rdfNS && nsa1!='xmlns') {
        inTriples.push(new Triple(subject,Namespaces['_'+nsa1]+nna1,nva1,"literal"))
   				}
       if (nsa1==rdfNS && nna1=='resource') {
        ii=1000
        inTriples.push(new Triple(subject,Namespaces['_'+ns1]+nn1,nva1,'resource'))
 						}
       if (nsa1==rdfNS && nna1=='literal') {
        ii=1000
        inTriples.push(new Triple(subject,Namespaces['_'+nsa1]+nn1,nv1,'resource'))
 						}
						}
     if (ii<1000) inTriples.push(new Triple(subject,Namespaces['_'+ns1]+nn1,elf.nodeValue,'resource'))
				} catch (e) {}
				}
   } else {
    inTriples.push(new Triple(subject,Namespaces['_'+ns]+nn,nv,typ))
			}
		}
  } 
	}
}		


function getNodeValue(el) {
 getNamespaces(el)
 var i,attr,els,subj
 attrs=el.attributes
 predicate=""
	for (j=0;j<attrs.length;j++) {
  attr=attrs.item(j)
  nn=String(':'+attr.nodeName+'::').split(':')
  ns=nn[1]
  nn=nn[2]
  nv=attr.nodeValue
  if (ns==rdfNS && nn=='parseType' && nv=='Resource') {
   subj=GenID()
   AnalyseChildren(subj,el.childNodes)
   return {val:subj,type:'resource'}
 	}
  if (ns==rdfNS && nn=='about') return {val:nv,type:'resource'}
  if (ns==rdfNS && nn=='resource') return {val:nv,type:'resource'}
  if (ns==rdfNS && nn=='literal') return {val:el.xml,type:'literal'}
 }
 els=el.childNodes
 elsl2=els.length
 if (elsl2==0) return ""
 if (elsl2==1 && els.item(0).nodeType==3) return {val:els.item(0).nodeValue,type:'literal'}
 var iii=0
 while (els.item(iii) && els.item(iii).nodeType==3) iii++
 var elsi=els.item(iii)
 subj=GenID()
 nn=String(':'+elsi.nodeName+'::').split(':')
 ns=nn[1]
 nn=nn[2]

 inTriples.push(new Triple(subj,Namespaces['_'+rdfNS]+"type",Namespaces['_'+ns]+nn,"resource"))

 attrs2=elsi.attributes
	if (attrs2) {
 	for (var ii=0;ii<attrs2.length;ii++) {
   var attr=attrs2.item(ii)
   nna1=String(':'+attr.nodeName+'::').split(':')
   nsa1=nna1[1]
   nna1=nna1[2] 
   nva1=attr.nodeValue
 		if (nsa1!=rdfNS && nsa1!='xmlns') {
    inTriples.push(new Triple(subj,Namespaces['_'+nsa1]+nna1,nva1,"literal"))
 		}
 	}
 	if (typeof elsi.getAttributeNS=='unknown' && elsi.getAttributeNS(RDF_NS,'about')!='') {
   genids.push({id:subj,subject:elsi.getAttributeNS(RDF_NS,'about')})
  } else {
  	if (elsi.getAttribute(rdfNS+':about')!='') {
    genids.push({id:subj,subject:elsi.getAttribute(rdfNS+':about')})
   }
		}
	}
 AnalyseChildren(subj,elsi.childNodes)
 return {val:subj,type:'resource'}
}

function GenID() {
 return "genid:"+(++GlobalID)
}

function getNamespaces(el) {
 if (el) {
  var nn,ns
  var attr=el.attributes
		if (attr) {
   var atl=attr.length
  	for (var i=0;i<atl;i++) {
    nn=':'+attr.item(i).nodeName+"::"
    nn=nn.split(':')[2]
    ns=attr.item(i).nodeValue
    Namespaces[Namespaces.length]=ns
    Namespaces['_'+nn]=Namespaces[Namespaces.length-1]
    if (ns==RDF_NS) rdfNS=nn
			} 
		}
 }
}


function Triple(subject,predicate,object,type) {
 this.subject=subject
 this.predicate=predicate
 this.object=object
 this.type=type
 return this
}

function Match(triples,s,p,o) {
 if (triples==null) triples=inTriples
 outTriples=new Array()
 for (var i=0;i<triples.length;i++) {
  var ti=triples[i]
  match=true
  if (!(s==null || ti.subject==s)) match=false
  if (!(p==null || ti.predicate==p)) match=false
  if (!(o==null || ti.object==o)) match=false
  if (match) outTriples.push(ti)
 }
 return outTriples
}


function SingleObject(triples,s,p,o) {
 if (triples==null) triples=inTriples
 for (var i=0;i<triples.length;i++) {
  var ti=triples[i]
  match=true
  if (!(s==null || ti.subject==s)) match=false
  if (!(p==null || ti.predicate==p)) match=false
  if (match) return(ti.object)
 }
 return ""
}

function SingleObjectResource(triples,s,p,o) {
 if (triples==null) triples=inTriples
 for (var i=0;i<triples.length;i++) {
  var ti=triples[i]
  match=true
  if (!(s==null || ti.subject==s)) match=false
  if (!(p==null || ti.predicate==p)) match=false
  if (!(ti.type=='resource')) match=false
  if (match) return(ti.object)
 }
 return ""
}



}


Array.prototype.toNTriples=function() {
 str=''
 for (i=0;i<this.length;i++) {
   window.status=i
			if (this[i].subject && this[i].subject.indexOf('genid')==0) str+='_:n'+this[i].subject.substr(6)+' ';
     else str+='<'+this[i].subject+'> ';
   str+='<'+this[i].predicate+'> '
   if (this[i].type=='literal') str+='"'+this[i].object+'"'
   else  if (this[i].object && this[i].object.indexOf('genid')==0) str+='_:n'+this[i].object.substr(6)+''
    else str+='<'+this[i].object+'>'
   str+=' .\n'
		}
 return str
}


Array.prototype.toNTriplesExpand=function(rdf) {
 str=''
 for (i=0;i<this.length;i++) {
   if (this[i].subject.indexOf('genid')==0) str+='_:n'+this[i].subject.substr(6)+' ';
    else str+='<'+this[i].subject+'> ';
   str+='<'+this[i].predicate+'> '
   if (this[i].type=='literal') str+='"'+this[i].object+'"'
   else  if (this[i].object && this[i].object.indexOf('genid')==0) {
    str+='_:n'+this[i].object.substr(6)+''
    str+=rdf.Match(null,this[i].object,null,null).toNTriplesExpand(rdf)
			}
    else str+='<'+this[i].object+'>'
   str+='.\n'
		}
 return str
}
function HTTP() {
 var xmlhttp
 /*@cc_on @*/
 /*@if (@_jscript_version >= 5)
   try {
   xmlhttp=new ActiveXObject("Msxml2.XMLHTTP")
  } catch (e) {
   try {
     xmlhttp=new ActiveXObject("Microsoft.XMLHTTP")
   } catch (E) {
    xmlhttp=false
   }
  }
 @else
  xmlhttp=false
 @end @*/
 if (!xmlhttp) {
  try {
   xmlhttp = new XMLHttpRequest();
  } catch (e) {
   xmlhttp=false
  }
 }
 return xmlhttp
}

if (typeof getURL=='undefined') {
 getURL=function(url,fn) { 
  var xmlhttp=new HTTP();
  if (xmlhttp) {
   xmlhttp.open("GET",url,true,'test','test');
   xmlhttp.onreadystatechange=function() {
    if (xmlhttp.readyState==4) {
     fn({status:xmlhttp.status,content:xmlhttp.responseText,
      contentType:xmlhttp.getResponseHeader("Content-Type")})
    }
   }
   xmlhttp.send(null)
  } else {
   //Some Appropriate Fallback...
     if (window.alert) window.alert("OK, I give up, you're not ASV, Batik, IE or\n a Mozilla build or anything else a bit like them.")
  }
 }
}
if (typeof postURL=='undefined') {
 postURL=function(url,txt,fn,type,enc) {
  var xmlhttp=new HTTP();
  if (xmlhttp) {
   xmlhttp.open("POST",url,true,'test','test');
   if (enc) xmlhttp.setRequestHeader("Content-Encoding",enc)
   if (type) xmlhttp.setRequestHeader("Content-Type",type)
   xmlhttp.onreadystatechange=function() {
    if (xmlhttp.readyState==4) {
     fn({status:xmlhttp.status,content:xmlhttp.responseText,
      contentType:xmlhttp.getResponseHeader("Content-Type")})
    }
   }
   xmlhttp.send(txt)
  } else {
   //Some Appropriate Fallback...
     if (window.alert) window.alert("OK, I give up, you're not ASV, Batik, IE or\n a Mozilla build or anything else a bit like them.")
  }
 }
}
