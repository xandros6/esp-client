package org.esp.publisher;

import com.vividsolutions.jts.geom.Polygon;

public interface TiffMeta {

    public abstract Double getMaxVal();

    public abstract void setMaxVal(Double maxVal);

    public abstract Polygon getEnvelope();

    public abstract void setEnvelope(Polygon envelope);

    public abstract String getSrid();

    public abstract void setSrid(String srid);

    public abstract Double getPixelSizeY();

    public abstract void setPixelSizeY(Double pixelSizeY);

    public abstract Double getPixelSizeX();

    public abstract void setPixelSizeX(Double pixelSizeX);

    public abstract Double getMinVal();

    public abstract void setMinVal(Double minVal);
    

}