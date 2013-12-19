package org.esp.publisher.form;

import java.util.Locale;

import it.jrc.auth.RoleManager;

import org.esp.domain.blueprint.IndicatorSurface;
import org.esp.domain.blueprint.IndicatorSurface_;
import org.jrc.persist.Dao;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LPolygon;
import org.vaadin.addon.leaflet.util.AbstractJTSField;
import org.vaadin.addon.leaflet.util.AbstractJTSField.Configurator;
import org.vaadin.addon.leaflet.util.CRSTranslator;
import org.vaadin.addon.leaflet.util.LinearRingField;
import org.vaadin.addon.leaflet.util.PolygonField;

import com.google.inject.Inject;
import com.vaadin.data.util.converter.Converter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

public class InlineIndicatorSurfaceEditor extends
        CutDownBaseEditor<IndicatorSurface> {

    private PolygonField f;

    @Inject
    public InlineIndicatorSurfaceEditor(Dao dao, RoleManager roleManager) {

        super(IndicatorSurface.class, dao);

        ff.addField(IndicatorSurface_.colourMap);
        ff.addField(IndicatorSurface_.minVal);
        ff.addField(IndicatorSurface_.maxVal);
        ff.addField(IndicatorSurface_.layerName);
        ff.addField(IndicatorSurface_.pixelSizeX);

        f = new PolygonField();


        f.setConverter(new Converter<LinearRing, Polygon>() {

            @Override
            public Polygon convertToModel(LinearRing value,
                    Class<? extends Polygon> targetType, Locale locale)
                    throws com.vaadin.data.util.converter.Converter.ConversionException {
                if (value != null) {

                    Polygon polygon = new Polygon(value, null,
                            new GeometryFactory());
                    polygon.setSRID(value.getSRID());
                    return polygon;
                }
                return null;
            }

            @Override
            public LinearRing convertToPresentation(Polygon value,
                    Class<? extends LinearRing> targetType, Locale locale)
                    throws com.vaadin.data.util.converter.Converter.ConversionException {

                // It's guaranteed to be a LinearRing
                if (value != null) {
                    LinearRing exteriorRing = (LinearRing) value
                            .getExteriorRing();
                    return exteriorRing;
                }
                return null;
            }

            @Override
            public Class<Polygon> getModelType() {
                return Polygon.class;
            }

            @Override
            public Class<LinearRing> getPresentationType() {
                return LinearRing.class;
            }
        });
        
        f.setCRSTranslator(new CRSTranslator<Geometry>() {

            @Override
            public Geometry toPresentation(Geometry geom) {
                geom.setSRID(4326);
                return geom;
            }

            @Override
            public Geometry toModel(Geometry geom) {
                geom.setSRID(4326);
                return geom;
            }
        });
        
        f.setHeight("200px");
        f.setWidth("300px");

        ff.addField(IndicatorSurface_.envelope, f);

        addFieldGroup("Other");

    }
    
    public LMap getMap() {
        return f.getMap();
    }
    
//    public void setMap(final LMap map) {
//        
//        Configurator x = new Configurator() {
//
//            @Override
//            public void configure(AbstractJTSField<?> field) {
//                PolygonField pf = (PolygonField) field;
//                pf.setMap(map);
//            }
//        };
//
//        f.setConfigurator(x);
//    }

    public boolean commit() {
        return commitForm();
    }

}
