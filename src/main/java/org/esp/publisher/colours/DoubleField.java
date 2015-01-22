package org.esp.publisher.colours;

import com.vaadin.data.util.converter.StringToDoubleConverter;
import com.vaadin.ui.TextField;

/**
 * Public visibility of {@link TextField#setInternalValue} allows value changes from code to not fire a change event.
 * 
 * @author Will Temperley
 * 
 */
public class DoubleField extends TextField {

    public DoubleField(String caption, StringToDoubleConverter std) {
        super(caption);
        setConverter(std);
        
    }

    @Override
    public void setInternalValue(String newValue) {
        super.setInternalValue(newValue);
    }

}
