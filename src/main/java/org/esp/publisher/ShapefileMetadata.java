package org.esp.publisher;

import java.util.List;


/**
 * Metadata class for uploaded shapefiles.
 * 
 * @author mauro.bartolomeoli@geo-solutions.it
 *
 */
public class ShapefileMetadata extends PublishedFileMetadata {

    String attributeName;
    
    List<String> attributes;

    /**
     * Sets attribute name to use for simple themas.
     * 
     * @param attributeName
     */
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeName() {
        return attributeName;
    }
    
    /**
     * Sets list of available attributes.
     * 
     * @param attributes
     */
    public void setAttributes(List<String> attributes) {
        this.attributes = attributes;
    }

    public List<String> getAttributes() {
        return attributes;
    }
    
}
