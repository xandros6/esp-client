package org.esp.publisher;

import java.io.File;

import org.esp.domain.publisher.ColourMap;

/**
 * Interface for spatial data file publisher modules.
 * Each implementation will be able to publish a different type of spatial data
 * (e.g. vector, raster, etc.)
 * 
 * @author mauro.bartolomeoli@geo-solutions.it
 *
 */
public interface SpatialDataPublisher {
    /**
     * Extract metadata from the given spatial data file.
     * 
     * @param file file to extract the metadata from.
     * @param layerName name to be assigned to the final published layer.
     * 
     * @return
     * @throws PublishException
     * @throws UnknownCRSException
     */
    public PublishedFileMetadata extractMetadata(File file, String layerName)
            throws PublishException, UnknownCRSException;
    
    /**
     * Creates a new style for the layer to be published.
     *  
     * @param metadata layer metadata
     * @param layerName name to be assigned to the layer
     * @param styleTemplate freemarker template to be used to create the style
     * @param colourMap map of colours to be used in the style
     * 
     * @return
     */
    public String createStyle(PublishedFileMetadata metadata, String layerName,
            String styleTemplate, ColourMap colourMap) throws PublishException;
    
    /**
     * Creates and publish the layer.
     * 
     * @param layerName
     * @param styleName
     * @param metadata
     * @return
     */
    public boolean createLayer(String layerName, String styleName, PublishedFileMetadata metadata)
            throws PublishException;
}
