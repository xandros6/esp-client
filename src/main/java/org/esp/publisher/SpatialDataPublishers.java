package org.esp.publisher;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.geotools.factory.FactoryCreator;
import org.geotools.factory.FactoryRegistry;

import com.google.inject.Guice;
import com.google.inject.Inject;

/**
 * Implements a registry for all the available SpatialDataPublisher
 * implementations.
 * Each implementation has and id, that binds it to spatial data type
 * metadata table record.
 * 
 * @author Mauro Bartolomeoli
 *
 */
public class SpatialDataPublishers {
    private static FactoryRegistry registry = null;
    private static Map<Integer, SpatialDataPublisher> publishers = null;
    
    private static GeoserverRestApi gsr = null;
    private static Map<Long, Map<String, Integer>> limits = new HashMap<Long, Map<String, Integer>>();
       
    
    
    public static void setLimits(Map<Long, Map<String, Integer>> limits) {
        SpatialDataPublishers.limits = limits;
    }

    /**
     * Gets the SpatialDataPublisher implementation bound to the given spatial data type
     * identifier.
     * 
     * @param spatialDataType
     * @return
     */
    public static SpatialDataPublisher getSpatialDataPublisher(int spatialDataType) {
        if(publishers == null) {
            publishers = getSpatialDataPublishers();
        }
        return publishers.get(spatialDataType);
    }
    
    private static FactoryRegistry getServiceRegistry() {
        assert Thread.holdsLock(SpatialDataPublishers.class);
        if (registry == null) {
            registry = new FactoryCreator(Arrays.asList(new Class<?>[] {
                    SpatialDataPublisher.class,}));
        }
        return registry;
    }
    
    /**
     * Builds a list of available SpatialDataPublisher implementation.
     * 
     * @return
     */
    public static synchronized Map<Integer, SpatialDataPublisher> getSpatialDataPublishers() {
        Iterator<SpatialDataPublisher> iter = getServiceRegistry().getServiceProviders(
                SpatialDataPublisher.class, null, null);
        Map<Integer, SpatialDataPublisher> result = new HashMap<Integer, SpatialDataPublisher>();
        while(iter.hasNext()) {
            SpatialDataPublisher publisher = iter.next();
            publisher.setGeoserverHandler(gsr);
            publisher.setLimits(limits.get((long)publisher.getId()));
            result.put(publisher.getId(), publisher);
        }
        return result;
    }

    public static void setGeoserverHandler(GeoserverRestApi gsr) {
        SpatialDataPublishers.gsr = gsr;
    }
    
    
}
