<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:preserve-space elements="*"/>

  <xsl:output method="html"/>

  <xsl:template match="/">

      <div style="font-size: small">
        <pre><xsl:apply-templates/></pre>
      </div>
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

  <!-- #xa is \n -->

  <xsl:template match="@*">
    <span class="{namespace-uri()}">
      <xsl:value-of select="concat(' &#xa;      ',name(),'=&quot;',.,'&quot;')"/>
    </span>
  </xsl:template>

  <xsl:template match="comment()">
    <xsl:value-of select="concat('&lt;!--',.,'-->')"/>
  </xsl:template>

  <xsl:template match="processing-instruction()">
    <xsl:value-of select="concat('&lt;?',.,'?>')"/>
  </xsl:template>

</xsl:stylesheet>
