package org.esp.publisher;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import javax.persistence.Query;

import org.esp.domain.blueprint.EcosystemServiceIndicator;
import org.esp.domain.blueprint.IndicatorSurface;
import org.esp.domain.publisher.ColourMap;
import org.esp.publisher.TiffUploader.ProcessingCompleteEvent;
import org.esp.publisher.form.InlineEcosystemServiceIndicatorEditor;
import org.jrc.persist.Dao;
import org.jrc.ui.SimpleHtmlHeader;
import org.jrc.ui.SimplePanel;
import org.jrc.ui.baseview.TwinPanelView;

import com.google.inject.Inject;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

/***
 * Interface for publishing maps.
 * 
 * Colour maps are stored in database entities.
 * TODO: allow for choosing either from default colour maps or personal ones
 * 
 *  
 * [ ] When editing colour schemes it can
 * be republished to geoserver. [ ] Support for multiple formats (geotiff, world
 * file etc) [ ]
 * 
 * @author Will Temperley
 * 
 */
public class MapPublisher extends TwinPanelView implements View {

    private CartographicKeyEditor cartographicKeyEditor;
    private TiffUploader tiffUploader;
    private TiffMeta tiffMeta;
    private GeoserverRestApi gsr;
    private Dao dao;
    private TestMap testMap;

    private IndicatorSurface indicatorSurface;
    private InlineEcosystemServiceIndicatorEditor esiEditor;
    private EcosystemServiceIndicator esi;

    @Inject
    public MapPublisher(TiffUploader gsp, CartographicKeyEditor cke,
            GeoserverRestApi gsr, Dao dao, TestMap layerViewer,
            InlineEcosystemServiceIndicatorEditor esiEditor) {

        this.cartographicKeyEditor = cke;
        this.tiffUploader = gsp;
        this.gsr = gsr;
        this.dao = dao;
        this.testMap = layerViewer;
        this.esiEditor = esiEditor;

        {
            VerticalLayout vl = new VerticalLayout();
            vl.setSizeFull();
            vl.addComponent(testMap);
            testMap.setSizeFull();
            getLeftPanel().addComponent(vl);
        }

        {
            SimplePanel rightPanel = getRightPanel();

            TabSheet tabSheet = new TabSheet();
            tabSheet.setSizeFull();
            replaceComponent(rightPanel, tabSheet);

            tabSheet.addTab(addPublishingComponents(), "Maps");

            tabSheet.addTab(addMetaComponents(), "Meta");
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

        displayPanel.addComponent(tiffUploader);

        tiffUploader
                .addProcessingCompleteListener(new TiffUploader.ProcessingCompleteListener() {

                    @Override
                    public void processingComplete(ProcessingCompleteEvent event) {
                        TiffMeta tiffMeta = event.getResults();
                        testMap.zoomTo(tiffMeta.getEnvelope());
                        MapPublisher.this.tiffMeta = tiffMeta;
                    }
                });

        displayPanel.addComponent(new SimpleHtmlHeader("Key"));
        displayPanel.addComponent(cartographicKeyEditor);

        displayPanel.addComponent(new SimpleHtmlHeader("Publish"));

        Button publish = new Button("Publish");
        displayPanel.addComponent(publish);
        publish.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                publish();
            }
        });
        return displayPanel;
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
            
            //Assumes all entities are set up already.
            esiEditor.doUpdate(esi);
            ColourMap cm = indicatorSurface.getColourMap();
            cartographicKeyEditor.setColourMap(cm);
        }

    }

    private void newEsi() {
        esi = new EcosystemServiceIndicator();
        indicatorSurface = new IndicatorSurface();
        esi.setIndicatorSurface(indicatorSurface);

        //get colour map
        //FIXME hardcode 1l
        ColourMap cm = dao.find(ColourMap.class, 1l);
        indicatorSurface.setColourMap(cm);

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

        } catch (NumberFormatException e) {
            Notification.show("This isn't a valid id: " + stringId);
        }
    }

    /**
     * 
     * States might be: 
     * 1. Already published, new file
     * 2. Already published, new style
     * 
     * 2. File not uploaded, default style
     * 3. File not uploaded, new style
     * 
     * 4. File uploaded, default style
     * 5. File uploaded, new style (need to publish as well) 
     * 
     * 
     */
    private void publish() {
        

        String layerName = generateLayerName();

        try {
            File f = tiffUploader.getFile();

            if (f == null) {
                Notification.show("No file found.");
                return;
            }

            if (tiffMeta == null) {
                Notification.show("File not uploaded yet.");
                return;
            }

            // TODO: save colour map first?

            gsr.publishSLD(layerName, cartographicKeyEditor.getColourMap()
                    .getColourMapEntries());

            gsr.publishTiff(f, tiffMeta.getSrid(), layerName, layerName);

            testMap.addWmsLayer(layerName);

        } catch (IOException e) {
            e.printStackTrace();
            Notification.show("Failed.");
        }
    }

    /**
     * Ensures this surface has an ID
     * 
     * @param surface
     * @return
     */
    private String generateLayerName() {

        Query q = dao.getEntityManager().createNativeQuery(
                "select nextval('blueprint.geoserver_layer');");
        Object res = q.getSingleResult();
        if (res instanceof BigInteger) {
            Long id = ((BigInteger) res).longValue();
            // surface.setId(id);
            return "esp-" + id;
        }
        return null;
    }

    // FIXME copy paste from ESI editor
    protected void doPreCommit(EcosystemServiceIndicator entity) {
        /*
         * Build the relationships
         */

        if (entity.getId() == null) {

            dao.getEntityManager().persist(entity);
            indicatorSurface.setEcosystemServiceIndicator(entity);
            dao.getEntityManager().persist(indicatorSurface);
            entity.setIndicatorSurface(indicatorSurface);
        }
    }
}
