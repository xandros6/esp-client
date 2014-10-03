package org.esp.publisher.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

import org.geotools.factory.Hints;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;

public class PublisherUtils {
    
    private static CoordinateReferenceSystem WGS84_CRS = null;
    
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
    
}
