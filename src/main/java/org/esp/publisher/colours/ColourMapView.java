package org.esp.publisher.colours;

import java.util.List;

import org.esp.domain.publisher.ColourMap;
import org.esp.domain.publisher.ColourMapEntry;
import org.esp.publisher.form.IEditorView;
import org.jrc.form.FieldGroup;
import org.jrc.form.editor.EntityTable;
import org.jrc.form.editor.SubmitPanel;

import com.vaadin.ui.Field;
import com.vaadin.ui.VerticalLayout;

/**
 * The view.
 * 
 * @author Will Temperley
 * 
 */
public class ColourMapView extends VerticalLayout implements
        IEditorView<ColourMap> {

    private SubmitPanel sb;

    public ColourMapView() {
        sb = new SubmitPanel();
    }


    @Override
    public SubmitPanel getSubmitPanel() {
        return sb;

    }

    @Override
    public void buildForm(List<FieldGroup<ColourMap>> fields) {

        for (FieldGroup<ColourMap> fieldGroup : fields) {
            for (Field<?> f : fieldGroup.getFieldGroup().getFields()) {
                addComponent(f);
            }
        }

//        EntityTable<ColourMapEntry> t = new EntityTable<ColourMapEntry>(this.colourMapEditor.bic);
//        t.setVisibleColumns("label");
//        t.setPageLength(2);
//        t.addGeneratedColumn("color", new ColourColumnGenerator());
//        t.setEditable(true);
//        t.setSelectable(false);
//        
        addComponent(sb);
        
    }
}