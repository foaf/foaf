<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<axsl:stylesheet xmlns:axsl="http://www.w3.org/1999/XSL/Transform" xmlns:sch="http://www.ascc.net/xml/schematron" version="1.0" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" rdf:dummy-for-xmlns="" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" rdfs:dummy-for-xmlns="" xmlns:foaf="http://xmlns.com/foaf/0.1/" foaf:dummy-for-xmlns="" xmlns:dc="http://purl.org/dc/elements/1.1/" dc:dummy-for-xmlns="">
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
<h1 title=" ">FOAF "Classic" Validator</h1>
<div class="errors">
<ul>
<h3>Namespace Checking</h3>
<axsl:apply-templates mode="M5" select="/"/>
<h3>Basic Checks</h3>
<axsl:apply-templates mode="M6" select="/"/>
<h3>RDF Validation</h3>
<axsl:apply-templates mode="M7" select="/"/>
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
<axsl:template mode="M5" priority="4000" match="*">
<axsl:choose>
<axsl:when test="namespace-uri()='http://www.w3.org/1999/02/22-rdf-syntax-ns#' or                     namespace-uri()='http://www.w3.org/2000/01/rdf-schema#' or                     namespace-uri()='http://xmlns.com/foaf/0.1/' or                     namespace-uri()='http://purl.org/dc/elements/1.1/'"/>
<axsl:otherwise>
<li>
<a title="Link to where this pattern was expected" target="out" href="schematron-out.html#{generate-id(.)}">
<i/>Element<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>is from an unknown namespace.<b>This validator only checks elements from the RDF, RDFS, FOAF, and DC namespaces. Either the FOAF file includes elements from another namespace (which will not be validated by this schema) or the namespace declarations are incorrect.</b>
</a>
</li>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M5"/>
</axsl:template>
<axsl:template mode="M5" priority="-1" match="text()"/>
<axsl:template mode="M6" priority="4000" match="/">
<axsl:choose>
<axsl:when test="rdf:RDF"/>
<axsl:otherwise>
<li>
<a title="Link to where this pattern was expected" target="out" href="schematron-out.html#{generate-id(.)}">
<i/>Root element must be rdf:RDF<b/>
</a>
</li>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M6"/>
</axsl:template>
<axsl:template mode="M6" priority="3999" match="rdf:RDF">
<axsl:choose>
<axsl:when test="foaf:Person"/>
<axsl:otherwise>
<li>
<a title="Link to where this pattern was expected" target="out" href="schematron-out.html#{generate-id(.)}">
<i/>The root rdf:RDF element must contain a foaf:Person element<b>FOAF Classic restricts a document to containing a single "root" foaf:Person element. This is the main or primary person in the document.</b>
</a>
</li>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="count(foaf:Person) = 1"/>
<axsl:otherwise>
<li>
<a title="Link to where this pattern was expected" target="out" href="schematron-out.html#{generate-id(.)}">
<i/>There should only be a single root foaf:Person<b>FOAF Classic restricts a document to containing a single "root" foaf:Person element. This is the main or primary person in the document.</b>
</a>
</li>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M6"/>
</axsl:template>
<axsl:template mode="M6" priority="3998" match="rdf:RDF/foaf:Person">
<axsl:choose>
<axsl:when test="foaf:mbox or foaf:mbox_sha1sum"/>
<axsl:otherwise>
<li>
<a title="Link to where this pattern was expected" target="out" href="schematron-out.html#{generate-id(.)}">
<i/>You should include an email address, preferably a encrypted one using foaf:mbox_sha1sum<b/>
</a>
</li>
</axsl:otherwise>
</axsl:choose>
<axsl:if test="foaf:mbox">
<li>
<a title="Link to where this pattern was found" target="out" href="schematron-out.html#{generate-id(.)}">
<i/>Warning plain text email found<b>Including plain-text emails in a FOAF document opens the possibility that the email can be used for spamming and other nefarious uses.</b>
</a>
</li>
</axsl:if>
<axsl:apply-templates mode="M6"/>
</axsl:template>
<axsl:template mode="M6" priority="3997" match="foaf:knows/foaf:Person">
<axsl:if test="foaf:mbox">
<li>
<a title="Link to where this pattern was found" target="out" href="schematron-out.html#{generate-id(.)}">
<i/>Friends should never have a plain-text specified in a FOAF Classic document<b>Including plain-text emails in a FOAF document opens the possibility that the email can be used for spamming and other nefarious uses.</b>
</a>
</li>
</axsl:if>
<axsl:apply-templates mode="M6"/>
</axsl:template>
<axsl:template mode="M6" priority="-1" match="text()"/>
<axsl:template mode="M7" priority="3999" match="rdfs:seeAlso|foaf:homepage|foaf:weblog|foaf:depiction|foaf:workplaceHomepage|foaf:schoolHomepage">
<axsl:apply-templates mode="M7"/>
</axsl:template>
<axsl:template mode="M7" priority="3998" match="foaf:mbox">
<axsl:choose>
<axsl:when test="starts-with(@rdf:resource, 'mailto:')"/>
<axsl:otherwise>
<li>
<a title="Link to where this pattern was expected" target="out" href="schematron-out.html#{generate-id(.)}">
<i/>Mail-boxes must be specified as URIs<b>RDF Resources are always URIs. E.g. http://, mailto:, tel:, etc.</b>
</a>
</li>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M7"/>
</axsl:template>
<axsl:template mode="M7" priority="-1" match="text()"/>
<axsl:template priority="-1" match="text()"/>
</axsl:stylesheet>
