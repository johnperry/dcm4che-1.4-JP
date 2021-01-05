<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" version="1.0" encoding="UTF-8"/>
    
    <xsl:template match="/">
        <dicomfile>
          <xsl:apply-templates select="dicomfile/dataset"/>
        </dicomfile>
    </xsl:template>

    <xsl:template match="dataset">
      <Dimension
        rows="{elm[@tag='00280010']/val/@data}"
        columns="{elm[@tag='00280011']/val/@data}"
      />

      <xsl:variable name="center" select="elm[@tag='00281050']/val/@data"/>      
      <xsl:variable name="width" select="elm[@tag='00281051']/val/@data"/>

      <xsl:variable name="intercept" select="elm[@tag='00281052']/val/@data"/>
      <xsl:variable name="intercept0">
        <xsl:choose>
          <xsl:when test="$intercept">
            <xsl:value-of select="$intercept"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="'0'"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      
      <xsl:variable name="slope" select="elm[@tag='00281053']/val/@data"/>
      <xsl:variable name="slope1">
        <xsl:choose>
          <xsl:when test="$slope">
            <xsl:value-of select="$slope"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="'1'"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      
      <xsl:if test="$center and $width"> 
          <Window intercept="{$intercept0}"
                  slope="{$slope1}"
                  width="{$width}"
                  center="{$center}"/>
      </xsl:if>
    </xsl:template>    

</xsl:stylesheet> 