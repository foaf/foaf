<!DOCTYPE stylesheet [
  <!ENTITY tab "<xsl:text>&#9;</xsl:text>">
  <!ENTITY cr "<xsl:text>&#xD;</xsl:text>">
  <!ENTITY sp "<xsl:text> </xsl:text>">
]>

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fm="http://www.ldodds.com/ns/foaf-a-matic">

<xsl:output method="text"/>

<xsl:strip-space elements="*"/>

<xsl:template match="/">
   <xsl:apply-templates select="//fm:field"/>
   <xsl:apply-templates select="//fm:message"/>
</xsl:template>


<xsl:template match="fm:field">
var field_<xsl:value-of select="@id"/>='<xsl:value-of select="@name"/>';&cr;
</xsl:template>

<xsl:template match="fm:message">
var msg_<xsl:value-of select="@id"/>='<xsl:value-of select="normalize-space(.)"/>';&cr;
</xsl:template>  
</xsl:stylesheet>  