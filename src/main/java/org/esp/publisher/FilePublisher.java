package org.esp.publisher;

import java.io.File;

import org.esp.domain.publisher.ColourMap;

public interface FilePublisher {
    public PublishedFileMeta extractMetadata(File file, String layerName) throws PublishException, UnknownCRSException;
    
    public boolean publishStyle(PublishedFileMeta metadata, String layerName, String styleTemplate, ColourMap colourMap);
    
    public boolean publishLayer(String layerName, PublishedFileMeta metadata);
}
