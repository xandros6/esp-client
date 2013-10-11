package org.esp.publisher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.EventObject;
import java.util.Set;

import javax.persistence.Query;

import org.esp.domain.blueprint.SpatialDataType;
import org.esp.upload.TiffMetaEx;
import org.esp.upload.UnknownCRSException;
import org.jrc.persist.Dao;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.google.common.net.MediaType;
import com.google.inject.Inject;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.VerticalLayout;

public class TiffUploader extends Panel implements Upload.FinishedListener, Upload.FailedListener,
        Upload.Receiver {

    private static final int FILE_SIZE_LIMIT = 157286400;

    private static final String GEOTIFF_ERROR_MSG = "Please ensure this is a GeoTiff with a valid Coordinate Reference System defined.";

    Logger logger = LoggerFactory.getLogger(TiffUploader.class);

    private static final String UPLOAD_BUTTON_CAPTION = "Upload";
    
    private static final String UPLOAD_MESSAGE = "Choose a GeoTiff file to upload.  The file size limit is 150 Megabytes.";

    private VerticalLayout root;
    
    private File file;

    private TiffMetadataExtractor tme;

    private ProcessingCompleteListener processingCompleteListener;


    /**
     * Used to capture completed processing jobs
     * 
     * @author tempewi
     * 
     */
    public class ProcessingCompleteEvent extends EventObject {

        private TiffMeta results;

        public ProcessingCompleteEvent(Object source, TiffMeta tiffMeta) {
            super(source);
            this.results = tiffMeta;
        }

        public TiffMeta getResults() {
            return results;
        }
    }

    public interface ProcessingCompleteListener {
        public void processingComplete(ProcessingCompleteEvent event);
    }

    @Inject
    public TiffUploader(TiffMetadataExtractor tme) {
        
        this.tme = tme;

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

        setContent(root);
    }
    
    public void addProcessingCompleteListener(ProcessingCompleteListener listener) {
        this.processingCompleteListener = listener;
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
        
        if (mt.is(MediaType.TIFF)) {
            try {
                
                TiffMeta tiffMeta = new TiffMeta();

                tme.extractTiffMetadata(file, tiffMeta);
                
                if (processingCompleteListener != null) {
                    processingCompleteListener.processingComplete(new ProcessingCompleteEvent(this, tiffMeta));
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

        }

    }

    public File getFile() {
        return file;
    }


}