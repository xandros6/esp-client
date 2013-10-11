package org.esp.ui;

import com.google.inject.Inject;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

public class HeaderView extends HorizontalLayout implements View {
    
    
    @Inject
    public HeaderView() {
        
        addLogo();
        setStyleName("header");
        
    }

    private void addLogo() {
        Label label = new Label();
        label.setContentMode(ContentMode.HTML);

        String text = "<div><span class='esp'>ESP</span>"
                + "<span class='mapping'>Mapping</span></div>";

        label.setValue(text);

        addComponent(label);
    }
    

    @Override
    public void enter(ViewChangeEvent event) {
        
    }

}
