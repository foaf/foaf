<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
         xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
         xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
         xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
         xmlns:owl="http://www.w3.org/2002/07/owl#"
         xmlns:dc="http://purl.org/dc/elements/1.1/">

   <xsl:output method="xml" indent="yes"/>
   <xsl:include href="utils.xsl"/>

   <xsl:variable name="schema" select="document('../schema.rdf')"/>
   
   <xsl:template match="schema">
      <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates/>
            <xsl:apply-templates select="$schema/rdf:RDF" mode="schema"/>   
      </xsl:copy>
   </xsl:template>

   <xsl:template match="rdf:RDF" mode="schema">
         <xsl:comment>Begin Auto-Generated Block: Literals</xsl:comment>
         
         <pattern name="Content Model (Literals)">
         <xsl:for-each select="//rdf:Property">
            <xsl:variable name="name">
               <xsl:call-template name="substring-after-last">
                  <xsl:with-param name="input" select="@rdf:about"/>
                  <xsl:with-param name="marker" select="'/'"/>
               </xsl:call-template>
            </xsl:variable>
            <xsl:if test="rdfs:range/@rdf:resource='http://www.w3.org/2000/01/rdf-schema#Literal'">
            <rule context="foaf:{$name}">
               <report test="child::*">Literals should not have child elements</report>
            </rule>
            </xsl:if>
         </xsl:for-each>
         </pattern>
         <xsl:comment>End Auto-Generated Block: Literals</xsl:comment>
   </xsl:template>
   
   <xsl:template match="node()|@*">
      <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
      </xsl:copy>
    </xsl:template>

       
</xsl:stylesheet>