package org.esp.editor;

import org.jrc.form.editor.EditorPanel;
import org.jrc.form.editor.SelectAndEditUI;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

public class TwinPanelEditorUI extends HorizontalLayout implements
        SelectAndEditUI {


    EditorPanel leftPanel = new EditorPanel();
    EditorPanel rightPanel = new EditorPanel();
    
    VerticalLayout hackPanel = new VerticalLayout();
    
    private Button createButton;

    public TwinPanelEditorUI(String title, String description) {

        this.setSizeFull();
        this.setSpacing(true);

        //Just to put something in the RHS as a placeholder
        addComponent(hackPanel);
        addComponent(leftPanel);
        leftPanel.addHeading(title);
        
        this.createButton = new Button("New");
        leftPanel.addCreateButton(createButton); 
        leftPanel.addDescription(description);
        
        
        /*
         * edit panel
         */
        rightPanel.getCloseButton().addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
               setEditingState(false); 
            }
        });
        rightPanel.addHeading("Create new " + title);

        addComponent(hackPanel);
        hackPanel.addComponent(rightPanel);

        /*
         * Begin not in edit mode
         */
        setEditingState(false);
    }

    public void addFormComponent(Component form, String label, String description) {
        rightPanel.addComponent(form);
    }

    public void setTable(Component table) {
        leftPanel.addComponent(table);
    }

    public void setSubmitPanel(Component submitPanel) {
        rightPanel.addComponent(submitPanel);
    }

    public void setEditingState(boolean isEditing) {
        rightPanel.setVisible(isEditing);
        if (createButton != null) {
            createButton.setEnabled(!isEditing);
        }
    }

    @Override
    public Button getCreateButton() {
        return createButton;
    }

    @Override
    public void addSelectionComponent(Component filterPanel) {
        leftPanel.addComponent(filterPanel);
    }


}
