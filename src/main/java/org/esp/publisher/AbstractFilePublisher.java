package org.esp.publisher;

import java.io.File;
import java.util.List;

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
    
    
    public AbstractFilePublisher(GeoserverRestApi gsr) {
        super();
        this.gsr = gsr;
    }

    @Override
    public PublishedFileMetadata extractMetadata(File file, String layerName) throws PublishException,
            UnknownCRSException {
        Preconditions.checkArgument(file != null, "File is null.");
        return  createMetadata(file, layerName);
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
     * Creates and publish the new layer, with the given name.
     * 
     */
    public abstract boolean createLayer(String layerName, String styleName,
            PublishedFileMetadata metadata) throws PublishException;

    /**
     * Creates and publish a new style for the layer.
     */
    public String createStyle(PublishedFileMetadata metadata, String layerName,
            String styleTemplate, ColourMap colourMap) throws PublishException {
        boolean stylePublished = gsr.publishStyle(layerName, getAttributeName(metadata),
                styleTemplate, getColourMapEntries(colourMap));
        logger.info("Style published: " + stylePublished);
        if(stylePublished) {
            return layerName;
        } else {
            return null;
        }
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
    protected List<ColourMapEntry> getColourMapEntries(ColourMap colourMap) {
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
    
}
