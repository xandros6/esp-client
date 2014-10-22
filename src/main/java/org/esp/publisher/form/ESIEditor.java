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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import org.esp.domain.blueprint.Status;
import org.esp.domain.blueprint.Study;
import org.esp.domain.blueprint.TemporalUnit_;
import org.esp.domain.publisher.ColourMap;
import org.esp.publisher.ESPClientUploadField;
import org.esp.publisher.GeoTiffPublisher;
import org.esp.publisher.GeoserverRestApi;
import org.esp.publisher.PublishException;
import org.esp.publisher.PublishedFileMetadata;
import org.esp.publisher.ShapefileMetadata;
import org.esp.publisher.ShapefilePublisher;
import org.esp.publisher.SpatialDataPublisher;
import org.esp.publisher.StylingMetadata;
import org.esp.publisher.UnknownCRSException;
import org.esp.publisher.colours.ColourMapFieldGroup;
import org.esp.publisher.colours.ColourMapFieldGroup.StyleChangeListener;
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
import com.vaadin.ui.ComboBox;
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
    public static final String SPATIAL_DATA_TYPE = "Spatial Data Type";
    private static final Long NOT_VALIDATED_STATUS = 2l;

    private Logger logger = LoggerFactory.getLogger(ESIEditor.class);

    private PolygonField envelopeField;

    private GeoTiffPublisher tme;

    private ColourMapFieldGroup colourMapFieldGroup;
    
    private ComboBox spatialDataTypeField;

    private GeoserverRestApi gsr;

    private LayerPublishedListener listener;

    private TextField sridField;

    private ESIEditorView esiEditorView;

    private TextArea spatialReferenceInfoField;

    private RoleManager roleManager;
    
    private ESPClientUploadField uploadField;
    
    private Map<Long, SpatialDataPublisher> filePublishers = new HashMap<Long, SpatialDataPublisher>();
    private static Map<Long, String> templates = new HashMap<Long, String>();
    
    static {
        
        
        templates.put(1l, "SldRaster.ftl");
        templates.put(2l, "SldVector.ftl");
    }

    @Inject
    public ESIEditor(Dao dao, RoleManager roleManager,
            GeoTiffPublisher tme, GeoserverRestApi gsr) {

        super(EcosystemServiceIndicator.class, dao);

        filePublishers.put(1l, new GeoTiffPublisher(gsr));
        filePublishers.put(2l, new ShapefilePublisher(gsr));
        
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
        super.doUpdate(entity);
        esiEditorView.setNewStatus(false);
        SpatialDataType spatialDataType = entity.getSpatialDataType();
        SpatialDataPublisher filePublisher = filePublishers.get(spatialDataType.getId());
        List<String> attributes;
        try {
            attributes = filePublisher.getAttributes(entity.getLayerName());
            colourMapFieldGroup.setAttributes(attributes, entity.getAttributeName());
            
        } catch (PublishException e) {
            Notification.show("Error getting attributes for the layer: " + e.getMessage(), Type.ERROR_MESSAGE);
        }
        
        uploadField.updateSpatialDataType(spatialDataType);
        
        colourMapFieldGroup.enableUpdateStyle(true);
    }

    @Override
    public void doCreate() {

        esiEditorView.setNewStatus(true);
        super.doCreate();
        
        
        EcosystemServiceIndicator entity = getEntity();
        
        Status status = new Status();
        status.setId(NOT_VALIDATED_STATUS);
        entity.setStatus(status);
        
        entity.setIntervalsNumber(ColourMapFieldGroup.DEFAULT_N_INTERVALS);
        
        colourMapFieldGroup.setDefaultValues();
    }
    
    @Override
    protected void doPreCommit(EcosystemServiceIndicator entity) {
        entity.setRole(roleManager.getRole());
    }

    @Override
    protected void doPostCommit(EcosystemServiceIndicator entity) {
        esiEditorView.setNewStatus(false);
        uploadField.updateSpatialDataType(entity.getSpatialDataType());
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
        
        
        spatialDataTypeField = (ComboBox)ff.addField(EcosystemServiceIndicator_.spatialDataType);
        addFieldGroup(THE_ECOSYSTEM_SERVICE);

        /*
         * Uploading
         */
        uploadField = new ESPClientUploadField();

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

        
        colourMapFieldGroup.setUpdateStyleListener(new StyleChangeListener() {
            
            @Override
            public void onValueChanged(ColourMap colourMap, String attributeName,
                    String classificationMethod, int intervalsNumber) {
                updateStyle(colourMapFieldGroup);
                
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


    protected void updateColourMap(ColourMap colourMap) {
        String layerName = getLayerName();
        if (colourMapFieldGroup.isValid() && layerName != null) {

            try {
                updateStyle(layerName, colourMapFieldGroup);
                updateEntityDate();
            } catch (PublishException e) {
                showError("Error updating style: " + e.getMessage());
            }
            firePublishEvent();
        }
    }

    private void updateEntityDate() {
        getEntity().setDateUpdated(Calendar.getInstance().getTime());
    }
    
    protected void updateAttributeBasedStyle(String attributeName, String classificationMethod,
            int intervalsNumber) {
        EcosystemServiceIndicator entity = getEntity();
        if(entity != null) {
            String layerName = entity.getLayerName();
            if(colourMapFieldGroup.isValid() && layerName != null) {
                double[] extrema = gsr.getExtrema(layerName, attributeName);
                if(extrema != null) {
                    Double minVal = extrema[0];
                    Double maxVal = extrema[1];
                    if (minVal < 0) {
                        colourMapFieldGroup.setMinValue("0");
                    } else {
                        colourMapFieldGroup.setMinValue(minVal.toString());
                    }
                    colourMapFieldGroup.setMaxValue(maxVal.toString());
                    try {
                        updateStyle(layerName, colourMapFieldGroup);
                        updateEntityDate();
                        firePublishEvent();
                    } catch (PublishException e) {
                        showError("Error updating style: " + e.getMessage());
                    }
                } else {
                    showError("Cannot retrieve min - max values for the layer");
                }
                
                
            }
        } else {
            
        }
    }

    private boolean updateStyle(String layerName, StylingMetadata metadata) throws PublishException {
        SpatialDataType spatialDataType = (SpatialDataType)spatialDataTypeField.getValue();
        Long spatialDataTypeId = spatialDataType.getId();
        SpatialDataPublisher filePublisher = filePublishers.get(spatialDataTypeId);
        return filePublisher.updateStyle(layerName, templates.get(spatialDataTypeId), metadata);
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
     * Publish an uploaded file.
     * 
     * @param f
     */
    private void publishFile(File f) {
        SpatialDataType spatialDataType = (SpatialDataType)spatialDataTypeField.getValue();
        
        String layerName = getLayerName();
        // new file
        if(layerName == null) {
            layerName = generateLayerName();
        }
        try {
            publishFile(f, layerName, spatialDataType);
        } catch (UnknownCRSException e) {
            showError("Error extracting  CRS: " + e.getMessage());
        } catch (PublishException e) {
            showError("Publishing error: " + e.getMessage());
        }
    }
    
    /**
     * Publish a new uploaded file of a specific data type with the given layer name.
     * 
     * @param f
     * @param layerName
     * @param spatialDataType
     * @throws UnknownCRSException
     * @throws PublishException
     */
    private void publishFile(File f, String layerName, SpatialDataType spatialDataType) throws UnknownCRSException, PublishException {
        Long spatialDataTypeId = spatialDataType.getId();
        // we use a different publisher for each kind of data type
        // to take into account a different publishing workflow
        // for each of them
        SpatialDataPublisher filePublisher = filePublishers.get(spatialDataTypeId);
        
        // extracts basic metadata from the uploaded file to store them in the EPS-Client database
        PublishedFileMetadata metadata = filePublisher.extractMetadata(f ,layerName);
        
        // update UI with the extracted metadata (min - max, srs, etc.)
        updateUIAfterPublish(metadata);
        
        if (getLayerName() == null) {
            // new layer, set the chosen layer name
            setLayerName(layerName);
        } else {
            // remove the old published data, before replacing it with the new one
            removePublishedEntity(getEntity());
        }
        // create a basic style for the layer
        String styleTemplate = templates.get(spatialDataTypeId);
        String styleName = filePublisher.createStyle(metadata, layerName,
                styleTemplate, colourMapFieldGroup.getColourMap());
        
        if(styleName != null) {
            logger.info("Basic style created: " + styleName);
            // publish the new layer
            if(filePublisher.createLayer(layerName, styleName, metadata)) {
                logger.info("Layer created: " + layerName);
                // some data types require the layer to be published before
                // we can create the final style for it
                if(filePublisher.supportsAdHocStyling()) {
                    if(updateStyle(layerName, colourMapFieldGroup)) {
                        logger.info("Ad hoc style created");
                    } else {
                        throw new PublishException("Error creating ad hoc style");
                    }
                }
                boolean saved = commitForm(false);
                if(saved) {
                    // enable the Update Style button
                    colourMapFieldGroup.enableUpdateStyle(true);
                }
                logger.info("File saved: " + saved);

                firePublishEvent();
            } else {
                throw new PublishException("Error publishing layer");
            }
        } else {
            throw new PublishException("Error creating style");
        }
    }

    /**
     * Updates the UI after a new layer has been updloaded and published.
     * Metadata extracted from the file is shown to the user.
     * 
     * @param metadata
     */
    private void updateUIAfterPublish(PublishedFileMetadata metadata) {
        Double minVal = metadata.getMinVal();

        // A bit hacky but there's no good way to determine real no-data
        // values.
        if (minVal < 0) {
            colourMapFieldGroup.setMinValue("0");
        } else {
            colourMapFieldGroup.setMinValue(metadata.getMinVal().toString());
        }
        colourMapFieldGroup.setMaxValue(metadata.getMaxVal().toString());

        sridField.setValue(metadata.getSrid().toString());

        spatialReferenceInfoField.setValue(metadata
                .getDescription());

        envelopeField.setValue((LinearRing) metadata.getEnvelope().getBoundary());
        
        if(metadata instanceof ShapefileMetadata) {
            ShapefileMetadata shapefileMeta = (ShapefileMetadata) metadata;
            colourMapFieldGroup.setAttributes(shapefileMeta.getAttributes(), shapefileMeta.getAttributeName());
        }
    }

    private void showError(String message) {
        Notification.show(message, Type.ERROR_MESSAGE);
    }

    @Override
    protected void doPostDelete(EcosystemServiceIndicator entity) {
        try {
            removePublishedEntity(entity);
            UI.getCurrent().getNavigator().navigateTo(ViewModule.HOME);
            super.doPostDelete(entity);
        } catch (PublishException e) {
            showError("Error during delete: " + e.getMessage());
        }
    }

    private void removePublishedEntity(EcosystemServiceIndicator entity) throws PublishException {
        if(isRaster(entity)) {
            gsr.removeRasterStore(entity.getLayerName());
        } else {
            gsr.removeShapefile(entity.getLayerName());
        }
        gsr.removeStyle(entity.getLayerName());
    }
    

    private boolean isRaster(EcosystemServiceIndicator entity) {
        return entity.getSpatialDataType() != null && entity.getSpatialDataType().getId() == 1;
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
            listener.onLayerPublished(getLayerName(), getEntity().getEnvelope(), getEntity().getTimestamp());
        }
    }

    private void updateStyle(StylingMetadata metadata) {
        String layerName = getLayerName();
        String attributeName = metadata.getAttributeName();
        if (colourMapFieldGroup.isValid() && layerName != null) {
            try {

                if(attributeName != null) {
                    double[] extrema = gsr.getExtrema(layerName, attributeName);
                    if(extrema != null) {
                        Double minVal = extrema[0];
                        Double maxVal = extrema[1];
                        if (minVal < 0) {
                            colourMapFieldGroup.setMinValue("0");
                        } else {
                            colourMapFieldGroup.setMinValue(minVal.toString());
                        }
                        colourMapFieldGroup.setMaxValue(maxVal.toString());
                    } else {
                        showError("Cannot retrieve min - max values for the layer");
                        return;
                    }
                }
                updateStyle(layerName, metadata);
                updateEntityDate();
            } catch (PublishException e) {
                showError("Error updating style: " + e.getMessage());
            }
            firePublishEvent();
        }
    }

    
}
