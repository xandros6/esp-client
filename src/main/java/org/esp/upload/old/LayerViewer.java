package org.esp.upload.old;

import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LTileLayer;
import org.vaadin.addon.leaflet.LWmsLayer;
import org.vaadin.addon.leaflet.shared.Bounds;

import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;

public class LayerViewer extends Panel {

    private LMap map;

    public LayerViewer() {
        
        LTileLayer bl = new LTileLayer("http://{s}.tile.osm.org/{z}/{x}/{y}.png");
        bl.setAttributionString("&copy; <a href='http://osm.org/copyright'>OpenStreetMap</a> contributors");
        map = new LMap();
        setContent(map);
        map.addBaseLayer(bl, "OSM");
        
    }

    public void addWmsLayer(String layerName) {
        
        LWmsLayer l = new LWmsLayer();
        l.setUrl("http://lrm-maps.jrc.ec.europa.eu/geoserver/esp/wms");
        l.setTransparent(true);
        l.setFormat("image/png");
        l.setLayers(layerName);
        
        map.addLayer(l);
    }
    
    
    public void zoomTo(Polygon p) {
        
        if (p == null) {
            return;
        }
        
        Envelope env = p.getEnvelopeInternal();
        
        Bounds b = new Bounds();
        
        b.setSouthWestLat(env.getMinY());
        b.setSouthWestLon(env.getMinX());
        
        b.setNorthEastLat(env.getMaxY());
        b.setNorthEastLon(env.getMaxX());
        
        map.zoomToExtent(b);
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
