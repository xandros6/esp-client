<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.0.0" xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:sld="http://www.opengis.net/sld"
  xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd">
  <NamedLayer>
    <Name>${styleName}</Name>
    <UserStyle>
      <Name>${styleName}</Name>
      <Title>${styleName}</Title>
      <Abstract>${styleName}</Abstract>
      <FeatureTypeStyle>
        <#list colourMapEntries as colourMapEntry>
	        <sld:Rule>
	          <ogc:Filter>
	          <ogc:And>
	          <#if colourMapEntry_has_next>
	            
	              <ogc:PropertyIsGreaterThanOrEqualTo>
	                <ogc:PropertyName>${attributeName}</ogc:PropertyName>
	                <ogc:Literal>${colourMapEntry.getFrom()?c}</ogc:Literal>
	              </ogc:PropertyIsGreaterThanOrEqualTo>
	              <ogc:PropertyIsLessThan>
	                <ogc:PropertyName>${attributeName}</ogc:PropertyName>
	                <ogc:Literal>${colourMapEntry.getTo()?c}</ogc:Literal>
	              </ogc:PropertyIsLessThan>
	            
	          <#else>
	               <ogc:PropertyIsGreaterThanOrEqualTo>
                    <ogc:PropertyName>${attributeName}</ogc:PropertyName>
                    <ogc:Literal>${colourMapEntry.getFrom()?c}</ogc:Literal>
                  </ogc:PropertyIsGreaterThanOrEqualTo>
                  <ogc:PropertyIsLessThanOrEqualTo>
                    <ogc:PropertyName>${attributeName}</ogc:PropertyName>
                    <ogc:Literal>${colourMapEntry.getTo()?c}</ogc:Literal>
                  </ogc:PropertyIsLessThanOrEqualTo>
	          </#if>
	          </ogc:And>
	          </ogc:Filter>
	          <sld:PolygonSymbolizer>            
	            <sld:Fill>
	              <sld:CssParameter name="fill">${colourMapEntry.getColor().getCSS()}</sld:CssParameter>
	            </sld:Fill>
	          </sld:PolygonSymbolizer>
	        </sld:Rule>
        </#list>
        ${rules}
      </FeatureTypeStyle>
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>