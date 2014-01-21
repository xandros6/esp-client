package org.esp.publisher.form;

import it.jrc.auth.RoleManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Locale;

import org.esp.domain.blueprint.ArealUnit_;
import org.esp.domain.blueprint.EcosystemServiceIndicator;
import org.esp.domain.blueprint.EcosystemServiceIndicator_;
import org.esp.domain.blueprint.Indicator_;
import org.esp.domain.blueprint.QuantificationUnit_;
import org.esp.domain.blueprint.TemporalUnit_;
import org.esp.domain.publisher.ColourMap;
import org.esp.domain.publisher.ColourMapEntry;
import org.esp.publisher.GeoserverRestApi;
import org.esp.publisher.TiffMetaImpl;
import org.esp.publisher.TiffMetadataExtractor;
import org.esp.publisher.TiffUploadField;
import org.esp.upload.old.UnknownCRSException;
import org.jrc.form.filter.YearField;
import org.jrc.persist.Dao;
import org.jrc.persist.adminunits.Grouping;
import org.jrc.persist.adminunits.Grouping_;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.util.CRSTranslator;

import com.google.inject.Inject;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.converter.StringToDoubleConverter;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

public class ESIEditor extends CutDownBaseEditor<EcosystemServiceIndicator> {

    Logger logger = LoggerFactory.getLogger(ESIEditor.class);

    private PolygonField f;
    private TiffMetadataExtractor tme;
    private TextField minVal;
    private TextField maxVal;
    private ColourMapField cmf;

    private GeoserverRestApi gsr;

    private LayerPublishedListener listener;

    @Inject
    public ESIEditor(Dao dao, RoleManager roleManager,
            TiffMetadataExtractor tme, GeoserverRestApi gsr) {

        super(EcosystemServiceIndicator.class, dao);

        this.tme = tme;
        this.gsr = gsr;

        buildPublishForm();

        buildMetaForm();
    }

