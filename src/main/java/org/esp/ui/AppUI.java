package org.esp.ui;

import org.jrc.auth.AccountActions;
import org.jrc.auth.domain.Role;
import org.jrc.form.permission.RoleManager;
import org.jrc.form.view.GuicedViewProvider;
import org.jrc.persist.Dao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.vaadin.application.ui.ScopedUI;
import com.google.code.vaadin.application.uiscope.UIScoped;
import com.google.inject.Inject;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.CssLayout;

//@Theme("biopama")
@Theme("dashboard")
@UIScoped
public class AppUI extends ScopedUI  {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private CssLayout root = new CssLayout();
    
	/*
	 * This holds the dynamically changing content
	 */
    private CssLayout content = new CssLayout();

	private Navigator nav;

	private GuicedViewProvider viewProvider;

    private HeaderView headerView;

    private AccountActions accAct;

    private Role role;

//    private MenuView menuView;
	
	
	@Inject
	public AppUI(GuicedViewProvider viewProvider, HeaderView headerView, AccountActions accAct, RoleManager roleManager) {
		this.viewProvider = viewProvider;
		this.headerView = headerView;
		this.accAct = accAct;
		this.role = roleManager.getRole();
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
