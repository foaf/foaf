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
   
   <xsl:template match="pattern[@name='Basic Validation']">
      <xsl:copy>
         <xsl:apply-templates select="@*"/>
         <xsl:apply-templates/>
         <xsl:comment>Begin Auto-Generated Block: Legal Elements</xsl:comment>

         <xsl:apply-templates select="$schema/rdf:RDF" mode="schema1"/>   

         <xsl:apply-templates select="$schema/rdf:RDF" mode="schema2"/>   
         
         <xsl:comment>End Auto-Generated Block: Legal Elements</xsl:comment>
         
      </xsl:copy>
   </xsl:template>
   
   <xsl:template match="rdf:RDF" mode="schema1">
      <xsl:variable name="qnames">
         <xsl:for-each select="//rdf:Property[rdfs:domain/@rdf:resource='http://xmlns.com/foaf/0.1/Person'
                                              or rdfs:domain/@rdf:resource='http://xmlns.com/foaf/0.1/Agent'
                                              or rdfs:domain/@rdf:resource='http://www.w3.org/2000/01/rdf-schema#Resource']">
            <xsl:variable name="name">
               <xsl:call-template name="substring-after-last">
                  <xsl:with-param name="input" select="@rdf:about"/>
                  <xsl:with-param name="marker" select="'/'"/>
               </xsl:call-template>
            </xsl:variable>
            
            <xsl:value-of select="concat('foaf:', $name)"/>
             <xsl:if test="position() != last()">
               <xsl:text> | </xsl:text>
             </xsl:if>       
               
         </xsl:for-each>
      </xsl:variable>

      <rule context="{$qnames}"/>            
   </xsl:template>

   <xsl:template match="rdf:RDF" mode="schema2">
      <xsl:variable name="qnames">
         <xsl:for-each select="//rdf:Property[rdfs:domain/@rdf:resource != 'http://xmlns.com/foaf/0.1/Person'
                                              and rdfs:domain/@rdf:resource !='http://xmlns.com/foaf/0.1/Agent'
                                              and rdfs:domain/@rdf:resource !='http://www.w3.org/2000/01/rdf-schema#Resource']">
            <xsl:variable name="name">
               <xsl:call-template name="substring-after-last">
                  <xsl:with-param name="input" select="@rdf:about"/>
                  <xsl:with-param name="marker" select="'/'"/>
               </xsl:call-template>
            </xsl:variable>
            
            <xsl:value-of select="concat('foaf:', $name)"/>
             <xsl:if test="position() != last()">
               <xsl:text> | </xsl:text>
             </xsl:if>       
               
         </xsl:for-each>
      </xsl:variable>

      <rule context="{$qnames}">
         <report test="true()">This element is not allowed in FOAF Minimal</report>
      </rule>
   </xsl:template>
   
   <xsl:template match="node()|@*">
      <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
      </xsl:copy>
    </xsl:template>

       
</xsl:stylesheet>