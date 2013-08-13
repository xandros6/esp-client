/**
 * DISCLAIMER
 * 
 * The quality of the code is such that you should not copy any of it as best
 * practice how to build Vaadin applications.
 * 
 * @author jouni@vaadin.com
 * 
 */

package org.esp.ui;

import java.util.Iterator;
import java.util.Locale;

import org.jrc.auth.AccountActions;
import org.jrc.auth.domain.Role;
import org.jrc.form.permission.RoleManager;
import org.jrc.form.view.GuicedViewProvider;
import org.jrc.persist.Dao;

import com.google.inject.Inject;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("dashboard")
@Title("Blueprint")
public class DashboardUI extends UI {

public static final String TODO = "TODO";


    private static final long serialVersionUID = 1L;

    CssLayout root = new CssLayout();

    VerticalLayout loginLayout;

    CssLayout menu = new CssLayout();
    CssLayout content = new CssLayout();
    CssLayout actualContent = new CssLayout();
    CssLayout header = new CssLayout(){{ setStyleName("header"); }};

    private Navigator nav;

    private HelpManager helpManager;

    private GuicedViewProvider viewProvider;

    private RoleManager roleManager;

    private Role role;


    private AccountActions accAct;
    
	@Inject
	public DashboardUI(Dao dao, GuicedViewProvider viewProvider, RoleManager roleManager, AccountActions accAct) {
		this.viewProvider = viewProvider;
		this.role = roleManager.getRole();
		this.accAct = accAct;
	}

    protected void init(VaadinRequest request) {
//        getSession().setConverterFactory(new MyConverterFactory());

        helpManager = new HelpManager(this);

        setLocale(Locale.US);

        setContent(root);
        root.addStyleName("root");
        root.setSizeFull();


        nav = new Navigator(this, actualContent);
        nav.addProvider(viewProvider);
       
        
        content.addStyleName("dashboard-view");
        buildMainView();

    }

    private void buildMainView() {

        helpManager.closeAll();

        root.addComponent(getMainView());

        menu.removeAllComponents();

        
        addMenuButton(ViewModule.HOME);
//        addMenuButton(ViewModule.WIZARD);
//        addMenuButton(ViewModule.ES_INDICATORS);
        addMenuButton(ViewModule.HOME);
        addMenuButton(ViewModule.QUANTIFICATION_UNIT);
        addMenuButton(ViewModule.AREAL_UNIT);
        
        menu.addStyleName("menu");
        menu.setHeight("100%");

        

        String current = Page.getCurrent().getUriFragment();
//        if (current != null) {
//            nav.navigateTo(current);
//        } else {
//            nav.navigateTo(ViewModule.STUDY);
//        }
        
//        if (f != null && f.startsWith("!")) {
//            f = f.substring(1);
//        }
//        if (f == null || f.equals("") || f.equals("/")) {
//            nav.navigateTo(ViewModule.WIZARD);
//            menu.getComponent(0).addStyleName("selected");
////            helpManager.showHelpFor(DashboardView.class);
//        } else {
//            nav.navigateTo(f);
////            helpManager.showHelpFor(routes.get(f));
//            viewNameToMenuButton.get(f).addStyleName("selected");
//        }

//        nav.addViewChangeListener(new ViewChangeListener() {
//
//            @Override
//            public boolean beforeViewChange(ViewChangeEvent event) {
//                helpManager.closeAll();
//                return true;
//            }
//
//            @Override
//            public void afterViewChange(ViewChangeEvent event) {
//                View newView = event.getNewView();
//                helpManager.showHelpFor(newView);
////                if (autoCreateReport && newView instanceof ReportsView) {
////                    ((ReportsView) newView).autoCreate(2, items, transactions);
////                }
//            }
//        });

    }

    private Button addMenuButton(final String viewName) {
        Button b = new NativeButton(viewName.substring(0, 1).toUpperCase()
                + viewName.substring(1).replace('-', ' '));
        b.addStyleName("icon-" + viewName);
        b.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                clearMenuSelection();
                event.getButton().addStyleName("selected");
                nav.navigateTo(viewName);
            }
        });
        menu.addComponent(b);
        return b;
    }

    private HorizontalLayout getMainView() {
        return new HorizontalLayout() {
            {
                setSizeFull();
                addStyleName("main-view");
                addComponent(new VerticalLayout() {
                    // Sidebar
                    {
                        addStyleName("sidebar");
                        setWidth(null);
                        setHeight("100%");

                        // Branding element
                        addComponent(new CssLayout() {
                            {
                                addStyleName("branding");
                                Label logo = new Label(
                                        "<span>ESP</span> Blueprint",
                                        ContentMode.HTML);
                                logo.setSizeUndefined();
                                addComponent(logo);
                                // addComponent(new Image(null, new
                                // ThemeResource(
                                // "img/branding.png")));
                            }
                        });

                        // Main menu
                        addComponent(menu);
                        setExpandRatio(menu, 1);

                        // User menu
                        addComponent(getUserView(role.getFirstName()));
                    }
                });
                // Content
                addComponent(content);
                content.setSizeFull();
                content.addStyleName("view-content");
                setExpandRatio(content, 1);
                content.addComponent(header);
                content.addComponent(actualContent);
                
            }

        };
    }


    private void clearMenuSelection() {
        for (Iterator<Component> it = menu.getComponentIterator(); it.hasNext();) {
            Component next = it.next();
            if (next instanceof NativeButton) {
                next.removeStyleName("selected");
            } else if (next instanceof DragAndDropWrapper) {
                // Wow, this is ugly (even uglier than the rest of the code)
                ((DragAndDropWrapper) next).iterator().next()
                        .removeStyleName("selected");
            }
        }
    }

//    void updateReportsButtonBadge(String badgeCount) {
//        viewNameToMenuButton.get("/reports").setHtmlContentAllowed(true);
//        viewNameToMenuButton.get("/reports").setCaption(
//                "Reports<span class=\"badge\">" + badgeCount + "</span>");
//    }
//
//    void clearDashboardButtonBadge() {
//        viewNameToMenuButton.get("/dashboard").setCaption("Dashboard");
//    }


    HelpManager getHelpManager() {
        return helpManager;
    }

    private VerticalLayout getUserView(final String userLabel) {
        return new VerticalLayout() {
            {
                setSizeUndefined();
                addStyleName("user");
                Image profilePic = new Image(
                        null,
                        new ThemeResource("img/profile-pic.png"));
                profilePic.setWidth("34px");
                addComponent(profilePic);
                Label userName = new Label(userLabel);
                userName.setSizeUndefined();
                addComponent(userName);
    
                Command cmd = new Command() {
                    @Override
                    public void menuSelected(
                            MenuItem selectedItem) {
                        Notification
                                .show("Not implemented in this demo");
                    }
                };
                MenuBar settings = new MenuBar();
                MenuItem settingsMenu = settings.addItem("",
                        null);
                settingsMenu.setStyleName("icon-cog");
                settingsMenu.addItem("Settings", cmd);
                settingsMenu.addItem("Preferences", cmd);
                settingsMenu.addSeparator();
                settingsMenu.addItem("My Account", cmd);
                addComponent(settings);
    
                Button exit = new NativeButton("Exit");
                exit.addStyleName("icon-cancel");
                exit.setDescription("Sign Out");
                addComponent(exit);
                exit.addClickListener(new ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        accAct.logout();
                    }
                });
            }
        };
    }

}
