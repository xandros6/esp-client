<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.0.0" xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd">
  <NamedLayer>
    <Name>${styleName}</Name>
    <UserStyle>
      <Name>${styleName}</Name>
      <Title>${styleName}</Title>
      <Abstract>${styleName}</Abstract>
      <FeatureTypeStyle>
        <Rule>
          <RasterSymbolizer>
            <Opacity>1.0</Opacity>
            <ColorMap>
            
              <ColorMapEntry color="#000000" quantity="0" label="nodata" opacity="0" />
<#list colourMapEntries as colourMapEntry>
              <ColorMapEntry color="${colourMapEntry.getColor().getCSS()}" quantity="${colourMapEntry.getValue()?c}" label="${colourMapEntry.getLabel()}" opacity="1" />
</#list> 
            
            </ColorMap>
          </RasterSymbolizer>
        </Rule>
      </FeatureTypeStyle>
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>