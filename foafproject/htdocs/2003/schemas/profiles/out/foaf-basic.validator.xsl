<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<axsl:stylesheet xmlns:axsl="http://www.w3.org/1999/XSL/Transform" xmlns:sch="http://www.ascc.net/xml/schematron" version="1.0" rdf:dummy-for-xmlns="" rdfs:dummy-for-xmlns="" foaf:dummy-for-xmlns="">
<axsl:output method="html"/>
<axsl:template mode="schematron-get-full-path" match="*|@*">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:if test="count(. | ../@*) = count(../@*)">@</axsl:if>
<axsl:value-of select="name()"/>
<axsl:text>[</axsl:text>
<axsl:value-of select="1+count(preceding-sibling::*[name()=name(current())])"/>
<axsl:text>]</axsl:text>
</axsl:template>
<axsl:template match="/">
<html>
<style>
         a:link    { color: black}
         a:visited { color: gray}
         a:active  { color: #FF0088}
         h3        { background-color:black; color:white;
                     font-family:Arial Black; font-size:12pt; }
         h3.linked { background-color:black; color:white;
                     font-family:Arial Black; font-size:12pt; }
      </style>
<h2 title="Schematron contact-information is at the end of                   this page">
<font color="#FF0080">Schematron</font> Report
      </h2>
<h1 title=" ">FOAF "Basic" Validator</h1>
<div class="errors">
<ul>
<h3>Namespace Checking</h3>
<axsl:apply-templates mode="M4" select="/"/>
<h3>Basic Validation</h3>
<axsl:apply-templates mode="M5" select="/"/>
<h3>Best Practices</h3>
<axsl:apply-templates mode="M6" select="/"/>
<h3>RDF Validation</h3>
<axsl:apply-templates mode="M7" select="/"/>
<h3>Content Model (Literals)</h3>
<axsl:apply-templates mode="M9" select="/"/>
<h3>Identifying Properties</h3>
<axsl:apply-templates mode="M10" select="/"/>
</ul>
</div>
<hr color="#FF0080"/>
<p>
<font size="2">Schematron Report by David Carlisle.
      <a title="Link to the home page of the Schematron,                  a tree-pattern schema language" href="http://www.ascc.net/xml/resource/schematron/schematron.html">
<font color="#FF0080">The Schematron</font>
</a> by
      <a title="Email to Rick Jelliffe (pronounced RIK JELIF)" href="mailto:ricko@gate.sinica.edu.tw">Rick Jelliffe</a>,
      <a title="Link to home page of Academia Sinica" href="http://www.sinica.edu.tw">Academia Sinica Computing Centre</a>.
      </font>
</p>
</html>
</axsl:template>
<axsl:template mode="M4" priority="4000" match="*">
<axsl:choose>
<axsl:when test="namespace-uri()='http://www.w3.org/1999/02/22-rdf-syntax-ns#' or                     namespace-uri()='http://www.w3.org/2000/01/rdf-schema#' or                     namespace-uri()='http://xmlns.com/foaf/0.1/'"/>
<axsl:otherwise>
<li>
<a title="Link to where this pattern was expected" target="out" href="schematron-out.html#{generate-id(.)}">Error: Element<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>is from an unknown namespace.
         ()
      </a>
</li>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M4"/>
</axsl:template>
<axsl:template mode="M4" priority="-1" match="text()"/>
<axsl:template mode="M5" priority="4000" match="/">
<axsl:choose>
<axsl:when test="rdf:RDF"/>
<axsl:otherwise>
<li>
<a title="Link to where this pattern was expected" target="out" href="schematron-out.html#{generate-id(.)}">Root element must be rdf:RDF
         ()
      </a>
</li>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M5"/>
</axsl:template>
<axsl:template mode="M5" priority="3999" match="rdf:RDF">
<axsl:choose>
<axsl:when test="foaf:Person"/>
<axsl:otherwise>
<li>
<a title="Link to where this pattern was expected" target="out" href="schematron-out.html#{generate-id(.)}">Error: The root rdf:RDF element must contain a foaf:Person element
         ()
      </a>
</li>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="count(foaf:Person) = 1"/>
<axsl:otherwise>
<li>
<a title="Link to where this pattern was expected" target="out" href="schematron-out.html#{generate-id(.)}">Error: There should only be a single root foaf:Person
         ()
      </a>
</li>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="count(foaf:Person) = count(*)"/>
<axsl:otherwise>
<li>
<a title="Link to where this pattern was expected" target="out" href="schematron-out.html#{generate-id(.)}">Error: There must be a single element asa child of rdf:RDF and that must be a foaf:Person element.
         ()
      </a>
</li>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M5"/>
</axsl:template>
<axsl:template mode="M5" priority="3998" match="rdf:RDF/foaf:Person">
<axsl:choose>
<axsl:when test="foaf:name or foaf:firstName or foaf:surname or foaf:nick"/>
<axsl:otherwise>
<li>
<a title="Link to where this pattern was expected" target="out" href="schematron-out.html#{generate-id(.)}">Error: You should include a name
         ()
      </a>
</li>
</axsl:otherwise>
</axsl:choose>
<axsl:if test="@*[namespace-uri()='http://xmlns.com/foaf/0.1/']">
<li>
<a title="Link to where this pattern was found" target="out" href="schematron-out.html#{generate-id(.)}">Error: Illegal use of atrtibute in FOAF namespace
         ()
      </a>
</li>
</axsl:if>
<axsl:apply-templates mode="M5"/>
</axsl:template>
<axsl:template mode="M5" priority="3997" match="foaf:knows/foaf:Person">
<axsl:choose>
<axsl:when test="foaf:name or foaf:firstName or foaf:surname or foaf:nick"/>
<axsl:otherwise>
<li>
<a title="Link to where this pattern was expected" target="out" href="schematron-out.html#{generate-id(.)}">Error: You should include a name
         ()
      </a>
</li>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M5"/>
</axsl:template>
<axsl:template mode="M5" priority="3996" match="foaf:Person">
<axsl:if test="true()">
<li>
<a title="Link to where this pattern was found" target="out" href="schematron-out.html#{generate-id(.)}">Error: People should only appear in a foaf:knows element, or within the root rdf:RDF element
         ()
      </a>
</li>
</axsl:if>
<axsl:apply-templates mode="M5"/>
</axsl:template>
<axsl:template mode="M5" priority="3995" match="foaf:mbox">
<axsl:choose>
<axsl:when test="starts-with(@rdf:resource, 'mailto:')"/>
<axsl:otherwise>
<li>
<a title="Link to where this pattern was expected" target="out" href="schematron-out.html#{generate-id(.)}">Mail-boxes must be specified as URIs
         ()
      </a>
</li>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M5"/>
</axsl:template>
<axsl:template mode="M5" priority="3994" match="foaf:mbox | foaf:mbox_sha1sum | foaf:geekcode | foaf:jabberID | foaf:aimChatID | foaf:icqChatID | foaf:yahooChatID | foaf:msnChatID | foaf:firstName | foaf:surname | foaf:family_name | foaf:homepage | foaf:weblog | foaf:plan | foaf:made | foaf:maker | foaf:img | foaf:depiction | foaf:myersBriggs | foaf:workplaceHomepage | foaf:workInfoHomepage | foaf:schoolHomepage | foaf:knows | foaf:interest | foaf:topic_interest | foaf:publications | foaf:currentProject | foaf:pastProject | foaf:fundedBy | foaf:logo | foaf:page | foaf:theme | foaf:holdsAccount">
<axsl:apply-templates mode="M5"/>
</axsl:template>
<axsl:template mode="M5" priority="3993" match="foaf:sha1 | foaf:based_near | foaf:depicts | foaf:thumbnail | foaf:topic | foaf:accountServiceHomepage | foaf:accountName | foaf:member | foaf:membershipClass">
<axsl:if test="true()">
<li>
<a title="Link to where this pattern was found" target="out" href="schematron-out.html#{generate-id(.)}">This element is not allowed inFOAF Minimal
         ()
      </a>
</li>
</axsl:if>
<axsl:apply-templates mode="M5"/>
</axsl:template>
<axsl:template mode="M5" priority="-1" match="text()"/>
<axsl:template mode="M6" priority="4000" match="rdf:RDF/foaf:Person">
<axsl:if test="foaf:mbox">
<li>
<a title="Link to where this pattern was found" target="out" href="schematron-out.html#{generate-id(.)}">Warning: Plain text email found
         ()
      </a>
</li>
</axsl:if>
<axsl:apply-templates mode="M6"/>
</axsl:template>
<axsl:template mode="M6" priority="3999" match="foaf:knows/foaf:Person">
<axsl:if test="foaf:mbox">
<li>
<a title="Link to where this pattern was found" target="out" href="schematron-out.html#{generate-id(.)}">Warning: Friends shouldnever have a plain-text specified in a FOAF Basic document
         ()
      </a>
</li>
</axsl:if>
<axsl:apply-templates mode="M6"/>
</axsl:template>
<axsl:template mode="M6" priority="-1" match="text()"/>
<axsl:template mode="M7" priority="3999" match="foaf:mbox">
<axsl:choose>
<axsl:when test="starts-with(@rdf:resource, 'mailto:')"/>
<axsl:otherwise>
<li>
<a title="Link to where this pattern was expected" target="out" href="schematron-out.html#{generate-id(.)}">Error: Mail-boxes must be specified as mailto: URIs
         ()
      </a>
</li>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M7"/>
</axsl:template>
<axsl:template mode="M7" priority="3998" match="foaf:phone">
<axsl:choose>
<axsl:when test="starts-with(@rdf:resource, 'tel:')"/>
<axsl:otherwise>
<li>
<a title="Link to where this pattern was expected" target="out" href="schematron-out.html#{generate-id(.)}">Error: Telephone numbers must be specified as tel: URIs
         ()
      </a>
</li>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M7"/>
</axsl:template>
<axsl:template mode="M7" priority="3997" match="rdfs:seeAlso|foaf:homepage|foaf:weblog|foaf:depiction|foaf:workplaceHomepage|foaf:schoolHomepage">
<axsl:apply-templates mode="M7"/>
</axsl:template>
<axsl:template mode="M7" priority="-1" match="text()"/>
<axsl:template mode="M9" priority="4000" match="foaf:mbox_sha1sum">
<axsl:if test="child::*">
<li>
<a title="Link to where this pattern was found" target="out" href="schematron-out.html#{generate-id(.)}">Literals shouldnot have child elements
         ()
      </a>
</li>
</axsl:if>
<axsl:apply-templates mode="M9"/>
</axsl:template>
<axsl:template mode="M9" priority="3999" match="foaf:geekcode">
<axsl:if test="child::*">
<li>
<a title="Link to where this pattern was found" target="out" href="schematron-out.html#{generate-id(.)}">Literals should not have child elements
         ()
      </a>
</li>
</axsl:if>
<axsl:apply-templates mode="M9"/>
</axsl:template>
<axsl:template mode="M9" priority="3998" match="foaf:dnaChecksum">
<axsl:if test="child::*">
<li>
<a title="Link to where this pattern was found" target="out" href="schematron-out.html#{generate-id(.)}">Literals should not have child elements
         ()
      </a>
</li>
</axsl:if>
<axsl:apply-templates mode="M9"/>
</axsl:template>
<axsl:template mode="M9" priority="3997" match="foaf:jabberID">
<axsl:if test="child::*">
<li>
<a title="Link to where this pattern was found" target="out" href="schematron-out.html#{generate-id(.)}">Literals should not have child elements
         ()
      </a>
</li>
</axsl:if>
<axsl:apply-templates mode="M9"/>
</axsl:template>
<axsl:template mode="M9" priority="3996" match="foaf:aimChatID">
<axsl:if test="child::*">
<li>
<a title="Link to where this pattern was found" target="out" href="schematron-out.html#{generate-id(.)}">Literals should not have child elements
         ()
      </a>
</li>
</axsl:if>
<axsl:apply-templates mode="M9"/>
</axsl:template>
<axsl:template mode="M9" priority="3995" match="foaf:icqChatID">
<axsl:if test="child::*">
<li>
<a title="Link to where this pattern was found" target="out" href="schematron-out.html#{generate-id(.)}">Literals should not have child elements
         ()
      </a>
</li>
</axsl:if>
<axsl:apply-templates mode="M9"/>
</axsl:template>
<axsl:template mode="M9" priority="3994" match="foaf:yahooChatID">
<axsl:if test="child::*">
<li>
<a title="Link to where this pattern was found" target="out" href="schematron-out.html#{generate-id(.)}">Literals should nothave child elements
         ()
      </a>
</li>
</axsl:if>
<axsl:apply-templates mode="M9"/>
</axsl:template>
<axsl:template mode="M9" priority="3993" match="foaf:msnChatID">
<axsl:if test="child::*">
<li>
<a title="Link to where this pattern was found" target="out" href="schematron-out.html#{generate-id(.)}">Literals should not have child elements
         ()
      </a>
</li>
</axsl:if>
<axsl:apply-templates mode="M9"/>
</axsl:template>
<axsl:template mode="M9" priority="3992" match="foaf:name">
<axsl:if test="child::*">
<li>
<a title="Link to where this pattern was found" target="out" href="schematron-out.html#{generate-id(.)}">Literals should not have child elements
         ()
      </a>
</li>
</axsl:if>
<axsl:apply-templates mode="M9"/>
</axsl:template>
<axsl:template mode="M9" priority="3991" match="foaf:firstName">
<axsl:if test="child::*">
<li>
<a title="Link to where this pattern was found" target="out" href="schematron-out.html#{generate-id(.)}">Literals should not have child elements
         ()
      </a>
</li>
</axsl:if>
<axsl:apply-templates mode="M9"/>
</axsl:template>
<axsl:template mode="M9" priority="3990" match="foaf:surname">
<axsl:if test="child::*">
<li>
<a title="Link to where this pattern was found" target="out" href="schematron-out.html#{generate-id(.)}">Literals should not have child elements
         ()
      </a>
</li>
</axsl:if>
<axsl:apply-templates mode="M9"/>
</axsl:template>
<axsl:template mode="M9" priority="3989" match="foaf:family_name">
<axsl:if test="child::*">
<li>
<a title="Link to where this pattern was found" target="out" href="schematron-out.html#{generate-id(.)}">Literals should not have child elements
         ()
      </a>
</li>
</axsl:if>
<axsl:apply-templates mode="M9"/>
</axsl:template>
<axsl:template mode="M9" priority="3988" match="foaf:plan">
<axsl:if test="child::*">
<li>
<a title="Link to where this pattern was found" target="out" href="schematron-out.html#{generate-id(.)}">Literals should not have child elements
         ()
      </a>
</li>
</axsl:if>
<axsl:apply-templates mode="M9"/>
</axsl:template>
<axsl:template mode="M9" priority="3987" match="foaf:myersBriggs">
<axsl:if test="child::*">
<li>
<a title="Link to where this pattern was found" target="out" href="schematron-out.html#{generate-id(.)}">Literals should not have child elements
         ()
      </a>
</li>
</axsl:if>
<axsl:apply-templates mode="M9"/>
</axsl:template>
<axsl:template mode="M9" priority="3986" match="foaf:accountName">
<axsl:if test="child::*">
<li>
<a title="Link to where this pattern was found" target="out" href="schematron-out.html#{generate-id(.)}">Literals should not have child elements
         ()
      </a>
</li>
</axsl:if>
<axsl:apply-templates mode="M9"/>
</axsl:template>
<axsl:template mode="M9" priority="-1" match="text()"/>
<axsl:template mode="M10" priority="4000" match="rdf:RDF/foaf:Person">
<axsl:choose>
<axsl:when test="foaf:mbox or foaf:mbox_sha1sum or foaf:jabberID or foaf:aimChatID or foaf:icqChatID or foaf:yahooChatID or foaf:msnChatID or foaf:homepage or foaf:weblog"/>
<axsl:otherwise>
<li>
<a title="Link to where this pattern was expected" target="out" href="schematron-out.html#{generate-id(.)}">You should include at least one inverse functional property
         ()
      </a>
</li>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M10"/>
</axsl:template>
<axsl:template mode="M10" priority="3999" match="foaf:knows/foaf:Person">
<axsl:choose>
<axsl:when test="foaf:mbox or foaf:mbox_sha1sum or foaf:jabberID or foaf:aimChatID or foaf:icqChatID or foaf:yahooChatID or foaf:msnChatID or foaf:homepage or foaf:weblog"/>
<axsl:otherwise>
<li>
<a title="Link to where this pattern was expected" target="out" href="schematron-out.html#{generate-id(.)}">You should include at least one inverse functional property
         ()
      </a>
</li>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M10"/>
</axsl:template>
<axsl:template mode="M10" priority="-1" match="text()"/>
<axsl:template priority="-1" match="text()"/>
</axsl:stylesheet>
