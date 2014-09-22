package org.esp.publisher.form;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.ui.Component;

/**
 * A very long-winded way of making an AbstractSelect internal value visible
 * 
 * @author Will Temperley
 *
 * @param <T>
 */
public interface IAbstractSelect<T> extends Container, Container.Viewer, Container.PropertySetChangeListener,
Container.PropertySetChangeNotifier, Container.ItemSetChangeNotifier,
Container.ItemSetChangeListener, Property.Viewer, Property.ValueChangeNotifier, Property<T>, Component {
    
    public void setInternalValue(T newValue);

    public void setImmediate(boolean b);


}
