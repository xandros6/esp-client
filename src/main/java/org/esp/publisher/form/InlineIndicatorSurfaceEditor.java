package org.esp.publisher.form;

import it.jrc.auth.RoleManager;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.esp.domain.blueprint.ArealUnit_;
import org.esp.domain.blueprint.EcosystemService;
import org.esp.domain.blueprint.EcosystemServiceIndicator;
import org.esp.domain.blueprint.EcosystemServiceIndicator_;
import org.esp.domain.blueprint.EcosystemService_;
import org.esp.domain.blueprint.Indicator_;
import org.esp.domain.blueprint.QuantificationUnit_;
import org.esp.domain.blueprint.TemporalUnit_;
import org.esp.domain.publisher.ColourMap;
import org.esp.domain.publisher.ColourMapEntry;
import org.esp.publisher.TiffMetaImpl;
import org.esp.publisher.TiffMetadataExtractor;
import org.esp.upload.old.UnknownCRSException;
import org.jrc.form.component.SelectionTable;
import org.jrc.form.filter.YearField;
import org.jrc.persist.Dao;
import org.jrc.persist.adminunits.Grouping;
import org.jrc.persist.adminunits.Grouping_;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.util.CRSTranslator;

import com.google.inject.Inject;
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

public class InlineIndicatorSurfaceEditor extends
        CutDownBaseEditor<EcosystemServiceIndicator> {
    
    Logger logger = LoggerFactory.getLogger(IndicatorEditor.class);

    private PolygonField f;
    private TiffMetadataExtractor tme;
    private TextField minVal;
    private TextField maxVal;
    private ColourMapField cmf;


    @Inject
    public InlineIndicatorSurfaceEditor(Dao dao, RoleManager roleManager,
            TiffMetadataExtractor tme) {

        super(EcosystemServiceIndicator.class, dao);

        this.tme = tme;
        
        buildPublishForm();

        buildMetaForm();
    }


    private void buildPublishForm() {

        ff.addField(EcosystemServiceIndicator_.ecosystemService);
        ff.addSelectAndCreateField(EcosystemServiceIndicator_.indicator, Indicator_.label);
        ff.addField(EcosystemServiceIndicator_.study);

        cmf = new ColourMapField(dao);
        ff.addField(EcosystemServiceIndicator_.colourMap, cmf);

        this.minVal = ff.addTextField(EcosystemServiceIndicator_.minVal);
        this.maxVal = ff.addTextField(EcosystemServiceIndicator_.maxVal);
        StringToDoubleConverter std = new StringToDoubleConverter();
        minVal.setConverter(std);
        maxVal.setConverter(std);

//        ff.addField(EcosystemServiceIndicator_.layerName);
//        ff.addField(EcosystemServiceIndicator_.pixelSizeX);
//        ff.addField(EcosystemServiceIndicator_.pixelSizeY);

        getGeometryField();

        // Invisible form field
        ff.addField(EcosystemServiceIndicator_.envelope, f);
        f.setVisible(false);

        addFieldGroup("Other");
    }
    
    
    private void buildMetaForm() {

//        SelectionTable<EcosystemService> st = ff.addSelectionTable(EcosystemServiceIndicator_.ecosystemService);
//        st.addColumn(EcosystemService_.label, "Name");
//        st.addFilterField(EcosystemService_.ecosystemServiceCategory, "Filter by category");
        
//        ff.addField(EcosystemServiceIndicator_.study);

        ff.addField(EcosystemServiceIndicator_.ecosystemServiceAccountingType);
        ff.addField(EcosystemServiceIndicator_.ecosystemServiceBenefitType);
        
//        addFieldGroup("The Ecosystem Service");
        
        ff.addSelectAndCreateField(EcosystemServiceIndicator_.quantificationUnit, QuantificationUnit_.label, QuantificationUnit_.quantificationUnitCategory);
        ff.addSelectAndCreateField(EcosystemServiceIndicator_.arealUnit, ArealUnit_.label);
        ff.addSelectAndCreateField(EcosystemServiceIndicator_.temporalUnit, TemporalUnit_.label);
        
//        addFieldGroup("Quantification");
        
        ff.addField(EcosystemServiceIndicator_.startYear, new YearField());
        ff.addField(EcosystemServiceIndicator_.endYear, new YearField());
        ff.addField(EcosystemServiceIndicator_.spatialLevel);
        
        //Quick hack to get a filtered TwinColSelect
        TwinColSelect groupingField = new TwinColSelect();
        ff.addField(EcosystemServiceIndicator_.groupings, groupingField);
        List<Grouping> groupings = dao.all(Grouping.class, Grouping_.id);
        for (Grouping grouping : groupings) {
            if (grouping.getGroupingType().getId().equals("Region")) {
                groupingField.addItem(grouping);
            }
        }
        groupingField.setCaption("Regions");
        
//        addFieldGroup("Spatio-temporal");
        
        ff.addField(EcosystemServiceIndicator_.quantificationMethod);

        ff.addField(EcosystemServiceIndicator_.dataSources);
        
//        addFieldGroup("Model and data");
        
        ff.addField(EcosystemServiceIndicator_.minimumMappingUnit);
        
//        addFieldGroup("Spatial data");
        
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

    public String getLayerName() {
        return getEntity().getLayerName();
    }

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

}
