package org.esp.publisher;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Thrown when the {@link CoordinateReferenceSystem} could not be found in the EPSG database.
 * 
 * @author Will Temperley
 * 
 */
public class UnknownCRSException extends Exception {

    public UnknownCRSException(String crsName) {

        super(
                String.format(
                        "The Coordinate Reference System (CRS) '%s' could not be found. Please provide data referenced to a standard CRS.",
                        crsName));

    }

}
