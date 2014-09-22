package org.esp.publisher.ui;

import it.jrc.auth.RoleManager;
import it.jrc.domain.auth.Role;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;

class AccountDetails extends GridLayout {

        /**
         * 
         */
        private Label label;
        private MenuBar settings;

        @Inject
        public AccountDetails(final RoleManager roleManager, @Named("logout_path") final String path) {
            
            super(2, 1);

//            addComponent(loginLink);
            addStyleName("account-details");
//            setHeight("30px");

            settings = new MenuBar();
            MenuItem settingsMenu = settings.addItem("", null);
            settingsMenu.setStyleName("icon-cog");

//            settingsMenu.addItem("Issues", new Command() {
//                @Override
//                public void menuSelected(MenuItem selectedItem) {
//                    getUI().getNavigator().navigateTo(ViewModule.ISSUE_EDITOR);
//                }
//            });

            settingsMenu.addSeparator();
            settingsMenu.addItem("Logout", new Command() {
                @Override
                public void menuSelected(MenuItem selectedItem) {
                    UI.getCurrent().getPage().setLocation(path);
                }
            });

            addComponent(settings);
            label = new Label();
            addComponent(label);
            
//            setExpandRatio(label, 1);
            setRole(roleManager.getRole());
        }

        void setRole(Role role) {

            boolean showSettings;
            if (role == null) {
                showSettings = false;
            } else {
                showSettings = ! role.isAnonymous();
            }

            settings.setVisible(showSettings);
            label.setVisible(showSettings);
//            loginLink.setVisible(!showSettings);

            label.setValue(role.toString());

        }
        

        private Link getLoginLink(String contextPath) {
            ExternalResource r = new ExternalResource(contextPath
                    + "/login?action=change");
            Link link = new Link("Login", r);
//            link.setTargetName("_blank");
            return link;
        }

    }