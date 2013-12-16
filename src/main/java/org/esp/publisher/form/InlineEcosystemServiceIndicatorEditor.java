package org.esp.publisher.form;

import it.jrc.auth.RoleManager;

import java.util.List;

import javax.persistence.metamodel.SingularAttribute;

import org.apache.shiro.authz.UnauthorizedException;
import org.esp.domain.blueprint.ArealUnit_;
import org.esp.domain.blueprint.DataSource;
import org.esp.domain.blueprint.EcosystemService;
import org.esp.domain.blueprint.EcosystemServiceIndicator;
import org.esp.domain.blueprint.EcosystemServiceIndicator_;
import org.esp.domain.blueprint.EcosystemService_;
import org.esp.domain.blueprint.IndicatorSurface;
import org.esp.domain.blueprint.Indicator_;
import org.esp.domain.blueprint.QuantificationUnit_;
import org.esp.domain.blueprint.Study;
import org.esp.domain.blueprint.TemporalUnit_;
import org.esp.upload.GeoserverRest;
import org.esp.upload.GeoserverUploadField;
import org.jrc.form.component.SelectionTable;
import org.jrc.form.editor.BaseEditor;
import org.jrc.form.editor.EntityTable;
import org.jrc.form.filter.YearField;
import org.jrc.persist.Dao;
import org.jrc.persist.adminunits.Grouping;
import org.jrc.persist.adminunits.Grouping_;
import org.vaadin.tokenfield.TokenField;
import org.vaadin.tokenfield.TokenField.InsertPosition;

import com.google.inject.Inject;
import com.vaadin.data.util.BeanItemContainer;
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

public class InlineEcosystemServiceIndicatorEditor extends CutDownBaseEditor<EcosystemServiceIndicator> {

    private Study study;
    private GeoserverUploadField geoserverUpload;
    private RoleManager roleManager;

    @Inject
    public InlineEcosystemServiceIndicatorEditor(Dao dao, RoleManager roleManager) {
        
        super(EcosystemServiceIndicator.class, dao);
        
        this.roleManager = roleManager;

        SelectionTable<EcosystemService> st = ff.addSelectionTable(EcosystemServiceIndicator_.ecosystemService);
        st.addColumn(EcosystemService_.label, "Name");
        st.addFilterField(EcosystemService_.ecosystemServiceCategory, "Filter by category");
        
        ff.addField(EcosystemServiceIndicator_.study);

        ff.addSelectAndCreateField(EcosystemServiceIndicator_.indicator, Indicator_.label);
        
        ff.addField(EcosystemServiceIndicator_.ecosystemServiceAccountingType);
        ff.addField(EcosystemServiceIndicator_.ecosystemServiceBenefitType);
        
//        addFieldGroup("The Ecosystem Service");
        
        ff.addSelectAndCreateField(EcosystemServiceIndicator_.quantificationUnit, QuantificationUnit_.label, QuantificationUnit_.quantificationUnitCategory);
        ff.addSelectAndCreateField(EcosystemServiceIndicator_.arealUnit, ArealUnit_.label);
        ff.addSelectAndCreateField(EcosystemServiceIndicator_.temporalUnit, TemporalUnit_.label);
        
//        addFieldGroup("Quantification");
        
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
        
//        addFieldGroup("Spatio-temporal");
        
        ff.addField(EcosystemServiceIndicator_.quantificationMethod);

        ff.addTokenField(EcosystemServiceIndicator_.dataSources);
        
//        addFieldGroup("Model and data");
        
        ff.addField(EcosystemServiceIndicator_.minimumMappingUnit);
        
//        addFieldGroup("Spatial data");
        
        ff.addTokenField(EcosystemServiceIndicator_.biomes);

        ff.addField(EcosystemServiceIndicator_.studyObjectiveMet);
        ff.addTextArea(EcosystemServiceIndicator_.comments);

        addFieldGroup("Other");
        
    }

    @Override
    protected void doPreCommit(EcosystemServiceIndicator entity) {
        /*
         * Build the relationships
         */
//        entity.setStudy(study);
//        IndicatorSurface indicatorSurface = geoserverUpload.getValue();
//        
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
        IndicatorSurface indicatorSurface = entity.getIndicatorSurface();
        if (indicatorSurface != null) {
            GeoserverRest gsr = geoserverUpload.getGsr();
            gsr.removeStore(indicatorSurface);
        }
        super.doPostDelete(entity);
    }

    
}
