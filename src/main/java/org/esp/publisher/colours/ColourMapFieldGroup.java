package org.esp.publisher.colours;

import java.util.List;

import org.esp.domain.blueprint.EcosystemServiceIndicator;
import org.esp.domain.blueprint.EcosystemServiceIndicator_;
import org.esp.domain.publisher.ColourMap;
import org.esp.domain.publisher.ColourMapEntry;
import org.esp.domain.publisher.ColourMap_;
import org.esp.publisher.form.ComboWithPopup;
import org.jrc.form.FieldGroup;
import org.jrc.form.component.SelectOrCreateField;
import org.jrc.persist.Dao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.util.converter.StringToDoubleConverter;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class ColourMapFieldGroup extends FieldGroup<EcosystemServiceIndicator> {

    private Dao dao;
    private ComboWithPopup<ColourMap> cb;
    private CartographicKey ck;
    private DoubleField minValField;
    private DoubleField maxValField;
    private VerticalLayout vl;
    private ColourMapChangeListener listener;
    private ColourMap defaultValue;
    
    Logger logger = LoggerFactory.getLogger(ColourMapFieldGroup.class);

    public Component getContent() {
        return vl;
    }

    public interface ColourMapChangeListener {

        public void onValueChanged(ColourMap colourMap);

    }


    /**
     * Public visibility of {@link TextField#setInternalValue} allows value
     * changes from code to not fire a change event.
     * 
     * @author Will Temperley
     * 
     */
    class DoubleField extends TextField {

        public DoubleField(String caption, StringToDoubleConverter std) {
            super(caption);
            setConverter(std);
        }

        @Override
        public void setInternalValue(String newValue) {
            super.setInternalValue(newValue);
        }

    }

    public ColourMapFieldGroup(Dao dao) {

        super(new BeanFieldGroup<EcosystemServiceIndicator>(
                EcosystemServiceIndicator.class), "Styling", null);

        this.dao = dao;

        List<ColourMap> cms = dao.all(ColourMap.class);
        defaultValue = cms.get(0);

        vl = new VerticalLayout();

        // Fields
        cb = new ComboWithPopup<ColourMap>(ColourMap.class, dao);
        ColourMapEditor cme = new ColourMapEditor(dao);
        cme.init(new ColourMapView());
        cb.setEditor(cme);


        StringToDoubleConverter std = new StringToDoubleConverter();
        minValField = new DoubleField("Min. value", std);
        maxValField = new DoubleField("Max. value", std);
        minValField.setImmediate(true);
        maxValField.setImmediate(true);

        vl.addComponent(cb);
        cb.setImmediate(true);
        // cb.setNullSelectionAllowed(false);

        {
            GridLayout gl = new GridLayout(2, 4);
            ck = new CartographicKey();
            ck.setWidth("30px");

            // Rowspan of 2
            gl.addComponent(ck, 0, 0, 0, 3);
            gl.addComponent(maxValField, 1, 0);
            gl.addComponent(minValField, 1, 3);

            vl.addComponent(gl);

        }

        // for (ColourMap colourMap : cms) {
        // cb.addItem(colourMap);
        // }

        cb.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                fireValueChanged();
            }
        });

        minValField.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                fireValueChanged();
            }
        });

        maxValField.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                fireValueChanged();
            }
        });

        // Bind the fields

        getFieldGroup().bind(minValField,
                EcosystemServiceIndicator_.minVal.getName());
        getFieldGroup().bind(maxValField,
                EcosystemServiceIndicator_.maxVal.getName());
        getFieldGroup()
                .bind(cb, EcosystemServiceIndicator_.colourMap.getName());

    }

    public boolean isValid() {
        if (minValField.getValue() == null) {
            return false;
        }
        if (maxValField.getValue() == null) {
            return false;
        }
        if (cb.getValue() == null) {
            return false;
        }
        return true;
    }

    public void setMinValue(String string) {
        minValField.setInternalValue(string);
    }

    public void setMaxValue(String string) {
        maxValField.setInternalValue(string);
    }

    public ColourMap getColourMap() {

        ColourMap cm = (ColourMap) cb.getValue();

        Double min = (Double) minValField.getConvertedValue();
        Double max = (Double) maxValField.getConvertedValue();

        if (cm == null) {
            cb.setInternalValue(defaultValue);
            return null;
        }

        if (min == null) {
            min = 0d;
            setMinValue("0");
        }

        if (max == null) {
            max = 1d;
            setMaxValue("1");
        }

        List<ColourMapEntry> colourMapEntries = cm.getColourMapEntries();

        if (colourMapEntries.size() != 2) {
            logger.error("Empty colour map entries");
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
            logger.error("Null colormap");
            return;
        }

        List<ColourMapEntry> colourMapEntries = cm.getColourMapEntries();
        ck.setColours(colourMapEntries);

        if (listener != null) {
            listener.onValueChanged(cm);
        }
    }

    public ColourMap getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValues() {
        cb.setInternalValue(defaultValue);
        setMinValue("0");
        setMaxValue("1");
        ck.setColours(defaultValue.getColourMapEntries());
    }

}
