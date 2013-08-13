package org.esp.editor;

import org.esp.domain.blueprint.IndicatorSurface;
import org.esp.upload.GeoserverUploadField;

import com.vaadin.data.Property;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;


public class IndicatorSurfaceField extends CustomField<IndicatorSurface> {
    
    private GeoserverUploadField geoserverUploader;

    public IndicatorSurfaceField(GeoserverUploadField geoserverUploader) {
        this.geoserverUploader = geoserverUploader;
    }

    @Override
    protected Component initContent() {
        return geoserverUploader;
    }

    @Override
    public Class<? extends IndicatorSurface> getType() {
        return IndicatorSurface.class;
    }

    
    @Override
    public void setPropertyDataSource(@SuppressWarnings("rawtypes") Property newDataSource) {
        
        IndicatorSurface entity = (IndicatorSurface) newDataSource.getValue();
        
        if (entity == null) {
            entity = new IndicatorSurface();
        }
        
        geoserverUploader.setValue(entity);
        
        super.setPropertyDataSource(newDataSource);
    }
    
    @Override
    public void setValue(IndicatorSurface newFieldValue) throws Property.ReadOnlyException {
        
        super.setValue(newFieldValue);
    }
}
