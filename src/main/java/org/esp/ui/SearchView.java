package org.esp.ui;

import org.esp.domain.blueprint.EcosystemServiceIndicator;
import org.jrc.form.editor.EntityTable;
import org.jrc.persist.ContainerManager;
import org.jrc.persist.Dao;
import org.jrc.ui.SimpleHtmlHeader;
import org.jrc.ui.SimpleHtmlLabel;
import org.jrc.ui.SimplePanel;
import org.jrc.ui.baseview.TwinPanelView;

import com.google.inject.Inject;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerItem;
import com.vaadin.data.Property;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class SearchView extends TwinPanelView implements View {

    private JPAContainer<EcosystemServiceIndicator> esiContainer;

            VerticalLayout content = new VerticalLayout();

    @Inject
    public SearchView(Dao dao) {
        ContainerManager<EcosystemServiceIndicator> containerManager = new ContainerManager<EcosystemServiceIndicator>(
                dao, EcosystemServiceIndicator.class);
        this.esiContainer = containerManager.getContainer();
        
        
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

            hl.addComponent(new Label("Filter box"));
            hl.addComponent(getEcosystemServiceIndicatorTable());
            
        }
        
    }

    class ImpactVisualizationColumn implements Table.ColumnGenerator {

        public Component generateCell(Table source, Object itemId,
                Object columnId) {
            JPAContainerItem<?> item = (JPAContainerItem<?>) source
                    .getItem(itemId);
            final EcosystemServiceIndicator si = (EcosystemServiceIndicator) item
                    .getEntity();
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

        private String getLink(EcosystemServiceIndicator species) {
            StringBuilder sb = new StringBuilder("<a href='#!");
            // sb.append(ViewModule.getSpeciesLink(species));
            sb.append("'>");
            sb.append(species.toString());
            sb.append("</a>");
            return sb.toString();
        }
    }

    @Override
    public void enter(ViewChangeEvent event) {
        // TODO Auto-generated method stub

    }

    private EntityTable<EcosystemServiceIndicator> getEcosystemServiceIndicatorTable() {

        EntityTable<EcosystemServiceIndicator> table = new EntityTable<EcosystemServiceIndicator>(esiContainer);
        table.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);

        table.setHeight("100%");
        table.setPageLength(20);

        table.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {

                Long si = (Long) event.getProperty().getValue();

                // speciesImpactSelected(si);
            }
        });

        ImpactVisualizationColumn generatedColumn = new ImpactVisualizationColumn();
        table.addGeneratedColumn("id", generatedColumn);
        table.setColumnWidth("id", 400);
        return table;

    }

}
