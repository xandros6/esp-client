package org.esp.ui;

import org.esp.domain.blueprint.EcosystemServiceIndicator;
import org.esp.domain.blueprint.EcosystemServiceIndicator_;
import org.esp.publisher.LayerViewer;
import org.jrc.form.editor.EntityTable;
import org.jrc.form.filter.FilterPanel;
import org.jrc.persist.ContainerManager;
import org.jrc.persist.Dao;
import org.jrc.ui.SimpleHtmlHeader;
import org.jrc.ui.SimpleHtmlLabel;
import org.jrc.ui.SimplePanel;
import org.jrc.ui.baseview.TwinPanelView;

import com.google.inject.Inject;
import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerItem;
import com.vaadin.data.Property;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vividsolutions.jts.geom.Polygon;

public class SearchView extends TwinPanelView implements View {

    private JPAContainer<EcosystemServiceIndicator> esiContainer;

    private VerticalLayout content = new VerticalLayout();

    private Dao dao;

    private LayerViewer layerViewer;

    GridLayout gl = new GridLayout(3, 3);

    @Inject
    public SearchView(Dao dao, LayerViewer layerViewer) {
        ContainerManager<EcosystemServiceIndicator> containerManager = new ContainerManager<EcosystemServiceIndicator>(
                dao, EcosystemServiceIndicator.class);
        this.esiContainer = containerManager.getContainer();
        this.dao = dao;
        this.layerViewer = layerViewer;

        {

            SimplePanel leftPanel = getLeftPanel();
            content.setSizeFull();
            leftPanel.addComponent(content);

            SimpleHtmlHeader c = new SimpleHtmlHeader("Search box");
            c.setHeight("200px");
            content.addComponent(c);

            HorizontalLayout hl = new HorizontalLayout();
            hl.setSizeFull();
            content.addComponent(hl);
            content.setExpandRatio(hl, 1);

            hl.addComponent(getFilterPanel());
            hl.addComponent(getEcosystemServiceIndicatorTable());

        }

        {
            SimplePanel rightPanel = getRightPanel();
            
            VerticalLayout vl = new VerticalLayout();
            vl.setSizeFull();
            rightPanel.addComponent(vl);
            
            vl.addComponent(layerViewer);
            layerViewer.setSizeFull();
            
            HorizontalLayout hl = new HorizontalLayout();
            hl.setSizeFull();
            vl.addComponent(hl);

            hl.addComponent(gl);

            
        }

    }

    private FilterPanel<EcosystemServiceIndicator> getFilterPanel() {

        FilterPanel<EcosystemServiceIndicator> fp = new FilterPanel<EcosystemServiceIndicator>(
                esiContainer, dao);
        fp.addFilterField(EcosystemServiceIndicator_.ecosystemService);
        fp.addFilterField(EcosystemServiceIndicator_.study);
        return fp;

    }

    class ImpactVisualizationColumn implements Table.ColumnGenerator {

        public Component generateCell(Table source, Object itemId,
                Object columnId) {
            JPAContainerItem<?> item = (JPAContainerItem<?>) source
                    .getItem(itemId);
            if (item == null ){
                return new ESIViz(null);
            }
            Object entity = item.getEntity();
            final EcosystemServiceIndicator si = (EcosystemServiceIndicator) entity;
            return new ESIViz(si);
        }
    }

    public class ESIViz extends Panel {

        public ESIViz(EcosystemServiceIndicator esi) {

            VerticalLayout hl = new VerticalLayout();

            hl.addComponent(new SimpleHtmlLabel(getLink(esi)));

            hl.addComponent(new SimpleHtmlLabel(esi.getStudy().getStudyName()));

            setContent(hl);
        }

        private String getLink(EcosystemServiceIndicator esi) {
            StringBuilder sb = new StringBuilder("<a href='#!");
            sb.append(ViewModule.getESILink(esi));
            sb.append("'>");
            sb.append(esi.toString());
            sb.append("</a>");
            return sb.toString();
        }
    }

    @Override
    public void enter(ViewChangeEvent event) {
        // TODO Auto-generated method stub

    }

    private EntityTable<EcosystemServiceIndicator> getEcosystemServiceIndicatorTable() {

        EntityTable<EcosystemServiceIndicator> table = new EntityTable<EcosystemServiceIndicator>(
                esiContainer);
        table.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);

        table.setHeight("100%");
        table.setPageLength(20);

        table.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {

                Long si = (Long) event.getProperty().getValue();
                entitySelected(si);

            }
        });

        ImpactVisualizationColumn generatedColumn = new ImpactVisualizationColumn();
        table.addGeneratedColumn("id", generatedColumn);
        table.setColumnWidth("id", 400);
        return table;

    }

    protected void entitySelected(Long id) {
        
        if (id == null) {
            //Todo - nothing is selected - clear the view, or not?
            return;
        }


        EntityItem<EcosystemServiceIndicator> x = esiContainer.getItem(id);
        EcosystemServiceIndicator entity = x.getEntity();
        
        if (entity == null) {
            return;
        }
        
        String layerName = entity.getLayerName();
        if (layerName != null) {
            layerViewer.addWmsLayer(layerName);
        }
            
        Polygon env = entity.getEnvelope();
        if (env != null) {
            layerViewer.zoomTo(env);
        }
        
//        gl.addComponent(new Label(entity.getEcosystemService().toString()), 0,0);
//        gl.addComponent(new Label(entity.getComments()), 0,1);
//        gl.addComponent(new Label(entity.getMinimumMappingUnit()), 0,2);
//        gl.addComponent(new Label(entity.getIndicator().toString()), 1,2);
    }
}
