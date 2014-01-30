package org.esp.publisher.colours;

import org.esp.domain.publisher.ColourMapEntry;
import org.jrc.form.component.ColorField;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItem;
import com.vaadin.shared.ui.colorpicker.Color;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;

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
                    }
                });

        return colourField;
    }
}