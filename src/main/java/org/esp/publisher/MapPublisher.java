package org.esp.publisher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.esp.domain.blueprint.EcosystemServiceIndicator;
import org.esp.domain.blueprint.IndicatorSurface;
import org.esp.domain.publisher.ColourMap;
import org.esp.domain.publisher.ColourMapEntry;
import org.esp.publisher.TiffUploader.ProcessingCompleteEvent;
import org.esp.publisher.form.InlineEcosystemServiceIndicatorEditor;
import org.esp.publisher.form.InlineIndicatorSurfaceEditor;
import org.esp.upload.old.UnknownCRSException;
import org.jrc.persist.Dao;
import org.jrc.ui.SimpleHtmlHeader;
import org.jrc.ui.SimplePanel;
import org.jrc.ui.baseview.TwinPanelView;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.easyuploads.UploadField;

import com.google.inject.Inject;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

/***
 * Interface for publishing maps.
 * 
 * Colour maps are stored in database entities. TODO: allow for choosing either
 * from default colour maps or personal ones
 * 
 * 
 * [ ] When editing colour schemes it can be republished to geoserver.
 * 
 * [ ] Support for multiple formats (geotiff, world * file etc) [ ]
 * 
 * @author Will Temperley
 * 
 */
public class MapPublisher extends TwinPanelView implements View {
    
    private static final String UPLOAD_MESSAGE = "??";

    private Logger logger = LoggerFactory.getLogger(MapPublisher.class);

    // private CartographicKeyEditor cartographicKeyEditor;
//    private TiffUploader tiffUploader;
//    private TiffMetadataExtractor tme;
    
    private GeoserverRestApi gsr;
    private Dao dao;
    private LayerManager layerManager;

    private IndicatorSurface indicatorSurface;
    private InlineEcosystemServiceIndicatorEditor esiEditor;
    private InlineIndicatorSurfaceEditor surfaceEditor;
    private EcosystemServiceIndicator esi;

    private TiffMetadataExtractor tme;

    private TiffUploadField uploadField;

    @Inject
    public MapPublisher(GeoserverRestApi gsr, Dao dao,
            InlineEcosystemServiceIndicatorEditor esiEditor,
            InlineIndicatorSurfaceEditor surfaceEditor) {

        this.gsr = gsr;
        this.dao = dao;

        this.esiEditor = esiEditor;
        this.surfaceEditor = surfaceEditor;
        this.tme = new TiffMetadataExtractor(dao);

        {
            VerticalLayout vl = new VerticalLayout();
            vl.setSizeFull();

            LMap map = surfaceEditor.getMap();
            layerManager = new LayerManager(map);
            vl.addComponent(map);
            map.setSizeFull();

            getLeftPanel().addComponent(vl);
        }

        {
            SimplePanel rightPanel = getRightPanel();

            TabSheet tabSheet = new TabSheet();
            tabSheet.setSizeFull();
            replaceComponent(rightPanel, tabSheet);

            tabSheet.addTab(addPublishingComponents(), "Maps");
            tabSheet.addTab(addMetaComponents(), "Metadata");
        }

    }

    private Component addMetaComponents() {

        CssLayout displayPanel = new CssLayout();
        displayPanel.addStyleName("display-panel");
        displayPanel.addStyleName("display-panel-padded");
        displayPanel.setSizeFull();

        // displayPanel.addComponent(esiEditor);
        esiEditor.init(displayPanel);

        return displayPanel;
    }

