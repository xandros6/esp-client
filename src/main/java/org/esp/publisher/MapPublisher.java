package org.esp.publisher;

import org.esp.domain.blueprint.EcosystemServiceIndicator;
import org.esp.publisher.form.ESIEditor;
import org.esp.publisher.form.EditorController;
import org.esp.publisher.form.LayerPublishedListener;
import org.esp.publisher.form.ESIEditorView;
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

    private Dao dao;

    private LayerManager layerManager;

    private EditorController<EcosystemServiceIndicator> surfaceEditor;

    @Inject
    public MapPublisher(Dao dao, ESIEditor surfaceEditor) {

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



            ESIEditorView esiEditorView2 =  new ESIEditorView();
            surfaceEditor.init(esiEditorView2);

            tabSheet.addTab(esiEditorView2, "Maps");
        }
    }


    @Override
    public void enter(ViewChangeEvent event) {

        String params = event.getParameters();
        if (params != null && !params.isEmpty()) {
            if (params.equals("new")) {
                layerManager.reset();
                surfaceEditor.doCreate();
            } else {
                doEditById(params);
            }
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
