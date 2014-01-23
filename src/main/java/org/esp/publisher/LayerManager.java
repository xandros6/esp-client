package org.esp.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LTileLayer;
import org.vaadin.addon.leaflet.LWmsLayer;

import com.vaadin.ui.Component;
import com.vividsolutions.jts.geom.Polygon;

/**
 * FIXME
 * 
 * @author Will Temperley
 * 
 */
public class LayerManager {

    private Logger logger = LoggerFactory.getLogger(MapPublisher.class);

    private LMap map;

    private LWmsLayer singleLayer;

    public LayerManager(LMap map) {

        this.map = map;

        LTileLayer bl = new LTileLayer(
                "http://{s}.tile.osm.org/{z}/{x}/{y}.png");
        bl.setAttributionString("&copy; <a href='http://osm.org/copyright'>OpenStreetMap</a> contributors");
        map.addBaseLayer(bl, "OSM");

    }

    public void setSurfaceLayerName(String layerName) {

            if (singleLayer == null) {
                singleLayer = createDefaultWMSLayer(layerName);
                map.addOverlay(singleLayer, layerName);
            } else {
                map.removeComponent(singleLayer);
                singleLayer = createDefaultWMSLayer(layerName);
//                singleLayer.setLayers(layerName);
                map.addOverlay(singleLayer, layerName);
            }
        
    }

//    public void addWmsLayer(String layerName) {
//
//        LWmsLayer l = createDefaultWMSLayer(layerName);
//        map.addOverlay(l, layerName);
//    }

    private LWmsLayer createDefaultWMSLayer(String layerName) {
        LWmsLayer l = new LWmsLayer();

        l.setUrl("http://lrm-maps.jrc.ec.europa.eu/geoserver/esp/wms");
        l.setTransparent(true);
        l.setFormat("image/png");
        l.setLayers(layerName);
        l.setOpacity(1d);
        l.setActive(true);
        l.setVisible(true);
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

}
