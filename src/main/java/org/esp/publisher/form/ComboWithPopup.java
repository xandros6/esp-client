package org.esp.publisher.form;

import java.util.List;

import org.esp.publisher.form.EditorController.EditCompleteListener;
import org.jrc.form.component.FormConstants;
import org.jrc.persist.Dao;

import com.vaadin.data.Property;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * A combo box which can manage the items within it.
 * 
 * Deletions have not yet been implemented.
 * 
 * @author Will Temperley
 *
 * @param <T>
 */
public class ComboWithPopup<T> extends CustomField<T> {

    private NoEventCombo combo;
    private Window w;
    private Class<T> clazz;
    private EditorController<T> editor;
    private Dao dao;

    /**
     * Just makes the internal value method visible
     * 
     * @author Will Temperley
     *
     */
    class NoEventCombo extends ComboBox {
        @Override
        protected void setInternalValue(Object newValue) {
            super.setInternalValue(newValue);
        }
    }

    public ComboWithPopup(final Class<T> clazz, Dao dao) {

        this.clazz = clazz;
        this.dao = dao;

        /*
         * The window
         */
        w = new Window();
        w.setModal(true);
        w.setWidth("728px");
        w.setHeight("450px");

        /*
         * The selection widget
         */
        this.combo = new NoEventCombo();
        combo.setWidth(FormConstants.FIELD_DEFAULT_WIDTH);

        combo.setImmediate(true);

        populateCombo();

        combo.addValueChangeListener(new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                Object obj = event.getProperty().getValue();
                setInternalValue((T) obj);
            }
        });
    }

    private MenuBar getEditMenu() {

        MenuBar settings = new MenuBar();
        MenuItem settingsMenu = settings.addItem("", null);
        settingsMenu.setStyleName("icon-cog");

        settingsMenu.addItem("New", new Command() {
            @Override
            public void menuSelected(MenuItem selectedItem) {
                doCreate();
            }
        });

        settingsMenu.addItem("Edit", new Command() {
            @Override
            public void menuSelected(MenuItem selectedItem) {
                doUpdate();
            }
        });
        return settings;
    }

    public void setEditor(EditorController<T> editor) {
        this.editor = editor;
        w.setContent(editor);

        /*
         * Decide what to do
         */
        editor.addEditCompleteListener(new EditCompleteListener<T>() {
            @Override
            public void onEditComplete(T entity) {

//                combo.removeAllItems();
//                populateCombo();
                if (combo.containsId(entity)) {
                    System.out.println("Contained already.");
                } else {
                    System.out.println("Adding.");
                    combo.addItem(entity);
                    combo.setValue(entity);
                }
                
                fireValueChange(false);

                UI.getCurrent().removeWindow(w);

            }
        });
    }

    @Override
    public void setInternalValue(T newValue) {
        combo.setInternalValue(newValue);
        super.setInternalValue(newValue);
    }

    protected void populateCombo() {
        List<T> items = dao.all(clazz);
        for (T t : items) {
            combo.addItem(t);
        }
    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {

        combo.setPropertyDataSource(newDataSource);
        super.setPropertyDataSource(newDataSource);
    }

    @Override
    public T getValue() {
        return (T) combo.getValue();
    }

    protected Component initContent() {
        CssLayout l = new CssLayout();
        l.addStyleName("select-create-field");
        l.addComponent(combo);
        l.addComponent(getEditMenu());
        return l;
    }

    protected void doCreate() {
        UI.getCurrent().addWindow(w);
        w.center();
        editor.doCreate();
    }

    protected void doUpdate() {
        Object value = combo.getValue();
        if (value == null) {
            return;
        }
        UI.getCurrent().addWindow(w);
        w.center();
        editor.doUpdate((T) value);
    }

    @Override
    public Class<? extends T> getType() {
        return clazz;
    }

}
