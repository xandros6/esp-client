package org.esp.publisher.ui;

import org.esp.domain.blueprint.EcosystemServiceIndicator;

import com.vaadin.ui.Window;

public class ModalMessageWindow extends Window{

    private EcosystemServiceIndicator selectedEntity;

    public ModalMessageWindow(EcosystemServiceIndicator selectedEntity) {
        super("Send message to user");
        this.selectedEntity = selectedEntity;
        setStyleName("messagewindow");
        setModal(true);
        setDraggable(false);
        setResizable(false);
        setHeight("200px");
        setWidth("400px");
        center();
    }
}
