package org.esp.publisher;

import java.util.HashMap;
import java.util.Map;

import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LTileLayer;
import org.vaadin.addon.leaflet.LWmsLayer;

import com.vaadin.ui.Component;
import com.vividsolutions.jts.geom.Polygon;

/**
 * 
 * @author Will Temperley
 * 
 */
public class LayerManager {

    private static final String OSM = "OSM";

    private LMap map;

    private LWmsLayer singleLayer;

    private LTileLayer bl;
    
    private String defaultWms;
    
    private static Map<String, Integer> timestamps = new HashMap<String, Integer>();

    public LayerManager(LMap map,
            String defaultWms) {

        this.map = map;

        this.defaultWms = defaultWms;
        
        bl = new LTileLayer(
                "http://{s}.tile.osm.org/{z}/{x}/{y}.png");
        bl.setAttributionString("&copy; <a href='http://osm.org/copyright'>OpenStreetMap</a> contributors");
        map.addBaseLayer(bl, OSM);

    }

    public void setSurfaceLayerName(String layerName, long timestamp) {

            if (singleLayer == null) {
                singleLayer = createDefaultWMSLayer(layerName, timestamp);
                map.addOverlay(singleLayer, layerName);
            } else {
                map.removeComponent(singleLayer);
                singleLayer = createDefaultWMSLayer(layerName, timestamp);
                map.addOverlay(singleLayer, layerName);
            }
        
    }

    /*private static synchronized int getTimestamp(String layerName) {
        if(timestamps.containsKey(layerName)) {
            Integer timestamp = timestamps.get(layerName);
            timestamp++;
            timestamps.put(layerName, timestamp);
            return timestamp;
        } else {
            timestamps.put(layerName, 1);
            return 1;
        }
        
    }*/

    private LWmsLayer createDefaultWMSLayer(String layerName, long timestamp) {
        LWmsLayer l = new LWmsLayer();

        //l.setUrl("http://lrm-maps.jrc.ec.europa.eu/geoserver/esp/wms");
        l.setUrl(this.defaultWms);
        l.setTransparent(true);
        l.setFormat("image/png");
        l.setLayers(layerName);
        l.setActive(true);
        l.setVisible(true);
        l.setCustomOption("_dc", timestamp + "");
        return l;
    }

    public void zoomTo(Polygon p) {
        if (p == null) {
            return;
        }
        map.zoomToExtent(p);
    }

    public void addComponent(Component lPoly) {
        map.addComponent(lPoly);
    }

    public void removeComponent(Component lPoly) {
        map.removeComponent(lPoly);
    }

    public LMap getMap() {
        return map;
    }

    public void reset() {
        map.removeAllComponents();
        map.addBaseLayer(bl, OSM);
    }

}
