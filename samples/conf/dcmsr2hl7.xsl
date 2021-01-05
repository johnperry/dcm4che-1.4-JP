<?xml version="1.0" encoding="UTF-8" ?>
<!-- $Id -->
<!--**************************************************************************
 *                                                                           *
 *  Copyright (c) 2001,2002 by TIANI MEDGRAPH AG                             *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 ***************************************************************************-->

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"/>

<xsl:param name="SendingApplication" select="'SendingApplication'"/>
<xsl:param name="SendingFacility" select="'SendingFacility'"/>
<xsl:param name="ReceivingApplication" select="'ReceivingApplication'"/>
<xsl:param name="ReceivingFacility" select="'ReceivingFacility'"/>
<xsl:param name="MessageControlID" select="'MessageControlID'"/>
<xsl:param name="IssuerOfPatientID" select="'IssuerOfPatientID'"/>
<xsl:param name="PatientAccountNumber" select="'PatientAccountNumber'"/>
<xsl:param name="UniversalServiceID" select="'UniversalServiceID'"/>
<xsl:param name="PlacerOrderNumber" select="'PlacerOrderNumber'"/>
<xsl:param name="FillerOrderNumber" select="'FillerOrderNumber'"/>


<xsl:template match="/">
    <xsl:apply-templates select="dicomfile/dataset"/>
</xsl:template>

<xsl:template match="dataset">
    <!-- MSH -->
    <xsl:text>MSH|^~\&amp;|</xsl:text>
    <xsl:value-of select="$SendingApplication"/>
    <xsl:text>|</xsl:text>
    <xsl:value-of select="$SendingFacility"/>
    <xsl:text>|</xsl:text>
    <xsl:value-of select="$ReceivingApplication"/>
    <xsl:text>|</xsl:text>
    <xsl:value-of select="$ReceivingFacility"/>
    <xsl:text>|ORU^R01|||</xsl:text>
    <xsl:value-of select="$MessageControlID"/>
    <xsl:text>|P|2.3.1|</xsl:text>
    <xsl:value-of select="normalize-space(elm[@tag='00080005']/val/@data)"/>
    <xsl:text>&#xd;</xsl:text>
    
    <!-- PID -->
    <xsl:text>PID|||</xsl:text>
    <xsl:value-of select="normalize-space(elm[@tag='00100020']/val/@data)"/>
    <xsl:text>^^^</xsl:text>
    <xsl:value-of select="$IssuerOfPatientID"/>
    <xsl:text>||</xsl:text>
    <xsl:value-of select="normalize-space(elm[@tag='00100010']/val/@data)"/>
    <xsl:text>||</xsl:text>
    <xsl:value-of select="normalize-space(elm[@tag='00100030']/val/@data)"/>
    <xsl:text>|</xsl:text>
    <xsl:value-of select="normalize-space(elm[@tag='00100040']/val/@data)"/>
    <xsl:text>||</xsl:text>
    <xsl:value-of select="normalize-space(elm[@tag='00102160']/val/@data)"/>
    <xsl:text>||||||||</xsl:text>
    <xsl:value-of select="$PatientAccountNumber"/>
    <xsl:text>&#xd;</xsl:text>
    
    <!-- OBR -->
    <xsl:text>OBR|1|</xsl:text>
    <xsl:value-of select="$PlacerOrderNumber"/>
    <xsl:text>|</xsl:text>
    <xsl:value-of select="$FillerOrderNumber"/>
    <xsl:text>|</xsl:text>
    <xsl:value-of select="$UniversalServiceID"/>
    <xsl:text>|||</xsl:text>
    <xsl:choose>
        <xsl:when test="elm[@tag='0040a032']">
            <xsl:value-of select="normalize-space(elm[@tag='0040a032']/val/@data)"/>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="normalize-space(elm[@tag='00080023']/val/@data)"/>
            <xsl:value-of select="normalize-space(elm[@tag='00080033']/val/@data)"/>
        </xsl:otherwise>    
    </xsl:choose>
    <xsl:text>||||||||||||||||||F|||||||</xsl:text>
    <!-- TODO Principal Results Interpreter
        Person Name value of the Content item that is related to the root of 
        the SR document with the relation ship HAS OBS CONTEXT and whose
        Concept Name Code is (121008,DCM,  Person Observer Name )    
      -->
    <xsl:text>&#xd;</xsl:text>
    
    <!-- OBX 1 -->
    <xsl:text>OBX|1|HD|^SR Instance UID||</xsl:text>
    <xsl:value-of select="normalize-space(elm[@tag='00080018']/val/@data)"/>
    <xsl:text>||||||F</xsl:text>
    <xsl:text>&#xd;</xsl:text>
    <xsl:variable name="images" select="//elm[@tag='0040a040' and val/@data='IMAGE ']"/>
    <xsl:apply-templates select="$images" mode="img"/>
    <xsl:apply-templates select="//elm[@tag='0040a040' and val/@data='TEXT']" mode="tx">
        <xsl:with-param name="setid" select="1 + 4 * count($images)"/>
    </xsl:apply-templates>
