

 GetCOUNT=0
 _ONLINE=true

// The urls, for online, and offline viewing:
// startURL+Sha1+endURL  gives the url to retrieve
// for info on that sha1

	if (_ONLINE) {
  startURL="person-summary.id.1?"
  endURL=""
	} else {
  startURL="cache/"
  endURL=".cz"
	}

// Lots of global variables, Namespaces etc.  Many are not
// used, but this I keep the same everywhere.

 var BLUB_SIZE_SMALL=0.6
 var BLUB_EXP_DISTANCE_DEL=62
 var BLUB_EXP_DISTANCE=2.2
 var BLUB_EXPLOSION_DELTA=6
 var BLUB_MOVE_GROUP=true
 var Templates=[]
 var gdoc=document
 var gdocDE=gdoc.documentElement
 var svgNS="http://www.w3.org/2000/svg";
 var xlinkNS="http://www.w3.org/1999/xlink"
 var anNS="http://rdf.desire.org/vocab/recommend.rdf#"
 var rdfNS="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
 var imgNS="http://jibbering.com/2002/3/svg/#"
 var DCNS="http://purl.org/dc/elements/1.1/"
 var foafNS="http://xmlns.com/foaf/0.1/"
 var wordnetNS="http://xmlns.com/wordnet/1.6/"
 var svgrNS="http://www.w3.org/2001/svgRdf/axsvg-schema.rdf#"
 var jimNS="http://jibbering.com/foaf/jim.rdf#"

 var globalBlubBatikId;
 var svgelement=gdoc.rootElement;
 var currentBlub=null
 var gCirc=null
 var gPanelCount=3
 var photoCount=0
	var searchCount=1

 // The colours used for paths get shown.
 var highlightColours=['red','blue','black','#ffff00','green','#00ffff','#ff00ff']
 var pathCount=0

 // Creates references to the svg elements used to add the
 // blub/sproings to later

 var blubsgroup
 var sproingsgroup
	var form

 // create some SVG elements for later cloning...
 // element creation is dead slow, avoid, either clone
 // or use parseXML. createElement should be avoided!

 var globalgEL=gdoc.createElementNS(svgNS, "g");


var FADE=true;
var nextId=0;
var blubs=new Array();
var blubhash=new Array();
var sproings=new Array();
var currentBlubId=-1;
var dataReady=0;
var detailPanel;
var dirList=new Array();

	function parse(str) {

  // A simple query string parser, initialise the object
  // with default values like email/sha1 here or there 
  // will be errors.

  var o=new Object()
  o['email']=''
  o['sha1']=''
  o['fade']='true'
  o['template']='default.template'
   var arr=str.split('&')
		for (i=0;i<arr.length;i++) {
   try {
    arr1=arr[i].split('=')
    if (arr1 && arr1[1]) o[arr1[0]]=unescape(arr1[1]).replace(/\+/gim,' ').Trim()
			} catch (E) {}
		}
  return o
 }
function init(e) {

 // called onload, get the querystring, and check for
 // email/sha1 call appropriate blubs.

 var str=decodeURIComponent(gdoc.URL+'?')
 output=parse(str.split('?')[1])
 LoadTemplate(output['template'])
}

function LoadTemplate(url) {

 // Load up the templates.

 getURL(url+".xml",TemplateCallBack)
 getURL(url+".svg",BackgroundCallBack)
}

function BackgroundCallBack(obj) {
 
 // Add in the background SVG doc, if both callbacks finished
 // run the StartIt func which draws people.

 gdocDE.appendChild(parseXML(obj.content,gdoc))
 if (GetCOUNT==1) StartIt()
  else GetCOUNT=1
}
function TemplateCallBack(obj) {

 // Parse the templates into the array.
 // if both callbacks finished
 // run the StartIt func which draws people.

tdoc=parseXML(obj.content,null)
tblubs=tdoc.getElementsByTagName('BLUB')
for (i=0;i<tblubs.length;i++) {
 try {
  tscript=tblubs.item(i).getElementsByTagName('script').item(0)
  scr=tscript.firstChild.nodeValue
 } catch (e) {
  scr=""
 }
 try {
  tsvg=tblubs.item(i).getElementsByTagName('svg').item(0)
  svg=tsvg.firstChild.nodeValue
 } catch (e) {
  svg=""
 }
 try {
  tsvg=tblubs.item(i).getElementsByTagName('focus').item(0)
  focus=tsvg.firstChild.nodeValue
 } catch (e) {
  focus=""
 }
 Templates[tblubs.item(i).getAttribute("type")]={script:scr,svg:svg,focus:focus}
}
 tblubs=tdoc.getElementsByTagName('SPROING')
 for (i=0;i<tblubs.length;i++) {
  try {
   tsvg=tblubs.item(i).getElementsByTagName('svg').item(0)
   svg=tsvg.firstChild.nodeValue
  } catch (e) {
   svg=""
  }
  Templates[tblubs.item(i).getAttribute("type")]={svg:svg}
	}
 tblubs=tdoc.getElementsByTagName('PANEL')
 for (i=0;i<tblubs.length;i++) {
  try {
   tsvg=tblubs.item(i).getElementsByTagName('svg').item(0)
   svg=tsvg.firstChild.nodeValue
  } catch (e) {
   svg=""
  }
  Templates[tblubs.item(i).getAttribute("type")+';PANEL']={svg:svg}
	}
 if (GetCOUNT==1) StartIt()
  else GetCOUNT=1
}


