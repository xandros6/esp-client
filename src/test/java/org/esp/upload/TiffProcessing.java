package org.esp.upload;

import java.io.File;
import java.io.IOException;

import org.esp.publisher.GeoserverRestApi;
import org.esp.upload.old.UnknownCRSException;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

public class TiffProcessing {
    
    File tifFile = new File("src/test/data/not-a-geotiff.tif");

    @Test(expected=UnknownCRSException.class)
    public void test() throws FactoryException, IOException, TransformException, UnknownCRSException {
        GeoserverRestApi gsr = new GeoserverRestApi(null, null, null, null, null, null);


//        Object surface;
//        gsr.extractTiffMetadata(tifFile, surface);

    }

}
