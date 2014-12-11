package org.esp.publisher.colours;

import it.jrc.form.FieldGroup;
import it.jrc.form.component.IntegerField;
import it.jrc.persist.Dao;

import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.esp.domain.blueprint.Classification;
import org.esp.domain.blueprint.EcosystemServiceIndicator;
import org.esp.domain.blueprint.EcosystemServiceIndicator_;
import org.esp.domain.publisher.ColourMap;
import org.esp.domain.publisher.ColourMapEntry;
import org.esp.publisher.PublishException;
import org.esp.publisher.StylingMetadata;
import org.esp.publisher.form.EditableCombo;
import org.esp.publisher.form.EditableField;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.util.converter.StringToDoubleConverter;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class ColourMapFieldGroup extends FieldGroup<EcosystemServiceIndicator>  implements StylingMetadata {

    public static final int DEFAULT_N_INTERVALS = 10;
    
    private EditableField<ColourMap> editableCombo;

    private CartographicKey ck;

    private DoubleField minValField;

    private DoubleField maxValField;
    
    private IntegerField intervalsNumberField;

    private ComboBox attributesField;
    
    private ComboBox classificationMethodField;

    private VerticalLayout vl;

    private ColourMapChangeListener listener;

    private ColourMapAttributeChangeListener attributeListener;
    
    private StyleChangeListener styleListener;

    private ColourMap defaultValue;
    
    private OptionGroup stylerType;
    
    private Label advancedStylerContainer;
    
    private TextField advancedStylerConfig;
    
    private Button updateStyleButton;
    
    private boolean confirmStylerType = true;
    
    //Button advancedStylerButton;

    Logger logger = LoggerFactory.getLogger(ColourMapFieldGroup.class);
    
    List<Classification> classifications;

    public Component getContent() {
        return vl;
    }

    public interface ColourMapChangeListener {

        public void onValueChanged(ColourMap colourMap);

    }

    public interface ColourMapAttributeChangeListener {
        public void onValueChanged(String attributeName);
    }
    
    public interface StyleChangeListener {
        public void onValueChanged(ColourMap colourMap, String attributeName,
                String classificationMethod, int intervalsNumber, String SLD);
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
                "Lay-out", null);

        List<ColourMap> cms = dao.all(ColourMap.class);
        defaultValue = cms.get(0);

        vl = new VerticalLayout();

        // Styler type Radio Buttons
        List<String> values = new ArrayList<String>(2);
        values.add("Basic");
        values.add("Advanced");                
        stylerType = new OptionGroup("Type",values);
        stylerType.setValue("Basic");
        stylerType.setImmediate(true);
        stylerType.setVisible(false);
        vl.addComponent(stylerType);
        
        stylerType.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                if (stylerType.getValue().equals("Basic")) {
                    if(confirmStylerType) {
                        ConfirmDialog.show(UI.getCurrent(),
                                "Are you sure you wish to switch to Basic Style? You will loose all the changes made",
                                new ConfirmDialog.Listener() {
    
                                    public void onClose(ConfirmDialog dialog) {
                                        if (dialog.isConfirmed()) {
                                            configureBasicStyle();
                                        } else {
                                            stylerType.setValue("Advanced");
                                        }
                                    }
    
                                });
                    } else {
                        configureBasicStyle();
                    }
                    
                } else {
                    setAdvancedVisibility(true);
                    JavaScript.getCurrent().execute("ESPStyler.showStyler()");
                }
        }

            private void configureBasicStyle() {
                if(attributesField.getItemIds().size() > 0) {
                    attributesField.select(attributesField.getItemIds().iterator().next());
                    fireValueChanged();
                }
                setAdvancedVisibility(false);
            }});
        
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
        attributesField.setImmediate(true);
        attributesField.setVisible(false);
        vl.addComponent(attributesField);
        
        classifications = dao.all(Classification.class);
        classificationMethodField = new ComboBox("Classification");
        classificationMethodField.setImmediate(true);
        classificationMethodField.setVisible(false);
        
        for (Classification classification : classifications) {
            classificationMethodField.addItem(classification);
        }
        
        vl.addComponent(classificationMethodField);
        
        intervalsNumberField = new IntegerField("Intervals #");
        intervalsNumberField.setImmediate(true);
        intervalsNumberField.setVisible(false);
        vl.addComponent(intervalsNumberField);
        
        /*advancedStylerButton = new Button("Advanced style");
        advancedStylerButton.setImmediate(true);
        advancedStylerButton.setEnabled(true);
        advancedStylerButton.setStyleName("update-style-button");
        vl.addComponent(advancedStylerButton);*/
        
        advancedStylerContainer = new Label("<div class=\"advanced-styler\" id=\"advancedStyler\"></div>");
        advancedStylerContainer.setContentMode(ContentMode.HTML);
        advancedStylerContainer.setVisible(false);
        vl.addComponent(advancedStylerContainer);
        
        advancedStylerConfig = new TextField();
        advancedStylerConfig.setId("advancedStylerConfig");
        advancedStylerConfig.setValue(null);
        advancedStylerConfig.setImmediate(true);
        advancedStylerConfig.setStyleName("advanced-styler-config");
        vl.addComponent(advancedStylerConfig);
        
        updateStyleButton = new Button("Update style");
        updateStyleButton.setImmediate(true);
        updateStyleButton.setEnabled(false);
        updateStyleButton.setStyleName("update-style-button");
        vl.addComponent(updateStyleButton);
        
        updateStyleButton.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                fireStyleChanged();
            }
            
        });
        
        /*advancedStylerButton.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                JavaScript.getCurrent().execute("ESPStyler.showStyler()");
            }
            
        });*/

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

        attributesField.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                fireAttributeValueChanged();
            }
        });
        
        /*classificationMethodField.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                fireAttributeValueChanged();
            }
        });
        
        intervalsNumberField.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                fireAttributeValueChanged();
            }
        });*/

        // Bind the fields

        getFieldGroup().bind(minValField, EcosystemServiceIndicator_.minVal.getName());
        getFieldGroup().bind(maxValField, EcosystemServiceIndicator_.maxVal.getName());
        getFieldGroup().bind(editableCombo, EcosystemServiceIndicator_.colourMap.getName());
        getFieldGroup().bind(attributesField, EcosystemServiceIndicator_.attributeName.getName());
        getFieldGroup().bind(classificationMethodField, EcosystemServiceIndicator_.classification.getName());
        getFieldGroup().bind(intervalsNumberField, EcosystemServiceIndicator_.intervalsNumber.getName());

    }

    protected void setAdvancedVisibility(boolean visibility) {
        attributesField.setVisible(!visibility);
        classificationMethodField.setVisible(!visibility);
        intervalsNumberField.setVisible(!visibility);
        editableCombo.setVisible(!visibility);
        minValField.setVisible(!visibility);
        maxValField.setVisible(!visibility);
        ck.setVisible(!visibility);
        advancedStylerContainer.setVisible(visibility);
    }

    public void setAttributes(List<String> attributes, String selectedAttribute) {
        attributesField.removeAllItems();
        if(attributes != null && !attributes.isEmpty()) {
            for (String attribute : attributes) {
                attributesField.addItem(attribute);
            }
            attributesField.select(selectedAttribute);
            if(classificationMethodField.getValue() == null) {
                classificationMethodField.select(classifications.get(0));
            }
            setAttributeVisibility(true);
            if(selectedAttribute != null) {
                confirmStylerType = false;
                if(selectedAttribute.equals("*")) {
                    stylerType.setValue("Advanced");
                    JavaScript.getCurrent().execute("ESPStyler.showStyler()");
                } else {
                    stylerType.setValue("Basic");
                }
                confirmStylerType = true;
            
                setAdvancedVisibility(selectedAttribute.equals("*"));
            }
        } else {
            
            setAttributeVisibility(false);
            advancedStylerContainer.setVisible(false);
            editableCombo.setVisible(true);
            minValField.setVisible(true);
            maxValField.setVisible(true);
            ck.setVisible(true);
            advancedStylerConfig.setValue(null);
        }
    }

    private void setAttributeVisibility(boolean visibility) {
        attributesField.setVisible(visibility);
        classificationMethodField.setVisible(visibility);
        intervalsNumberField.setVisible(visibility);
        stylerType.setVisible(visibility);
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

    public String getAttributeName() {
        return attributesField.getValue() == null ? null : attributesField.getValue().toString();
    }
    
    public String getClassificationMethod() {
        return classificationMethodField.getValue() == null ? "equalInterval"
                : ((Classification) classificationMethodField.getValue()).getMethod();
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
    
    public void setUpdateStyleListener(StyleChangeListener listener) {
        this.styleListener = listener;
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
    
    private void fireStyleChanged() {

        ColourMap cm = getColourMap();

        if (cm == null) {
            logger.error("Null colormap");
            return;
        }

        List<ColourMapEntry> colourMapEntries = cm.getColourMapEntries();
        ck.setColours(colourMapEntries);

        if (styleListener != null) {
            styleListener.onValueChanged(cm, 
                    getAttributeName(), 
                    getClassificationMethod(),
                    getIntervalsNumber(), getSLD());
        }
    }

    private boolean isAdvanced() {
        return stylerType.getValue().equals("Advanced");
    }

    private void fireAttributeValueChanged() {
        if (attributeListener != null && getAttributeName() != null) {
            attributeListener.onValueChanged(getAttributeName());
        }
    }

    public int getIntervalsNumber() {
        if(intervalsNumberField.getValue() == null) {
            return DEFAULT_N_INTERVALS;
        }
        try {
            return Integer.parseInt(intervalsNumberField.getValue());
        } catch(NumberFormatException e) {
            return -1;
        }
    }

    public ColourMap getDefaultValue() {
        return defaultValue;
    }

    public void enableUpdateStyle(boolean enabled) {
        updateStyleButton.setEnabled(enabled);
    }
    
    public void setDefaultValues() {
        editableCombo.setInternalValue(defaultValue);
        setMinValue("0");
        setMaxValue("1");
        ck.setColours(defaultValue.getColourMapEntries());

        attributesField.removeAllItems();
        setAttributeVisibility(false);
        updateStyleButton.setEnabled(false);
        
        editableCombo.setVisible(true);
        minValField.setVisible(true);
        maxValField.setVisible(true);
        ck.setVisible(true);
        advancedStylerContainer.setVisible(false);
    }

    public void updateStyle(String styleName,  String style, String symbolType, String attributes) throws PublishException {
        JSONObject json = new JSONObject();
        try {
            json.accumulate("style", style);
            json.accumulate("layer", styleName);
            if(symbolType != null) {
                json.accumulate("symbolType", symbolType);
            }
            if(attributes != null) {
                json.accumulate("attributes", attributes);
            }
            
            JavaScript.getCurrent().execute("ESPStyler.configureStyler("+json.toString()+")");
            advancedStylerConfig.setValue(style);
        } catch (JSONException e) {
            throw new PublishException("Error configuring the advanced styler", e);
        }
        
    }

    @Override
    public String getSLD() {
        return isAdvanced() ? advancedStylerConfig.getValue() : null;
    }
    
    

}
