<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fm="http://www.ldodds.com/ns/foaf-a-matic" xmlns:fmt="http://www.ldodds.com/ns/foaf-a-matic-template"
exclude-result-prefixes="fm fmt">

<xsl:variable name="template" select="document('../src/foaf-a-matic.xml')"/>
<xsl:variable name="input" select="/"/>

<xsl:output method="xml"  doctype-public="-//W3C//DTD XHTML 1.1//EN"
	doctype-system= "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"/>

<xsl:template match="/">
    <xsl:apply-templates select="$template/*"/>
</xsl:template>

<!-- templates to match elements in foaf-a-matic.xml -->

<!-- add messages Javascript -->
<xsl:template match="head">
   <head>
   <script language="javascript" type="text/javascript">
     <xsl:attribute name="src">js/<xsl:value-of select="$input/fm:translation/@lang"/>.js</xsl:attribute>
   </script>
   <xsl:apply-templates/>
   </head>
</xsl:template>

<!-- add an id element to body, in case we want to use CSS tabs -->
<xsl:template match="body">
    <body>
        <xsl:attribute name="id">body.<xsl:value-of select="$input/fm:translation/@lang"/></xsl:attribute>
        <xsl:apply-templates/>
    </body>
</xsl:template>

<xsl:template match="input[@type='button' or @type='checkbox' or @type='submit' or @type='reset']">
    <xsl:variable name="id" select="@id"/>
    <xsl:copy>
        <xsl:attribute name="value"><xsl:value-of select="$input//fm:button[@id = $id]/@title"/></xsl:attribute>
        <xsl:apply-templates select="@type|@onClick|@onSubmit|@name|@checked"/>
    </xsl:copy>
</xsl:template>

<xsl:template match="fmt:field">
    <xsl:variable name="id" select="@id"/>
    <xsl:value-of select="$input//fm:field[@id = $id]/@name"/>
</xsl:template>

<xsl:template match="fmt:by-line">
    <xsl:apply-templates select="$input//fm:by-line"/>
</xsl:template>

<xsl:template match="fmt:author">
    <xsl:apply-templates select="$input//fm:author"/>
</xsl:template>

<xsl:template match="fmt:translator">
    <xsl:apply-templates select="$input//fm:translator"/>
</xsl:template>

<xsl:template match="fmt:languages">
    <xsl:apply-templates select="$input//fm:languages"/>
</xsl:template>

<xsl:template match="fmt:introduction">
    <xsl:apply-templates select="$input//fm:introduction"/>
</xsl:template>

<xsl:template match="fmt:forms">
    <xsl:apply-templates select="$input//fm:forms"/>
</xsl:template>

<xsl:template match="fmt:personal">
    <xsl:apply-templates select="$input//fm:personal"/>
</xsl:template>

<xsl:template match="fmt:work">
    <xsl:apply-templates select="$input//fm:work"/>
</xsl:template>

<xsl:template match="fmt:school">
    <xsl:apply-templates select="$input//fm:school"/>
</xsl:template>

<xsl:template match="fmt:friends">
    <xsl:apply-templates select="$input//fm:friends"/>
</xsl:template>

<xsl:template match="fmt:results">
    <xsl:apply-templates select="$input//fm:results"/>
</xsl:template>

<xsl:template match="fmt:what-next">
    <xsl:apply-templates select="$input//fm:what-next"/>
</xsl:template>

<xsl:template match="fmt:license">
    <xsl:apply-templates select="$input//fm:license"/>
</xsl:template>


<!-- templates to match contents of foo.xml -->
<xsl:template match="fm:by-line|fm:author|fm:translator">
    <xsl:apply-templates/>
</xsl:template>

<xsl:template match="fm:introduction|fm:forms|fm:work|fm:school|fm:friends|fm:results|fm:what-next|fm:personal|fm:license">
    <a>
        <xsl:attribute name="name">
            <xsl:value-of select="local-name()"/>
        </xsl:attribute>
    </a>
    <h2><xsl:value-of select="@title"/></h2>
    <xsl:apply-templates/>
</xsl:template>

<!-- copy through -->
<xsl:template match="@*|node()">
    <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
</xsl:template>

</xsl:stylesheet>
