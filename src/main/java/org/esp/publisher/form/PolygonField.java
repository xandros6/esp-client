package org.esp.publisher.form;

import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LPolygon;
import org.vaadin.addon.leaflet.draw.LDraw.FeatureDrawnEvent;
import org.vaadin.addon.leaflet.draw.LDraw.FeatureDrawnListener;
import org.vaadin.addon.leaflet.draw.LDraw.FeatureModifiedEvent;
import org.vaadin.addon.leaflet.draw.LDraw.FeatureModifiedListener;
import org.vaadin.addon.leaflet.draw.LDrawPolygon;
import org.vaadin.addon.leaflet.draw.LEditing;
import org.vaadin.addon.leaflet.shared.Bounds;
import org.vaadin.addon.leaflet.shared.Point;
import org.vaadin.addon.leaflet.util.AbstractJTSField;
import org.vaadin.addon.leaflet.util.JTSUtil;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vividsolutions.jts.geom.LinearRing;

public class PolygonField extends AbstractJTSField<LinearRing> {

	private LPolygon lPolygon;

	public PolygonField() {
	}

	public PolygonField(String caption) {
		this();
		setCaption(caption);
	}
	
	@Override
	protected Component initContent() {
		map.setZoomLevel(2);
		initBaseLayers();
	    Label label = new Label("Nothing to see here");
	    label.setVisible(false);
        return label;

	}

	@Override
	public Class<? extends LinearRing> getType() {
		return LinearRing.class;
	}

	public void setMap(LMap lMap) {
	    map = lMap;
    }

	protected void prepareEditing() {

		if (lPolygon == null) {
			lPolygon = new LPolygon();
//			lPolygon.setOpacity(0d);
			lPolygon.setFill(false);
			map.addLayer(lPolygon);
		}
		Point[] lPointArray = JTSUtil.toLeafletPointArray(getCrsTranslator()
				.toPresentation(getInternalValue()));
		lPolygon.setPoints(lPointArray);
		LEditing editing = new LEditing(lPolygon);
		editing.addFeatureModifiedListener(new FeatureModifiedListener() {

			@Override
			public void featureModified(FeatureModifiedEvent event) {
				setValue(getCrsTranslator().toModel(
						JTSUtil.toLinearRing(lPolygon)));
			}
		});
		map.zoomToExtent(new Bounds(lPolygon.getPoints()));
	}

	protected void prepareDrawing() {

//		LDrawPolygon lDrawFeature = new LDrawPolygon(map);
//
//		lDrawFeature.addFeatureDrawnListener(new FeatureDrawnListener() {
//
//			@Override
//			public void featureDrawn(FeatureDrawnEvent event) {
//				// TODO fill Vaadin bug report: exception from here has horrible
//				// stack trace (non informative), even more horrible than the
//				// usual that has some irrelevant stuff in front
//				setValue(getCrsTranslator()
//						.toModel(
//								JTSUtil.toLinearRing((LPolygon) event
//										.getDrawnFeature())));
//			}
//		});

	}

}