    /**
     * Currently a bit of a mess
     * 
     * [ ] Update the state based on {@link IndicatorSurface} values [ ]
     * Potentially use polymorphic surface entities
     * 
     * @return
     */
    private CssLayout addPublishingComponents() {

        CssLayout displayPanel = new CssLayout();
        displayPanel.addStyleName("display-panel");
        displayPanel.addStyleName("display-panel-padded");
        displayPanel.setSizeFull();

        displayPanel.addComponent(new SimpleHtmlHeader("Upload"));

        uploadField = new TiffUploadField();

        uploadField.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                        File f = (File) event.getProperty().getValue();
                        
                        try {
                            tme.extractTiffMetadata(f, getIndicatorSurface());
                            setIndicatorSurface(getIndicatorSurface());
                            
                        } catch (FactoryException e) {
                            showError(UPLOAD_MESSAGE);
                            e.printStackTrace();
                        } catch (IOException e) {
                            showError(UPLOAD_MESSAGE);
                            e.printStackTrace();
                        } catch (TransformException e) {
                            showError(UPLOAD_MESSAGE);
                            e.printStackTrace();
                        } catch (UnknownCRSException e) {
                            showError(UPLOAD_MESSAGE);
                            e.printStackTrace();
                        }
                        
                        setIndicatorSurface(indicatorSurface);

            }
        });

        displayPanel.addComponent(uploadField);

        displayPanel.addComponent(new SimpleHtmlHeader("Key"));

        /*
         * Custom button passed to editor
         */
        Button publish = new Button("Publish");
        displayPanel.addComponent(publish);
        publish.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                doPublish();
            }
        });

        CssLayout editorPanel = new CssLayout();
        surfaceEditor.init(editorPanel, publish);
        displayPanel.addComponent(surfaceEditor);

        return displayPanel;
    }

    /**
     * Potential actions:
     * 
     * 
     * 1. New layer 2. Update layer data 3. Update layer style
     * 
     * 
     */
    private void doPublish() {
        
        System.out.println("PUBLISHING");

        /*
         * Save the surface
         */
        if (!surfaceEditor.commit()) {
            logger.info("Could not save form");
            return;
        }

        if (getIndicatorSurface().getColourMap() == null) {
            showError("Null colourmap");
            return;
        }

        ColourMap cm = getIndicatorSurface().getColourMap();

        List<ColourMapEntry> cmes = cm.getColourMapEntries();
        if (cmes.isEmpty()) {
           showError("Where are the CMES?");
           return;
        }

        if (getIndicatorSurface().getLayerName() == null) {

            /*
             * New layer required
             */
            String layerName = generateLayerName();
            getIndicatorSurface().setLayerName(layerName);
            setIndicatorSurface(getIndicatorSurface());

            boolean stylePublished = gsr.publishStyle(layerName, colourMapHack());
            logger.info("Style published: " + stylePublished);
            System.out.println("LAYER NAME in model:" + getIndicatorSurface().getLayerName());

            /*
             * Publish new data
             */
            File f = uploadField.getFile();
            boolean tiffPublished = publishTiff(cm, f);

            logger.info("Tiff published: " + tiffPublished);

            if (tiffPublished) {
                boolean surfaceSaved = surfaceEditor.commit();
                logger.info("Surface saved: " + surfaceSaved);
                setIndicatorSurface(indicatorSurface);
                
            }

        } else {

            /*
             * We have the layer already
             */
            File f = uploadField.getFile();

            /*
             * Always publishing the SLD. a little redundant but simpler.
             */
            gsr.updateStyle(getIndicatorSurface().getLayerName(), colourMapHack());

            /*
             * User wants to change the data.
             */
            if (f != null) {
                gsr.removeRasterStore(getIndicatorSurface().getLayerName());
                publishTiff(cm, f);
//                map.setIndicatorSurface(indicatorSurface);
            }
        }

    }
    
    private List<ColourMapEntry> colourMapHack() {
        List<ColourMapEntry> list = getIndicatorSurface().getColourMap().getColourMapEntries();
        list.get(0).setValue(getIndicatorSurface().getMinVal());
        list.get(1).setValue(getIndicatorSurface().getMaxVal());
        return list;
    }

    private boolean publishTiff(ColourMap cm, File f) {
        try {
            if (f == null) {
                Notification.show("File not uploaded yet.");
                return false;
            }
            return gsr.publishTiff(f, getIndicatorSurface().getSrid(), getIndicatorSurface().getLayerName());
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
    public void enter(ViewChangeEvent event) {

        String params = event.getParameters();
        if (params != null && !params.isEmpty()) {

            if (params.equals("new")) {
                newEsi();
            } else {
                doEditById(params);
            }

            // Assumes all entities are set up already.
            esiEditor.doUpdate(esi);
            surfaceEditor.doUpdate(getIndicatorSurface());

//            ColourMap cm = indicatorSurface.getColourMap();
            // cartographicKeyEditor.setColourMap(cm);
        }

    }

    private void newEsi() {
        esi = new EcosystemServiceIndicator();
        setIndicatorSurface(new IndicatorSurface());
        esi.setIndicatorSurface(getIndicatorSurface());

        // get colour map
        // FIXME hardcode 1l
        ColourMap cm = dao.find(ColourMap.class, 1l);
        getIndicatorSurface().setColourMap(cm);

    }

    private void doEditById(String stringId) {
        try {
            Long id = Long.valueOf(stringId);
            EcosystemServiceIndicator entity = dao.find(
                    EcosystemServiceIndicator.class, id);
            if (entity == null) {
                Notification.show("No entity with id: " + stringId);
                esi = new EcosystemServiceIndicator();
            }
            esi = entity;
            setIndicatorSurface(esi.getIndicatorSurface());
            if (getIndicatorSurface() == null) {
                setIndicatorSurface(new IndicatorSurface());
            }

            logger.info("Layer has SRID: " + getIndicatorSurface().getEnvelope().getSRID());
//            map.setIndicatorSurface(indicatorSurface);


        } catch (NumberFormatException e) {
            Notification.show("This isn't a valid id: " + stringId);
        }
    }

    private String generateLayerName() {
        return "esp-layer-"
                + dao.getNextValueInSequence("blueprint.geoserver_layer");
    }


    // FIXME copy paste from ESI editor
    protected void doPreCommit(EcosystemServiceIndicator entity) {

        /*
         * Build the relationships
         */
        if (entity.getId() == null) {

            dao.getEntityManager().persist(entity);
            getIndicatorSurface().setEcosystemServiceIndicator(entity);
            dao.getEntityManager().persist(getIndicatorSurface());
            entity.setIndicatorSurface(getIndicatorSurface());
        }
    }

    private IndicatorSurface getIndicatorSurface() {
        return indicatorSurface;
    }

    private void setIndicatorSurface(IndicatorSurface indicatorSurface) {
        this.indicatorSurface = indicatorSurface;
        layerManager.setIndicatorSurface(indicatorSurface);
        surfaceEditor.doUpdate(getIndicatorSurface());
    }
}
