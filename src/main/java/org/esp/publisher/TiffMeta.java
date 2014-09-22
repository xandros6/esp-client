package org.esp.publisher;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Polygon;

public class TiffMeta {

    private Double maxVal;

    public Double getMaxVal() {
        return maxVal;
    }

    public void setMaxVal(Double maxVal) {
        this.maxVal = maxVal;
    }

    private Polygon envelope;

    public Polygon getEnvelope() {
        return envelope;
    }

    public void setEnvelope(Polygon envelope) {
        this.envelope = envelope;
    }

    private String srid;

    public String getSrid() {
        return srid;
    }

    public void setSrid(String srid) {
        this.srid = srid;
    }

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

    private Double minVal;

    public Double getMinVal() {
        return minVal;
    }

    public void setMinVal(Double minVal) {
        this.minVal = minVal;
    }

    private CoordinateReferenceSystem crs;
    private int numSampleDimensions;

    public CoordinateReferenceSystem getCrs() {
        return crs;
    }

    public void setCRS(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }
    
    private String getAxisUnit(int axisNo) {
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

    public String getSpatialReferenceDescription() {
        StringBuilder repr = new StringBuilder();
        repr.append("Spatial reference: ").append(getSrid());
        repr.append("\n");
        repr.append("X-axis pixel size: ").append(getPixelSizeX());
        repr.append(getAxisUnit(0));
        repr.append("\n");
        repr.append("Y-axis pixel size: ").append(getPixelSizeY());
        repr.append(getAxisUnit(1));
        return repr.toString();
    }

    public void setNumSampleDimensions(int nDims) {
        this.numSampleDimensions = nDims;
    }
    
    public int getNumSampleDimensions() {
        return numSampleDimensions;
    }
    
}
