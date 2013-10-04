package org.esp.publisher;


import org.esp.upload.GeoserverUploadField;
import org.jrc.ui.baseview.TwinPanelView;

import com.google.inject.Inject;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.VerticalLayout;

public class MapPublisher extends TwinPanelView implements View {
    
    private TestMap testMap;

    @Inject
    public MapPublisher(GeoserverUploadField gsu) {
        
        testMap = new TestMap();
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.addComponent(testMap);
        getLeftPanel().addComponent(vl);
        
        {
            getRightPanel().addComponent(gsu);
        }
        
        
    }

    @Override
    public void enter(ViewChangeEvent event) {
        
    }

}
