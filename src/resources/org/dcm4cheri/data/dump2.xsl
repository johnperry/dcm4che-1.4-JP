<?xml version="1.0" encoding="UTF-8"?>
<!-- $Id -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="text"/>
  <xsl:param name="maxlen" select="79"/>
  <xsl:param name="vallen" select="64"/>
  <xsl:param name="valtail" select="8"/>
  <xsl:param name="ellipsis" select="'...'"/>
  <xsl:param name="prefix" select="''"/>
  <xsl:template match="/">
    <xsl:apply-templates select="dataset"/>
  </xsl:template>
  <xsl:template match="dataset">
    <xsl:apply-templates select="attr">
      <xsl:with-param name="level" select="$prefix"/>
    </xsl:apply-templates>
  </xsl:template>
  <xsl:template match="attr">
    <xsl:param name="level"/>
    <xsl:variable name="line">
      <xsl:value-of select="format-number(@pos,'0000 ')"/>
      <xsl:value-of select="$level"/>
      <xsl:text>(</xsl:text>
      <xsl:value-of select="substring(@tag,1,4)"/>
      <xsl:text>,</xsl:text>
      <xsl:value-of select="substring(@tag,5,4)"/>
      <xsl:text>) </xsl:text>
      <xsl:value-of select="@vr"/>
      <xsl:text> #</xsl:text>
      <xsl:value-of select="@len"/>
      <xsl:if test="not(item)">
        <xsl:text> *</xsl:text>
        <xsl:value-of select="@vm"/>
        <xsl:call-template name="promptValue">
          <xsl:with-param name="val" select="text()"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:variable>
    <xsl:call-template name="promptLine">
      <xsl:with-param name="line" select="$line"/>
      <xsl:with-param name="name" select="@name"/>
    </xsl:call-template>
    <xsl:apply-templates select="item">
      <xsl:with-param name="level" select="concat($level,'&gt;')"/>
    </xsl:apply-templates>
    <xsl:if test="@len=-1">
      <xsl:call-template name="promptLine">
        <xsl:with-param name="line" select="concat('     ',$level,'(fffe,e0dd)    #0')"/>
        <xsl:with-param name="name" select="'Sequence Delimitation Item'"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  <xsl:template name="promptValue">
    <xsl:param name="val"/>
    <xsl:variable name="dataLen" select="string-length($val)"/>
    <xsl:text> [</xsl:text>
    <xsl:choose>
      <xsl:when test="$dataLen &gt; $vallen">
        <xsl:value-of select="substring($val,1,$vallen - $valtail - string-length($ellipsis))"/>
        <xsl:value-of select="$ellipsis"/>
        <xsl:value-of select="substring($val,1 + $dataLen - $valtail)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$val"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>]</xsl:text>
  </xsl:template>
  <xsl:template name="promptLine">
    <xsl:param name="line"/>
    <xsl:param name="name"/>
    <xsl:variable name="prompt" select="concat($line,' //',$name)"/>
    <xsl:choose>
      <xsl:when test="string-length($prompt) &gt; $maxlen">
        <xsl:value-of select="substring($prompt,1,$maxlen)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$prompt"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>
</xsl:text>
  </xsl:template>
  <xsl:template match="item">
    <xsl:param name="level"/>
    <xsl:variable name="line">
      <xsl:value-of select="format-number(@pos,'0000 ')"/>
      <xsl:value-of select="$level"/>
      <xsl:text>(fffe,e000)    #</xsl:text>
      <xsl:value-of select="@len"/>
      <xsl:if test="not(attr)">
        <xsl:call-template name="promptValue">
          <xsl:with-param name="val" select="text()"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:variable>
    <xsl:call-template name="promptLine">
      <xsl:with-param name="line" select="$line"/>
      <xsl:with-param name="name" select="concat('Item-',@id)"/>
    </xsl:call-template>
    <xsl:apply-templates select="attr">
      <xsl:with-param name="level" select="$level"/>
    </xsl:apply-templates>
    <xsl:if test="@len = -1">
      <xsl:call-template name="promptLine">
        <xsl:with-param name="line" select="concat('     ',$level,'(fffe,e00d)    #0')"/>
        <xsl:with-param name="name" select="'Item Delimitation Item'"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
