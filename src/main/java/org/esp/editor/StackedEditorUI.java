package org.esp.editor;

import org.jrc.form.editor.EditorPanel;
import org.jrc.form.editor.SelectAndEditUI;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;

public class StackedEditorUI extends Panel implements
        SelectAndEditUI {

    EditorPanel displayPanel = new EditorPanel();
    EditorPanel editPanel = new EditorPanel();
    private Button createButton;

    public StackedEditorUI() {

        this.setWidth("800px");
        this.setContent(displayPanel);
        
        createButton = new Button("New");
        displayPanel.addComponent(createButton);

        /*
         * Begin not in edit mode
         */
        setEditingState(false);
    }

    public void addFormComponent(Component form, String label, String description) {
        editPanel.addComponent(form);
    }
    

    public void setTable(Component table) {
        displayPanel.addComponent(table);
    }

    public void setSubmitPanel(Component submitPanel) {
        editPanel.addComponent(submitPanel);
    }

    public void setEditingState(boolean isEditing) {
        if (isEditing) {
            setContent(editPanel);
        } else {
            setContent(displayPanel);
        }
    }

    @Override
    public Button getCreateButton() {
        return createButton;
    }

    @Override
    public void addSelectionComponent(Component filterPanel) {
//        leftPanel.addComponent(filterPanel);
    }

}
