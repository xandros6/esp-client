package org.esp.upload;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.esp.domain.blueprint.IndicatorSurface;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.scannotation.AnnotationDB;
import org.scannotation.ClasspathUrlFinder;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

public class BootstrapTest {
    
//    @Test
    public void testImageOps() throws FactoryException, IOException, TransformException {
        
        GeoserverRest gsr = new GeoserverRest(null, null, null, null, null);
        IndicatorSurface surface = new IndicatorSurface();
        
        File tifFile = new File("Z:/temp/egrid/fgd.tif");
        
        gsr.extractTiffMetadata(tifFile, surface);
        
        printProps(surface);
    }
    
    
//    @Test
    public void testShpOps() throws FactoryException, IOException, TransformException {
        
        GeoserverRest gsr = new GeoserverRest(null, null, null, null, null);
        IndicatorSurface surface = new IndicatorSurface();
        
        File shpFile = new File("Z:/temp/ErosionControl.shp");
    
        gsr.extractShpMetadata(shpFile, surface);
        
        printProps(surface);
    }
    
    public void printProps(IndicatorSurface surface) {
        
        Polygon p = surface.getEnvelope();
        Envelope wgs84Env = p.getEnvelopeInternal();
        
        System.out.println("wgsMin x:" + wgs84Env.getMinX());
        System.out.println("wgsMin y:" + wgs84Env.getMinY());
        System.out.println("wgsMax x:" + wgs84Env.getMaxX());
        System.out.println("wgsMax y:" + wgs84Env.getMaxY());
        
        System.out.println("min: " + surface.getMinVal());
        System.out.println("max: " + surface.getMaxVal());
        
    }

    public static void main(String[] args) throws IOException {
        URL[] urls = ClasspathUrlFinder.findClassPaths(); // scan
                                                          // java.class.path
        AnnotationDB db = new AnnotationDB();
        db.scanArchives(urls);
        Set<String> entityClasses = db.getAnnotationIndex().get(
                Entity.class.getName());

        for (String string : entityClasses) {
            String x = String.format("<class>%s</class>", string);
            System.out.println(x);
        }
    }

    @Test
    public void testInjection() {
        
       EntityManagerFactory emfInstance = Persistence.createEntityManagerFactory("esp-domain");
       emfInstance.createEntityManager();

    }


}
