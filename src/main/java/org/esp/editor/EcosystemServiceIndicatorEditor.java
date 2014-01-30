package org.esp.editor;

import it.jrc.auth.RoleManager;

import java.util.List;

import org.apache.shiro.authz.UnauthorizedException;
import org.esp.domain.blueprint.ArealUnit_;
import org.esp.domain.blueprint.EcosystemService;
import org.esp.domain.blueprint.EcosystemServiceIndicator;
import org.esp.domain.blueprint.EcosystemServiceIndicator_;
import org.esp.domain.blueprint.EcosystemService_;
import org.esp.domain.blueprint.Indicator_;
import org.esp.domain.blueprint.QuantificationUnit_;
import org.esp.domain.blueprint.Study;
import org.esp.domain.blueprint.TemporalUnit_;
import org.jrc.form.component.SelectionTable;
import org.jrc.form.editor.BaseEditor;
import org.jrc.form.editor.EntityTable;
import org.jrc.form.filter.YearField;
import org.jrc.persist.Dao;
import org.jrc.persist.adminunits.Grouping;
import org.jrc.persist.adminunits.Grouping_;

import com.google.inject.Inject;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.Compare.Equal;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TwinColSelect;

@Deprecated
public class EcosystemServiceIndicatorEditor extends
        BaseEditor<EcosystemServiceIndicator> {

    private Study study;
    private ESIEditorUI theView;
    private RoleManager roleManager;

    @Inject
    public EcosystemServiceIndicatorEditor(Dao dao, final InlineStudyEditor editor, RoleManager roleManager) {
        
        super(EcosystemServiceIndicator.class, dao);
        
        this.roleManager = roleManager;
        
        EntityTable<EcosystemServiceIndicator> table = buildTable();
        table.addColumns(EcosystemServiceIndicator_.ecosystemService, EcosystemServiceIndicator_.indicator);

        SelectionTable<EcosystemService> st = ff.addSelectionTable(EcosystemServiceIndicator_.ecosystemService);
        st.addColumn(EcosystemService_.label, "Name");
        st.addFilterField(EcosystemService_.ecosystemServiceCategory, "Filter by category");
        
        ff.addSelectAndCreateField(EcosystemServiceIndicator_.indicator, Indicator_.label);
        
        ff.addField(EcosystemServiceIndicator_.ecosystemServiceAccountingType);
        ff.addField(EcosystemServiceIndicator_.ecosystemServiceBenefitType);
        
        addFieldGroup("The Ecosystem Service");
        
        ff.addSelectAndCreateField(EcosystemServiceIndicator_.quantificationUnit, QuantificationUnit_.label, QuantificationUnit_.quantificationUnitCategory);
        ff.addSelectAndCreateField(EcosystemServiceIndicator_.arealUnit, ArealUnit_.label);
        ff.addSelectAndCreateField(EcosystemServiceIndicator_.temporalUnit, TemporalUnit_.label);
        
        addFieldGroup("Quantification");
        
        ff.addField(EcosystemServiceIndicator_.startYear, new YearField());
        ff.addField(EcosystemServiceIndicator_.endYear, new YearField());
        ff.addField(EcosystemServiceIndicator_.spatialLevel);
        
        //Quick hack to get a filtered TwinColSelect
        TwinColSelect groupingField = new TwinColSelect();
        ff.addField(EcosystemServiceIndicator_.groupings, groupingField);
        List<Grouping> groupings = dao.all(Grouping.class, Grouping_.id);
        for (Grouping grouping : groupings) {
            if (grouping.getGroupingType().getId().equals("Region")) {
                groupingField.addItem(grouping);
            }
        }
        groupingField.setCaption("Regions");
        
        addFieldGroup("Spatio-temporal");
        
        ff.addField(EcosystemServiceIndicator_.quantificationMethod);
        ff.addField(EcosystemServiceIndicator_.dataSources);       
        
        addFieldGroup("Model and data");
        
        ff.addField(EcosystemServiceIndicator_.minimumMappingUnit);
//        ff.addField(EcosystemServiceIndicator_.indicatorSurface, new IndicatorSurfaceField(geoserverUpload));
        
        addFieldGroup("Spatial data");
        
        ff.addField(EcosystemServiceIndicator_.biomes);
        ff.addField(EcosystemServiceIndicator_.studyObjectiveMet);
        ff.addTextArea(EcosystemServiceIndicator_.comments);

        addFieldGroup("Other");
        
        theView = new ESIEditorUI("Ecosystem Service Indicators");
        theView.setTable(table);
        
        theView.getEditButton().addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                editor.doUpdate(study);
            }
        });
        
        /*
         * Inline editor
         */
        InlineStudyEditView isev = new InlineStudyEditView(theView.getHackPanel(), theView.getEditButton());
