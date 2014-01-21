package org.esp.publisher;

import org.esp.domain.blueprint.EcosystemServiceIndicator;
import org.esp.publisher.form.ESIEditor;
import org.esp.publisher.form.LayerPublishedListener;
import org.esp.publisher.form.ViewToRename;
import org.jrc.persist.Dao;
import org.jrc.ui.SimplePanel;
import org.jrc.ui.baseview.TwinPanelView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.leaflet.LMap;

import com.google.inject.Inject;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vividsolutions.jts.geom.Polygon;

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

    private ESIEditor surfaceEditor;

    private TiffUploadField uploadField;

    @Inject
    public MapPublisher(GeoserverRestApi gsr, Dao dao,
            ESIEditor surfaceEditor) {

        this.gsr = gsr;
        this.dao = dao;

        this.surfaceEditor = surfaceEditor;
        
        surfaceEditor.setPublishEventListener(new LayerPublishedListener() {

            @Override
            public void onLayerPublished(String layerName, Polygon extent) {
                layerManager.setSurfaceLayerName(layerName);
                layerManager.zoomTo(extent);
            }
        });

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


            ViewToRename displayPanel = new ViewToRename();

            surfaceEditor.init(displayPanel);

            tabSheet.addTab(displayPanel, "Maps");
        }
    }


    @Override
    public void enter(ViewChangeEvent event) {

        String params = event.getParameters();
        if (params != null && !params.isEmpty()) {

            if (params.equals("new")) {
                surfaceEditor.doCreate();
                EcosystemServiceIndicator entity = surfaceEditor.getEntity();
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
            layerManager.setSurfaceLayerName(entity.getLayerName());
            layerManager.zoomTo(entity.getEnvelope());

        } catch (NumberFormatException e) {
            Notification.show("This isn't a valid id: " + stringId);
        }
    }
}
