package org.esp.publisher.form;

import com.vaadin.ui.ComboBox;

    /**
     * Just makes the internal value method public
     * 
     * @author Will Temperley
     *
     */
    public class ComboBox2<T> extends ComboBox implements IAbstractSelect<Object> {
        
        @Override
        public void setInternalValue(Object newValue) {
            super.setInternalValue(newValue);
        }

        public void setImmediate(boolean immediate) {
            super.setImmediate(immediate);
        }

    }