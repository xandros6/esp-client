package org.esp.publisher.form;

import it.jrc.auth.RoleManager;
import it.jrc.domain.adminunits.Grouping;
import it.jrc.domain.adminunits.Grouping_;
import it.jrc.form.FieldGroup;
import it.jrc.form.component.YearField;
import it.jrc.form.controller.EditorController;
import it.jrc.form.view.DefaultEditorView;
import it.jrc.form.view.IEditorView;
import it.jrc.persist.Dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Locale;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;

import org.esp.domain.blueprint.ArealUnit_;
import org.esp.domain.blueprint.DataSource;
import org.esp.domain.blueprint.DataSource_;
import org.esp.domain.blueprint.EcosystemServiceIndicator;
import org.esp.domain.blueprint.EcosystemServiceIndicator_;
import org.esp.domain.blueprint.Indicator_;
import org.esp.domain.blueprint.QuantificationUnit_;
import org.esp.domain.blueprint.SpatialDataType;
import org.esp.domain.blueprint.Study;
import org.esp.domain.blueprint.TemporalUnit_;
import org.esp.domain.publisher.ColourMap;
import org.esp.domain.publisher.ColourMapEntry;
import org.esp.publisher.GeoserverRestApi;
import org.esp.publisher.TiffMeta;
import org.esp.publisher.TiffMetadataExtractor;
import org.esp.publisher.TiffUploadField;
import org.esp.publisher.UnknownCRSException;
import org.esp.publisher.colours.ColourMapFieldGroup;
import org.esp.publisher.colours.ColourMapFieldGroup.ColourMapChangeListener;
import org.esp.publisher.ui.ViewModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.util.CRSTranslator;

import com.google.inject.Inject;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.ui.Field;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.UI;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

public class ESIEditor extends EditorController<EcosystemServiceIndicator> {

    public static final String THE_ECOSYSTEM_SERVICE = "The Ecosystem Service";

    private Logger logger = LoggerFactory.getLogger(ESIEditor.class);

    private PolygonField envelopeField;

    private TiffMetadataExtractor tme;

    private ColourMapFieldGroup colourMapFieldGroup;

    private GeoserverRestApi gsr;

    private LayerPublishedListener listener;

    private TextField sridField;

    private ESIEditorView esiEditorView;

    private TextArea spatialReferenceInfoField;

    private RoleManager roleManager;

    @Inject
    public ESIEditor(Dao dao, RoleManager roleManager,
            TiffMetadataExtractor tme, GeoserverRestApi gsr) {

        super(EcosystemServiceIndicator.class, dao);

        this.tme = tme;
        this.gsr = gsr;
        this.roleManager = roleManager;

        buildPublishForm();
        buildMetaForm();
    }

    @Override
    public void init(IEditorView<EcosystemServiceIndicator> view) {
        this.setContent(view);

        esiEditorView = (ESIEditorView) view;

        List<FieldGroup<EcosystemServiceIndicator>> fieldGroups = getFieldGroups();
        view.buildForm(fieldGroups);

        buildSubmitPanel(view.getSubmitPanel());
    }

    @Override
    public void doUpdate(EcosystemServiceIndicator entity) {
        if (!roleManager.isOwner(entity) && !roleManager.getRole().getIsSuperUser()) {
            Notification.show("You do not have permission to edit this.", Type.ERROR_MESSAGE);
            UI.getCurrent().getNavigator().navigateTo(ViewModule.HOME);
            return;
        }

        esiEditorView.setNewStatus(false);
        super.doUpdate(entity);
    }

    @Override
    public void doCreate() {

        esiEditorView.setNewStatus(true);
        super.doCreate();
        colourMapFieldGroup.setDefaultValues();
    }
    
    @Override
    protected void doPreCommit(EcosystemServiceIndicator entity) {
        entity.setRole(roleManager.getRole());
        //hack!
        entity.setSpatialDataType(dao.find(SpatialDataType.class, 1l));
    }

    @Override
    protected void doPostCommit(EcosystemServiceIndicator entity) {
        esiEditorView.setNewStatus(false);
    }

