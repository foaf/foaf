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

 _rdfNS=""
 GlobalID=0
 genids=[]
 inTriples=new Array()
 Namespaces=new Array()
 xmld=null
 xml=null
 this.Match=Match
 this.predSmush=predSmush
 this.getSingleObject=SingleObject
 this.getSingleObjectResource=SingleObjectResource
 this.toNTriples=outputNTriples
 this.getRDFURL=getRDF
 this.getRDFURLNTriples=getRDF_NT
	this.getTriples=function() { return inTriples }
 callbackfn=null
 baseURL=''

	function getRDF(url,func) {
  /*
   This is called to get the RDF from a url
    url: The url containing the RDF/XML
    func: A pointer to a function to call 
          when it is downloaded.
    
    sets the baseURL, and calls the getURL
    function which actually makes the HTTP req.
  */
  callbackfn=func
		if (url.indexOf('#')==-1) {
   baseURL=url
 	} else {
   baseURL=url.substr(0,url.indexOf('#'))
  }
  getURL(url,ReturnRDF)
	}

	function getRDF_NT(url,func) {
  // NTriples parser:
  callbackfn=func
  getURL(url,ReturnRDF_NT)
	}


 function ReturnRDF(obj) {
  /*
   This function is called when the getURL function returns with the
   XML stream, it attempts to parse into an XML tree by whichever
   parser is available..
  */
  if (typeof parseXML=='undefined') {
   try {
    // IE 5+
    xml=new ActiveXObject ("Microsoft.XMLDOM");
    xml.async=false
    xml.validateOnParse=false
    xml.resolveExternals=false
    xml.loadXML(obj.content)
			} catch (e) {
    try {
     // Mozilla 
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
   // Adobe SVG
   xml=parseXML(obj.content,null)
   if (''+xml=='null') {
    // Batik
    xml=parseXML(obj.content,SVGDoc)
			}
		}
  // Same thing - Batik returns a documentFragment, the others a documentElement.
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
  // Get the Triples
  if (gettriples) GetTriples()
  // Call the callback function
  callbackfn()
 }

 function ReturnRDF_NT(obj) {
  // Call back for getURL for the NTriples parser.
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


function GetTriples() {
  /*
   Get the namespaces declared on the root node,
   and stick them in the global Namespaces hash.
   (in javascript Arrays are hashes.)
  */
  getNamespaces(xmld)

  /* 
   Sort out the baseURL, from the baseURL global var declared
   earlier if xmlbase is not defined.
  */
  xmlbase=xmld.getAttribute('xml:base')
  if (xmlbase && xmlbase!='')  baseURL=xmlbase.substr(0,xmlbase.lastIndexOf('/')+1)

  /*
   Make the predicates
  */
  createPredicates(xmld.childNodes)

  /*
   Post Process the triples, ensuring that baseURL is added, and
   removing some spurious triples that appear in the parsing -
   probably should not let these ever appear, but they do, and this
   seems safe
  */
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

  /*
   Processed the genids, if we learnt that a genid has a real uri,
   after we have inserted it into the triple store, the previous
   genids would be broken, this repairs that.
  */
 	for (i=0;i<genids.length;i++) {
			if (genids[i].subject) {
    g=genids[i].id
    for (j=0;j<inTriples.length;j++) {
     if (inTriples[j].subject==g) inTriples[j].subject=genids[i].subject
     if (inTriples[j].object==g) inTriples[j].object=genids[i].subject
  	 }
			}
 	}

  /*
   inTriples is the global array of all Triples, it is returned here.
  */
  return inTriples
}

function Triple(subject,predicate,object,type) {
 /*
  This is the triple object, should be self explanatory.
 */
 this.subject=subject
 this.predicate=predicate
 this.object=object
 this.type=type
 return this
}


function createPredicates(els) {
 /*
  els is a NodeList
 */
 var el,i,j,attr,nn,nv,attrs,ns

 for (i=0;i<els.length;i++) { // Iterate over all elements
  subject=GenID(); // Generate an ID for the subject.
  /*
   get the next node in the list
  */

  el=els.item(i) 
  while (el && el.nodeType!=1) el=els.item(++i)
		if (el) {
   /*
    Get any namespaces declared on the element,
    This is a bug in the script, as this will override
    known namespaces not just in elements under this
    node, but even after this node ends, fix Appreciated!
   */
   getNamespaces(el)
   attrs=el.attributes

   /*
    Annoying method to cope between various browsers, it 
    tries to get the rdf:about / rdf:ID value.
   */
			if (typeof el.getAttributeNS=='unknown') {
    vl=el.getAttributeNS(RDF_NS,'about')
    if (!vl) {
     vl=el.getAttributeNS(RDF_NS,'ID')
     if (vl) vl='#'+vl
    }
			} else {
    vl=el.getAttribute(_rdfNS+':about')
    if (!vl) {
     vl=el.getAttribute(_rdfNS+':ID')
     if (vl) vl='#'+vl
    }
			}
 		if (vl && vl!='') { // If we get a value, make it the subject.
    subject=vl
			}	

   /*
    Scan the attributes.
   */
 		for (j=0;j<attrs.length;j++) {

    /*
     Get the namespace, nodename, and node value of the attribute.
    */
    attr=attrs.item(j)
    nn=String(':'+attr.nodeName+'::').split(':')
    ns=nn[1]
    nn=nn[2]
    nv=attr.nodeValue

    /*
     if it is not the RDF, or xmlns, or xml namespaces
     then it is a literal triple, add it to the triples.
    */
				if (ns!=_rdfNS && ns!='xmlns' && ns!='xml') {
     inTriples.push(new Triple(subject,Namespaces['_'+ns]+nn,nv,"literal"))
				}

    /*
     if it is rdf:about, add it to the genids,subject hash for
     later, why we did not do this before I have no idea.
     I will test this improvement later.
    */
    if (ns==_rdfNS && nn=='about') {
     genids.push({id:subject,subject:nv})
 			}
			}
  }

		if (el) {
   /*
    Similar to the attributes, but now we look at the node name itself,
    and create an appropriate triple.
   */
   nn=String(':'+el.nodeName+'::').split(':')
   ns=nn[1]
   nn=nn[2]
 		if (ns!=_rdfNS) {
    if (el.nodeName.indexOf(':')==-1) ses=['','',el.nodeName]
     else {
      var ses=String(':'+el.nodeName+'::').split(':')
			  }
    inTriples.push(new Triple(subject,Namespaces['_'+_rdfNS]+"type",Namespaces['_'+ses[1]]+ses[2],"resource"))
 		}
		}
  /*
   If there are children, do the stuff again, createPredicates
   is sort of AnalyseChildren so could probbably be made one 
   function anyway.
  */
  if (el && el.childNodes) AnalyseChildren(subject,el.childNodes)
 }
}


function AnalyseChildren(subject,els) {
 var el,i,n,attr,nn,nv,attr,ns,elsl
	if (els) {
  /*
   This is all the same as createPredicates
  */
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
    vl=el.getAttribute(_rdfNS+':about')
    if (!vl) {
     vl=el.getAttribute(_rdfNS+':ID')
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
				if (nsa!=_rdfNS && nsa!='xmlns') {
     if (Namespaces['_'+nsa]) inTriples.push(new Triple(subject,Namespaces['_'+nsa]+nna,nva,"literal"))
				}
    if (nsa==_rdfNS && nna=='about') {
     mysubject=nva
     genids.push({id:subject,subject:nva})

     /*
      I am really none too sure how this works, it creates 
      the type triples, but why it is not in createPredicates
      I am not sure, maybe just a bug, should probably be there,
      testcase to test this would be nice.
     */
     if (!(Namespaces['_'+_rdfNS]+"type"=="http://www.w3.org/1999/02/22-rdf-syntax-ns#type" && Namespaces['_'+ns]+el.nodeName=="http://www.w3.org/1999/02/22-rdf-syntax-ns#rdf:Description")) {
     inTriples.push(new Triple(subject,Namespaces['_'+_rdfNS]+"about",mysubject,"resource"))
				}
 			}
			}

   /*
    gets Node name and node value
   */
   if (nn.indexOf(':')==-1) ns=''
    else {
     ns=nn.split(':')[0]
     nn=nn.split(':')[1]
    }
   /*
    getNodeValue is the function which calculates the "object"
    of the triple, either a URI, or a blank node with further
    triples, getNodeValue calls back AnalyseChildren.
   */
   nvobj=getNodeValue(el)
   nv=nvobj.val;typ=nvobj.type

   /*
    If it is rdf:Description, do some stuff on the first child,
    otherwise just stuff in the triple, I think this is because
    rdf:Description does not work properly with my getNodeValue.
   */
   if (ns==_rdfNS && nn=='Description') {
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
   				if (nsa1!=_rdfNS && nsa1!='xmlns') {
        inTriples.push(new Triple(subject,Namespaces['_'+nsa1]+nna1,nva1,"literal"))
   				}
       if (nsa1==_rdfNS && nna1=='resource') {
        ii=1000
        inTriples.push(new Triple(subject,Namespaces['_'+ns1]+nn1,nva1,'resource'))
 						}
       if (nsa1==_rdfNS && nna1=='literal') {
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
 /*
  getNamespaces, same problem as before.
 */
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

  /*
   if it is a rdf:parseType resource, analyse the children: 
  */
  if (ns==_rdfNS && nn=='parseType' && nv=='Resource') {
   subj=GenID()
   AnalyseChildren(subj,el.childNodes)
   return {val:subj,type:'resource'}
 	}
  /*
   Return value/type depending on nodename of attribute
  */
  if (ns==_rdfNS && nn=='about') return {val:nv,type:'resource'} 
  if (ns==_rdfNS && nn=='resource') return {val:nv,type:'resource'}
  /*
 		parsetype:literal is only supported here for MSXML - IE, for ASV you
   could do it with printNode(el), for others, I am not so sure.
  */
  if (ns==_rdfNS && nn=='literal') return {val:el.xml,type:'literal'}
 }
 els=el.childNodes
 elsl2=els.length
 /*
  If this node has no children, end the function, with "", this is
  either a bug, or would signal an RDF problem - I am not sure.
 */
 if (elsl2==0) return ""
 /*
  If the child is a string, return it as a string.
 */
 if (elsl2==1 && els.item(0).nodeType==3) return {val:els.item(0).nodeValue,type:'literal'}
 var iii=0
 while (els.item(iii) && els.item(iii).nodeType==3) iii++
 var elsi=els.item(iii)
 subj=GenID()
 nn=String(':'+elsi.nodeName+'::').split(':')
 ns=nn[1]
 nn=nn[2]

 /*
  If we are here, the triple we have has a rdf:type of ...
 */
 inTriples.push(new Triple(subj,Namespaces['_'+_rdfNS]+"type",Namespaces['_'+ns]+nn,"resource"))

 attrs2=elsi.attributes
	if (attrs2) {
  /*
   This is the same as in AnalyseChildren for the attributes
  */
 	for (var ii=0;ii<attrs2.length;ii++) {
   var attr=attrs2.item(ii)
   nna1=String(':'+attr.nodeName+'::').split(':')
   nsa1=nna1[1]
   nna1=nna1[2] 
   nva1=attr.nodeValue
 		if (nsa1!=_rdfNS && nsa1!='xmlns') {
    inTriples.push(new Triple(subj,Namespaces['_'+nsa1]+nna1,nva1,"literal"))
 		}
 	}
 	if (typeof elsi.getAttributeNS=='unknown' && elsi.getAttributeNS(RDF_NS,'about')!='') {
   genids.push({id:subj,subject:elsi.getAttributeNS(RDF_NS,'about')})
  } else {
  	if (elsi.getAttribute(_rdfNS+':about')!='') {
    genids.push({id:subj,subject:elsi.getAttribute(_rdfNS+':about')})
   }
		}
	}
 /*
  Recurse Away!
 */
 AnalyseChildren(subj,elsi.childNodes)
 return {val:subj,type:'resource'}
}

function GenID() {
 /*
  Generate a unique ID - add one to the Global counter...
 */
 return "genid:"+(++GlobalID)
}

function getNamespaces(el) {
 /*
  This looks through all nodes attributes and if it
  is xmlns:something, it adds it to the array, as a 
  numeric index, and as a name/url pair.
 */
 if (el) {
  var nn,ns
  var attr=el.attributes
		if (attr) {
   var atl=attr.length
  	for (var i=0;i<atl;i++) {
    nn=attr.item(i).nodeName
				if (nn.split(':')[0]=='xmlns') {
     nn=':'+nn+"::"
     nn=nn.split(':')[2]
     ns=attr.item(i).nodeValue
 				if (Namespaces['_'+nn]==null) {
      Namespaces[Namespaces.length]=ns
      Namespaces['_'+nn]=Namespaces[Namespaces.length-1]
      if (ns==RDF_NS) _rdfNS=nn
					}
				}
			} 
		}
 }
}



function outputNTriples() {
 /*
  Simplistic NTriples writer.
 */
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

function predSmush(p1,p2) {
 /*
  A predicate smusher, IE if we know foaf:mbox is equivalent
  to vcard:mbox, we can replace all the vcard:mbox which will
  allow simpler searching.  Well I wanted it :-)
 */
 triples=inTriples
 for (var i=0;i<triples.length;i++) {
  if (triples[i].predicate==p1) triples[i].predicate=p2
 }
}

function Match(triples,s,p,o) {
 /*
  The heart of the sophisticated query engine.
  well a query tool, should be obvious.
 */
 if (triples==null) triples=inTriples
 outTriples=new Array()
 for (var i=0;i<triples.length;i++) {
  var ti=triples[i]
  match=true
  if (!(s==null || ti.subject==s)) match=false
  if (!(p==null || ti.predicate==p)) match=false
  if (!(o==null || ti.object==o)) match=false
//alert(o+'\n'+ti.object+'\n\n'+p+'\n'+ti.predicate)
  if (match) outTriples.push(ti)
 }
 return outTriples
}


function SingleObject(triples,s,p,o) {
 /*
  Same as Match, but stops after one is found, and if
  none are found an empty string is returned.
 */
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
 /*
  Same as singleObject, but has to be a resource.
 */
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
 /*
  adds the ability to go Triples (which are just arrays) .toNTriples() 
  window.status is for feedback in the window as it progresses.
 */
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


/*
 Everything below this is script to provide getURL
 to IE and Mozilla, and is sort of documented in
 http://jibbering.com/2002/5/dynamic-update-svg.html
*/

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
