package org.esp.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LTileLayer;
import org.vaadin.addon.leaflet.LWmsLayer;

import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vividsolutions.jts.geom.Polygon;

/**
 * FIXME
 * 
 * @author Will Temperley
 * 
 */
@Deprecated
public class LayerViewer extends Panel {

    private Logger logger = LoggerFactory.getLogger(MapPublisher.class);

    private LMap map;

    private LWmsLayer singleLayer;

    public LayerViewer() {

        LTileLayer bl = new LTileLayer(
                "http://{s}.tile.osm.org/{z}/{x}/{y}.png");
        // bl.setZIndex(0);
        bl.setAttributionString("&copy; <a href='http://osm.org/copyright'>OpenStreetMap</a> contributors");
        map = new LMap();
        setContent(map);
        map.addBaseLayer(bl, "OSM");

    }

//    public void setIndicatorSurface(IndicatorSurface surface) {
//
//        if (surface.getEnvelope() != null) {
//            map.zoomToExtent(surface.getEnvelope());
//        } else {
//            logger.info("Missing envelope");
//        }
//
//        if (surface.getLayerName() != null) {
//            if (singleLayer == null) {
//                singleLayer = createDefaultWMSLayer(surface.getLayerName());
//                map.addOverlay(singleLayer, surface.getLayerName());
//            } else {
//                singleLayer.setLayers(surface.getLayerName());
//            }
//        } else {
//            logger.info("Missing layer name");
//        }
//
//        logger.info("Surface: " + surface.getLayerName());
//        logger.info("Leaflet layer: " + singleLayer);
//
//    }

    public void addWmsLayer(String layerName) {

        LWmsLayer l = createDefaultWMSLayer(layerName);
        map.addOverlay(l, layerName);
    }

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
