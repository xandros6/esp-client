package org.esp.publisher;

import org.esp.upload.GeoserverUploadField;

import com.google.inject.Inject;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.VerticalLayout;

public class LayerManager extends VerticalLayout implements View {
    
    @Inject
    public LayerManager(GeoserverUploadField gsu, ColourMapEditor cme) {
        
//        addComponent(gsu);
        addComponent(cme);
        
    }

    @Override
    public void enter(ViewChangeEvent event) {
        // TODO Auto-generated method stub
        
    }

}