function StartIt() {
  detailPanel=gdoc.getElementById('detailPanel')

  blubsgroup=gdoc.getElementById("blubs");
  sproingsgroup=gdoc.getElementById("sproings");
	 form=gdoc.getElementById("form");

// try {
  var str=decodeURIComponent(gdoc.URL+'?')
  output=parse(str.split('?')[1])
  FADE=output['fade']
  done=false
		if (output['email']!='') {
 		expandBlub(doStuff(' '+output['email']))
   done=true
		} else {
   if (output['sha1']!='') {
    z=1
			 while (typeof output['sha'+z]=='string') {
     if (typeof output['sha'+z]=='string' && output['sha'+z]!='') {
     	doStuffSha1(output['sha'+z])
      done=true
      z++
					}
			 }
			}
		}
			if (!done) {
				// Random blub, only available online.
				if (_ONLINE) doStuffSha1('')
			}
//	} catch (e) {}
}

function Director(name, link, type,nick,img) {
 
	// Something to do with Dean... but a simple object for 
 // adding "directors" which are simply people.

  this.name=name;
  this.link=link;
  this.type=type;
  this.nick=String(nick);
  this.img=img;
}

function myReplace(txt,hash) {
 txt= txt.replace(/\@\@(.*?)\@\@/gim,function($0,$1) {
  return hash[$1]
	})
 return txt
}

function Blub(x, y, startx, starty, corp, name, link,nick) {
 // The main blub object ("class") each blub is one of these
 // first check is that the blub does not already exist, to
 // prevent duplications.
 if (typeof blubhash[name]=='undefined') {

  // properties, hopefully self explanatory.
  this.x=x;
  this.y=y;
  this.blubType="http://xmlns.com/foaf/0.1/Person"
  this.corp=false;
  this.id='a'+nextId;
  this.dragState=0;
  this.operationComplete=opComplete
  this.expanded=0;  this.name=name;
  this.neighbours=[];
  this.outNeighbours=[];
  this.realName='';
  this.moveInGroup=true;
  this.link=link;
  this.homepage='';
  this.deleted=0;
  this.kCount=0;
  this.cCount=0;
  this.knownCount=0;
  this.inlines=new Array();
  this.outlines=new Array();
  this.startx=startx;
  this.starty=starty;
  nextId++;
  blubs[this.id]=this;
  blubs.push(this)
  blubhash[name]=this

		// get a name for the person.
  if (nick+''=='undefined' || nick=='') nick=name.substr(7,name.indexOf('@')-7)
		if (nick.indexOf('.')>3) nick=nick.substr(0,nick.indexOf('.'))
  str=nick.toUpperCase()
  if (nick+''=='undefined') nick='?'
  this.nick=nick
  template=Templates[this.blubType+';Initial']
  myHash=new Function(template.script)()
  myHash.NICK=str
  myHash.ID=this.id
  myTxt=myReplace(template.svg,myHash)

  // create the svg for the blub, IDs are important
  this.group=parseXML('<g>'+myTxt+'</g>',gdoc).firstChild
  this.text=this.group.getElementsByTagName('tspan').item(0)
  moveBlubTo(this, startx, starty);
  blubsgroup.appendChild(this.group);
  setTimeout("moveBlub('" + this.id + "')", 10);
	}
 return this
}

function addLine(from, to,type,img,existing) {

 // adds a line from node from, to node to, with type type, and image
 // img (if codepicted) This is only for lines to new blubs, existing is
 // true if the blub is expanded.
 
 // type is a string looking something like "knows-codepicts-isknownby"
 // pretty odd way of doing it, but it grew to organically, do it better
 // next time.

 var gel=globalgEL.cloneNode(true)
	for (i=0;i<type.length;i++) {
  template=Templates[type[i]]
  myHash={}
  myHash.FROMID=from.id
  myHash.TOID=to.id
  myHash.img=img
		if (existing) {
   myHash.X2=from.x
   myHash.Y2=from.y
   myHash.X1=to.x
   myHash.Y1=to.y
	 	myHash.CX=(+from.x+ +to.x)/2;
 		myHash.CY=(+from.y+ +to.y)/2;
		} else {
   myHash.X1=from.x
   myHash.Y1=from.y
   myHash.X2=from.x
   myHash.Y2=from.y
	 	myHash.CX=from.x
 		myHash.CY=from.y
		}
  myTxt=myReplace(template.svg,myHash)
  gel.appendChild(parseXML('<g>'+myTxt+'</g>',gdoc).firstChild.firstChild)
	}
  var lines=gel.getElementsByTagName('line')
	 for (var i=0;i<lines.length;i++) {
   from.outlines.push(lines.item(i));
   to.inlines.push(lines.item(i));
  }
  from.outNeighbours.push(to)
  sproingsgroup.appendChild(gel);
}

