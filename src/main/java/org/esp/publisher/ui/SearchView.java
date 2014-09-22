package org.esp.publisher.ui;

import it.jrc.auth.RoleManager;
import it.jrc.domain.auth.Role;
import it.jrc.form.editor.EntityTable;
import it.jrc.form.filter.FilterPanel;
import it.jrc.form.view.TwinPanelView;
import it.jrc.persist.ContainerManager;
import it.jrc.persist.Dao;
import it.jrc.ui.HtmlLabel;
import it.jrc.ui.SimplePanel;

import java.util.Iterator;

import org.esp.domain.blueprint.EcosystemServiceIndicator;
import org.esp.domain.blueprint.EcosystemServiceIndicator_;
import org.esp.publisher.LayerManager;
import org.vaadin.addon.leaflet.LMap;

import com.google.inject.Inject;
import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerItem;
import com.vaadin.data.Property;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vividsolutions.jts.geom.Polygon;

public class SearchView extends TwinPanelView implements View {

    private JPAContainer<EcosystemServiceIndicator> esiContainer;

    private VerticalLayout content = new VerticalLayout();

    private Dao dao;

    private LayerManager layerManager;

    private MapLegend mapLegend;

    private EntityTable<EcosystemServiceIndicator> table;

    private EcosystemServiceIndicator selectedEntity;

    private RoleManager roleManager;

    private static int COL_WIDTH = 400;

    @Inject
    public SearchView(Dao dao, RoleManager roleManager) {

        ContainerManager<EcosystemServiceIndicator> containerManager = new ContainerManager<EcosystemServiceIndicator>(
                dao, EcosystemServiceIndicator.class);
        this.esiContainer = containerManager.getContainer();
        esiContainer.sort(new String[] {"dateUpdated"}, new boolean[]{false});
        this.dao = dao;
        this.layerManager = new LayerManager(new LMap());
        this.roleManager = roleManager;
        {
            SimplePanel leftPanel = getLeftPanel();
            leftPanel.setWidth((COL_WIDTH + 80) +  "px");
            content.setSizeFull();
            leftPanel.addComponent(content);

            setExpandRatio(getRightPanel(), 1);

            FilterPanel<EcosystemServiceIndicator> filterPanel = getFilterPanel();

            HorizontalLayout hl = new HorizontalLayout();
            content.addComponent(hl);
            hl.setWidth("100%");
            HtmlLabel l = new HtmlLabel("Select a map to preview or edit.");

            hl.addComponent(l);
            l.setWidth("300px");
            hl.setExpandRatio(l, 1);

            content.addComponent(filterPanel);

            EntityTable<EcosystemServiceIndicator> ecosystemServiceIndicatorTable = getEcosystemServiceIndicatorTable();
            content.addComponent(ecosystemServiceIndicatorTable);

            content.setExpandRatio(ecosystemServiceIndicatorTable, 1);
        }

        /*
         * Right panel
         */
        {
            SimplePanel rightPanel = getRightPanel();

            HorizontalLayout vl = new HorizontalLayout();
            vl.setSizeFull();
            rightPanel.addComponent(vl);

            vl.addComponent(layerManager.getMap());
            layerManager.getMap().setSizeFull();

            HorizontalLayout hl = new HorizontalLayout();
            hl.setSizeFull();
            vl.addComponent(hl);

            mapLegend = new MapLegend();
            hl.addComponent(mapLegend);
            hl.setSpacing(true);
            
        }

    }

    private FilterPanel<EcosystemServiceIndicator> getFilterPanel() {

        FilterPanel<EcosystemServiceIndicator> fp = new FilterPanel<EcosystemServiceIndicator>(esiContainer, dao);
        fp.addFilterField(EcosystemServiceIndicator_.ecosystemService);
        fp.addFilterField(EcosystemServiceIndicator_.study);
        return fp;

    }

    class ESVisualizationColumn implements Table.ColumnGenerator {
        
        private Role role;

        public ESVisualizationColumn(Role role) {
            this.role = role;
        }

        public Component generateCell(Table source, Object itemId,
                Object columnId) {
            JPAContainerItem<?> item = (JPAContainerItem<?>) source.getItem(itemId);

            HtmlLabel label = new HtmlLabel();

            if (item == null) {
                return label;
            }
            Object entity = item.getEntity();

            final EcosystemServiceIndicator esi = (EcosystemServiceIndicator) entity;
            
            /*
             * Check ownership
             */
            boolean isowner = false;
            if(esi.getRole().equals(role) || role.getIsSuperUser()) {
                isowner = true;
            }
            
            StringBuilder sb = new StringBuilder();

            if (isowner) {
                sb.append("<a href='#!");
                sb.append(ViewModule.getESILink(esi));
                sb.append("'>");
                sb.append(esi.toString());
                sb.append("</a>");
            } else {
                sb.append("<span class='indicator-name'>");
                sb.append(esi.toString());
                sb.append("</span>");
            }
            
            sb.append("<br/>");
            sb.append(esi.getStudy().getStudyName());

            label.setValue(sb.toString());

            return label;

        }
    }


    @Override
    public void enter(ViewChangeEvent event) {
        if (selectedEntity == null) {

            Iterator<?> it = table.getItemIds().iterator();
            Object next = it.next();
            EcosystemServiceIndicator obj = dao.find(EcosystemServiceIndicator.class, next);
            selectedEntity = obj;
            table.select(next);

        }
    }

    private EntityTable<EcosystemServiceIndicator> getEcosystemServiceIndicatorTable() {

        table = new EntityTable<EcosystemServiceIndicator>(esiContainer);
        table.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);

        table.setHeight("100%");
        table.setPageLength(20);

        table.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {

                Long si = (Long) event.getProperty().getValue();
                entitySelected(si);

            }
        });

        ESVisualizationColumn generatedColumn = new ESVisualizationColumn(roleManager.getRole());
        table.addGeneratedColumn("id", generatedColumn);
        table.setColumnWidth("id", COL_WIDTH);
        return table;

    }

    protected void entitySelected(Long id) {
        
        if (id == null) {
            // Todo - nothing is selected - clear the view, or not?
            selectedEntity = null;
            return;
        }

        EntityItem<EcosystemServiceIndicator> x = esiContainer.getItem(id);
        EcosystemServiceIndicator entity = x.getEntity();
        
        this.selectedEntity = entity;

        if (entity == null) {
            return;
        }

        String layerName = entity.getLayerName();
        if (layerName != null) {
            layerManager.setSurfaceLayerName(layerName);
        } else {
            layerManager.setSurfaceLayerName("");
        }

        Polygon env = entity.getEnvelope();
        if (env != null) {
            layerManager.zoomTo(env);
        }

        mapLegend.setValue(entity);

        // gl.addComponent(new Label(entity.getEcosystemService().toString()),
        // 0,0);
        // gl.addComponent(new Label(entity.getComments()), 0,1);
        // gl.addComponent(new Label(entity.getMinimumMappingUnit()), 0,2);
        // gl.addComponent(new Label(entity.getIndicator().toString()), 1,2);
    }
}
