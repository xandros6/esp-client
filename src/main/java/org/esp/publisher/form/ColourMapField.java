package org.esp.publisher.form;

import java.util.List;

import org.esp.domain.publisher.ColourMap;
import org.esp.domain.publisher.ColourMapEntry;
import org.esp.publisher.CartographicKey;
import org.jrc.persist.Dao;

import com.vaadin.data.Property;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.VerticalLayout;

public class ColourMapField extends CustomField<ColourMap> {
    
    private Dao dao;
    private ComboBox cb;
    private CartographicKey ck;

    public ColourMapField(Dao dao) {
        this.dao = dao;
    }

    @Override
    protected Component initContent() {
        
        VerticalLayout vl = new VerticalLayout();

        cb = new ComboBox();
        vl.addComponent(cb);
        cb.setImmediate(true);

        ck = new CartographicKey();
        vl.addComponent(ck);

        List<ColourMap> cms = dao.all(ColourMap.class);
        for (ColourMap colourMap : cms) {
            cb.addItem(colourMap);
        }

        cb.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                @SuppressWarnings("rawtypes")
                Property property = event.getProperty();
                
                ColourMap cm = (ColourMap) property.getValue();
                ColourMapField.this.setInternalValue(cm);

                if (cm == null) {
                    return;
                }

                List<ColourMapEntry> colourMapEntries = cm.getColourMapEntries();
                colourMapEntries.get(0).setValue(0.0);
                colourMapEntries.get(1).setValue(1.0);
                ck.setColours(colourMapEntries);
            }

        });
        

        return vl;
    }
    
    @Override
    protected void setInternalValue(ColourMap newValue) {
        super.setInternalValue(newValue);
        
        cb.setValue(newValue);
        if (newValue != null) {
            ck.setColours(newValue.getColourMapEntries());
        }
        
    }


    @Override
    public Class<? extends ColourMap> getType() {
        return ColourMap.class;
    }

}
