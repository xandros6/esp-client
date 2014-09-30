package org.esp.publisher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Polygon;

public class PublishedFileMeta {
    private Double maxVal;
    
    private File file;

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

    private Double minVal;

    public Double getMinVal() {
        return minVal;
    }

    public void setMinVal(Double minVal) {
        this.minVal = minVal;
    }

    private CoordinateReferenceSystem crs;

    public CoordinateReferenceSystem getCrs() {
        return crs;
    }

    public void setCRS(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }
    
    public String getDescription() {
        StringBuilder repr = new StringBuilder();
        appendDescription(repr);
        return repr.toString();
    }

    protected void appendDescription(StringBuilder repr) {
        repr.append("Spatial reference: ").append(getSrid());
        repr.append("\n");
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
    
    
}
