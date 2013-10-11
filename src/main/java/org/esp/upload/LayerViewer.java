package org.esp.upload;

import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.shared.BaseLayer;
import org.vaadin.addon.leaflet.shared.Bounds;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;

public class LayerViewer extends LMap {

    BaseLayer bl = new BaseLayer();
//    LMap lMap = new LMap();

    public LayerViewer() {
        
        this.setSizeFull();
        bl.setUrl("http://{s}.tile.osm.org/{z}/{x}/{y}.png");
        bl.setName("OSM");
        bl.setAttributionString("&copy; <a href='http://osm.org/copyright'>OpenStreetMap</a> contributors");

        this.setBaseLayers(bl);
    }

    public void addWmsLayer(String layerName) {
        
        BaseLayer l = new BaseLayer();
        l.setWms(true);
        l.setUrl("http://lrm-maps.jrc.ec.europa.eu/geoserver/esp/wms");
        l.setTransparent(true);
        l.setFormat("image/png");
        l.setLayers(layerName);
        
        this.setBaseLayers(bl, l);
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
        
        this.setCenter(b);
        this.zoomToExtent(b);
    }

}
