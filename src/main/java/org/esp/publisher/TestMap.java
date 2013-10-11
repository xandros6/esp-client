package org.esp.publisher;

import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.shared.BaseLayer;
import org.vaadin.addon.leaflet.shared.Bounds;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;

public class TestMap extends LMap {

    private BaseLayer bl = new BaseLayer();
    private String geoserverWmsEndpoint;

    // LMap lMap = new LMap();

    @Inject
    public TestMap(
            @Named("gs_rest_url") String restUrl,
            @Named("gs_workspace") String workspace) {

        bl.setUrl("http://{s}.tile.osm.org/{z}/{x}/{y}.png");
        bl.setName("OSM");
        bl.setAttributionString("&copy; <a href='http://osm.org/copyright'>OpenStreetMap</a> contributors");

        this.geoserverWmsEndpoint = restUrl + workspace + "/wms";

        this.setBaseLayers(bl);
        this.setZoomLevel(0);
    }

    public void addWmsLayer(String layerName) {

        BaseLayer l = new BaseLayer();
        l.setWms(true);
        l.setUrl(geoserverWmsEndpoint);
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
