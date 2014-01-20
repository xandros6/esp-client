package org.esp.publisher.form;

import java.util.List;

import org.jrc.form.FieldGroupMeta;

import com.vaadin.ui.Component;

/**
 * Form views should implement this
 * 
 * @author Will Temperley
 *
 */
public interface IEditorView<T> extends Component {
    

    public void setSubmitPanel(Component panel);
    
    public void buildForm(List<FieldGroupMeta<T>> fields);

}
