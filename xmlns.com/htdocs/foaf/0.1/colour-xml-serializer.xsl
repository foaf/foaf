<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:preserve-space elements="*"/>

  <xsl:output method="html"/>

  <xsl:template match="/">
    <html>
      <head>
        <style type="text/css">
          .http\:\/\/www\.w3\.org\/1999\/02\/22-rdf-syntax-ns\# { color: green }
	  .http\:\/\/www\.w3\.org\/2000\/01\/rdf-schema# { color: red }
          .http\:\/\/www\.w3\.org\/2002\/07\/owl# { color: purple }
          .http\:\/\/xmlns\.com\/foaf\/0.1\/ {color: blue }
          .http\:\/\/purl\.org\/dc\/elements\/1\.1\/ {color: yellow }
          .http\:\/\/purl\.org\/rss\/1\.0\/modules\/syndication\/ {color: red }
          .http\:\/\/www\.w3\.org\/2003\/06\/sw-vocab-status\/ns# { color: orange }
        </style>
      </head>
      <body bgcolor="#FFFFFF">
        <pre><xsl:apply-templates/></pre>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="*">
    <span class="{namespace-uri()}"><xsl:value-of select="concat('&lt;',name())"/></span>
    <xsl:if test="@*">
      <span class="{namespace-uri()}"><xsl:apply-templates select="@*"/></span>        
    </xsl:if>
    <span class="{namespace-uri()}">
      <xsl:text>></xsl:text>
      <xsl:apply-templates select="node()"/>
      <xsl:value-of select="concat('&lt;/',name(),'&gt;')"/>
    </span>
  </xsl:template>

  <xsl:template match="@*">
    <xsl:value-of select="concat(' ',name(),'&quot;',.,'&quot;')"/>
  </xsl:template>

  <xsl:template match="comment()">
    <xsl:value-of select="concat('&lt;--',.,'-->')"/>
  </xsl:template>

  <xsl:template match="processing-instruction()">
    <xsl:value-of select="concat('&lt;?',.,'?>')"/>
  </xsl:template>

</xsl:stylesheet>
