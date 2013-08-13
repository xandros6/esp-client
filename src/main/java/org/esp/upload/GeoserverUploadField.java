package org.esp.upload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.EventObject;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import net.lingala.zip4j.exception.ZipException;

import org.esp.domain.blueprint.IndicatorSurface;
import org.esp.domain.blueprint.SpatialDataType;
import org.jrc.persist.Dao;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.google.common.net.MediaType;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.vaadin.data.Property;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.VerticalLayout;
import com.vividsolutions.jts.geom.Polygon;

public class GeoserverUploadField extends CustomField<IndicatorSurface>
        implements Upload.FinishedListener, Upload.FailedListener,
        Upload.Receiver {

    private static final int FILE_SIZE_LIMIT = 157286400;

    private static final String GEOTIFF_ERROR_MSG = "Please ensure this is a GeoTiff with a valid Coordinate Reference System defined.";

    Logger logger = LoggerFactory.getLogger(GeoserverUploadField.class);

    private static final String UPLOAD_BUTTON_CAPTION = "Upload";
    
    private static final String UPLOAD_MESSAGE = "Choose either a GeoTiff file or a zipped shapefile to upload.  The file size limit is 150 Megabytes.";

    private VerticalLayout root;
    
    private File file;

    private GeoserverRest gsr;

    private DatasetManager dsm = new DatasetManager();

    private LayerViewer lv = new LayerViewer();

    private Provider<EntityManager> entityManagerProvider;

    /**
     * Used to capture completed processing jobs
     * 
     * @author tempewi
     * 
     */
    public class ProcessingCompleteEvent extends EventObject {

        private Set<Object> results;

        public ProcessingCompleteEvent(Object source, Set<Object> results) {
            super(source);
            this.results = results;
        }

        public Set<Object> getResults() {
            return results;
        }
    }

    public interface ProcessingCompleteListener {
        public void processingComplete(ProcessingCompleteEvent event);
    }

    @Inject
    public GeoserverUploadField(GeoserverRest gsr, Dao dao, Provider<EntityManager> entityManagerProvider) {

        this.gsr = gsr;
        
        this.entityManagerProvider = entityManagerProvider;

        root = new VerticalLayout();

        /*
         * The upload component
         */
        final Upload upload = new Upload(UPLOAD_MESSAGE, this);
        upload.setButtonCaption(UPLOAD_BUTTON_CAPTION);

        /*
         * Listen for upload completion events
         */
        upload.addFinishedListener(this);

        root.addComponent(upload);

        root.addComponent(lv);

    }

    /**
     * Provides an {@link OutputStream} to which the uploaded file is written
     */
    public OutputStream receiveUpload(String filename, String MIMEType) {
        FileOutputStream fos = null; // Output stream to write to

        File tempUploadLoc = Files.createTempDir();
        file = new File(tempUploadLoc, filename);

        try {
            fos = new FileOutputStream(file);

        } catch (final java.io.FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        return fos;
    }

    public void uploadFailed(Upload.FailedEvent event) {
        Notification.show("Uploading " + event.getFilename() + " of type '"
                + event.getMIMEType() + "' failed.");
    }

    public void uploadFinished(FinishedEvent event) {
        
        if (file.length() > FILE_SIZE_LIMIT) {
            Notification.show("The file size exceeds the limit of 150 Megabytes. Processing aborted.");
            return;
        }

        /*
         * Mime type info
         */
        String mimeType = event.getMIMEType();
        MediaType mt = MediaType.parse(mimeType);

        /*
         * 
         */
        IndicatorSurface surface = getValue();

        if (mt.is(MediaType.TIFF)) {
            try {
                SpatialDataType sdt = getEm().find(SpatialDataType.class, 1l);
                surface.setSpatialDataType(sdt);

                gsr.extractTiffMetadata(file, surface);

                
                /*
                 * If the layer name is already there we must remove the old layer
                 */
                String layerName = surface.getLayerName();
                if (layerName == null || layerName.isEmpty()) {
                    layerName = generateLayerName();
                } else {
                    gsr.removeStore(surface);
                }
                
                /*
                 * Generate a unique layer name
                 */
                String styleName = layerName;

                surface.setLayerName(layerName);

                boolean styleSuccess = gsr.publishStyle(styleName,
                        surface.getMinVal(), surface.getMaxVal());
                logger.debug("Style {} published correctly: {}.", styleName,
                        styleSuccess);

                boolean publishSuccess = gsr.publishTiff(file,
                        surface.getSrid(), layerName, styleName);
                
                if (publishSuccess) {
                    Notification.show("Published successfully.");
                }

            } catch (FactoryException e) {
                Notification
                        .show(GEOTIFF_ERROR_MSG);
            } catch (IOException e) {
                Notification
                        .show(GEOTIFF_ERROR_MSG);
            } catch (TransformException e) {
                Notification
                        .show(GEOTIFF_ERROR_MSG);
            } catch (UnknownCRSException e) {
                Notification
                        .show(e.getMessage());
            }

        } else if (Files.getFileExtension(file.getName()).equals("zip")) {

            try {

                SpatialDataType sdt = getEm().find(SpatialDataType.class, 2l);
                surface.setSpatialDataType(sdt);

                File extracted = dsm.unzip(file);

                String layerAndStoreName = surface.getLayerName();
                if (layerAndStoreName == null || layerAndStoreName.isEmpty()) {
                    layerAndStoreName = generateLayerName();
                } else {
                    gsr.removeStore(surface);
                    //Nullify layer name to avoid situation when layer isn't published but has been deleted.
                    surface.setLayerName(null);
                }

                File newZip = dsm.renameShp(extracted, layerAndStoreName);

                File ex = dsm.unzip(newZip);

                for (File f : ex.listFiles()) {
                    if (Files.getFileExtension(f.getName()).equals("shp")) {
                        try {
                            File shpFile = f;
                            gsr.extractShpMetadata(shpFile, surface);

                            surface.setLayerName(layerAndStoreName);

                            // FIXME default
                            String styleName = "esp-shp-default";
                            boolean publishSuccess = gsr.publishShp(newZip,
                                    surface.getSrid(), layerAndStoreName,
                                    styleName);
                            if (publishSuccess) {
                                Notification.show("Published ok.");
                            } else {
                                Notification.show("Publish error.");
                            }
                        } catch (IOException e) {
                            Notification.show(e.getMessage());
                        } catch (FactoryException e) {
                            Notification.show(e.getMessage());
                        } catch (TransformException e) {
                            Notification.show(e.getMessage());
                        }
                    }
                }

                newZip.delete();
                ex.delete();

            } catch (ZipException e) {
                e.printStackTrace();
                Notification.show(e.getMessage());
            }
        } else {
            Notification
                    .show("This file type was not recognised.  Please ensure you have either provided a zipped shapefile or a GeoTiff.");
        }
        setValue(surface);

    }

    /**
     * Ensures this surface has an ID
     * 
     * @param surface
     * @return
     */
    private String generateLayerName() {

        Query q = getEm()
                .createNativeQuery("select nextval('blueprint.geoserver_layer');");
        Object res = q.getSingleResult();
        if (res instanceof BigInteger) {
            Long id = ((BigInteger) res).longValue();
            // surface.setId(id);
            return "esp-" + id;
        }
        return null;
    }

    public void setValue(IndicatorSurface entity) {

        if (entity != null) {
            String layerName = entity.getLayerName();
            if (layerName != null && !layerName.isEmpty()) {
                lv.addWmsLayer("esp:" + layerName);
                lv.setVisible(true);
                Polygon envelope = entity.getEnvelope();
                if (envelope != null) {
                    lv.zoomTo(envelope);
                }
            } else {
                lv.setVisible(false);
            }
        }

        super.setValue(entity);
    }

    @Override
    public void setPropertyDataSource(@SuppressWarnings("rawtypes") Property newDataSource) {
        setValue((IndicatorSurface) newDataSource.getValue());
        super.setPropertyDataSource(newDataSource);
    }

    @Override
    protected Component initContent() {
        return root;
    }
    
    public GeoserverRest getGsr() {
        return gsr;
    }

    @Override
    public Class<? extends IndicatorSurface> getType() {
        return IndicatorSurface.class;
    }

    private EntityManager getEm() {
        return entityManagerProvider.get();
    }
}