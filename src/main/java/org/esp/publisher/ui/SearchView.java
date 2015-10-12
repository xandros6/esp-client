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
import org.esp.domain.blueprint.PublishStatus;
import org.esp.publisher.LayerManager;
import org.vaadin.addon.leaflet.LMap;

import com.google.inject.Inject;
import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerItem;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.filter.Compare.Equal;
import com.vaadin.data.util.filter.Or;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
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
        addPublishFilter(esiContainer);
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
            
            /*
             * Top button bar
             */
            if(roleManager.getRole().getIsSuperUser()) {
                HorizontalLayout buttonBar = new HorizontalLayout();
                buttonBar.setSpacing(true);
                buttonBar.setHeight("50px");
                Button pb = new Button("Publish");
                buttonBar.addComponent(pb);
                buttonBar.setComponentAlignment(pb, Alignment.MIDDLE_CENTER);
                Button sb = new Button("Send back");
                buttonBar.addComponent(sb);
                buttonBar.setComponentAlignment(sb, Alignment.MIDDLE_CENTER);
                content.addComponent(buttonBar);
            }

            /*
             * Table
             */
            final EntityTable<EcosystemServiceIndicator> ecosystemServiceIndicatorTable = getEcosystemServiceIndicatorTable();
            content.addComponent(ecosystemServiceIndicatorTable);
            content.setExpandRatio(ecosystemServiceIndicatorTable, 1);
            
            /*
             * Bottom button bar
             */
            if (roleManager.getRole().getIsSuperUser()) {
                HorizontalLayout buttonBar = new HorizontalLayout();
                buttonBar.setSpacing(true);
                buttonBar.setHeight("50px");
                Button sa = new Button("Select all");
                sa.addClickListener(new Button.ClickListener() {
                    public void buttonClick(ClickEvent event) {
                        Iterator<Component> i = ecosystemServiceIndicatorTable.iterator();
                        while (i.hasNext()) {
                            Component c = i.next();
                            if (c.getId() != null && c.getId().equals("published")
                                    && c instanceof CheckBox) {
                                CheckBox cb = (CheckBox) c;
                                cb.setValue(true);
                            }
                        }
                    }
                });
                buttonBar.addComponent(sa);
                buttonBar.setComponentAlignment(sa, Alignment.MIDDLE_CENTER);
                Button da = new Button("Deselect all");
                da.addClickListener(new Button.ClickListener() {
                    public void buttonClick(ClickEvent event) {
                        Iterator<Component> i = ecosystemServiceIndicatorTable.iterator();
                        while (i.hasNext()) {
                            Component c = i.next();
                            if (c.getId() != null && c.getId().equals("published")
                                    && c instanceof CheckBox) {
                                CheckBox cb = (CheckBox) c;
                                cb.setValue(false);
                            }
                        }
                    }
                });
                buttonBar.addComponent(da);
                buttonBar.setComponentAlignment(da, Alignment.MIDDLE_CENTER);
                content.addComponent(buttonBar);
            }
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

        FilterPanel<EcosystemServiceIndicator> fp = new FilterPanel<EcosystemServiceIndicator>(esiContainer, dao){
            @Override
            public void doFiltering() {
                super.doFiltering();
                addPublishFilter(esiContainer);
            }
        };
        fp.addFilterField(EcosystemServiceIndicator_.ecosystemService);
        fp.addFilterField(EcosystemServiceIndicator_.study);
        return fp;

    }

    class ESPublishColumn implements Table.ColumnGenerator {

        public Component generateCell(Table source, Object itemId,
                Object columnId) {
            JPAContainerItem<?> item = (JPAContainerItem<?>) source.getItem(itemId);

            Component checkBox = new Label("");
           

            Object entity = item.getEntity();

            final EcosystemServiceIndicator esi = (EcosystemServiceIndicator) entity;

            /*
             * Check if map is not already published
             */
            boolean isPublished = (esi.getPublishStatus() == PublishStatus.VALIDATED);
            if(!isPublished) {
                checkBox = new CheckBox();
            }            
            checkBox.setId("published");
            return checkBox;

        }
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
            boolean isOwnerOrSupervisor = false;
            if(esi.getRole().equals(role) || role.getIsSuperUser()) {
                isOwnerOrSupervisor = true;
            }

            StringBuilder sb = new StringBuilder();

            if (isOwnerOrSupervisor) {
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
            if (it.hasNext()) {
                Object next = it.next();
                EcosystemServiceIndicator obj = dao.find(EcosystemServiceIndicator.class, next);
                selectedEntity = obj;
                table.select(next);
            }

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

        /*
         * Check super user
         */
        boolean isSuperUser = roleManager.getRole().getIsSuperUser();
        int CHECK_SIZE = 40;
        
        if (isSuperUser) {
            ESPublishColumn publishGeneratedColumn = new ESPublishColumn();
            table.addGeneratedColumn("publish", publishGeneratedColumn);
            table.setColumnWidth("publish", CHECK_SIZE);
        }

        ESVisualizationColumn visualizationGeneratedColumn = new ESVisualizationColumn(roleManager.getRole());
        table.addGeneratedColumn("id", visualizationGeneratedColumn);
        table.setColumnWidth("id", COL_WIDTH - (isSuperUser?CHECK_SIZE:0) );


        return table;

    }

    protected void addPublishFilter(Filterable toFilter) {
        if (!roleManager.getRole().getIsSuperUser()) {
            toFilter.addContainerFilter(
                    new Or(
                            new Equal("publishStatus", PublishStatus.VALIDATED),
                            new Equal("role", roleManager.getRole())
                    )
            );
        }
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
