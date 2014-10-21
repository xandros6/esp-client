package org.esp.publisher.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

import org.apache.commons.io.FileUtils;
import org.esp.publisher.PublishException;
import org.geotools.factory.Hints;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

public class PublisherUtils {
    
    private static CoordinateReferenceSystem WGS84_CRS = null;
    
    private static Logger logger = LoggerFactory.getLogger(PublisherUtils.class);
    
    protected static CoordinateReferenceSystem getWgs84Crs() throws NoSuchAuthorityCodeException, FactoryException {
        if(WGS84_CRS == null) {
            Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
            CRSAuthorityFactory factory = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", hints);
            WGS84_CRS = factory.createCoordinateReferenceSystem("EPSG:4326");
        }
        return WGS84_CRS;
    }
    
    public static Geometry envelopeToWgs84(ReferencedEnvelope e)
            throws TransformException, FactoryException {

        ReferencedEnvelope transformed = e.transform(getWgs84Crs(), true);
        return JTS.toGeometry(transformed);
    }
    
    /**
     * Unzip file
     * @param zipFile input zip file
     * @param output zip file output folder
     * @throws ZipException 
     */
    public static void unzipFile(String source, String password, String destination) throws ZipException{
        ZipFile zipFile = new ZipFile(source);
        if (zipFile.isEncrypted()) {
            zipFile.setPassword(password);
        }
        zipFile.extractAll(destination);
   }

    public static void zipFile(File zip, String baseFolder, List<String> filesToZip) throws ZipException {
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setDefaultFolderPath(baseFolder);
        zipParameters.setIncludeRootFolder(false);
        ArrayList<File> sourceFileList = new ArrayList<File>();
        for(String file : filesToZip) {
            sourceFileList.add(new File(baseFolder + File.separator + file));
        }
        new ZipFile(zip).createZipFile(sourceFileList, zipParameters);
        
    }
    
    public static void removeFilesFromFolder(String baseFolder, List<String> filesToZip) {
        for(String zippedFile : filesToZip) {
            new File(baseFolder + File.separator + zippedFile).delete();
        }
    }
    
    /**
     * Uncompress the input zip file into a temporary folder
     * @param f
     * @return
     * @throws PublishException
     */
    public static File uncompress(File f) throws PublishException {
        File tempFolder = null; 
        try {
            
            tempFolder = File.createTempFile("esp", "upload");
            tempFolder.delete();
            tempFolder.mkdirs();
            String baseFolder = tempFolder.getAbsolutePath();
            
            PublisherUtils.unzipFile(f.getAbsolutePath(), "", baseFolder);
            File[] files = new File(baseFolder).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".shp");
                }
                
            });
            if(files.length == 1) {
                return files[0];
            } else if(files.length == 0) {
                cleanTempFolder(tempFolder);
                throw new PublishException("No shapefile included in zip");
            } else {
                cleanTempFolder(tempFolder);
                throw new PublishException("Only one shapefile should be included in zip");
            }
            
        } catch(ZipException e) {
            cleanTempFolder(tempFolder);
            throw new PublishException("Input file is not a zip file or is corrupted", e);
        } catch (IOException e) {
            cleanTempFolder(tempFolder);
            throw new PublishException("Cannot create temporary folder to uncompress zip file", e);
        } 
    }

    public static void cleanTempFolder(File tempFolder) {
        if(tempFolder != null && tempFolder.exists() && tempFolder.isDirectory()) {
            try {
                FileUtils.cleanDirectory(tempFolder);
            } catch (IOException e1) {
                logger.warn("Cannot clean temporary folder: " + tempFolder.getAbsolutePath());
            }
        }
    }
    
}
