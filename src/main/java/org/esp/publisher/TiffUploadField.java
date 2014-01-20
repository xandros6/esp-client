package org.esp.publisher;

import java.io.File;

import org.vaadin.easyuploads.UploadField;

import com.google.common.net.MediaType;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Upload.FinishedEvent;

public class TiffUploadField extends UploadField {

    private static final String UPLOAD_MESSAGE = "Choose a GeoTiff file to upload.  The file size limit is 150 Megabytes.";
    private static final int FILE_SIZE_LIMIT = 157286400;

    public TiffUploadField() {
        super();
        setFieldType(FieldType.FILE);
        setCaption(UPLOAD_MESSAGE);
    }

    private File file;
    private MediaType mt;

    @Override
    protected String getDisplayDetails() {

        StringBuilder sb = new StringBuilder();
        sb.append("File: ");
        sb.append(getLastFileName());
        return sb.toString();

    }

    public void uploadFinished(FinishedEvent event) {

        super.uploadFinished(event);

        String mimeType = event.getMIMEType();
        mt = MediaType.parse(mimeType);

    }

    public File getFile() {

        if (getValue() != null) {

            File file = (File) getValue();
            if (file.length() > FILE_SIZE_LIMIT) {
                Notification
                        .show("The file size exceeds the limit of 150 Megabytes. Processing aborted.");
                return null;
            }
        }

        if (mt != null && mt.is(MediaType.TIFF)) {
            return file;
        }
        return null;
    }
}
