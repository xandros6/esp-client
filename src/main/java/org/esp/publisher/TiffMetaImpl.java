package org.esp.publisher;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Polygon;

public class TiffMetaImpl implements TiffMeta {

    private Double maxVal;

    /*
     * (non-Javadoc)
     * 
     * @see org.esp.publisher.TiffMeta#getMaxVal()
     */
    @Override
    public Double getMaxVal() {
        return maxVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.esp.publisher.TiffMeta#setMaxVal(java.lang.Double)
     */
    @Override
    public void setMaxVal(Double maxVal) {
        this.maxVal = maxVal;
    }

    private Polygon envelope;

    /*
     * (non-Javadoc)
     * 
     * @see org.esp.publisher.TiffMeta#getEnvelope()
     */
    @Override
    public Polygon getEnvelope() {
        return envelope;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.esp.publisher.TiffMeta#setEnvelope(com.vividsolutions.jts.geom.Polygon
     * )
     */
    @Override
    public void setEnvelope(Polygon envelope) {
        this.envelope = envelope;
    }

    private String srid;

    /*
     * (non-Javadoc)
     * 
     * @see org.esp.publisher.TiffMeta#getSrid()
     */
    @Override
    public String getSrid() {
        return srid;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.esp.publisher.TiffMeta#setSrid(java.lang.String)
     */
    @Override
    public void setSrid(String srid) {
        this.srid = srid;
    }

    private Double pixelSizeY;

    /*
     * (non-Javadoc)
     * 
     * @see org.esp.publisher.TiffMeta#getPixelSizeY()
     */
    @Override
    public Double getPixelSizeY() {
        return pixelSizeY;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.esp.publisher.TiffMeta#setPixelSizeY(java.lang.Double)
     */
    @Override
    public void setPixelSizeY(Double pixelSizeY) {
        this.pixelSizeY = pixelSizeY;
    }

    private Double pixelSizeX;

    /*
     * (non-Javadoc)
     * 
     * @see org.esp.publisher.TiffMeta#getPixelSizeX()
     */
    @Override
    public Double getPixelSizeX() {
        return pixelSizeX;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.esp.publisher.TiffMeta#setPixelSizeX(java.lang.Double)
     */
    @Override
    public void setPixelSizeX(Double pixelSizeX) {
        this.pixelSizeX = pixelSizeX;
    }

    private Double minVal;

    /*
     * (non-Javadoc)
     * 
     * @see org.esp.publisher.TiffMeta#getMinVal()
     */
    @Override
    public Double getMinVal() {
        return minVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.esp.publisher.TiffMeta#setMinVal(java.lang.Double)
     */
    @Override
    public void setMinVal(Double minVal) {
        this.minVal = minVal;
    }

    private CoordinateReferenceSystem crs;

    @Override
    public CoordinateReferenceSystem getCrs() {
        return crs;
    }

    @Override
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

    @Override
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

}
