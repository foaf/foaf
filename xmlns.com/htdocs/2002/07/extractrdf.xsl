<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">

<!-- by Edd Dumbill; see http://groups.yahoo.com/group/rdfweb-dev/message/187 -->

<xsl:template match="@*|*|processing-instruction()|comment()">

<xsl:copy>
<xsl:apply-templates
select="*|@*|text()|processing-instruction()|comment()" />
</xsl:copy>
</xsl:template>

<xsl:template match='/'>
<xsl:apply-templates select='//rdf:RDF' />
</xsl:template>

</xsl:stylesheet>
