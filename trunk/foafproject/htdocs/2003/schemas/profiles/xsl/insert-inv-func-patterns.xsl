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
         <xsl:comment>Begin Auto-Generated Block: Inverse Functional Properties</xsl:comment>
         
         <pattern name="Identifying Properties">
            <rule context="rdf:RDF/foaf:Person">
               <xsl:apply-templates select="$schema/rdf:RDF" mode="schema"/>   
            </rule> 
            <rule context="foaf:knows/foaf:Person">
               <xsl:apply-templates select="$schema/rdf:RDF" mode="schema"/>   
            </rule>            
         </pattern>         
         <xsl:comment>End Auto-Generated Block: Inverse Functional Properties</xsl:comment>
         
      </xsl:copy>
   </xsl:template>

   <xsl:template match="diagnostics">
      <xsl:copy>
         <xsl:apply-templates select="@*"/>
         <xsl:apply-templates/>

         <diagnostic id="identifying-properties">
         "Inverse Functional Properties" uniquely identify a foaf:Person and are used to collate 
         statements made about that person from multiple sources.
         </diagnostic>
      </xsl:copy>
   </xsl:template>
   
   <xsl:template match="rdf:RDF" mode="schema">
      <xsl:variable name="qnames">
         <xsl:for-each select="//rdf:Property[rdf:type/@rdf:resource='http://www.w3.org/2002/07/owl#InverseFunctionalProperty'
                                              and (rdfs:domain/@rdf:resource='http://xmlns.com/foaf/0.1/Person' or
                             rdfs:domain/@rdf:resource='http://xmlns.com/foaf/0.1/Agent')]">
            <xsl:variable name="name">
               <xsl:call-template name="substring-after-last">
                  <xsl:with-param name="input" select="@rdf:about"/>
                  <xsl:with-param name="marker" select="'/'"/>
               </xsl:call-template>
            </xsl:variable>
            
            <xsl:if test="rdf:type/@rdf:resource='http://www.w3.org/2002/07/owl#InverseFunctionalProperty'">
               <xsl:if test="rdfs:domain/@rdf:resource='http://xmlns.com/foaf/0.1/Person' or
                             rdfs:domain/@rdf:resource='http://xmlns.com/foaf/0.1/Agent'">
                  <xsl:value-of select="concat('foaf:', $name)"/>
                   <xsl:if test="position() != last()">
                     <xsl:text> or </xsl:text>
                   </xsl:if>                  
               </xsl:if>
            </xsl:if>
         </xsl:for-each>
   
      </xsl:variable>
      
      <assert test="{$qnames}" diagnostics="identifying-properties">You should include at least one inverse functional property</assert>
   </xsl:template>
   
   <xsl:template match="node()|@*">
      <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
      </xsl:copy>
    </xsl:template>

       
</xsl:stylesheet>