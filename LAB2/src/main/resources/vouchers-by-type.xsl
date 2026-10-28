<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xalan="http://xml.apache.org/xslt"
                exclude-result-prefixes="xalan">

    <xsl:output method="xml" encoding="UTF-8" indent="yes"
                xalan:indent-amount="4"/>
    <xsl:strip-space elements="*"/>

    <!-- Групування Мюнха: перша путівка кожного типу відкриває групу -->
    <xsl:key name="by-type" match="touristVoucher" use="@type"/>

    <xsl:template match="/touristVouchers">
        <xsl:text>&#10;</xsl:text>
        <touristVouchers>
            <xsl:for-each select="touristVoucher[generate-id()
                    = generate-id(key('by-type', @type)[1])]">
                <xsl:sort select="@type"/>
                <xsl:element name="{@type}">
                    <xsl:apply-templates select="key('by-type', @type)"/>
                </xsl:element>
            </xsl:for-each>
        </touristVouchers>
    </xsl:template>

    <!-- Копіює елемент без успадкованих оголошень просторів імен -->
    <xsl:template match="*">
        <xsl:element name="{name()}">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>
