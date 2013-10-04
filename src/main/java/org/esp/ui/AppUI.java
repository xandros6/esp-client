package org.esp.ui;

import org.jrc.form.view.GuicedViewProvider;

import com.google.code.vaadin.application.ui.ScopedUI;
import com.google.inject.Inject;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.CssLayout;

//@Theme("biopama")
@Theme("dashboard")
public class AppUI extends ScopedUI  {

	
	private CssLayout root = new CssLayout();
    
	/*
	 * This holds the dynamically changing content
	 */
    private CssLayout content = new CssLayout();

	private Navigator nav;

	private GuicedViewProvider viewProvider;

    private HeaderView headerView;
	
	
	@Inject
	public AppUI(GuicedViewProvider viewProvider, HeaderView headerView) {
		this.viewProvider = viewProvider;
		this.headerView = headerView;
	}

	@Override
	protected void init(VaadinRequest request) {
	    
		/*
		 * Set up main panels
		 */
		setContent(root);
		
		root.setSizeFull();
        root.addStyleName("dashboard-view");
        
        content.addStyleName("view-content");
        
        headerView.addStyleName("header");
        
		root.addComponent(headerView);
		root.addComponent(content);
		
		nav = new Navigator(this, content);
		nav.addProvider(viewProvider);
        nav.navigateTo(ViewModule.HOME);
	}
	

}