function getCod(evt,a,b,img) {

 // getCod - this displays the codepiction popup, obviously 
 // does not work in Batik due to previous problems, could
 // rewrite codepictions with a global var to store the images etc.
 // but not so neat, not that any of it is exactly neat...

 try {
  var panel=gdoc.getElementById('photoPanel')
  try {
   var trans=gdocDE.currentTranslate;
   var scale=gdocDE.currentScale;
		} catch (e) {
   trans={x:0,y:0}
   scale=1
		}
  x=window.innerWidth-230
  y=50+(photoCount++)*15
  if (y>window.innerHeight) photoCount=0
  y=50+(photoCount++)*15
  codX=(x-trans.x)/scale;
  codY=(y-trans.y)/scale;
  template=Templates[foafNS+"codepiction;PANEL"]
  myHash={}
  myHash.CODX=codX
  myHash.CODY=codY
  myHash.IMG=img
  myHash.PANELCOUNT=gPanelCount
  myHash.SCALE=(1/scale)
  myHash.NICKFROM=blubs[a].nick.toUpperCase()
  myHash.NICKTO=blubs[b].nick.toUpperCase()
  myTxt=myReplace(template.svg,myHash)
  gPanelCount++
  panel.appendChild(parseXML('<g>'+myTxt+'</g>',gdoc).firstChild)
	} catch (e) {}
}

function moveBlubGroup(blub, x, y) {
 nhbs=blub.outNeighbours
 var x=blub.x;
 var y=blub.y;
 var numBlubsToCreate=nhbs.length
	for (var i=0;i<numBlubsToCreate;i++) {
		if (nhbs[i].moveInGroup) {
   newX=x + Math.round(Math.cos(2*Math.PI/numBlubsToCreate*(i%numBlubsToCreate)) * (numBlubsToCreate*BLUB_EXP_DISTANCE+BLUB_EXP_DISTANCE_DEL));
   newY=y + 1.2*Math.round(Math.sin(2*Math.PI/numBlubsToCreate*(i%numBlubsToCreate)) * (numBlubsToCreate*BLUB_EXP_DISTANCE+BLUB_EXP_DISTANCE_DEL));
   setTimeout('moveBlubToName("'+nhbs[i].name+'",'+newX+','+newY+')',10)
		}
	}
}
function moveBlubToName(name,x,y) {
 var blb=blubhash[name]
 blb.x=x
 blb.y=y
 moveBlubTo(blb,x,y)
}
function moveBlubTo(blub, x, y) {

 // This moves a blub to position x,y !
 // taking its lines with it.

 blub.group.setAttribute("transform", "translate("+x+","+y+")");
 blub.groupx=x;
 blub.groupy=y;
 var bi=blub.inlines
 var circles
 var bil=bi.length
 for (var i=0; i<bil; i++) {
  var bili=bi[i]
  bili.setAttribute("x1",x)
  bili.setAttribute("y1",y)
  x2=bili.getAttribute("x2")
  y2=bili.getAttribute("y2")
  cx=(Number(x2)+x)/2
  cy=(Number(y2)+y)/2
  circles=bili.getElementsByTagName('circle')
		for (k=0;k<circles.length;k++) {
   circles.item(k).setAttribute("cx",cx)
   circles.item(k).setAttribute("cy",cy)
 	}
 }
 var bo=blub.outlines
 var bol=bo.length
 for (var i=0; i<bol; i++) {
  var boli=bo[i]
  boli.setAttribute("x2",x)
  boli.setAttribute("y2",y)
  x2=boli.getAttribute("x1")
  y2=boli.getAttribute("y1")
  cx=(Number(x2)+x)/2
  cy=(Number(y2)+y)/2
  circles=boli.getElementsByTagName('circle')
		for (k=0;k<circles.length;k++) {
   circles.item(k).setAttribute("cx",cx)
   circles.item(k).setAttribute("cy",cy)
		}
	}
}


function blubDragOn(id) {
 // Called when you start dragging a blub.
 try {
  el=gdoc.getElementById("highlightGroup")
 	while (el.hasChildNodes) {
   el.removeChild(el.firstChild)
 	}
 } catch (e) {}
 draggingPanels=false
 blubs[id].dragState=1;
 currentBlubId=id;
}

function blubDragOff() {

 // called when you finish dragging a blub.

 draggingPanels=false
 if (currentBlubId != -1) {
  blubs[currentBlubId].dragState=0;
  currentBlubId=-1;
 }
}

function blubPositions() {
 str=''
	for (i=0;i<blubs.length;i++) {
  str+=blubs[i].nick+' - \t'+blubs[i].x+','+blubs[i].y+'\n'
	}
 return str
}

