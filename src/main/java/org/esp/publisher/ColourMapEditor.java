package org.esp.publisher;

import java.util.List;

import org.esp.domain.publisher.ColourMap;
import org.esp.domain.publisher.ColourMapEntry;
import org.esp.domain.publisher.ColourMapEntry_;
import org.esp.editor.TwinPanelEditorUI;
import org.esp.upload.TiffMetaEx;
import org.jrc.form.editor.BaseEditor;
import org.jrc.form.editor.EntityTable;
import org.jrc.form.permission.RoleManager;
import org.jrc.persist.Dao;

import com.google.inject.Inject;

public class ColourMapEditor extends BaseEditor<ColourMapEntry> {

    private RoleManager roleManager;
    private CartographicKey key = new CartographicKey();
    private TiffMetaEx gsr;

    @Inject
    public ColourMapEditor(final Dao dao, RoleManager roleManager, TiffMetaEx gsr) {

        super(ColourMapEntry.class, dao);
        
        this.gsr = gsr;
        
        this.roleManager = roleManager;

        EntityTable<ColourMapEntry> table = buildTable();
        
        table.addColumns(ColourMapEntry_.value, ColourMapEntry_.label);

        ff.addField(ColourMapEntry_.value);
        ff.addField(ColourMapEntry_.label);
        ff.addColorField("color");
        
        addFieldGroup("Colour Map Entry");

        TwinPanelEditorUI theView = new TwinPanelEditorUI("Colour Map Entry", "");
        theView.setTable(table);
        init(theView);
        
        theView.addComponent(key);
        key.setHeight("100px");
        key.setWidth("20px");
        key.setValue("TEST");
    }
    
    @Override
    protected void doPreCommit(ColourMapEntry obj) {
        ColourMap cm = dao.find(ColourMap.class, 1l);
        obj.setColourMap(cm);
    }
    
    @Override
    protected void doPostCommit(ColourMapEntry entity) {
        
        entity.getColor().getCSS();

        List<ColourMapEntry> cmes = dao.all(ColourMapEntry.class, ColourMapEntry_.value);
        
        key.setColours(cmes);
        
//        try {
//            boolean x = gsr.publishSLD("esp9999", cmes);
//            if (x) Notification.show("yep!");
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }

}
