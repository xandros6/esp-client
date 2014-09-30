package org.esp.upload;

import it.jrc.persist.Dao;

import java.io.File;

import org.esp.publisher.ShapefileMeta;
import org.esp.publisher.ShapefilePublisher;
import org.esp.publisher.UnknownCRSException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Injector;

public class ShapefileProcessingTests {
    
    //private Injector injector = TestResourceFactory.getInjector();

    private long t0;
    
    @Before
    public void startTiming() {
        t0 = System.nanoTime();
    }

    @After
    public void endTiming() {
         printExecTime();
    }

    private void printExecTime() {
        long x = System.nanoTime() - t0;
        System.out.println("Time elapsed: ");
        System.out.println(((double) x / 1000000000) + " s");
    }

    @Test
    public void testExtractMetadata() throws UnknownCRSException, Exception {

        File shapeFile = new File("src/test/data/states.shp");

        ShapefilePublisher tme = new ShapefilePublisher(null);

        ShapefileMeta surface = (ShapefileMeta)tme.extractMetadata(shapeFile, "aa");

        Assert.assertTrue(surface.getSrid().equals("EPSG:4326"));
    }

    /*
    @Test
    public void testTripleBand() throws UnknownCRSException, Exception {
        
        printExecTime();

        File tifFile = new File("src/test/data/cropland.tif");

        Dao dao = injector.getInstance(Dao.class);

        TiffMetadataExtractor tme = new TiffMetadataExtractor(dao);

        TiffMeta surface = new TiffMeta();
        tme.extractTiffMetadata(tifFile, surface);

        assertTrue(surface.getNumSampleDimensions() == 3);
//        Assert.assertTrue(surface.getPixelSizeY() == 1000);
//        Assert.assertTrue(surface.getSrid().equals("EPSG:3035"));
    }

    @Test
    public void testPublish() throws FileNotFoundException, IllegalArgumentException {
        
        File tifFile = new File("src/test/data/cropland.tif");

        GeoserverRestApi gsr = injector.getInstance(GeoserverRestApi.class);
        
        gsr.publishTiff(tifFile, "EPSG:"+4326, "AutomatedTest", "raster");
        
    }*/
}