function dragBlub(evt) {
 
 // Called as you drag the blub.

 if (currentBlubId != -1) {
  var theBlub=blubs[currentBlubId];
  if (theBlub.dragState) {
   theBlub.moveInGroup=false;
   var nowToX=evt.getClientX();
   var nowToY=evt.getClientY();
   // handle the current zoom and pan
  try {
   var trans=gdocDE.currentTranslate;
   var scale=gdocDE.currentScale;
		} catch (e) {
   trans={x:0,y:0}
   scale=1
		}
   nowToX=(nowToX-trans.x)/scale;
   nowToY=(nowToY-trans.y)/scale;
   theBlub.x=nowToX;
   theBlub.y=nowToY;
   moveBlubTo(theBlub, nowToX, nowToY);
   if (BLUB_MOVE_GROUP) moveBlubGroup(theBlub, nowToX, nowToY);
   try {
				if (currentBlubId==theBlub.id) {
 focusCirc=gdoc.getElementById('focusCirc')
 while(focusCirc.hasChildNodes()) {
  focusCirc.removeChild(focusCirc.firstChild)
	} 
  template=Templates[theBlub.blubType+';'+(theBlub.expanded ? 'Initial' : 'Expanded')]
  myHash={}
  myHash.X=nowToX
  myHash.Y=nowToY
  myTxt=myReplace(template.focus,myHash)

 focusCirc.appendChild(parseXML(myTxt,gdoc).firstChild)
				}
  	} catch (e) {}
  }
 }
 panelDrag(evt)
}

draggingPanels=false
function panelDrag(evt) {
 // called as you drag any of the panels.

 if (draggingPanels) {
  var X=evt.getClientX()-24;
  var Y=evt.getClientY()-18;
  // handle the current zoom and pan
  try {
   var trans=gdocDE.currentTranslate;
   var scale=gdocDE.currentScale;
		} catch (e) {
   trans={x:0,y:0}
   scale=1
		}
  X=((X-trans.x)/scale);
  Y=((Y-trans.y)/scale);
  var panel=gdoc.getElementById(draggingPanel)
  panel.setAttribute("transform","translate("+X+","+Y+")");
	}
}

function bringTop(id) {

 // cannot remember what this does, brings something to the
 // top presumably...  It appears to bring the codepiction panel 
 // to the top, but also changes the colour of the two blubs mentioned 
 // in that codepictions aprons.

 var panelID=gdoc.getElementById(id).parentNode
 var panel=panelID.parentNode
 var nde=panel.removeChild(panelID)
 panel.appendChild(nde)
}


function deleteBlub(id) {

 // deletes a blub, probably want to change this to not delete the svg
 // but simply to change the display property to none, so you can bring
 // it back later.

  var theBlub=blubs[id];
  for (var i=0; i<theBlub.outlines.length; i++) {
   try {
    sproingsgroup.removeChild(theBlub.outlines[i].parentNode);
			} catch (e) {}
		}
  for (var i=0; i<theBlub.inlines.length; i++) {
   try {
    sproingsgroup.removeChild(theBlub.inlines[i].parentNode);
			} catch (e) {}
 	}
  theBlub.group.parentNode.removeChild(theBlub.group)

  theBlub.deleted=1;


 focusCirc=gdoc.getElementById('focusCirc')
 while(focusCirc.hasChildNodes()) {
  focusCirc.removeChild(focusCirc.firstChild)
	} 

}


function findBlub(name) {

 // Silly little function which should not exist any more
 // the function is one of the few relics from Dean, when
 // it was more complicated.

 return (blubhash[name])
}

function expandBlub(id) {
 // Expand the blub, changes the + to o and requests the new 
 // data from the server, does nothing if it has already been
 // expanded. 
 // Also changes the function on the o to updatePanel, be
 // careful with the structure of the blub, at the moment it 
 // is the 3rd g element if you change this, you must also 
 // change the way you get the onclick reference here.
 theBlub=blubs[id]; 

 if (!theBlub.expanded) {

  str=theBlub.nick.toUpperCase()
  template=Templates[theBlub.blubType+';Expanded']
  myHash=new Function(template.script)()
  myHash.NICK=theBlub.nick.toUpperCase()
  myHash.ID=theBlub.id
  myTxt=myReplace(template.svg,myHash)
  // create the svg for the blub, IDs are important.
  plus=theBlub.group
  while (plus.hasChildNodes()) plus.removeChild(plus.firstChild)
  plus.appendChild(parseXML('<g>'+myTxt+'</g>',gdoc).firstChild)
  dataReady=0;
  theBlub.getStuff(theBlub.link,theBlub)
 }
}

