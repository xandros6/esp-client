package org.esp.publisher;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.esp.domain.blueprint.EcosystemServiceIndicator;
import org.esp.domain.publisher.ColourMap;
import org.esp.domain.publisher.ColourMapEntry;
import org.esp.publisher.form.IndicatorEditor;
import org.esp.publisher.form.InlineIndicatorSurfaceEditor;
import org.esp.publisher.form.ViewToRename;
import org.jrc.persist.Dao;
import org.jrc.ui.SimpleHtmlHeader;
import org.jrc.ui.SimplePanel;
import org.jrc.ui.baseview.TwinPanelView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.leaflet.LMap;

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
    
    private Logger logger = LoggerFactory.getLogger(MapPublisher.class);

    // private CartographicKeyEditor cartographicKeyEditor;
    
    private GeoserverRestApi gsr;
    private Dao dao;
    private LayerManager layerManager;

    private IndicatorEditor esiEditor;

    private InlineIndicatorSurfaceEditor surfaceEditor;

    private TiffUploadField uploadField;

    @Inject
    public MapPublisher(GeoserverRestApi gsr, Dao dao,
            IndicatorEditor esiEditor,
            InlineIndicatorSurfaceEditor surfaceEditor) {

        this.gsr = gsr;
        this.dao = dao;

        this.esiEditor = esiEditor;
        this.surfaceEditor = surfaceEditor;

        {
            VerticalLayout vl = new VerticalLayout();
            vl.setSizeFull();

            LMap map = surfaceEditor.getMap();
//            LMap map = new LMap();
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
        }
    }

    private Component addMetaComponents() {

        ViewToRename view = new ViewToRename();

        esiEditor.init(view);

        return view;
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

        ViewToRename displayPanel = new ViewToRename();

        uploadField = new TiffUploadField();

        uploadField.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                File f = (File) event.getProperty().getValue();
                surfaceEditor.extractTiffMetaData(f);
            }
        });

        /*
         * Custom button passed to editor
         */
        Button publish = new Button("Publish");
        publish.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                doPublish();
            }
        });

        displayPanel.addComponent(new SimpleHtmlHeader("Upload"));
        displayPanel.addComponent(uploadField);

        surfaceEditor.init(displayPanel);

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
        
        /*
         * Save the surface
         */
        if (!surfaceEditor.commit()) {
            logger.info("Could not save form");
            return;
        }

        if (surfaceEditor.getColourMap() == null) {
            showError("Null colourmap");
            return;
        }

        ColourMap cm = surfaceEditor.getColourMap();

        List<ColourMapEntry> cmes = cm.getColourMapEntries();
        if (cmes.isEmpty()) {
           showError("Where are the CMES?");
           return;
        }

        /*
         * Key section
         * 
         * If we have no layer, create a style and a name
         * 
         */
        if (surfaceEditor.getLayerName() == null) {

            /*
             * New layer required
             */
            String layerName = generateLayerName();
            surfaceEditor.setLayerName(layerName);

            boolean stylePublished = gsr.publishStyle(layerName, surfaceEditor.getColourMap().getColourMapEntries());
            logger.info("Style published: " + stylePublished);

            /*
             * Publish new data
             */
            File f = uploadField.getFile();
            boolean tiffPublished = publishTiff(cm, f);

            logger.info("Tiff published: " + tiffPublished);

            if (tiffPublished) {
                boolean surfaceSaved = surfaceEditor.commit();
                logger.info("Surface saved: " + surfaceSaved);

                layerManager.setSurfaceLayerName(surfaceEditor.getLayerName());
                layerManager.zoomTo(surfaceEditor.getEntity().getEnvelope());
            }

        } else {

            /*
             * We have the layer already
             */
            File f = uploadField.getFile();

            /*
             * Always publishing the SLD. a little redundant but simpler.
             */
            gsr.updateStyle(surfaceEditor.getLayerName(), surfaceEditor.getColourMap().getColourMapEntries());

            /*
             * User wants to change the data.
             */
            if (f != null) {
                gsr.removeRasterStore(surfaceEditor.getLayerName());
                publishTiff(cm, f);
                layerManager.setSurfaceLayerName(surfaceEditor.getLayerName());
            }
        }

    }

    private boolean publishTiff(ColourMap cm, File f) {
        
        EcosystemServiceIndicator surface = surfaceEditor.getEntity();
        try {
            if (f == null) {
                Notification.show("File not uploaded yet.");
                return false;
            }
            return gsr.publishTiff(f, surface.getSrid(), surface.getLayerName());
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
                surfaceEditor.doCreate();
                EcosystemServiceIndicator entity = surfaceEditor.getEntity();
                esiEditor.doUpdate(entity);
            } else {
                doEditById(params);
            }

            // Assumes all entities are set up already.

//            ColourMap cm = indicatorSurface.getColourMap();
            // cartographicKeyEditor.setColourMap(cm);
        }

    }


    private void doEditById(String stringId) {
        try {
            Long id = Long.valueOf(stringId);
            EcosystemServiceIndicator entity = dao.find(EcosystemServiceIndicator.class, id);

            if (entity == null) {
                Notification.show("No entity with id: " + stringId);
                return;
            }

            surfaceEditor.doUpdate(entity);
            esiEditor.doUpdate(entity);
            layerManager.setSurfaceLayerName(entity.getLayerName());
            layerManager.zoomTo(entity.getEnvelope());

        } catch (NumberFormatException e) {
            Notification.show("This isn't a valid id: " + stringId);
        }
    }

    private String generateLayerName() {
        return "esp-layer-"
                + dao.getNextValueInSequence("blueprint.geoserver_layer");
    }
}
