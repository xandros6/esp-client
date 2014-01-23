package org.esp.publisher.form;

import java.util.List;

import org.esp.domain.blueprint.EcosystemServiceIndicator;
import org.esp.domain.blueprint.EcosystemServiceIndicator_;
import org.esp.domain.publisher.ColourMap;
import org.esp.domain.publisher.ColourMapEntry;
import org.esp.publisher.CartographicKey;
import org.jrc.form.FieldGroup;
import org.jrc.persist.Dao;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.util.converter.StringToDoubleConverter;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class ColourMapFieldGroup extends FieldGroup<EcosystemServiceIndicator> {

    private Dao dao;
    private ComboBox cb;
    private CartographicKey ck;
    private TextField minValField;
    private TextField maxValField;
    private VerticalLayout vl;
    private ColourMapChangeListener listener;

    public Component getContent() {
        return vl;
    }

    public interface ColourMapChangeListener {

        public void onValueChanged(ColourMap colourMap);

    }

    public ColourMapFieldGroup(Dao dao) {

        super(new BeanFieldGroup<EcosystemServiceIndicator>(
                EcosystemServiceIndicator.class), "Colours", null);
        this.dao = dao;

        vl = new VerticalLayout();

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
                fireValueChanged();
            }
        });

        minValField = new TextField();
        maxValField = new TextField();
        minValField.setImmediate(true);
        maxValField.setImmediate(true);

        minValField.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
//                System.out.println("min: " + event.getProperty().getValue());
                fireValueChanged();
            }
        });

        maxValField.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
//                System.out.println("max: " + event.getProperty().getValue());
                fireValueChanged();
            }
        });

        StringToDoubleConverter std = new StringToDoubleConverter();
        minValField.setConverter(std);
        maxValField.setConverter(std);

        vl.addComponent(minValField);
        vl.addComponent(maxValField);

        // Bind the fields
        getFieldGroup().bind(minValField,
                EcosystemServiceIndicator_.minVal.getName());
        getFieldGroup().bind(maxValField,
                EcosystemServiceIndicator_.maxVal.getName());

    }

    public void setMinValue(String string) {
        minValField.setValue(string);
    }

    public void setMaxValue(String string) {
        maxValField.setValue(string);
    }

    public ColourMap getColourMap() {

        ColourMap cm = (ColourMap) cb.getValue();

        if (cm == null) {
            return null;
            // DO SOMETHING to always return a ColourMap
        }

        List<ColourMapEntry> colourMapEntries = cm.getColourMapEntries();

        Double min = (Double) minValField.getConvertedValue();
        Double max = (Double) maxValField.getConvertedValue();

//        System.out.println("min in getColourMap: " + min);
//        System.out.println("max in getColourMap: " + max);

        if (colourMapEntries.size() != 2) {
            return null;
        }

        colourMapEntries.get(0).setValue(min);
        colourMapEntries.get(1).setValue(max);

        return cm;
    }

    public void setValueChangeListener(ColourMapChangeListener listener) {
        this.listener = listener;
    }

    private void fireValueChanged() {

        ColourMap cm = getColourMap();

        if (cm == null) {
            return;
        }

        List<ColourMapEntry> colourMapEntries = cm.getColourMapEntries();
        ck.setColours(colourMapEntries);

        if (listener != null) {
            listener.onValueChanged(cm);
        }
    }
    // @Override
    // public Class<? extends ColourMap> getType() {
    // return ColourMap.class;
    // }

}