Blub.prototype.getStuff= function (url,a) {

 // A getStuff function added to the Blub class
 // so we can use Blub.getStuff to get new data
 // and keep the data with the blub.
 // does not work in Batik, so we use global blud ID
 // there, which obviously means you can only expand
 // one at a time.

 // This now works in Batik beta 5, so hack removed.
 getURL(url,this)
}
function opComplete(urlRS) {
 // The callback function called when the data url is returned
 // from the server. 
try {
   var doc=parseXML(urlRS.content,null);
   thisProxy=this

   // get all the foaf:knows elements, and loop over them
   // not using RDF parser, but you could put mine in here
   // but it would be slower!
   // for each element create an object (a,b,c,d)
   // (sha1,knows|isknownby|codepicts,nick,img)
   // img is only for codepicts obviously.
   dirs=doc.getElementsByTagNameNS(foafNS,"knows");
   dirList=new Array();
   count=0;
   var tmp=[]
   for (var i=0; i < dirs.length; i++) {
    var d=dirs.item(i)
    tmp[count]={a:d.getAttributeNS(jimNS, "fID"),b:[foafNS+"knows"],c:d.getAttributeNS(foafNS, "nick")}
				tmp[tmp[count].a]=tmp[count]
    count++;
   }
   thisProxy.kCount=count
   // same again this time with jim:isKnownBy, but this time 
   // changing b if the person already exists.

   dirs=doc.getElementsByTagNameNS(jimNS,"isKnownBy");
   for (var i=0; i < dirs.length; i++) {
    var d=dirs.item(i)
    var r=d.getAttributeNS(foafNS, "mbox_sha1sum")
				if (!tmp[r]) {
     tmp[count]={a:r,b:[jimNS+"isKnownBy"],c:d.getAttributeNS(foafNS, "nick")}
	 			tmp[r]=tmp[count]
     count++;
				} else {
     tmp[r].b=[jimNS+"isKnownBy",foafNS+"knows"]
				}
   }
   thisProxy.knownCount=dirs.length

   // and again with codepicts.

   dirs=doc.getElementsByTagNameNS(foafNS,"codepiction");
   for (var i=0; i < dirs.length; i++) {
    var d=dirs.item(i)
    var r=d.getAttributeNS(foafNS, "mbox_sha1sum")
				if (!tmp[r]) {
     tmp[count]={a:r,b:[foafNS+"codepiction"],c:d.getAttributeNS(foafNS, "nick"),d:d.getAttributeNS(imgNS, "naughty")}
	 			tmp[r]=tmp[count]
     count++;
				} else {
     tmp[r].b.push(foafNS+"codepiction")
				}
   }
   thisProxy.cCount=dirs.length

   // go through all the people we just found, and create a new
   // Director object for each.
 
			for (i=0;i<tmp.length;i++) {
    dirList[i]=new Director(tmp[i].a,startURL+tmp[i].a+endURL,tmp[i].b,tmp[i].c,tmp[i].d);
			}

	  // Get the email addresses, sha1sums, nick etc. for the 
   // blub you expanded

   dirs=doc.getElementsByTagNameNS(foafNS,"mbox");
   thisProxy.mboxes=[]
   for (var i=0; i < dirs.length; i++) {
    var mbox=dirs.item(i).getAttributeNS(rdfNS, "resource")
    blubhash[mbox]=thisProxy
    thisProxy.mboxes[thisProxy.mboxes.length]=mbox
   }
   dirs=doc.getElementsByTagNameNS(jimNS,"fID");
   for (var i=0; i < dirs.length; i++) {
    var sha=dirs.item(i).firstChild.data
    blubhash[sha]=thisProxy
   }
    addDirs(thisProxy.id)
    try {
     thisProxy.nick=doc.getElementsByTagNameNS(foafNS,"nick").item(0).firstChild.data.toUpperCase()
     thisProxy.text.firstChild.setData(thisProxy.nick)
    } catch (e) {	}
    try {
     thisProxy.realName=doc.getElementsByTagNameNS(foafNS,"name").item(0).firstChild.data
    } catch (e) {	}
    try {
     thisProxy.homepage=doc.getElementsByTagNameNS(foafNS,"homepage").item(0).firstChild.data
    } catch (e) {	}

	   // Do Image stuff

   var img=doc.getElementsByTagNameNS(foafNS,"depiction");
   if (img.item(0)) {
    var imgurl=img.item(0).getAttributeNS(rdfNS, "resource")
    found=false

				// Is there an image with polypath etc.?

    descs=doc.getElementsByTagNameNS(rdfNS,"Description");
				for (i=0;i<descs.length;i++) {
     var di=descs.item(i)
					if (di.getAttributeNS(rdfNS, "about")==imgurl) {
      var polypath=di.getElementsByTagNameNS(imgNS,"polypath").item(0).firstChild.nodeValue;
      var height=di.getElementsByTagNameNS(imgNS,"height").item(0).firstChild.nodeValue;
      var width=di.getElementsByTagNameNS(imgNS,"width").item(0).firstChild.nodeValue;
      thisProxy.depiction=new Depiction(imgurl,height,width,polypath)
      found=true
					}
			 }
				if (!found) {
     thisProxy.depiction=new Depiction(imgurl,0,0,'')
				}
   } else {
    thisProxy.depiction=new Depiction('',0,0,'')
			}

	// Finally call updatePanel, which creates the control panels, and images.

  updatePanel(thisProxy)
	} catch (e) {}
		}