</xsl:template>

<xsl:template match="elm" mode="img">
    <xsl:variable name="setid" select="4 * position() - 2"/>
    <xsl:variable name="iuid" select="../elm[@tag='00081199']/seq/item/elm[@tag='00081155']/val/@data"/>
    <xsl:variable name="ref" select="//elm[@tag='00081115']/seq/item/elm[@tag='00081199']/seq/item/elm[@tag='00081155' and val/@data=$iuid]"/>
    
    <xsl:text>OBX|</xsl:text>
    <xsl:value-of select="$setid"/>
    <xsl:text>|HD|^Study Instance UID|</xsl:text>
    <xsl:value-of select="position()"/>
    <xsl:text>|</xsl:text>
    <xsl:value-of select="normalize-space($ref/../../../../../../../elm[@tag='0020000d']/val/@data)"/>  
    <xsl:text>||||||F</xsl:text>
    <xsl:text>&#xd;</xsl:text>

    <xsl:text>OBX|</xsl:text>
    <xsl:value-of select="$setid + 1"/>
    <xsl:text>|HD|^Series Instance UID|</xsl:text>
    <xsl:value-of select="position()"/>
    <xsl:text>|</xsl:text>
    <xsl:value-of select="normalize-space($ref/../../../../elm[@tag='0020000e']/val/@data)"/>  
    <xsl:text>||||||F</xsl:text>
    <xsl:text>&#xd;</xsl:text>

    <xsl:text>OBX|</xsl:text>
    <xsl:value-of select="$setid + 2"/>
    <xsl:text>|HD|^SOP Instance UID|</xsl:text>
    <xsl:value-of select="position()"/>
    <xsl:text>|</xsl:text>
    <xsl:value-of select="normalize-space($iuid)"/>  
    <xsl:text>||||||F</xsl:text>
    <xsl:text>&#xd;</xsl:text>
    
    <xsl:text>OBX|</xsl:text>
    <xsl:value-of select="$setid + 3"/>
    <xsl:text>|HD|^SOP Class UID|</xsl:text>
    <xsl:value-of select="position()"/>
    <xsl:text>|</xsl:text>
    <xsl:value-of select="normalize-space($ref/../elm[@tag='00081150']/val/@data)"/>  
    <xsl:text>||||||F</xsl:text>
    <xsl:text>&#xd;</xsl:text>
</xsl:template>

<xsl:template match="elm" mode="tx">
    <xsl:param name="setid" select="1"/>
    <xsl:text>OBX|</xsl:text>
    <xsl:value-of select="$setid + position()"/>
    <xsl:text>|TX|^SR Text||</xsl:text>
    <xsl:value-of select="normalize-space(../elm[@tag='0040a160']/val/@data)"/>  
    <xsl:text>||||||F</xsl:text>
    <xsl:text>&#xd;</xsl:text>
</xsl:template>

</xsl:stylesheet>