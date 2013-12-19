package org.esp.upload.old;

import java.io.File;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

import com.google.common.io.Files;

/**
 * Just unzips and renames shapefiles to make them work with Geoserver
 * 
 * @author Will Temperley
 * 
 */
public class DatasetManager {
    

    public File unzip(File file) throws ZipException {

        ZipFile zf = new ZipFile(file);

        File extractionDir = Files.createTempDir();

        if (!extractionDir.exists()) {
            extractionDir.mkdir();
        }
        
        zf.extractAll(extractionDir.getAbsolutePath());
        
        return extractionDir;
    }
    

    public File renameShp(File extracted, String layerAndStoreName) throws ZipException {
        
        //FIXME: just creating another temp dir as a quick fix
        File newTempDir = new File("/tmp");
        ZipFile zf = new ZipFile(new File(newTempDir, layerAndStoreName + ".zip"));
        
        ZipParameters zp = new ZipParameters();
        
        
        File[] fs = extracted.listFiles();
        for (File file : fs) {
            
            String extension = Files.getFileExtension(file.getName());
            
            File newFile = new File(newTempDir, layerAndStoreName + "." + extension);
            
            file.renameTo(newFile);
            
            zf.addFile(newFile, zp);
            
        }
        
        
        return zf.getFile();
    }
    
}
