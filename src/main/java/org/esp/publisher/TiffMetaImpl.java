package org.esp.publisher;


import com.vividsolutions.jts.geom.Polygon;

public class TiffMetaImpl implements TiffMeta {


    private Double maxVal;

    /* (non-Javadoc)
     * @see org.esp.publisher.TiffMeta#getMaxVal()
     */
    @Override
    public Double getMaxVal() {
        return maxVal;
    }

    /* (non-Javadoc)
     * @see org.esp.publisher.TiffMeta#setMaxVal(java.lang.Double)
     */
    @Override
    public void setMaxVal(Double maxVal) {
        this.maxVal = maxVal;
    }

    private Polygon envelope;

    /* (non-Javadoc)
     * @see org.esp.publisher.TiffMeta#getEnvelope()
     */
    @Override
    public Polygon getEnvelope() {
        return envelope;
    }

    /* (non-Javadoc)
     * @see org.esp.publisher.TiffMeta#setEnvelope(com.vividsolutions.jts.geom.Polygon)
     */
    @Override
    public void setEnvelope(Polygon envelope) {
        this.envelope = envelope;
    }

    private String srid;

    /* (non-Javadoc)
     * @see org.esp.publisher.TiffMeta#getSrid()
     */
    @Override
    public String getSrid() {
        return srid;
    }

    /* (non-Javadoc)
     * @see org.esp.publisher.TiffMeta#setSrid(java.lang.String)
     */
    @Override
    public void setSrid(String srid) {
        this.srid = srid;
    }

    private Double pixelSizeY;

    /* (non-Javadoc)
     * @see org.esp.publisher.TiffMeta#getPixelSizeY()
     */
    @Override
    public Double getPixelSizeY() {
        return pixelSizeY;
    }

    /* (non-Javadoc)
     * @see org.esp.publisher.TiffMeta#setPixelSizeY(java.lang.Double)
     */
    @Override
    public void setPixelSizeY(Double pixelSizeY) {
        this.pixelSizeY = pixelSizeY;
    }

    private Double pixelSizeX;

    /* (non-Javadoc)
     * @see org.esp.publisher.TiffMeta#getPixelSizeX()
     */
    @Override
    public Double getPixelSizeX() {
        return pixelSizeX;
    }

    /* (non-Javadoc)
     * @see org.esp.publisher.TiffMeta#setPixelSizeX(java.lang.Double)
     */
    @Override
    public void setPixelSizeX(Double pixelSizeX) {
        this.pixelSizeX = pixelSizeX;
    }

    private Double minVal;

    /* (non-Javadoc)
     * @see org.esp.publisher.TiffMeta#getMinVal()
     */
    @Override
    public Double getMinVal() {
        return minVal;
    }

    /* (non-Javadoc)
     * @see org.esp.publisher.TiffMeta#setMinVal(java.lang.Double)
     */
    @Override
    public void setMinVal(Double minVal) {
        this.minVal = minVal;
    }
    
}