    private void buildPublishForm() {

        ff.addField(EcosystemServiceIndicator_.ecosystemService);
        getFieldWithPopup(EcosystemServiceIndicator_.indicator,
                Indicator_.label);

        /*
         * The study
         */
        EditableField<Study> studyField = new EditableCombo<Study>(
                Study.class, dao);
        InlineStudyEditor studyEditor = new InlineStudyEditor(dao, roleManager);
        studyField.setEditor(studyEditor);
        studyEditor.init(new DefaultEditorView<Study>());
        ff.addField(EcosystemServiceIndicator_.study, studyField);

        addFieldGroup(THE_ECOSYSTEM_SERVICE);

        /*
         * Uploading
         */
        TiffUploadField uploadField = new TiffUploadField();

        uploadField.addListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                File f = (File) event.getProperty().getValue();

//                boolean success = ESIEditor.this.isFormValid();
//                if (!success) {
//                    Notification.show("Please ensure the form is valid before continuing.");
//                    return;
//                }
//
//                Preconditions.checkArgument(f != null, "Where is the file?");
                publishFile(f);
            }

        });

        ff.addField("file", uploadField);

        colourMapFieldGroup = new ColourMapFieldGroup(dao);
        // Add to the field groups
        getFieldGroups().add(colourMapFieldGroup);

        colourMapFieldGroup.setValueChangeListener(new ColourMapChangeListener() {

            @Override
            public void onValueChanged(ColourMap colourMap) {

                List<ColourMapEntry> colourMapEntries = colourMap
                        .getColourMapEntries();

                if (colourMapFieldGroup.isValid() && getLayerName() != null) {


                    // Is it published yet?
                    gsr.updateStyle(getLayerName(), colourMapEntries);


                    firePublishEvent();
                }

            }

        });

        this.sridField = ff.addTextField(EcosystemServiceIndicator_.srid);
        sridField.setVisible(false);
        sridField.setConverter(new StringToIntegerConverter());

        spatialReferenceInfoField = ff
                .addTextArea(EcosystemServiceIndicator_.spatialReferenceInfo);
        spatialReferenceInfoField.setReadOnly(true);

        getGeometryField();

        // Invisible form field
        ff.addField(EcosystemServiceIndicator_.envelope, envelopeField);
        envelopeField.setVisible(false);

        addFieldGroup("Geospatial data");
    }


    private void buildMetaForm() {

        ff.addField(EcosystemServiceIndicator_.ecosystemServiceAccountingType);
        ff.addField(EcosystemServiceIndicator_.ecosystemServiceBenefitType);

        addFieldGroup("Accounting");


        getFieldWithPopup(
                EcosystemServiceIndicator_.quantificationUnit,
                QuantificationUnit_.label,
                QuantificationUnit_.quantificationUnitCategory);

        getFieldWithPopup(EcosystemServiceIndicator_.arealUnit,
                ArealUnit_.label);
        getFieldWithPopup(EcosystemServiceIndicator_.temporalUnit,
                TemporalUnit_.label);

        addFieldGroup("Quantification");

        ff.addField(EcosystemServiceIndicator_.startYear, new YearField());
        ff.addField(EcosystemServiceIndicator_.endYear, new YearField());
        ff.addField(EcosystemServiceIndicator_.spatialLevel);

        // Quick hack to get a filtered TwinColSelect
        TwinColSelect groupingField = new TwinColSelect();
        ff.addField(EcosystemServiceIndicator_.regions, groupingField);
        List<Grouping> groupings = dao.all(Grouping.class, Grouping_.id);
        for (Grouping grouping : groupings) {
            if (grouping.getGroupingType().getId().equals("Region")) {
                groupingField.addItem(grouping);
            }
        }

        groupingField.setCaption("Regions");

        addFieldGroup("Spatio-temporal");

        ff.addField(EcosystemServiceIndicator_.quantificationMethod);

        {
            EditableTwinColSelect<DataSource> dataSourceField = new EditableTwinColSelect<DataSource>(DataSource.class, dao);
            dataSourceField.setEditor(new EditorController<DataSource>(DataSource.class, dao) {
                {
                    ff.addField(DataSource_.label);
                    ff.addField(DataSource_.url);
                    addFieldGroup("");
                    init(new DefaultEditorView<DataSource>());
                }
            });
            ff.addField(EcosystemServiceIndicator_.dataSources, dataSourceField);
        }

        addFieldGroup("Model and data");

        ff.addField(EcosystemServiceIndicator_.minimumMappingUnit);

        addFieldGroup("Spatial data");

        ff.addField(EcosystemServiceIndicator_.biomes);

        ff.addField(EcosystemServiceIndicator_.studyObjectiveMet);
        ff.addTextArea(EcosystemServiceIndicator_.comments);

        addFieldGroup("Other");

    }
    
    public <X> Field<X> getFieldWithPopup(
            Attribute<? extends EcosystemServiceIndicator, X> prop,
            final SingularAttribute<X, ?>... childProps) {

        EditableField<X> c = new EditableCombo<X>(prop.getJavaType(), dao);
        c.setEditor(new EditorController<X>(prop.getJavaType(), dao) {
            {
                for (int i = 0; i < childProps.length; i++) {
                    ff.addField(childProps[i]);
                }
                addFieldGroup("");
                init(new DefaultEditorView<X>());
            }
        });
        
        ff.addField(prop, c);
        return c;
    }

    /**
     * TODO all JTS Field stuff should be refactored
     */
    private void getGeometryField() {

        envelopeField = new PolygonField();

        envelopeField.setConverter(new Converter<LinearRing, Polygon>() {

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

        envelopeField.setCRSTranslator(new CRSTranslator<Geometry>() {

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

    }

    /**
     * Access the hidden map within the envelope field.
     * 
     * @return
     */
    public LMap getMap() {
        return envelopeField.getMap();
    }

    public String getLayerName() {
        if (getEntity() == null) {
            return null;
        }
        return getEntity().getLayerName();
    }

    public void setLayerName(String layerName) {
        if (getEntity() == null) {
            return;
        }
        getEntity().setLayerName(layerName);
    }

    /**
     * 
     * 
     * @param f
     */
    private void publishFile(File f) {

        TiffMeta tm = extractTiffMetaData(f);

        if (tm == null) {
            return;
        }

        /*
         * Save the surface
         */
//        if (!commitForm(false)) {
//            logger.info("Ensure form is valid.");
//            return;
//        }

        ColourMap cm = colourMapFieldGroup.getColourMap();
        if (cm == null) {
            showError("Null colourmap");
            return;
        }

        List<ColourMapEntry> cmes = cm.getColourMapEntries();
        if (cmes.isEmpty()) {
            showError("Invalid colour map.");
            return;
        }

        /*
         * If we have no layer, create a style and a name
         */
        if (getLayerName() == null) {

            /*
             * New layer required
             */
            String layerName = generateLayerName();
            setLayerName(layerName);

            boolean stylePublished = gsr.publishStyle(layerName, cm.getColourMapEntries());
            logger.info("Style published: " + stylePublished);

            /*
             * Publish new data
             */
            boolean tiffPublished = publishTiff(cm, f, tm);

            logger.info("Tiff published: " + tiffPublished);

            if (tiffPublished) {
                boolean surfaceSaved = commitForm(false);
                logger.info("Surface saved: " + surfaceSaved);

                firePublishEvent();
            }

        } else {

            /*
             * We have the layer already
             */
//             File f = getEntity().getFile();

            /*
             * Always publishing the SLD. a little redundant but simpler.
             */
            gsr.updateStyle(getLayerName(), cm.getColourMapEntries());

            /*
             * User wants to change the data.
             */
            if (f != null) {
                gsr.removeRasterStore(getLayerName());
                publishTiff(cm, f, tm);
                firePublishEvent();
            }
        }

        Notification
                .show("Published successfully.",
                        "Please note the max-min values will require editing manually.  The extent may need to be adjusted for optimal display in Web Mercator projection.", Type.HUMANIZED_MESSAGE);
        commitForm(false);

    }

    private TiffMeta extractTiffMetaData(File tiffFile) {

        try {

            TiffMeta tmi = new TiffMeta();
            tme.extractTiffMetadata(tiffFile, tmi);

            Double minVal = tmi.getMinVal();

            // A bit hacky but there's no good way to determine real no-data
            // values.
            if (minVal < 0) {
                colourMapFieldGroup.setMinValue("0");
            }
            colourMapFieldGroup.setMaxValue(tmi.getMaxVal().toString());

            sridField.setValue(tmi.getSrid().toString());

            spatialReferenceInfoField.setValue(tmi
                    .getSpatialReferenceDescription());

            envelopeField.setValue((LinearRing) tmi.getEnvelope().getBoundary());

            return tmi;

        } catch (UnknownCRSException e) {
            Notification.show("Unknown CRS found.", Type.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (Exception e) {
            Notification.show(e.getLocalizedMessage(), Type.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return null;

    }

    private boolean publishTiff(ColourMap cm, File f, TiffMeta tm) {

        EcosystemServiceIndicator surface = getEntity();
        try {
            if (f == null) {
                Notification.show("File not uploaded yet.");
                return false;
            }

            /*
             * TODO:
             * 
             * Is the null stylename a good idea?  
             * (detected in following method)
             * 
             * Uses default style when is a three band raster.
             */
            String styleName = null;
            if (tm.getNumSampleDimensions() == 3) {

                styleName = "raster";
            }

            return gsr.publishTiff(f, tm.getSrid(), surface.getLayerName(), styleName);

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
        UI.getCurrent().getNavigator().navigateTo(ViewModule.HOME);
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
        }
    }
}