function Depiction(img,h,w,path) {

 // Simple depiction object, added as an object so could later
 // add in the get image size ability needed to scale images
 // correctly.

 this.img=img
 this.height=h
 this.width=w
 this.path=path
 return this
}

function closePanel(id) {

 // closes a panel, called onclick from the close button.

 try {
  var panel=gdoc.getElementById(id)
  panel.parentNode.removeChild(panel)
 } catch (e) {}
}

function updatePanel(theBlub) {
try {
 currentBlubId=theBlub.id

 // changes the panels, to refer to the blub.

 detailPanel.setAttribute("display","block")
 theBlub.nick=String(theBlub.nick).toUpperCase()
 var d=theBlub.depiction
  try {
   var trans=gdocDE.currentTranslate;
   var scale=gdocDE.currentScale;
		} catch (e) {
   trans={x:0,y:0}
   scale=1
		}

 // removes the old, then adds the focus circle around the selected blub.

 focusCirc=gdoc.getElementById('focusCirc')
 while(focusCirc.hasChildNodes()) {
  focusCirc.removeChild(focusCirc.firstChild)
	} 

  template=Templates[theBlub.blubType+';'+(theBlub.expanded ? 'Initial' : 'Expanded')]
  myHash={}
  myHash.X=theBlub.x
  myHash.Y=theBlub.y
  myTxt=myReplace(template.focus,myHash)

 focusCirc.appendChild(parseXML(myTxt,gdoc).firstChild)
 // add all the email addresses, and homepage.
  template=Templates[jimNS+"NAMES;PANEL"]
  myHash={}
  myHash.NICK=theBlub.nick
  myHash.NAME=theBlub.realName
  str=myReplace(template.svg,myHash)

  try {
   template=Templates[jimNS+"MBOX;PANEL"]
  	for (i=0;i<theBlub.mboxes.length;i++) {
    myHash['MBOX'+(i+1)]=theBlub.mboxes[i].substr(7,1000)
  	}
   for (j=i;j<i+5;j++) {
    myHash['MBOX'+(j+1)]=""
			}
   str+=myReplace(template.svg,myHash)
  } catch (e) {}
  gdoc.getElementById('details').removeChild(gdoc.getElementById('details').firstChild)
  gdoc.getElementById('details').appendChild(parseXML('<g>'+str+'</g></g>',gdoc).firstChild)
  gdoc.getElementById('imgNick').firstChild.nodeValue=theBlub.nick
  gdoc.getElementById('knows').firstChild.nodeValue="KNOWS "+theBlub.kCount+" PEOPLE"
  gdoc.getElementById('knownby').firstChild.nodeValue="KNOWN BY "+theBlub.knownCount+" PEOPLE"
  gdoc.getElementById('codepicted').firstChild.nodeValue="PICTURED WITH "+theBlub.cCount+" PEOPLE"

 try {
  gdoc.getElementById('imageLoc').removeChild(gdoc.getElementById('imageLoc').firstChild)
	} catch (E) {}

 dep=theBlub.depiction
	if (dep && dep.img) {
   str='<g><image xmlns:xlink="http://www.w3.org/1999/xlink" xlink:href="'+dep.img+'" x="0" y="0" height="110" width="135"/></g>'
	 } else {
   str='<g><image xmlns:xlink="http://www.w3.org/1999/xlink" xlink:href="'+dep.img+'" x="0" y="0" height="110" width="135"/></g>'
  }

  gdoc.getElementById('imageLoc').appendChild(parseXML(str,gdoc).firstChild)
		// The code that makes blubs further away dim into the background
		// needs work, odd blubs not dimmed
		if (FADE) {
 		for (var i=0;i<blubs.length;i++) {
 			var bli=blubs[i];
 			if (bli!=theBlub && !theBlub.neighbours[bli.id]) {
 				bli.group.firstChild.setAttribute("style","opacity:0.2");
 				var ol=bli.outlines;
 				for (var j=0;j<ol.length;j++) {
      try {
 				 	ol[j].parentNode.setAttribute("style","opacity:0.2");
					 } catch (e) {}
 				}
 				var ol=bli.inlines;
 				for (var j=0;j<ol.length;j++) {
      try {
 					ol[j].parentNode.setAttribute("style","opacity:0.2");
					 } catch (e) {}
 				}
 			} else {
 				bli.group.firstChild.setAttribute("style","opacity:1");
 				var ol=bli.outlines;
 				for (var j=0;j<ol.length;j++) {
      try {
 					ol[j].parentNode.setAttribute("style","opacity:1");
					 } catch (e) {}
 				}
 				var ol=bli.inlines;
 				for (var j=0;j<ol.length;j++) {
      try {
 					ol[j].parentNode.setAttribute("style","opacity:1");
					 } catch (e) {}
 				}
 			}
 		}
}
} catch (e) {}
}


