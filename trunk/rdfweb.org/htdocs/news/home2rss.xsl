<!-- $Id: home2rss.xsl,v 1.1 2002-08-19 03:36:55 danbri Exp $ -->
<xsl:transform 
    xmlns:xsl  ="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:rdf  ="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns      ="http://purl.org/rss/1.0/"
    xmlns:dc   ="http://purl.org/dc/elements/1.1/" 
    xmlns:h    ="http://www.w3.org/1999/xhtml"
    xmlns:uri  ="http://www.w3.org/2000/07/uri43/uri.xsl?template="
    xmlns:hr   ="http://www.w3.org/2000/08/w3c-synd/#"
    exclude-result-prefixes="uri"
    >

 <!-- xalan doesn't grok relative URIs in imports. Sigh.
<xsl:import href="../../07/uri43/uri.xsl"/>
-->

<xsl:import href="http://www.w3.org/2000/07/uri43/uri.xsl"/>
<xsl:param name="Base"/>
<xsl:param name="Channel"/>
<xsl:param name="Page"/>

<xsl:variable name="Profile" select='"http://www.w3.org/2000/08/w3c-synd/#"'/>


<xsl:output method="xml" indent="yes" encoding="us-ascii"/>
 <!-- @@ encoding="us-ascii" is not honored by XT;
      per http://www.jclark.com/xml/xt.html
	Version 19991105 -->
<div xmlns="http://www.w3.org/1999/xhtml">

<div>
<h2>TODO</h2>
<ul>
<li>add pointer to RDF/RSS version from home page</li>
<li>finish upgrading this to <a 
href="http://www.egroups.com/files/rss-dev/RC1/specification.html">
RC1</a> version (danbri)</li>
</ul>
</div>

<div>
<h2>Share and Enjoy</h2>

<p>Copyright (c) 2000 W3C (MIT, INRIA, Keio), released under the <a
href="http://www.w3.org/Consortium/Legal/copyright-software-19980720">
W3C Open Source License</a> of August 14 1998.  </p>
</div>

<address><a href="../../../People/Connolly/">Dan Connolly</a>, Aug 2000<br />
ammended by <a href="mailto:danbri@w3.org">Dan Brickley</a>, Nov 2000 <br />
$Revision: 1.1 $ of $Date: 2002-08-19 03:36:55 $ by $Author: danbri $
</address>
</div>

<xsl:template match='h:html
	      [h:head/@profile="http://www.w3.org/2000/08/w3c-synd/#"]'>
  <xsl:processing-instruction name="xml-stylesheet">href="http://www.w3.org/2000/08/w3c-synd/style.css" type="text/css"</xsl:processing-instruction>
  <rdf:RDF>
    <xsl:variable name="RCSDate">
	<xsl:value-of
	 select = 'normalize-space(substring-before(substring-after(
					h:body/h:address,
					concat("$", "Date:")), "$"))'/>
    </xsl:variable>

    <!-- convert yyyy/mm/dd hh:mm:ss to yyyy-mm-ddThh:mm:ssZ -->
    <xsl:variable name="ISODate">
	<xsl:value-of
	 select = 'concat(translate($RCSDate, "/ ", "-T"), "Z")'/>
    </xsl:variable>

    <xsl:message>@@ date
	rcsdate: <xsl:value-of select="$RCSDate"/>
	isodate: <xsl:value-of select="$ISODate"/>
    </xsl:message>

    <channel rdf:about="{$Channel}">
      <title><xsl:value-of select="h:head/h:title"/></title>
      <description>
	<xsl:value-of select='h:body/h:h2[@id="slogan"]'/>
      </description>
      <link><xsl:value-of select="$Page"/></link> <!-- for RSS 0.9x consumers -->
      <!-- @@hmm... we're taking the RCS date of the HTML page
	as the dublin core date of the channel. Is that a good match? -->
      <dc:date><xsl:value-of select="$ISODate"/></dc:date>


      <items>
       <rdf:Seq>

        <!-- RSS now uses a table of contents list for channel order -->
        <xsl:for-each select='.//h:div[@class="item"]'>
          <xsl:variable name="itemURI">
  	    <xsl:call-template name="uri:expand">
	      <xsl:with-param name="there"
		      select='.//h:a[@rel="details"]/@href'/>
              <xsl:with-param name="base" select="$Base"/>
	    </xsl:call-template>
	   </xsl:variable>
        <rdf:li rdf:resource="{$itemURI}"/>
      </xsl:for-each>


       </rdf:Seq>
      </items>
    </channel>

      <xsl:for-each select='.//h:div[@class="item"]'>
        <xsl:variable name="itemURI">
	  <xsl:call-template name="uri:expand">
	    <xsl:with-param name="there"
		      select='.//h:a[@rel="details"]/@href'/>
            <xsl:with-param name="base" select="$Base"/>
	  </xsl:call-template>
	</xsl:variable>

        <item rdf:about="{$itemURI}">
	  <title><xsl:value-of select='normalize-space(h:h2)'/></title>
	  <description><xsl:value-of select='normalize-space(h:p)'/>
	  </description>
	  <link><xsl:value-of select="$itemURI"/></link>
	  <xsl:variable name="dateElt" select='h:p/*[@class="date"]'/>
	  <xsl:if test='$dateElt'>
	    <dc:date>
	      <xsl:call-template name="hr:format-date">
	        <xsl:with-param name="DDMonthYYYY" select="$dateElt"/>
	      </xsl:call-template>
	    </dc:date>
	  </xsl:if>
	</item>
      </xsl:for-each>
  </rdf:RDF>

</xsl:template>


<xsl:template name="hr:format-date">
  <xsl:param name="DDMonthYYYY"/>

  <xsl:variable name="DD" select='format-number(number(substring-before($DDMonthYYYY, " ")), "00")'/>
  <xsl:variable name="YYYY" select='format-number(number(substring-after(substring-after($DDMonthYYYY, " "), " ")), "0000")'/>
  <xsl:variable name="month" select='substring(substring-before(substring-after($DDMonthYYYY, " "), " "), 1, 3)'/>
  <xsl:variable name="m">
    <xsl:choose>
      <xsl:when test="$month='Jan'"><xsl:value-of select="1"/></xsl:when>
      <xsl:when test="$month='Feb'"><xsl:value-of select="2"/></xsl:when>
      <xsl:when test="$month='Mar'"><xsl:value-of select="3"/></xsl:when>
      <xsl:when test="$month='Apr'"><xsl:value-of select="4"/></xsl:when>
      <xsl:when test="$month='May'"><xsl:value-of select="5"/></xsl:when>
      <xsl:when test="$month='Jun'"><xsl:value-of select="6"/></xsl:when>
      <xsl:when test="$month='Jul'"><xsl:value-of select="7"/></xsl:when>
      <xsl:when test="$month='Aug'"><xsl:value-of select="8"/></xsl:when>
      <xsl:when test="$month='Sep'"><xsl:value-of select="9"/></xsl:when>
      <xsl:when test="$month='Oct'"><xsl:value-of select="10"/></xsl:when>
      <xsl:when test="$month='Nov'"><xsl:value-of select="11"/></xsl:when>
      <xsl:when test="$month='Dec'"><xsl:value-of select="12"/></xsl:when>
    </xsl:choose>
  </xsl:variable>   
  <xsl:variable name="MM" select='format-number($m, "00")'/>

  <xsl:value-of select='concat($YYYY, "-", format-number($MM, "00"), "-", $DD)'/>
</xsl:template>

<!-- don't pass text thru -->
<xsl:template match="text()|@*">
</xsl:template>

</xsl:transform>
