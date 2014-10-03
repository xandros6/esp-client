package org.esp.publisher.colours;

import it.jrc.form.FieldGroup;
import it.jrc.persist.Dao;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.esp.domain.blueprint.EcosystemServiceIndicator;
import org.esp.domain.blueprint.EcosystemServiceIndicator_;
import org.esp.domain.publisher.ColourMap;
import org.esp.domain.publisher.ColourMapEntry;
import org.esp.publisher.form.EditableCombo;
import org.esp.publisher.form.EditableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.util.converter.StringToDoubleConverter;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class ColourMapFieldGroup extends FieldGroup<EcosystemServiceIndicator> {

    private EditableField<ColourMap> editableCombo;

    private CartographicKey ck;

    private DoubleField minValField;

    private DoubleField maxValField;

    private ComboBox attributesField;

    private VerticalLayout vl;

    private ColourMapChangeListener listener;

    private ColourMapAttributeChangeListener attributeListener;

    private ColourMap defaultValue;

    Logger logger = LoggerFactory.getLogger(ColourMapFieldGroup.class);

    public Component getContent() {
        return vl;
    }

    public interface ColourMapChangeListener {

        public void onValueChanged(ColourMap colourMap);

    }

    public interface ColourMapAttributeChangeListener {

        public void onValueChanged(String attributeName);

    }

    /**
     * Public visibility of {@link TextField#setInternalValue} allows value changes from code to not fire a change event.
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

        super(new BeanFieldGroup<EcosystemServiceIndicator>(EcosystemServiceIndicator.class),
                "Styling", null);

        List<ColourMap> cms = dao.all(ColourMap.class);
        defaultValue = cms.get(0);

        vl = new VerticalLayout();

        // Fields
        editableCombo = new EditableCombo<ColourMap>(ColourMap.class, dao);
        ColourMapEditor cme = new ColourMapEditor(dao);
        cme.init(new ColourMapView());
        editableCombo.setEditor(cme);

        StringToDoubleConverter std = new StringToDoubleConverter() {

            @Override
            protected NumberFormat getFormat(Locale locale) {
                return super.getFormat(Locale.ENGLISH);
            }
            
        };
        minValField = new DoubleField("Min. value", std);
        maxValField = new DoubleField("Max. value", std);
        minValField.setImmediate(true);
        maxValField.setImmediate(true);

        // Descriptions
        editableCombo.setDescription(dao.getFieldDescription(EcosystemServiceIndicator_.colourMap));
        minValField.setDescription(dao.getFieldDescription(EcosystemServiceIndicator_.minVal));
        maxValField.setDescription(dao.getFieldDescription(EcosystemServiceIndicator_.maxVal));

        vl.addComponent(editableCombo);
        editableCombo.setImmediate(true);

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

        attributesField = new ComboBox("Attribute");
        attributesField.setVisible(false);
        vl.addComponent(attributesField);

        // for (ColourMap colourMap : cms) {
        // cb.addItem(colourMap);
        // }

        editableCombo.addValueChangeListener(new Property.ValueChangeListener() {
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

        attributesField.addBlurListener(new BlurListener() {


            @Override
            public void blur(BlurEvent event) {
                fireAttributeValueChanged();
                
            }
        });

        // Bind the fields

        getFieldGroup().bind(minValField, EcosystemServiceIndicator_.minVal.getName());
        getFieldGroup().bind(maxValField, EcosystemServiceIndicator_.maxVal.getName());
        getFieldGroup().bind(editableCombo, EcosystemServiceIndicator_.colourMap.getName());

    }

    public void setAttributes(List<String> attributes, String selectedAttribute) {
        for (String attribute : attributes) {
            attributesField.addItem(attribute);
        }
        attributesField.select(selectedAttribute);
        attributesField.setVisible(true);
    }

    public boolean isValid() {
        if (minValField.getValue() == null) {
            return false;
        }
        if (maxValField.getValue() == null) {
            return false;
        }
        if (editableCombo.getValue() == null) {
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

        ColourMap cm = (ColourMap) editableCombo.getValue();

        Double min = (Double) minValField.getConvertedValue();
        Double max = (Double) maxValField.getConvertedValue();

        if (cm == null) {
            editableCombo.setInternalValue(defaultValue);
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

    public void setAttributeListener(ColourMapAttributeChangeListener attributeListener) {
        this.attributeListener = attributeListener;
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

    private void fireAttributeValueChanged() {

        if (attributeListener != null) {
            attributeListener.onValueChanged(attributesField.getValue().toString());
        }
    }

    public ColourMap getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValues() {
        editableCombo.setInternalValue(defaultValue);
        setMinValue("0");
        setMaxValue("1");
        ck.setColours(defaultValue.getColourMapEntries());

        attributesField.removeAllItems();
        attributesField.setVisible(false);
    }

}
