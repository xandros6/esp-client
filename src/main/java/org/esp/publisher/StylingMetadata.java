package org.esp.publisher;

import org.esp.domain.publisher.ColourMap;

/**
 * Interface supported by components storing styling info for publishers.
 * 
 * @author Mauro Bartolomeoli
 *
 */
public interface StylingMetadata {
    
    /**
     * Attribute name to style on.
     * 
     * @return
     */
    public String getAttributeName();
    
    /**
     * Classification method to use for style generation.
     * 
     * @return
     */
    public String getClassificationMethod();
    
    /**
     * Colour Map to use for style generation.
     * 
     * @return
     */
    public ColourMap getColourMap();
    
    /**
     * Number of intervals to use to classify using the given classification method.
     * 
     * @return
     */
    public int getIntervalsNumber();
    
    /**
     * Returns the full SLD for the Style.
     * 
     * @return
     */
    public String getSLD();

}
