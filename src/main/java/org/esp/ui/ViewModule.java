package org.esp.ui;

import java.util.Map;

import org.esp.domain.blueprint.EcosystemServiceIndicator;
import org.esp.domain.blueprint.Study;
import org.esp.editor.EcosystemServiceIndicatorEditor;
import org.esp.editor.StudyEditor;
import org.esp.publisher.MapPublisher;
import org.jrc.form.view.AbstractViewModule;

import com.google.code.vaadin.application.uiscope.UIScoped;
import com.google.inject.multibindings.MapBinder;
import com.vaadin.navigator.View;

/**
 * Binds views to their URLs which results in an injectable {@link Map} of
 * views.
 * 
 * Editors will appear in navigation under the first part of the URL, named as
 * the second part of the URL.
 * 
 * Also provides reverse links through a class-to-url mapping
 * 
 * @author will
 */
public class ViewModule extends AbstractViewModule {

//    public static final String SEARCH = "Search";
    public static final String ECOSYSTEM_SERVICE_INDICATOR = "Ecosystem-Service-Indicator";
    public static final String PUBLISHER = "Publisher";
    public static final String HOME = "Home";
    
    public static final String QUANTIFICATION_UNIT = "Quantification-Unit";
    public static final String AREAL_UNIT = "Areal-Unit";

    @Override
    protected void configure() {
        mapbinder = MapBinder.newMapBinder(binder(), String.class,
                View.class);
        
        addBinding(ECOSYSTEM_SERVICE_INDICATOR, EcosystemServiceIndicatorEditor.class, EcosystemServiceIndicator.class);
//        addBinding(HOME, StudyEditor.class, Study.class); 
        
        mapbinder.addBinding(PUBLISHER).to(MapPublisher.class).in(UIScoped.class);
        mapbinder.addBinding(HOME).to(SearchView.class).in(UIScoped.class);
        
//        addBinding("Table-Descriptions", TableDescriptionEditor.class, TableDescription.class);
    }

    public static String getESILink(EcosystemServiceIndicator esi) {
        return PUBLISHER + "/" +  esi.getId();
    }
}
