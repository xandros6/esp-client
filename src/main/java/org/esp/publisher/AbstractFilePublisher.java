package org.esp.publisher;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.esp.domain.publisher.ColourMap;
import org.esp.domain.publisher.ColourMapEntry;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Base spatial data file publisher functionality.
 * 
 * @author mauro.bartolomeoli@geo-solutions.it
 *
 */
public abstract class AbstractFilePublisher implements SpatialDataPublisher {

    protected GeoserverRestApi gsr;
    
    private Logger logger = LoggerFactory.getLogger(AbstractFilePublisher.class);
    
    private Map<String, Integer> limits = new HashMap<String, Integer>();
    
    public AbstractFilePublisher() {
        super();
    }
    

    @Override
    public PublishedFileMetadata extractMetadata(File file, String layerName) throws PublishException,
            UnknownCRSException {
        Preconditions.checkArgument(file != null, "File is null.");
        return  createMetadata(file, layerName);
    }
    
    /**
     * Returns a configured limit for the publisher.
     * 
     * @param type
     * @return
     */
    protected int getLimit(String type) {
        return limits.get(type);
    }
    
    /**
     * Checks if the publisher support dynamic styling (to be applied only on 
     * already published data).
     * 
     * @return
     */
    public boolean supportsAdHocStyling() {
        return false;
    }
    
    /**
     * Fills metadata properties object reading the given file.
     * 
     * @param file
     * @param layerName
     * @return
     * @throws PublishException
     * @throws UnknownCRSException
     */
    protected abstract PublishedFileMetadata createMetadata(File file, String layerName)
            throws PublishException, UnknownCRSException;


    /**
     * Creates and publish a new style for the layer.
     */
    public String createStyle(PublishedFileMetadata metadata, String layerName, 
            String styleTemplate, ColourMap colourMap) throws PublishException {
       if(gsr.publishStyle(layerName, getAttributeName(metadata), 
                styleTemplate, getColourMapEntries(colourMap, 1), "")) {
           logger.info("Style published: " + layerName);
           return layerName;
       } else {
            return null;
       }
    }
    
    /**
     * Updates an existing style using the given colourmap.
     * 
     * @param styleName
     * @param string
     * @param colourMap
     */
    public boolean updateStyle(String styleName, String styleTemplate, StylingMetadata metadata)
            throws PublishException {
        if(gsr.updateStyle(styleName, metadata.getAttributeName(), styleTemplate, 
                getColourMapEntries(metadata.getColourMap(), metadata.getIntervalsNumber()), "")) {
            logger.info("Style published: " + styleName);
            return true;
        }
        return false;
    }
    
    /**
     * Updates an existing style using full SLD.
     * 
     * @param layerName
     * @param style
     */
    public boolean updateStyle(String styleName, String style) throws PublishException {
        return gsr.updateStyle(styleName, style);
    }
    
    /**
     * Updates an existing style using precompiled rules.
     * 
     * @param styleName
     * @param string
     * @param rules
     */
    public boolean updateStyle(String styleName, String styleTemplate, 
            String rules, StylingMetadata metadata) throws PublishException {
        if(gsr.updateStyle(styleName, metadata.getAttributeName(), styleTemplate, 
                new ArrayList<ColourMapEntry>(), rules)) {
            logger.info("Style published: " + styleName);
            return true;
        }
        
        return false;
    }

    /**
     * Applies the given crs to the file metadata object.
     * 
     * @param metadata
     * @param crs
     * @throws UnknownCRSException
     */
    protected void setCrs(PublishedFileMetadata metadata, CoordinateReferenceSystem crs)
            throws UnknownCRSException {
        
        ReferenceIdentifier name = crs.getName();
        String crsName = "Unknown";
        if (name != null) {
            crsName = name.toString();
        }
        metadata.setCRS(crs);
        try {
            Integer epsgCode = CRS.lookupEpsgCode(crs, true);

            if (epsgCode == null) {
                throw new UnknownCRSException(crsName);
            }
    
            String srid = "EPSG:" + epsgCode;
            metadata.setSrid(srid);
        } catch(FactoryException e) {
            throw new UnknownCRSException(crsName);
        }
    }
    

    /**
     * Extracts (and eventually preprocess) a set of colour map entries from the given colour map
     * to be used to create a new SLD style.
     * 
     * @param colourMap
     * @return
     */
    protected List<ColourMapEntry> getColourMapEntries(ColourMap colourMap, int intervalNumbers) {
        return colourMap.getColourMapEntries();
    }

    /**
     * Returns the name of the attribute (if defined) to be styled for the published layer.
     * 
     * @param metadata
     * @return
     */
    protected String getAttributeName(PublishedFileMetadata metadata) {
        return "";
    }
    
   /**
    * Returns the list of supported attributes for the given layerName.
    * 
    * @param layerName
    * @return
    * @throws PublishException 
    */
    public Map<String, Class<?>> getAttributes(String layerName) throws PublishException {
        return null;
    }
    
    public void setGeoserverHandler(GeoserverRestApi gsr) {
        this.gsr = gsr;
    }
    
    @Override
    public void unpublish(String layerName) throws PublishException {
        removeLayer(layerName);
        if(gsr.getStyle(layerName) != null && !gsr.removeStyle(layerName)) {
            throw new PublishException("Cannot remove style for layer " + layerName);
        }
    }


    protected abstract void removeLayer(String layerName) throws PublishException;
    
    /**
     * Gets a published style from GeoServer.
     * 
     * @param styleName
     * @return
     */
    public String getPublishedStyle(String styleName) {
        return null;
    }
    
    /**
     * Gets a published layer attribute information (name, type, etc.) from GeoServer.
     * 
     * @param styleName
     * @return
     * @throws PublishException 
     */
    public String getAttributesInfo(String layerName) throws PublishException {
        return null;
    }
    
    /**
     * Gets a published layer geometry type from GeoServer.
     * 
     * @param layerName
     * @return
     * @throws PublishException 
     */
    public String getGeometryType(String layerName) throws PublishException {
        return null;
    }
    
    /**
     * Configure Publisher limits.
     * 
     * @param limits
     */
    public void setLimits(Map<String, Integer> limits) {
        this.limits = limits;
    }
    
    /**
     * Returns a classified style with the given classification description.
     * 
     * @param layerName
     * @param attributeName
     * @param classificationMethod
     * @param intervalsNumber
     * @param colourMap
     * @return
     * @throws PublishException
     */
    public String classify(String layerName, String attributeName,
            String classificationMethod, int intervalsNumber, ColourMap colourMap)
            throws PublishException {
        return null;
    }
}
