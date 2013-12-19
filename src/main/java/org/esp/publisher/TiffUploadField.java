package org.esp.publisher;

import java.io.File;

import org.vaadin.easyuploads.UploadField;

public class TiffUploadField extends UploadField {

    private static final String UPLOAD_MESSAGE = "Choose a GeoTiff file to upload.  The file size limit is 150 Megabytes.";
    private static final int FILE_SIZE_LIMIT = 157286400;

    public TiffUploadField() {
        super();
        setFieldType(FieldType.FILE);
        setCaption(UPLOAD_MESSAGE);
    }
    
    
    @Override
    protected String getDisplayDetails() {

        StringBuilder sb = new StringBuilder();
        sb.append("File: ");
        sb.append(getLastFileName());
        return sb.toString();

    }

    public File getFile() {
        
        if (getValue() != null) {
            return (File) getValue();
        }
        return null;
//        String lastFileName = getLastFileName();
//        if (lastFileName != null) {
//            return new File(lastFileName); 
//        }
//        return null;
    }
}
