<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <!-- See: http://www.dpawson.co.uk/xsl/sect2/N7240.html#d7594e463 -->
    <xsl:template name="substring-after-last">
        <xsl:param name="input" />
        <xsl:param name="marker" />

        <xsl:choose>
          <xsl:when test="contains($input,$marker)">
            <xsl:call-template name="substring-after-last">
              <xsl:with-param name="input"
                  select="substring-after($input,$marker)" />
              <xsl:with-param name="marker" select="$marker" />
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
           <xsl:value-of select="$input" />
          </xsl:otherwise>
         </xsl:choose>

    </xsl:template>

</xsl:stylesheet>