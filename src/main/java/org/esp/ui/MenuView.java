package org.esp.ui;

import java.util.Collection;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.MenuBar;

public class MenuView extends MenuBar implements View {
    
    @Inject
    public MenuView(Multimap<String, String> menuTreeMap) {
        
    	setWidth("100%");
    
    	for (String key : menuTreeMap.keySet()) {
    
    		final MenuBar.MenuItem newItem = addItem(key, null);
    
    		Collection<String> x = menuTreeMap.get(key);
    
    		for (final String url : x) {
    			String[] headTail = url.split("/");
    			String tail = headTail[1];
    
    			newItem.addItem(tail, new Command() {
    				@Override
    				public void menuSelected(MenuItem selectedItem) {
    				    
    				    getUI().getNavigator().navigateTo(url);
    				    
    				}
    			});
    		}
    
    	}
    
    	addStyleName("bits-menubar");
    }
    
    @Override
    public void enter(ViewChangeEvent event) {
        
    }
}
