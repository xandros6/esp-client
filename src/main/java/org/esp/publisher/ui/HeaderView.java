package org.esp.publisher.ui;


import com.google.inject.Inject;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Panel;

public class HeaderView extends Panel {

    private AccountDetails accountDetails;
    private Label logo;
    private MenuBar menuBar;
    private GridLayout content;

    @Inject
    public HeaderView(AccountDetails accountDetails) {

        content = new GridLayout(3,1);
        content.setWidth("100%");
        addLogo();

        setStyleName("header");

        this.accountDetails = accountDetails;
        
        setContent(content);
    }

    private void addLogo() {
        this.logo = new Label();
        logo.setWidth("330px");
        logo.setContentMode(ContentMode.HTML);

        String text = "<div><span class='esp'>ESP</span>"
                + "<span class='mapping'>Mapping</span></div>";

        logo.setValue(text);

        content.addComponent(logo);

    }

    public void addMenuBar(MenuBar menuBar) {
        this.menuBar = menuBar;
        content.addComponent(menuBar);
        content.addComponent(accountDetails);
        content.setColumnExpandRatio(2, 1);
        content.setComponentAlignment(accountDetails, Alignment.TOP_RIGHT);
    }
    

}