function addDirs(id) {

  // adds all the "Director" blubs to the screen, called from the 
  // callback function

  var theBlub=blubs[id];
  var numBlubsToCreate=dirList.length;
   for (var i=0; i <dirList.length; i++) {
    var dir=dirList[i];
    var existing=findBlub(dir.name);
    if (typeof existing=='undefined' || existing == null) {
        newX=theBlub.x + Math.round(Math.cos(2*Math.PI/numBlubsToCreate*(i%numBlubsToCreate)) * (numBlubsToCreate*BLUB_EXP_DISTANCE+BLUB_EXP_DISTANCE_DEL));
        newY=theBlub.y + 1.2*Math.round(Math.sin(2*Math.PI/numBlubsToCreate*(i%numBlubsToCreate)) * (numBlubsToCreate*BLUB_EXP_DISTANCE+BLUB_EXP_DISTANCE_DEL));
        var blub=new Blub(newX, newY, theBlub.x, theBlub.y, false, dir.name, dir.link,dir.nick);
								theBlub.neighbours.push(blub)
								theBlub.neighbours[blub.id]=theBlub.neighbours[theBlub.neighbours.length-1]
								blub.neighbours.push(theBlub)
								blub.neighbours[theBlub.id]=blub.neighbours[blub.neighbours.length-1]
        addLine(theBlub, blub,dir.type,dir.img,false);

								// How much smaller the unexpanded blubs are.

								blub.group.firstChild.setAttribute("transform","scale("+BLUB_SIZE_SMALL+")");
      } else {
       existing.moveInGroup=false
							if (existing.deleted != 1) {

								// Add the lines to an existing blub.
										theBlub.neighbours.push(existing);
			  					theBlub.neighbours[existing.id]=theBlub.neighbours[theBlub.neighbours.length-1];
				  				existing.neighbours.push(theBlub);
										existing.neighbours[theBlub.id]=existing.neighbours[existing.neighbours.length-1]
          addLine(theBlub, existing,dir.type,dir.img,true);
									} else {

										// This is where you would bring a deleted blub back to life,
										// then add the existing lines etc.

									}
      }
 }


 theBlub.expanded=1;
}



function moveBlub(id) {

 // The recursively called moveBlub function 
 // creates the explosions.
 // BLUB_EXPLOSION_DELTA effects the number of steps it takes.

  var theBlub=blubs[id];
  nowX=theBlub.groupx;
  nowY=theBlub.groupy;
  var toX=theBlub.x;
  var toY=theBlub.y;
  var dx=toX-nowX;
  var dy=toY-nowY;

  if (dx > -3 && dx < 3 && dy > -3 && dy < 3) {
    var nowToX=toX;
    var nowToY=toY;
  } else {
    var nowToX=nowX + dx/BLUB_EXPLOSION_DELTA;
    var nowToY=nowY + dy/BLUB_EXPLOSION_DELTA;
    setTimeout("moveBlub('" + id + "')", 10);
  }

  moveBlubTo(theBlub, nowToX, nowToY);
}

// Key handling code on the form...

function keyPress(evt) {
  var cc=evt.charCode;
  if (cc < 32) {
   if (cc == 13 || cc == 10) {
    go();
   } 
   return;
  }
  var inp=String.fromCharCode(cc);
  form.getFirstChild().appendData(inp);
}

function keyDown(evt) {
 var code=evt.keyCode;
 if (code == 8) {
  var s=form.getFirstChild();
  if (s.length > 1) {
   s.deleteData(s.length-1, 1);
  }
 } 
}

function getFormValue() {
  return form.getFirstChild().nodeValue;
}

