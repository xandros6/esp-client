package org.esp.ui;

import com.google.inject.Inject;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.HorizontalLayout;

public class HeaderView extends HorizontalLayout implements View {
    
    
    @Inject
    public HeaderView() {
    }
    

    @Override
    public void enter(ViewChangeEvent event) {
        
    }

}