    private void buildPublishForm() {

        ff.addField(EcosystemServiceIndicator_.ecosystemService);
        ff.addSelectAndCreateField(EcosystemServiceIndicator_.indicator,
                Indicator_.label);
        ff.addField(EcosystemServiceIndicator_.study);

        addFieldGroup("The Ecosystem Service");

        TiffUploadField uploadField = new TiffUploadField();
        
        /*
         * TODO
         * 
         * Lots of publish events ... like
         * 
         * Change in value of colour map
         * 
         */

        uploadField.addListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                File f = (File) event.getProperty().getValue();
                extractTiffMetaData(f);
                doPublish();
            }
        });

        ff.addField("file", uploadField);

        cmf = new ColourMapField(dao);
        ff.addField(EcosystemServiceIndicator_.colourMap, cmf);

        this.minVal = ff.addTextField(EcosystemServiceIndicator_.minVal);
        this.maxVal = ff.addTextField(EcosystemServiceIndicator_.maxVal);
        StringToDoubleConverter std = new StringToDoubleConverter();
        minVal.setConverter(std);
        maxVal.setConverter(std);

        // ff.addField(EcosystemServiceIndicator_.layerName);
        // ff.addField(EcosystemServiceIndicator_.pixelSizeX);
        // ff.addField(EcosystemServiceIndicator_.pixelSizeY);

        getGeometryField();

        // Invisible form field
        ff.addField(EcosystemServiceIndicator_.envelope, f);
        f.setVisible(false);

        addFieldGroup("Geospatial data");
    }

    private void buildMetaForm() {

        // SelectionTable<EcosystemService> st =
        // ff.addSelectionTable(EcosystemServiceIndicator_.ecosystemService);
        // st.addColumn(EcosystemService_.label, "Name");
        // st.addFilterField(EcosystemService_.ecosystemServiceCategory,
        // "Filter by category");

        // ff.addField(EcosystemServiceIndicator_.study);

        ff.addField(EcosystemServiceIndicator_.status);
        ff.addField(EcosystemServiceIndicator_.ecosystemServiceAccountingType);
        ff.addField(EcosystemServiceIndicator_.ecosystemServiceBenefitType);

        addFieldGroup("Accounting");

        ff.addSelectAndCreateField(
                EcosystemServiceIndicator_.quantificationUnit,
                QuantificationUnit_.label,
                QuantificationUnit_.quantificationUnitCategory);
        ff.addSelectAndCreateField(EcosystemServiceIndicator_.arealUnit,
                ArealUnit_.label);
        ff.addSelectAndCreateField(EcosystemServiceIndicator_.temporalUnit,
                TemporalUnit_.label);

        addFieldGroup("Quantification");

        ff.addField(EcosystemServiceIndicator_.startYear, new YearField());
        ff.addField(EcosystemServiceIndicator_.endYear, new YearField());
        ff.addField(EcosystemServiceIndicator_.spatialLevel);

        // Quick hack to get a filtered TwinColSelect
        TwinColSelect groupingField = new TwinColSelect();
        ff.addField(EcosystemServiceIndicator_.groupings, groupingField);
        List<Grouping> groupings = dao.all(Grouping.class, Grouping_.id);
        for (Grouping grouping : groupings) {
            if (grouping.getGroupingType().getId().equals("Region")) {
                groupingField.addItem(grouping);
            }
        }
        groupingField.setCaption("Regions");

        addFieldGroup("Spatio-temporal");

        ff.addField(EcosystemServiceIndicator_.quantificationMethod);

        ff.addField(EcosystemServiceIndicator_.dataSources);

        addFieldGroup("Model and data");

        ff.addField(EcosystemServiceIndicator_.minimumMappingUnit);

        addFieldGroup("Spatial data");

        ff.addField(EcosystemServiceIndicator_.biomes);

        ff.addField(EcosystemServiceIndicator_.studyObjectiveMet);
        ff.addTextArea(EcosystemServiceIndicator_.comments);

        addFieldGroup("Other");

    }

    /**
     * TODO all JTS Field stuff should be refactored
     */
    private void getGeometryField() {

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
    }

    @Deprecated
    public LMap getMap() {
        return f.getMap();
    }

    public boolean commit() {
        return commitForm();
    }

    @Deprecated
    public String getLayerName() {
        return getEntity().getLayerName();
    }

    @Deprecated
    public void setLayerName(String layerName) {
        getEntity().setLayerName(layerName);
    }

    /**
     * FIXME -- to do this we need to ensure all colourmaps have two entries.
     * 
     * @return
     */
    @Deprecated
    public ColourMap getColourMap() {

        ColourMap cm = cmf.getValue();
        logger.info("CM: " + cm.getLabel());

        List<ColourMapEntry> list = cm.getColourMapEntries();

        Double minVald = (Double) minVal.getPropertyDataSource().getValue();
        Double maxVald = (Double) maxVal.getPropertyDataSource().getValue();

        list.get(0).setValue(minVald);
        list.get(1).setValue(maxVald);

        return cm;
    }

    public void extractTiffMetaData(File tiffFile) {
        try {

            TiffMetaImpl tmi = new TiffMetaImpl();
            tme.extractTiffMetadata(tiffFile, tmi);

            minVal.setValue(tmi.getMinVal().toString());
            maxVal.setValue(tmi.getMaxVal().toString());

            System.out.println("Extracting tiff");

        } catch (UnknownCRSException e) {
            Notification.show("Unknown CRS found.", Type.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (Exception e) {
            Notification.show(e.getLocalizedMessage(), Type.ERROR_MESSAGE);
            e.printStackTrace();
        }

    }

    private void doPublish() {

        /*
         * Save the surface
         */
        if (!commit()) {
            logger.info("Ensure form is valid.");
            return;
        }

        if (getColourMap() == null) {
            showError("Null colourmap");
            return;
        }

        ColourMap cm = getColourMap();

        List<ColourMapEntry> cmes = cm.getColourMapEntries();
        if (cmes.isEmpty()) {
            showError("Invalid colour map.");
            return;
        }

        /*
         * Key section
         * 
         * If we have no layer, create a style and a name
         */
        if (getLayerName() == null) {

            /*
             * New layer required
             */
            String layerName = generateLayerName();
            setLayerName(layerName);

            boolean stylePublished = gsr.publishStyle(layerName, getColourMap()
                    .getColourMapEntries());
            logger.info("Style published: " + stylePublished);

            /*
             * Publish new data
             */
            File f = getEntity().getFile();
            boolean tiffPublished = publishTiff(cm, f);

            logger.info("Tiff published: " + tiffPublished);

            if (tiffPublished) {
                boolean surfaceSaved = commit();
                logger.info("Surface saved: " + surfaceSaved);

                firePublishEvent();
            }

        } else {

            /*
             * We have the layer already
             */
            File f = getEntity().getFile();

            /*
             * Always publishing the SLD. a little redundant but simpler.
             */
            gsr.updateStyle(getLayerName(), getColourMap()
                    .getColourMapEntries());

            /*
             * User wants to change the data.
             */
            if (f != null) {
                gsr.removeRasterStore(getLayerName());
                publishTiff(cm, f);
                firePublishEvent();
            }
        }

    }

    private boolean publishTiff(ColourMap cm, File f) {

        EcosystemServiceIndicator surface = getEntity();
        try {
            if (f == null) {
                Notification.show("File not uploaded yet.");
                return false;
            }
            return gsr
                    .publishTiff(f, surface.getSrid(), surface.getLayerName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            showError(e.getMessage());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            showError(e.getMessage());
        }
        return false;
    }

    private void showError(String message) {
        Notification.show(message, Type.ERROR_MESSAGE);
    }

    @Override
    protected void doPostDelete(EcosystemServiceIndicator entity) {
        gsr.removeRasterStore(entity.getLayerName());
        super.doPostDelete(entity);
    }

    private String generateLayerName() {
        return "esp-layer-"
                + dao.getNextValueInSequence("blueprint.geoserver_layer");
    }

    public void setPublishEventListener(LayerPublishedListener listener) {
        this.listener = listener;
    }

    /**
     * Publish event
     */
    private void firePublishEvent() {
        if (listener == null) {
            logger.error("Null listener");
        } else {
            listener.onLayerPublished(getLayerName(), getEntity().getEnvelope());
//                layerManager.setSurfaceLayerName(getLayerName());
//                layerManager.zoomTo(getEntity().getEnvelope());
        }
    }
}
