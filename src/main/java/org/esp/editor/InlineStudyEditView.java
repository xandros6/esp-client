package org.esp.editor;

import org.jrc.form.editor.EditFormUI;
import org.jrc.form.editor.EditorPanel;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

public class InlineStudyEditView extends Panel implements EditFormUI {


    private EditorPanel editPanel = new EditorPanel();
    private Button editButton;
    

    public InlineStudyEditView(VerticalLayout hackPanel, Button button) {
        
        this.editButton = button;
        hackPanel.addComponent(editPanel);
        setEditingState(false);
        
        editPanel.getCloseButton().addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                setEditingState(false);
            }
        });
    }

    @Override
    public void addFormComponent(Component form, String label,
            String description) {
        
        editPanel.addComponent(form);

    }

    @Override
    public void setEditingState(boolean isEditing) {
        editPanel.setVisible(isEditing);
        editButton.setEnabled(!isEditing);
    }

    @Override
    public void setSubmitPanel(Component submitPanel) {
        editPanel.addComponent(submitPanel);
    }

}