//        editor.init(isev);
        
        init(theView);
    }
    
    @Override
    protected void doPreCommit(EcosystemServiceIndicator entity) {
        /*
         * Build the relationships
         */
//        entity.setStudy(study);
//        IndicatorSurface indicatorSurface = geoserverUpload.getValue();
        
//        if (entity.getId() == null) {
//            
//            dao.getEntityManager().persist(entity);
//            indicatorSurface.setEcosystemServiceIndicator(entity);
//            dao.getEntityManager().persist(indicatorSurface);
//            entity.setIndicatorSurface(indicatorSurface);
//        }
    }
    
    @Override
    protected void doPostDelete(EcosystemServiceIndicator entity) {
//        IndicatorSurface indicatorSurface = entity.getIndicatorSurface();
//        if (indicatorSurface != null) {
//            GeoserverRest gsr = geoserverUpload.getGsr();
//            gsr.removeStore(indicatorSurface);
//        }
//        super.doPostDelete(entity);
    }
    
    
    @Override
    public void enter(ViewChangeEvent event) {

        String params = event.getParameters();
        if (params != null && !params.isEmpty()) {
            try {
                Long id = Long.valueOf(params);
                setStudyId(id);
            } catch (NumberFormatException e) {
                Notification.show("This isn't a valid id: " + params);
            }
        }
        
        view.setEditingState(false);
    }
    
    protected void setStudyId(Long id) {
        /*
         * This is probably why it's slow ... lots of work done here
         */
        
        this.study = dao.find(Study.class, id);
        
        if (!study.getRole().equals(roleManager.getRole()) && !study.getRole().getIsSuperUser()) {
            throw new UnauthorizedException("You do not have permission to edit this study.");
        }
        
        Equal filter = new Compare.Equal(EcosystemServiceIndicator_.study.getName(), study);
        
        containerManager.getContainer().removeAllContainerFilters();
        containerManager.getContainer().addContainerFilter(filter);
        
        theView.setExtraStuff(getReadOnlyForm(study));
        
    }
    
    
    protected Component getReadOnlyForm(Study s) {
        
        FormLayout fl = new FormLayout();
//        GridLayout fl = new GridLayout(2,2);
        
        fl.addStyleName("extra-content");
        fl.setWidth("100%");
        
//        Label component = new Label("Study name:");
//        fl.addComponent(component);
//        fl.addComponent(new Label(s.getStudyName()));
//        fl.addComponent(new Label("Study purpose:"));
//        fl.addComponent(new Label(s.getStudyPurpose().toString()));
        
//        fl.setColumnExpandRatio(0, 0.3f);
        
        fl.addComponent(getTextField("Study name:", s.getStudyName(), 3));
        fl.addComponent(getTextField("Study purpose:", s.getStudyPurpose().toString(), 1));
        
        return fl;
    }
    
    protected Field<String> getTextField(String name, String purpose, int numRows) {
        
        TextArea f = new TextArea();
        f.setWordwrap(true);
        f.setRows(numRows);
        
        f.setCaption(name);
        f.setValue(purpose);
        f.setWidth("100%");
        f.setReadOnly(true);
        return f;
    }
    
}
