package org.esp.ui.wizard;


import com.vaadin.ui.CssLayout;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;

public class WizardElement extends CssLayout {
    
    private String description;

    public WizardElement(FormLayout formLayout, String description) {
        addStyleName("wizard-element");
        
        this.description = description;
        
        Label label = new Label(description);
        CssLayout layout = new CssLayout();
        layout.addComponent(label);
        this.addComponent(layout);
        
        this.addComponent(formLayout);
    }

}
