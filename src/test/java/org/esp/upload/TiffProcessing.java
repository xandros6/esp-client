package org.esp.upload;

import java.io.File;
import java.io.IOException;

import org.esp.domain.blueprint.IndicatorSurface;
import org.esp.upload.old.GeoserverRest;
import org.esp.upload.old.TiffMetaEx;
import org.esp.upload.old.UnknownCRSException;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

public class TiffProcessing {
    
    File tifFile = new File("src/test/data/not-a-geotiff.tif");

    @Test(expected=UnknownCRSException.class)
    public void test() throws FactoryException, IOException, TransformException, UnknownCRSException {
        TiffMetaEx gsr = new GeoserverRest(null, null, null, null, null);

        IndicatorSurface surface = new IndicatorSurface();

        gsr.extractTiffMetadata(tifFile, surface);

    }

}
