package org.esp.publisher.form;

import it.jrc.auth.RoleManager;
import it.jrc.domain.adminunits.Grouping;
import it.jrc.domain.adminunits.Grouping_;
import it.jrc.form.FieldGroup;
import it.jrc.form.component.FormConstants;
import it.jrc.form.component.YearField;
import it.jrc.form.controller.EditorController;
import it.jrc.form.view.DefaultEditorView;
import it.jrc.form.view.IEditorView;
import it.jrc.persist.Dao;

import java.io.File;
import java.lang.reflect.Member;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
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
import org.esp.publisher.SpatialDataPublisher;
import org.esp.publisher.SpatialDataPublishers;
import org.esp.publisher.StylingMetadata;
import org.esp.publisher.UnknownCRSException;
import org.esp.publisher.colours.ColourMapFieldGroup;
import org.esp.publisher.colours.ColourMapFieldGroup.ColourMapAttributeChangeListener;
import org.esp.publisher.colours.ColourMapFieldGroup.StyleChangeListener;
import org.esp.publisher.ui.ViewModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.util.CRSTranslator;

import com.google.inject.Inject;
import com.google.inject.name.Named;
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

    private ColourMapFieldGroup colourMapFieldGroup;
    
    private ComboBox spatialDataTypeField;

    private GeoserverRestApi gsr;

    private LayerPublishedListener listener;

    private TextField sridField;

    private ESIEditorView esiEditorView;

    private TextArea spatialReferenceInfoField;

    private RoleManager roleManager;
    
    private ESPClientUploadField uploadField;
    
    private Map<Long, Map<String, Integer>> limits = new HashMap<Long, Map<String, Integer>>();
    
    @Inject
    public ESIEditor(Dao dao, RoleManager roleManager,
            GeoserverRestApi gsr,
            @Named("geotiff_limit_mb") int geotiffSizeLimit,
            @Named("shapefile_limit_mb") int shapefileSizeLimit,
            @Named("shapefile_limit_records") int shapefileRecordsLimit) {

        super(EcosystemServiceIndicator.class, dao);
        
        this.gsr = gsr;
        SpatialDataPublishers.setGeoserverHandler(gsr);
        this.roleManager = roleManager;

        
        Map<String, Integer> rasterLimits = new HashMap<String, Integer>();
        rasterLimits.put("size", geotiffSizeLimit);
        limits.put(1l, rasterLimits);
        
        Map<String, Integer> vectorLimits = new HashMap<String, Integer>();
        vectorLimits.put("size", shapefileSizeLimit);
        vectorLimits.put("records", shapefileRecordsLimit);
        limits.put(2l, vectorLimits);
        
        SpatialDataPublishers.setLimits(limits);
        
        buildPublishForm();
        buildMetaForm();
    }

    @Override
    public void init(IEditorView<EcosystemServiceIndicator> view) {
        this.setContent(view);

        esiEditorView = (ESIEditorView) view;

        List<FieldGroup<EcosystemServiceIndicator>> fieldGroups = getFieldGroups();
        view.buildForm(fieldGroups);

        buildSubmitPanel(view.getTopSubmitPanel());
        buildSubmitPanel(view.getBottomSubmitPanel());
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
        SpatialDataPublisher filePublisher = getFilePublisher();
        Map<String, Class<?>> attributes;
        try {
            String layerName = entity.getLayerName();
            if(layerName != null) {
                attributes = filePublisher.getAttributes(layerName);
                String attributesInfo = filePublisher.getAttributesInfo(layerName);
                String symbolType = filePublisher.getGeometryType(layerName);
                
                updateUIStyle(layerName, attributesInfo, symbolType);
                colourMapFieldGroup.setAttributes(attributes, entity.getAttributeName());
            }
        } catch (PublishException e) {
            Notification.show("Error getting attributes for the layer: " + e.getMessage(), Type.ERROR_MESSAGE);
        }
        
        uploadField.updateSpatialDataType(entity.getSpatialDataType());
        
        colourMapFieldGroup.enableUpdateStyle(true);
    }

    private SpatialDataPublisher getFilePublisher(Long id) {
        return SpatialDataPublishers.getSpatialDataPublisher(id.intValue());
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
        entity.setDateUpdated(Calendar.getInstance().getTime());
        if(colourMapFieldGroup.getSLD() != null) {
            entity.setAttributeName("*");
        }
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
        uploadField = new ESPClientUploadField(limits);

        uploadField.addListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                File f = (File) event.getProperty().getValue();
                if(f == null) {
                    showError("File has not been correctly upload.");
                } else if(uploadField.checkLimits(f)) {

//                boolean success = ESIEditor.this.isFormValid();
//                if (!success) {
//                    Notification.show("Please ensure the form is valid before continuing.");
//                    return;
//                }
//
//                Preconditions.checkArgument(f != null, "Where is the file?");
                    publishFile(f);
                }
            }

        });

        ff.addField("file", uploadField);

        colourMapFieldGroup = new ColourMapFieldGroup(dao);
        

        
        colourMapFieldGroup.setUpdateStyleListener(new StyleChangeListener() {
            
            @Override
            public void onValueChanged(ColourMap colourMap, String attributeName,
                    String classificationMethod, int intervalsNumber, String SLD) {
                updateStyle(colourMapFieldGroup);
                
            }
        });
        
       

        colourMapFieldGroup.setAttributeListener(new ColourMapAttributeChangeListener() {
            
            @Override
            public void onValueChanged(String attributeName, Class<?> attributeType) {
                if(getLayerName() != null) {
                    if(!updateExtrema(getLayerName(), attributeName, attributeType)) {
                        showError("Unable to update min - max values");
                    }
                    
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
        
        // Add to the field groups
        getFieldGroups().add(colourMapFieldGroup);
    }
    
    

    /**
     * Updates a style definition with the given style metadata.
     * 
     * @param styleName
     * @param metadata
     * @return
     * @throws PublishException
     */
    private boolean updateStyle(String styleName, StylingMetadata metadata) throws PublishException {
        SpatialDataPublisher filePublisher = getFilePublisher();
        return filePublisher.updateStyle(styleName, filePublisher.getDefaultStyleTemplate(), metadata);
    }
    
    /**
     * Updates a style definition with the given style metadata.
     * 
     * @param styleName
     * @param metadata
     * @return
     * @throws PublishException
     */
    private void updateUIStyle(String styleName, String attributes, String symbolType) throws PublishException {
        SpatialDataPublisher filePublisher = getFilePublisher();
        String style = filePublisher.getPublishedStyle(styleName);
        
        if(style != null) {
            colourMapFieldGroup.updateStyle(styleName, style, symbolType, attributes);
        }
    }

    /**
     * Returns the appropriate SpatialDataPublisher for the current entity.
     * 
     * @return
     */
    private SpatialDataPublisher getFilePublisher() {
        return getFilePublisher(getEntity().getSpatialDataType().getId());
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
        groupingField.setWidth(FormConstants.LARGE_FIELD_DEFAULT_WIDTH);
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
                    ff.addField(DataSource_.name);
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

    /**
     * Updates the entity layer name.
     * 
     * @param layerName
     */
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
        SpatialDataPublisher filePublisher = getFilePublisher(spatialDataTypeId);
        
        // extracts basic metadata from the uploaded file to store them in the EPS-Client database
        PublishedFileMetadata metadata = filePublisher.extractMetadata(f ,layerName);
        
        // update UI with the extracted metadata (min - max, srs, etc.)
        updateUIAfterPublish(metadata);
        
        if (getLayerName() == null) {
            // new layer, set the chosen layer name
            setLayerName(layerName);
        } else {
            // remove the old published data, before replacing it with the new one
            unpublishEntity(getEntity());
        }
        // create a basic style for the layer
        String styleName = filePublisher.createStyle(metadata, layerName,
                filePublisher.getDefaultStyleTemplate(), colourMapFieldGroup.getColourMap());
        
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
                String attributesInfo = filePublisher.getAttributesInfo(layerName);
                String symbolType = filePublisher.getGeometryType(layerName);
                updateUIStyle(styleName, attributesInfo, symbolType);
                if(commitForm(false)) {
                    // enable the Update Style button
                    colourMapFieldGroup.enableUpdateStyle(true);
                    logger.info("File saved");
                } else {
                    // TODO: rollback
                    throw new PublishException("Error saving published layer metadata");
                }
                
                firePublishEvent();
            } else {
                // TODO: rollback
                throw new PublishException("Error publishing layer");
            }
        } else {
            // TODO: rollback
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
        if(metadata.getAttributes() != null && metadata.getAttributeName() != null) {
            colourMapFieldGroup.setAttributes(metadata.getAttributes(), metadata.getAttributeName());
        }
    }

    private void showError(String message) {
        Notification.show(message, Type.ERROR_MESSAGE);
    }

    @Override
    protected void doPostDelete(EcosystemServiceIndicator entity) {
        try {
            unpublishEntity(entity);
            UI.getCurrent().getNavigator().navigateTo(ViewModule.HOME);
            super.doPostDelete(entity);
        } catch (PublishException e) {
            showError("Error during delete: " + e.getMessage());
        }
    }

    /**
     * Removes all published objects related to an entity: layer, store, style, db table.
     * 
     * @param entity
     * @throws PublishException
     */
    private void unpublishEntity(EcosystemServiceIndicator entity) throws PublishException {
        getFilePublisher(entity.getSpatialDataType().getId()).unpublish(entity.getLayerName());
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

    private boolean updateExtrema(String layerName, String attributeName, Class<?> attributeType) {
        if(attributeType != null && Number.class.isAssignableFrom(attributeType)) {
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
                return true;
            } else {
                return false;
            }
        } else {
            colourMapFieldGroup.setMinValue("0");
            colourMapFieldGroup.setMaxValue("1");
            return true;
        }
    }
    
    private void updateStyle(StylingMetadata metadata) {
        String layerName = getLayerName();
        String attributeName = metadata.getAttributeName();
        Class<?> attributeType = metadata.getAttributeType();
        String sld = metadata.getSLD();
        if (colourMapFieldGroup.isValid() && layerName != null) {
            try {
                
                if(attributeName != null && sld == null) {
                    if(!updateExtrema(layerName, attributeName, attributeType)) {
                        showError("Cannot retrieve min - max values for the layer");
                        return;
                    }
                }
                if(updateStyle(layerName, metadata)) {
                    updateUIStyle(layerName, null, null);
                    if(commitForm(false)) {
                        logger.info("Style updated");
                        firePublishEvent();
                    } else {
                        // TODO: rollback
                        throw new PublishException("Error saving published layer metadata");
                    }
                } else {
                 // TODO: rollback
                    throw new PublishException("Error updating style");
                }
            } catch (PublishException e) {
                showError("Error updating style: " + e.getMessage());
            }
            
        }
    }

    
}
