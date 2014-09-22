package org.esp.publisher.colours;

import it.jrc.form.editor.EntityTable;

import java.util.ArrayList;
import java.util.List;

import org.esp.domain.publisher.ColourMapEntry;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.shared.ui.colorpicker.Color;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;

@SuppressWarnings("rawtypes")
public class CMEField extends CustomField {

    BeanItemContainer<ColourMapEntry> bic = new BeanItemContainer<ColourMapEntry>(
            ColourMapEntry.class);

    @Override
    protected Component initContent() {
        // TODO Auto-generated method stub
        EntityTable<ColourMapEntry> t = new EntityTable<ColourMapEntry>(bic);
        t.setVisibleColumns("label");
        t.setPageLength(2);
        t.addGeneratedColumn("color", new ColourColumnGenerator());
        t.setEditable(true);
        t.setSelectable(false);
        bic.addAll(getDefaultColours());

        return t;
    }

    @Override
    protected void setInternalValue(Object newValue) {
        // TODO Auto-generated method stub
        List<ColourMapEntry> cmel = (List<ColourMapEntry>) newValue;
        bic.removeAllItems();

        if (cmel == null || cmel.isEmpty()) {
            cmel = getDefaultColours();
            bic.addAll(cmel);
        } else {
            bic.addAll(cmel);
        }

        super.setInternalValue(cmel);
    }

    @Override
    public Object getValue() {
        return bic.getItemIds();
    }

    @Override
    public Class<?> getType() {
        return List.class;
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

}
