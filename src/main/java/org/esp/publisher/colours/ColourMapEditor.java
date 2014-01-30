package org.esp.publisher.colours;

import org.esp.domain.publisher.ColourMap;
import org.esp.domain.publisher.ColourMapEntry;
import org.esp.domain.publisher.ColourMap_;
import org.esp.publisher.form.EditorController;
import org.jrc.persist.Dao;

import com.vaadin.data.util.BeanItemContainer;

public class ColourMapEditor extends EditorController<ColourMap> {

    BeanItemContainer<ColourMapEntry> bic;

    public ColourMapEditor(final Dao dao) {

        super(ColourMap.class, dao);
        
        ff.addField(ColourMap_.label);
        ff.addField(ColourMap_.colourMapEntries, new CMEField());
        addFieldGroup("");

    }
    
    @Override
    protected void doPreCommit(ColourMap obj) {
        // TODO Auto-generated method stub
        for (ColourMapEntry cme : obj.getColourMapEntries()) {
            cme.setColourMap(obj);
        }
        super.doPreCommit(obj);
    }

}
