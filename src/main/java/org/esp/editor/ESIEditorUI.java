package org.esp.editor;

import org.esp.ui.ViewModule;
import org.jrc.form.editor.EditorPanel;
import org.jrc.form.editor.EditorPanelHeading;
import org.jrc.form.editor.SelectAndEditUI;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * 
 * The same as {@link TwinPanelEditorUI} but with tabs in the form
 * 
 * @author Will Temperley
 *
 */
public class ESIEditorUI extends HorizontalLayout implements
        SelectAndEditUI {

    EditorPanel leftPanel = new EditorPanel();
    EditorPanel rightPanel = new EditorPanel();
    VerticalLayout hackPanel = new VerticalLayout();
    
    private Button createButton;
    private TabSheet tabs;
    
    private Panel extraContent = new Panel();
    private Button editButton;
    private EditorPanelHeading studyHeading;

    public ESIEditorUI(String title) {

        this.setSizeFull();
        this.setSpacing(true);

        addComponent(leftPanel);
        
        /*
         * An extra section for extra content
         */
        studyHeading = leftPanel.addHeading("Manage study");
        
        Button back = new Button("List");
        back.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                UI.getCurrent().getNavigator().navigateTo(ViewModule.HOME);
            }
        });
        
        studyHeading.addComponent(back);
        
        
        this.editButton = new Button("Edit");
        leftPanel.addCreateButton(editButton);
        leftPanel.addComponent(extraContent);
        
        
        /*
         * Selection panel
         */
        leftPanel.addHeading(title);
        this.createButton = new Button("Add");
        leftPanel.addCreateButton(createButton); 
        
        
        /*
         * Edit panel
         */
        rightPanel.addHeading("Edit");
        rightPanel.getCloseButton().addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
               setEditingState(false); 
            }
        });
        
        this.tabs = new TabSheet();
        tabs.setHeight("500px");
        tabs.setWidth("100%");
        
        rightPanel.addComponent(tabs);

        addComponent(hackPanel);
        hackPanel.addComponent(rightPanel);
        
        /*
         * Begin not in edit mode
         */
        setEditingState(false);
    }
    

    public void addFormComponent(Component form, String label, String description) {
//        rightPanel.addComponent(form);
        tabs.addTab(form, label);
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
        if (editButton != null) {
            editButton.setEnabled(!isEditing);
        }
    }
    
    public void setIsEditingInline(boolean isEditing) {
        rightPanel.setVisible(isEditing);
        if (createButton != null) {
            createButton.setEnabled(!isEditing);
        }
    }


    public void setExtraStuff(Component extra) {
        extraContent.setContent(extra);
    }
    

    public Button getEditButton() {
        return editButton;
    }
    
    public VerticalLayout getHackPanel() {
        return hackPanel;
    }


    @Override
    public Button getCreateButton() {
        return createButton;
    }
    
    //FIXME HACK
    public void setHeading(String studyName) {
        studyHeading.setTitle(studyName);
    }


    @Override
    public void addSelectionComponent(Component filterPanel) {
        leftPanel.addComponent(filterPanel);
    }
}