function go() {

 // called on "submitting" the form, does a check that there is an @
 // in the field, this is where you could handle nick/name etc. with
 // another callback to a suitable service such as identify.1?

  var fullName=String(getFormValue());
		if (fullName.indexOf('@')>1) {
   doStuff(fullName)
		}
}
	function doStuff(fullName) {

  // creates a blub from an email address "fullname" add mailto:
  // if necessary.  Positioning changes in a pretty crap way.
 
		a={id:''}
		fullName=fullName.Trim()
  if (fullName.length > 0) {
   var nick=fullName.substring(0,fullName.indexOf('@'))
   if (fullName.indexOf('mailto:')!=0) fullName='mailto:'+fullName
			fullName=calcSHA1(fullName)
   var idName=startURL+fullName+endURL;
			if (typeof findBlub(fullName)=='undefined') {
    a=new Blub(280+((searchCount%3)*100), 180+((searchCount%3)*100), 280+((searchCount%3)*100), 180+((searchCount%3)*100), 1, fullName, idName,nick,'');
    searchCount++
			} else {

				// update a panel if the blub was found, should probably want 
    // to do some zoom/pan stuff here to bring it into the middle
    // of the viewport

    updatePanel(a=findBlub(fullName))
			}
    gdoc.getElementById("searchPanel").setAttribute('style','display:none')
    gdoc.getElementById("imagePanel").setAttribute('style','display:none')
    detailPanel.setAttribute("style","display:block")
    form.getFirstChild().setData(' ');
  }
		return a.id
	}

	function doStuffSha1(sha1) {
  // same as doStuff but for a sha1 - does not do updatePanel if it
  // exists - should probably combine these two functions.
  var nick='PERSON'
  var fullName=sha1
  var idName=startURL+sha1+endURL;
  var a=new Blub(280+((searchCount%3)*100), 200+((searchCount%3)*100), 280+((searchCount%3)*100), 200+((searchCount%3)*100), 1, fullName, idName,nick,'');
		expandBlub(a.id)
  searchCount++
  form.getFirstChild().setData(' ');
	}


	function zoom() {

  // handle the fixing the viewport - called on zoom.

  try {
   var trans=gdocDE.currentTranslate;
   var scale=gdocDE.currentScale;
		} catch (e) {
   trans={x:0,y:0}
   scale=1
		}
  X=((0-trans.x)/scale);
  Y=((0-trans.y)/scale);
  var p1=gdoc.getElementById('aboutbox')
  var p2=gdoc.getElementById('branding')
  p1.setAttribute("transform","translate("+X+","+Y+") scale("+(1/scale)+")");
  p2.setAttribute("transform","translate("+X+","+Y+") scale("+(1/scale)+")");
 }

	function ShowPath(b1,b2) {
		if (b1!=-1 && b2 !=-1) {
 		// Show a path between 2 blubs
   var col=highlightColours[pathCount++%highlightColours.length]
   var high1=blubs[b1]; 
   var high2=blubs[b2]; 
   makeGraph()
   high1.tree=findShortestPathsFromNode(high1.name)
   nde=high1.tree[high2.name]
 		while (nde) {
 			if (nde.par) {
     highlightPath(nde,nde.par,col)
    } 
    nde=nde.par
 		}
		}
 }

	function highlightPath(fromNde,toNde,col) {

  // Highlights the path between two nodes.

  from=blubhash[fromNde.node.id]
  to=blubhash[toNde.node.id]
  var gel=globalgEL.cloneNode(true)
  var line=globalLine.cloneNode(true);
  line.setAttribute("fill", "none");
  line.setAttribute("x1", from.groupx);
  line.setAttribute("y1", from.groupy);
  line.setAttribute("x2", to.groupx);
  line.setAttribute("y2", to.groupy);
  line.setAttribute("stroke", col);
  gel.appendChild(line);
  gdoc.getElementById("highlightGroup").appendChild(gel);
}

// Path Finding Algorithm.

TNodes=[]
TEdges=[]
function makeGraph() {

 // Create a graph see 
 // http://rdfweb.org/people/damian/2002/02/foafnation/
 // for more info, stolen from Damian.

 var bl;
	TNodes=[];
	TEdges=[];
 // create the Nodes
 for (var i=0;i<blubs.length;i++) {
  bl=blubs[i]
  TNodes.push({id:bl.name,edges:[],visited:false})
  TNodes[bl.name]=TNodes[TNodes.length-1]
	}
 // create the edges
 for (var i=0;i<TNodes.length;i++) {
  Tn=TNodes[i]
  nbs=blubhash[Tn.id].neighbours
		for (var j=0;j<nbs.length;j++) {
			if (nbs[j]) {
 			var nid=nbs[j].id
    if (!TEdges[nid+'-'+Tn.id]) {
     TEdges.push({n1:Tn,n2:TNodes[nid]})
 				TEdges[nid+'-'+Tn.id]=TEdges[TEdges.length-1]
 			}
			}
			Tn.edges.push(TEdges[nid+'-'+Tn.id])
		}
	}
}
function TElement(node,p,e) {
 this.node=node
 this.par=p
 this.edge=e
}
function findShortestPathsFromNode(id) {
 for (var j=0;j<TNodes.length;j++) {
		TNodes[j].visited=false
	}
	TNodes[id].visited=true
 root=new TElement(TNodes[id],null)
 var parents=[]
	parents.push(root)
 var leaves=[]
 var continuing=true
	while (continuing) {
  children=[]
  continuing=false
		for (var j=0;j<parents.length;j++) {
   par=parents[j]
   parentNode=par.node
			for (var k=0;k<parentNode.edges.length;k++) {
				edge=parentNode.edges[k]
    if (edge.n1.id==parentNode.id) childNode=edge.n2
					else childNode=edge.n1
				if (childNode && !childNode.visited) {
					childNode.visited=true
					child=new TElement(childNode,par,edge)
     leaves.push(child)
					leaves[childNode.id]=child;
					children.push(child)
					continuing=true
				}
			}
		}
		parents=children
	}
 return leaves
}

// Thats all folks...
