package org.esp.ui;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Link;

/**
 * The option bar which provides account tools
 * 
 * @author Will Temperley
 *
 */
public class AccountOptionView extends CssLayout implements View {

    private String logoutPath;

    @Inject
    public AccountOptionView(@Named("logout_path") String path) {
        this.logoutPath = path;
        setStyleName("account-option-view");
        
        Link link = new Link("logout", new ExternalResource(logoutPath));
        addComponent(link);
    }

    @Override
    public void enter(ViewChangeEvent event) {
        
    }
}
