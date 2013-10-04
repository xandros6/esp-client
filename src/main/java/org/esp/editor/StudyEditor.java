package org.esp.editor;

import org.esp.domain.blueprint.Study;
import org.esp.domain.blueprint.Study_;
import org.esp.ui.ViewModule;
import org.jrc.form.editor.BaseEditor;
import org.jrc.form.editor.EntityTable;
import org.jrc.form.permission.RoleManager;
import org.jrc.persist.Dao;

import com.google.inject.Inject;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.Compare.Equal;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.UI;

public class StudyEditor extends BaseEditor<Study> {

    private RoleManager roleManager;

    @Inject
    public StudyEditor(final Dao dao, RoleManager roleManager) {

        super(Study.class, dao);
        
        this.roleManager = roleManager;

        EntityTable<Study> table = buildTable();
        table.addColumns(Study_.studyName, Study_.studyPurpose);
        

        ff.addField(Study_.studyName);
        ff.addField(Study_.studyPurpose);
        ff.addField(Study_.studyLocation);
        ff.addField(Study_.studyDuration);
        ff.addField(Study_.projectType);
        ff.addTextArea(Study_.projectReferences);
        ff.addTextArea(Study_.contactDetails);
        ff.addTextArea(Study_.fundingSource);
        ff.addTextArea(Study_.keywords);
        ff.addTextArea(Study_.mainInvestigators);

        addFieldGroup("Study");

        // ff.addInlineTable(Study_.ecosystemServiceIndicators,
        // EcosystemServiceIndicator.class,
        // EcosystemServiceIndicator_.indicator,
        // EcosystemServiceIndicator_.ecosystemService);
        // init(new TwinLevelEditorView());

        String description = "Please select a study or create a new one.";
        TwinPanelEditorUI theView = new TwinPanelEditorUI("Study", description);
        theView.setTable(table);
        init(theView);

    }

    /**
     * Catch the table selection and override the normal behaviour and go edit a
     * study
     */
    @Override
    protected void fireObjectSelected(ValueChangeEvent event) {

        Object value = event.getProperty().getValue();
        Study study = containerManager.findEntity(value);
        goEdit(study);

    }
    
    @Override
    protected void doPreCommit(Study obj) {
        obj.setRole(roleManager.getRole());
    }

    @Override
    protected void doPostCommit(Study study) {
        goEdit(study);
    }

    protected void goEdit(Study study) {

        if (study != null) {
            String newUriFragment = ViewModule.ECOSYSTEM_SERVICE_INDICATOR + "/"
                                        + study.getId();
            UI.getCurrent().getNavigator().navigateTo(newUriFragment);
        }
    }
    
    @Override
    public void enter(ViewChangeEvent event) {
        
        Equal filter = new Compare.Equal(Study_.role.getName(), roleManager.getRole());
        containerManager.getContainer().removeAllContainerFilters();
        containerManager.getContainer().addContainerFilter(filter);
//        containerManager.refresh();
        
//        super.enter(event);
    }

}
