package org.esp.publisher;

import java.io.File;
import java.util.List;

import org.esp.domain.publisher.ColourMap;
import org.esp.domain.publisher.ColourMapEntry;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public abstract class AbstractFilePublisher implements FilePublisher {

    protected GeoserverRestApi gsr;
    
    private Logger logger = LoggerFactory.getLogger(AbstractFilePublisher.class);
    
    
    
    public AbstractFilePublisher(GeoserverRestApi gsr) {
        super();
        this.gsr = gsr;
    }

    @Override
    public PublishedFileMeta extractMetadata(File file, String layerName) throws PublishException,
            UnknownCRSException {
        Preconditions.checkArgument(file != null, "File is null.");
        return  createMetadata(file, layerName);
    }

    protected void setCrs(PublishedFileMeta metadata, CoordinateReferenceSystem crs)
            throws UnknownCRSException {
        
        ReferenceIdentifier name = crs.getName();
        String crsName = "Unknown";
        if (name != null) {
            crsName = name.toString();
        }
        metadata.setCRS(crs);
        try {
            Integer epsgCode = CRS.lookupEpsgCode(crs, true);

            if (epsgCode == null) {
                throw new UnknownCRSException(crsName);
            }
    
            String srid = "EPSG:" + epsgCode;
            metadata.setSrid(srid);
        } catch(FactoryException e) {
            throw new UnknownCRSException(crsName);
        }
    }
    
    public abstract boolean publishLayer(String layerName, PublishedFileMeta metadata);

    public boolean publishStyle(PublishedFileMeta metadata, String layerName, String styleTemplate, ColourMap colourMap) {
        boolean stylePublished = gsr.publishStyle(layerName, getAttributeName(metadata), styleTemplate, getColourMapEntries(colourMap));
        logger.info("Style published: " + stylePublished);
        return stylePublished;
    }

    protected List<ColourMapEntry> getColourMapEntries(ColourMap colourMap) {
        return colourMap.getColourMapEntries();
    }

    protected String getAttributeName(PublishedFileMeta metadata) {
        return "";
    }

    protected abstract PublishedFileMeta createMetadata(File file, String layerName) throws PublishException, UnknownCRSException;


}
