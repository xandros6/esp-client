package org.esp.publisher.form;

import java.util.List;

import org.jrc.form.FieldGroupMeta;
import org.jrc.form.editor.SubmitPanel;

import com.vaadin.ui.Component;

/**
 * Form views should implement this
 * 
 * @author Will Temperley
 *
 */
public interface IEditorView<T> extends Component {
    

    public SubmitPanel getSubmitPanel();
    
    public void buildForm(List<FieldGroupMeta<T>> fields);

}
