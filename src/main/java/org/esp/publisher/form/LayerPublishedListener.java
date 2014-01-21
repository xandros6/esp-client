package org.esp.publisher.form;

import com.vividsolutions.jts.geom.Polygon;

public interface LayerPublishedListener {

    void onLayerPublished(String layerName, Polygon extent);

}
