package org.esp.publisher;

import java.util.ArrayList;
import java.util.List;

import org.esp.domain.publisher.ColourMap;
import org.esp.domain.publisher.ColourMapEntry;
import org.jrc.form.component.ColorField;
import org.jrc.form.editor.EntityTable;
import org.jrc.persist.Dao;

import com.google.inject.Inject;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.shared.ui.colorpicker.Color;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Button.ClickEvent;

public class CartographicKeyEditor extends HorizontalLayout {

    private Dao dao;

    private CartographicKey ck;

    private BeanItemContainer<ColourMapEntry> bic;
    
    private ColourMap colourMap;

    @Inject
    public CartographicKeyEditor(final Dao dao) {

        this.dao = dao;

        bic = new BeanItemContainer<ColourMapEntry>(ColourMapEntry.class);
        ck = new CartographicKey();

        EntityTable<ColourMapEntry> t = new EntityTable<ColourMapEntry>(bic);

//        bic.addAll(dao.all(ColourMapEntry.class));
        //TODO temp

        t.setVisibleColumns("label");

        t.setPageLength(2);

        t.addGeneratedColumn("color", new ColourColumnGenerator());

        t.setEditable(true);
        t.setSelectable(false);

        addComponent(t);
        addComponent(ck);
        
        Button b = new Button("Save");
        b.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                
                colourMap.setLabel("test");

                if (colourMap.getId() == null) {
                    dao.persist(colourMap);
                } else {
                    dao.merge(colourMap);
                }

            }
        });
        addComponent(b);
    }
    

    class ColourColumnGenerator implements Table.ColumnGenerator {

        public Component generateCell(Table source, Object itemId,
                Object columnId) {

            BeanItem<?> item = (BeanItem<?>) source.getItem(itemId);

            final ColourMapEntry cme = (ColourMapEntry) item.getBean();

            final ColorField colourField = new ColorField();
            colourField.setValue(cme.getColor());

            colourField
                    .addValueChangeListener(new Property.ValueChangeListener() {

                        @Override
                        public void valueChange(ValueChangeEvent event) {
                            Color newC = colourField.getValue();
                            cme.setColor(newC);
                            // colourField.setValue(cme.getColor());
                            // setColours(bic.getItemIds());
                            // bic.addAll(cmes);
                            ck.setColours(bic.getItemIds());
                        }
                    });

            return colourField;
        }
    }

    private List<ColourMapEntry> getDefaultColours() {

        ColourMapEntry minCme = new ColourMapEntry();
        ColourMapEntry maxCme = new ColourMapEntry();

        minCme.setColor(new Color(255, 0, 0));
        minCme.setValue(0d);
        minCme.setLabel("Min");

        maxCme.setColor(new Color(255, 255, 0));
        maxCme.setValue(1d);
        maxCme.setLabel("Max");

        List<ColourMapEntry> cmes = new ArrayList<ColourMapEntry>();
        cmes.add(minCme);
        cmes.add(maxCme);

        return cmes;
    }

    
    
    public ColourMap getColourMap() {
        return colourMap;
    }
    
    public void setColourMap(ColourMap newColourMap) {
        
        if (newColourMap == null) {
            Notification.show("The colourmap should not be missing.");
            return;
        }

        this.colourMap = newColourMap;
        
        
        if (colourMap.getColourMapEntries() == null || newColourMap.getColourMapEntries().isEmpty()) {
            List<ColourMapEntry> defaultColours = getDefaultColours();
            for (ColourMapEntry colourMapEntry : defaultColours) {
                colourMapEntry.setColourMap(colourMap);
            }
            colourMap.setColourMapEntries(defaultColours);
        }

        List<ColourMapEntry> cmes = colourMap.getColourMapEntries();
        bic.removeAllItems();
        bic.addAll(cmes);
        ck.setColours(cmes);
    }
}
