package org.esp.publisher;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import javax.persistence.Query;

import org.esp.publisher.TiffUploader.ProcessingCompleteEvent;
import org.jrc.persist.Dao;
import org.jrc.ui.SimpleHtmlHeader;
import org.jrc.ui.SimplePanel;
import org.jrc.ui.baseview.TwinPanelView;

import com.google.inject.Inject;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;

/***
 * Interface for publishing maps.
 * 
 * TODO:
 * 
 * [ ] Map colours to database entities
 * [ ] When editing colour schemes it can be republished to geoserver.
 * [ ] Support for multiple formats (geotiff, world file etc)
 * [ ] 
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

    @Inject
    public MapPublisher(TiffUploader gsp, CartographicKeyEditor cke, GeoserverRestApi gsr, Dao dao, TestMap layerViewer) {

        this.cartographicKeyEditor = cke;
        this.tiffUploader = gsp;
        this.gsr = gsr;
        this.dao = dao;
        this.testMap = layerViewer;

        {
            VerticalLayout vl = new VerticalLayout();
            vl.setSizeFull();
            vl.addComponent(testMap);
            testMap.setSizeFull();
            getLeftPanel().addComponent(vl);
        }

        {
            SimplePanel rightPanel = getRightPanel();
            
            rightPanel.addComponent(new SimpleHtmlHeader("Upload"));
            
            rightPanel.addComponent(gsp);
            
            gsp.addProcessingCompleteListener(new TiffUploader.ProcessingCompleteListener() {

                @Override
                public void processingComplete(ProcessingCompleteEvent event) {
                    TiffMeta tiffMeta = event.getResults();
                    testMap.zoomTo(tiffMeta.getEnvelope());
                    MapPublisher.this.tiffMeta = tiffMeta;
                }
            });

            rightPanel.addComponent(new SimpleHtmlHeader("Key"));
            rightPanel.addComponent(cke);
            
            rightPanel.addComponent(new SimpleHtmlHeader("Publish"));
            
            Button publish = new Button("Publish");
            rightPanel.addComponent(publish);
            publish.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    publish();
                }
            });
        }
    }


    @Override
    public void enter(ViewChangeEvent event) {

        String params = event.getParameters();
        if (params != null && !params.isEmpty()) {

            try {
                Long id = Long.valueOf(params);

            } catch (NumberFormatException e) {
                Notification.show("This isn't a valid id: " + params);
            }
        }
        
    }
    
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
            
            //TODO: save colour map first?
            
            gsr.publishSLD(layerName, cartographicKeyEditor.getColours());
            
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

        Query q = dao.getEntityManager().createNativeQuery("select nextval('blueprint.geoserver_layer');");
        Object res = q.getSingleResult();
        if (res instanceof BigInteger) {
            Long id = ((BigInteger) res).longValue();
            // surface.setId(id);
            return "esp-" + id;
        }
        return null;
    }
}
