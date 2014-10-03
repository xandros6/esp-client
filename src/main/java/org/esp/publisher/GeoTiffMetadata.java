package org.esp.publisher;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Metadata class for uploaded GeoTiff files.
 * 
 * @author mauro.bartolomeoli@geo-solutions.it
 *
 */
public class GeoTiffMetadata extends PublishedFileMetadata {

    private Double pixelSizeY;

    public Double getPixelSizeY() {
        return pixelSizeY;
    }

    public void setPixelSizeY(Double pixelSizeY) {
        this.pixelSizeY = pixelSizeY;
    }

    private Double pixelSizeX;

    public Double getPixelSizeX() {
        return pixelSizeX;
    }

    public void setPixelSizeX(Double pixelSizeX) {
        this.pixelSizeX = pixelSizeX;
    }

    private int numSampleDimensions;
    
    private String getAxisUnit(int axisNo) {
        CoordinateReferenceSystem crs = getCrs();
        if (crs == null) {
            return null;
        }
        
        if (crs.getCoordinateSystem() == null) {
            return null;
        }
        
        if (crs.getCoordinateSystem().getAxis(axisNo) == null) {
            return null;
        }
        
        if (crs.getCoordinateSystem().getAxis(axisNo).getUnit() == null) {
            return null;
        }
        
        return crs.getCoordinateSystem().getAxis(axisNo).getUnit().toString();
    }

    @Override
    protected void appendDescription(StringBuilder repr) {
        super.appendDescription(repr);
        repr.append("X-axis pixel size: ").append(getPixelSizeX());
        repr.append(getAxisUnit(0));
        repr.append("\n");
        repr.append("Y-axis pixel size: ").append(getPixelSizeY());
        repr.append(getAxisUnit(1));
    }

    public void setNumSampleDimensions(int nDims) {
        this.numSampleDimensions = nDims;
    }
    
    public int getNumSampleDimensions() {
        return numSampleDimensions;
    }
    
}
