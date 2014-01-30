package org.esp.publisher.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.esp.domain.blueprint.EcosystemServiceIndicator;
import org.esp.publisher.colours.ColourMapFieldGroup;
import org.jrc.form.FieldGroup;
import org.jrc.form.editor.SubmitPanel;
import org.jrc.ui.SimpleHtmlHeader;
import org.jrc.ui.SimpleHtmlLabel;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;

public class ESIEditorView extends VerticalLayout implements
        IEditorView<EcosystemServiceIndicator> {

    // private Label status;
    private SubmitPanel submitPanel;
    private CssLayout mainPanel;
    private List<Component> componentsToToggle = new ArrayList<Component>();

    public ESIEditorView() {
        addStyleName("display-panel");
        addStyleName("display-panel-padded");
        setSizeFull();

        submitPanel = new SubmitPanel();
        addComponent(submitPanel);

        // status = new Label("Status");
        // addComponent(status);
        //
        // ProgressBar pb = new ProgressBar();
        // addComponent(pb);
        // pb.setWidth("100%");
        // pb.setValue(0.5f);

        SimpleHtmlLabel spacer = new SimpleHtmlLabel("&nbsp;");
        addComponent(spacer);

        this.mainPanel = new CssLayout();
        addComponent(mainPanel);

        mainPanel.setSizeFull();
        setExpandRatio(mainPanel, 1);
    }


    @Override
    public SubmitPanel getSubmitPanel() {
        return submitPanel;
    }
    
    public void setNewStatus(boolean isNew) {
        for (Component c : componentsToToggle) {
//            if (!(c instanceof PolygonField)) {
                c.setVisible(!isNew);
//            }
        }
    }

    @Override
    public void buildForm(List<FieldGroup<EcosystemServiceIndicator>> fields) {
        for (FieldGroup<EcosystemServiceIndicator> fieldGroupMeta : fields) {

            BeanFieldGroup<EcosystemServiceIndicator> bfg = fieldGroupMeta
                    .getFieldGroup();

            SimpleHtmlHeader c = new SimpleHtmlHeader(fieldGroupMeta.getLabel());
            mainPanel.addComponent(c);

            if(fieldGroupMeta.getLabel() != ESIEditor.THE_ECOSYSTEM_SERVICE) {
                componentsToToggle.add(c);
            }
            
            if (fieldGroupMeta instanceof ColourMapFieldGroup) {
                Component content = ((ColourMapFieldGroup) fieldGroupMeta).getContent();
                mainPanel.addComponent(content);
                componentsToToggle.add(content);
            } else {
                Collection<Field<?>> fieldGroupFields = bfg.getFields();
                for (Field<?> field : fieldGroupFields) {
                    mainPanel.addComponent(field);
                    //If it's invisible already, it should always be invisible
                    if(fieldGroupMeta.getLabel() != ESIEditor.THE_ECOSYSTEM_SERVICE && field.isVisible()) {
                        componentsToToggle.add(field);
                    }
                }
            }

            mainPanel.addComponent(new SimpleHtmlLabel("&nbsp;"));
            // view.addComponent(formLayout, fieldGroupMeta.getLabel(),
            // fieldGroupMeta.getDescription());
            // addComponent(formLayout);
        }
    }
}
