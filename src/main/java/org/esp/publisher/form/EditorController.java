package org.esp.publisher.form;

import java.util.Collection;
import java.util.List;

import org.esp.domain.blueprint.EcosystemServiceIndicator;
import org.jrc.form.ButtonFactory;
import org.jrc.form.FieldGroupManager;
import org.jrc.form.FieldGroup;
import org.jrc.form.JpaFieldFactory;
import org.jrc.form.editor.BaseEditor;
import org.jrc.form.editor.SubmitPanel;
import org.jrc.form.filter.FilterPanel;
import org.jrc.persist.ContainerManager;
import org.jrc.persist.Dao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;

/**
 * 
 * A simplified verion of {@link BaseEditor}
 * 
 * Probably do not require all the container stuff.
 * 
 */
public abstract class EditorController<T> extends Panel {

    private static final String EDITING_FORMAT_STRING = "Editing: %s";

    private static final String SAVE_MESSAGE = "Saved successfully.";

    private Logger logger = LoggerFactory.getLogger(EditorController.class);

    protected Dao dao;

    protected FilterPanel<T> filterPanel;

    private FieldGroupManager<T> fgm = new FieldGroupManager<T>();

    /*
     * The field factory
     */
    protected JpaFieldFactory<T> ff;

    protected ContainerManager<T> containerManager;

    public EditorController(final Class<T> clazz, final Dao dao) {

        this.dao = dao;
        this.ff = new JpaFieldFactory<T>(dao, clazz);

        containerManager = new ContainerManager<T>(dao, clazz);

        /*
         * Filter panel
         */
        filterPanel = new FilterPanel<T>(containerManager.getContainer(), dao);
    }

    protected void fireObjectSelected(ValueChangeEvent event) {

        Object id = event.getProperty().getValue();
        // Object id = table.getValue();
        if (id == null) {
            return;
        }
        doEditById(id);

    }

    /**
     * Designed to be overridden to allow customised form construction.
     * 
     * @param view
     */
    public void init(IEditorView<T> view) {

        this.setContent(view);

        view.buildForm(fgm.getFieldGroupReprs());

        buildSubmitPanel(view.getSubmitPanel());

    }

    protected FieldGroup<T> addFieldGroup(String name) {
        FieldGroup<T> fieldGroupMeta = ff.getFieldGroup(name);
        fgm.add(fieldGroupMeta);
        return fieldGroupMeta;
    }

    protected void buildSubmitPanel(SubmitPanel submitPanel) {
        /*
         * Submit panel
         */
        Button commit = ButtonFactory.getButton(
                ButtonFactory.SAVE_BUTTON_CAPTION, ButtonFactory.SAVE_ICON);
        commit.setEnabled(containerManager.canUpdate());

        commit.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                commitForm();
            }
        });

        Button delete = ButtonFactory.getButton(
                ButtonFactory.DELETE_BUTTON_CAPTION, ButtonFactory.DELETE_ICON);
        delete.setEnabled(containerManager.canDelete());

        delete.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {

                ConfirmDialog.show(UI.getCurrent(),
                        "Are you sure you wish to delete this record?",
                        new ConfirmDialog.Listener() {

                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    doDelete();

                                }
                            }

                        });
            }
        });

        submitPanel.addLeft(commit);
        submitPanel.addRight(delete);
    }

    /**
     * The field factory is a custom component for each editor. For polymorphic
     * entities, the field factory is passed the object to allow it to determine
     * the correct subclass.
     * 
     * @return the custom field factory
     */
    public JpaFieldFactory<T> getFieldFactory() {
        return ff;
    }

    protected boolean commitForm() {

        T entity = fgm.getEntity();

        boolean x = fgm.isValid();
        if (x == false) {
            Notification.show("Validation failed");
            return false;
        }

        try {
            fgm.commit();
        } catch (CommitException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

        /*
         * Subclasses may define tasks to perform pre commit.
         */
        doPreCommit(entity);

        Object id = containerManager.addEntity(entity);

        Notification.show(SAVE_MESSAGE);

        entity = containerManager.findEntity(id);

        /*
         * Subclasses may define tasks to perform post-commit.
         */
        doPostCommit(entity);

        editComplete();
        return true;
    }

    /**
     * Sets the id of the entity that should be edited. Should be overridden if
     * the ID isn't of type {@link Long}.
     * 
     * TODO: could determine the PK type and cast to this.
     * 
     */
    private void doEditById(Object id) {
        T entity = containerManager.findEntity(id);

        if (entity != null) {
            Notification.show(String.format(EDITING_FORMAT_STRING,
                    entity.toString()));
            doUpdate(entity);
        }
    }

    public void doCreate() {
        fgm.setEntity(containerManager.newEntity());
    }

    public void doUpdate(T entity) {
        fgm.setEntity(entity);
    }

    private void doDelete() {
        T entity = fgm.getEntity();
        if (entity == null) {
            logger.error("Delete attempted with null entity.");
            return;
        }
        containerManager.deleteEntity(entity);
        editComplete();
        doPostDelete(entity);
    }

    /**
     * Called after deletion for any cleanup that may be required.
     * 
     * @param entity
     */
    protected void doPostDelete(T entity) {
        // containerManager.refresh();
    }

    /**
     * Called when editing is over - cancel / save may have been called.
     */
    private void editComplete() {

    }

    /**
     * Can be overridden by classes that require parent child associations to be
     * fixed.
     */
    protected void doPreCommit(T obj) {
    }

    /**
     * Subclasses can use this method for obtaining notification of commit
     * actions.
     * 
     * @param entity
     */
    protected void doPostCommit(T entity) {
    }

    /**
     * Creates a new entity of the class via reflection, but is designed to be
     * overridden to add custom behaviour, e.g. creating a particular sub-class.
     */
    protected void addButtonClicked() {
        doUpdate(containerManager.newEntity());
    }

    public void refresh() {
        containerManager.refresh();
    }

    public T getEntity() {
        return fgm.getEntity();
    }

    protected List<FieldGroup<T>> getFieldGroups() {
        return fgm.getFieldGroupReprs();
    }
}
