<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE xsl:stylesheet [
  <!ENTITY rdfnsuri "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
]>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:rdf="&rdfnsuri;"
  version="1.0">
  
  <xsl:output indent="yes"/>

  <xsl:strip-space elements="*"/>

  <xsl:template match="/">
    <xsl:apply-templates select="rdf:RDF"/>
  </xsl:template>

  <xsl:template match="rdf:RDF">
    <rdf:RDF>
      <xsl:copy-of select="@*"/>
      <xsl:for-each select="*">
        <xsl:choose>
          <xsl:when test="self::rdf:Description">
            <xsl:apply-templates select="." mode="node"/>
          </xsl:when>
          <xsl:otherwise>
            <rdf:Description rdf:about="{@rdf:about}">
              <rdf:type rdf:resource="{namespace-uri()}{local-name()}"/>
              <xsl:apply-templates select="*" mode="arc"/>
            </rdf:Description>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </rdf:RDF>
  </xsl:template>

<!-- ##################################################################### -->

<xsl:template match="*" mode="arc">
  <xsl:copy>
    <!-- copy rdf attributes -->
    <xsl:copy-of select="@rdf:*"/>
    
    <!-- checking for namespaceless attributes -->
    <xsl:if test="@resource or @ID">
      <xsl:message>Warning: rdf attribute used without prefix. Assuming rdf namespace</xsl:message>
    </xsl:if>
    <xsl:if test="@resource">
      <xsl:attribute name="rdf:resource">
        <xsl:value-of select="@resource"/>
      </xsl:attribute>
    </xsl:if>
    <xsl:if test="@ID">
      <xsl:attribute name="rdf:ID">
        <xsl:value-of select="@ID"/>
      </xsl:attribute>
    </xsl:if>
    
    <!-- handle non-rdf attributes, make them children -->
    <xsl:for-each select="@*[namespace-uri()!='&rdfnsuri;']">
      <rdf:Description>
        <xsl:element name="{local-name()}" namespace="{namespace-uri()}">
          <xsl:value-of select="."/>
        </xsl:element>
      </rdf:Description>
    </xsl:for-each>
      
      
      <!-- child nodes -->
      <xsl:choose>
        <xsl:when test="*"> 
        <!-- if we have child elements -->
        <xsl:choose>
          <xsl:when test="namespace-uri(*[1])!='&rdfnsuri;'">
            <!-- children aren't rdf:Description, wrap one around them -->
            <rdf:Description>
              <xsl:apply-templates select="*" mode="arc"/>
            </rdf:Description>
          </xsl:when>
          <xsl:otherwise>
            <!-- child is rdf:Description. Proceed -->
            <xsl:apply-templates select="." mode="node"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates/>
      </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

<!-- ##################################################################### -->

  <xsl:template match="rdf:*" mode="node">
    <rdf:Description>
      <rdf:type rdf:resource="&rdfnsuri;{local-name()}"/>
      <xsl:apply-templates mode="arc"/>
    </rdf:Description>
  </xsl:template>


  <xsl:template match="rdf:Description" mode="node">
    <xsl:copy>
      <xsl:for-each select="@*">
        <xsl:choose>
          <xsl:when test="name()='about' or name()='resource' or name()='type' or name='ID' or name='value'">
            <xsl:message>Warning: rdf attribute used without prefix. Assuming rdf namespace</xsl:message>
            <xsl:attribute name="{local-name()}" namespace="&rdfnsuri;">
              <xsl:value-of select="."/>
            </xsl:attribute>
          </xsl:when>
          <xsl:when test="namespace-uri() = '&rdfnsuri;'">
            <xsl:copy>
              <xsl:apply-templates/>
            </xsl:copy>
          </xsl:when>
          <xsl:otherwise>
            <xsl:element name="{local-name()}" namespace="{namespace-uri()}">
              <xsl:value-of select="."/>
            </xsl:element>
            <xsl:apply-templates/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
      <xsl:apply-templates select="*" mode="arc"/>
    </xsl:copy>
  </xsl:template>

<!-- ##################################################################### -->

</xsl:stylesheet>
