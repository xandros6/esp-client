package org.esp.publisher;

import it.geosolutions.geoserver.rest.decoder.RESTFeatureType;
import it.geosolutions.geoserver.rest.decoder.RESTFeatureType.Attribute;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;
import org.esp.domain.publisher.ColourMap;
import org.esp.domain.publisher.ColourMapEntry;
import org.esp.publisher.utils.PublisherUtils;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Function;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Shapefile publisher module.
 * 
 * @author mauro.bartolomeoli@geo-solutions.it
 *
 */
public class ShapefilePublisher extends AbstractFilePublisher {

    private Logger logger = LoggerFactory.getLogger(ShapefilePublisher.class);
    private FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
    


    protected SimpleFeatureType schema;

    @Inject
    public ShapefilePublisher(GeoserverRestApi gsr) {
        super(gsr);
    }
    
    protected PublishedFileMetadata createMetadata(File zipFile, String layerName)
            throws PublishException, UnknownCRSException {
               
        ShapefileMetadata metadata = new ShapefileMetadata();
        
        ShapefileDataStore dataStore = null;
        try {
            File shapeFile = PublisherUtils.uncompress(zipFile);

            dataStore = new ShapefileDataStore(shapeFile.toURL());
            List<String> attributes = new ArrayList<String>();
            schema = dataStore.getSchema();
            // pick only numeric fields (for themas) 
            for(AttributeDescriptor descriptor : schema.getAttributeDescriptors()) {
                if(Number.class.isAssignableFrom(descriptor.getType().getBinding())) {
                    attributes.add(descriptor.getLocalName());
                }
            }
            metadata.setAttributes(attributes);
            
            ContentFeatureSource featureSource = dataStore.getFeatureSource();
            if(attributes.size() > 0) {
                String attributeName = attributes.get(0);
                configureAttribute(metadata, featureSource, attributeName);
            } else {
                metadata.setMinVal(0d);
                metadata.setMaxVal(100d);

            }
            CoordinateReferenceSystem crs = schema.getCoordinateReferenceSystem();
            setCrs(metadata, crs);

            ReferencedEnvelope e = featureSource.getBounds();
            
            Geometry poly = PublisherUtils.envelopeToWgs84(e);

            if (poly instanceof Polygon) {
                metadata.setEnvelope((Polygon) poly);
            }           

            zipFile = createFinalZip(shapeFile.getParent(),
                    getFileNameWithoutExtension(shapeFile), layerName);
            metadata.setFile(zipFile);
        } catch (MalformedURLException e) {
            throw new PublishException("Error opening the shapefile", e);
        } catch (TransformException e) {
            throw new PublishException("Error transforming bbox to WGS84", e);
        } catch (FactoryException e) {
            throw new PublishException("Error transforming bbox to WGS84", e);
        } catch (IOException e) {
            throw new PublishException("Error reading the shapefile", e);
        } finally {
            dataStore.dispose();
        }

        return metadata;
    }

    private String getFileNameWithoutExtension(File shapeFile) {
        return shapeFile.getName().substring(0, shapeFile.getName().length() - 3);
    }

    
    
    private File createFinalZip(String baseFolder, final String oldName, String layerName) throws PublishException {
        try {
            String[] toRename = new File(baseFolder).list(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(oldName);
                }
                
            });
            List<String> filesToZip = new ArrayList<String>();
            for(String fileToRename : toRename) {
                String fileToZip = layerName + fileToRename.substring(fileToRename.length() - 4);
                new File(baseFolder + File.separator + fileToRename).renameTo(
                        new File(baseFolder + File.separator + fileToZip)
                );
                filesToZip.add(fileToZip);
            }
            File finalZipFile = new File(baseFolder + File.separator + layerName + ".zip");
            PublisherUtils.zipFile(finalZipFile, baseFolder, filesToZip);
            // remove zipped files
            for(String fileToRemove : filesToZip) {
                new File(baseFolder + File.separator + fileToRemove).delete();
            }
            return finalZipFile;
        } catch(ZipException e) {
            throw new PublishException("Cannot create final zip file for publishing", e);
        }
    }

    
    private void configureAttribute(ShapefileMetadata metadata, ContentFeatureSource featureSource,
            String attributeName) throws IOException {
        metadata.setAttributeName(attributeName);                
        Function min = ff.function("Collection_Min", ff.property(attributeName));
        Function max = ff.function("Collection_Max", ff.property(attributeName));
        
        ContentFeatureCollection features = featureSource.getFeatures();
        
        Number value = (Number)min.evaluate( features );
        metadata.setMinVal(value.doubleValue());
        
        value = (Number)max.evaluate( features );
        metadata.setMaxVal(value.doubleValue());
    }

    @Override
    protected String getAttributeName(PublishedFileMetadata metadata) {
        return ((ShapefileMetadata)metadata).getAttributeName();
    }
    
    @Override
    public boolean createLayer(String layerName, String styleName, PublishedFileMetadata metadata)
            throws PublishException {
        return gsr.publishShp(metadata.getFile(), metadata.getSrid(), layerName, styleName);
    }

    
    
    @Override
    public boolean updateStyle(String layerName, String styleTemplate,
            StylingMetadata metadata) throws PublishException {
        String attributeName = metadata.getAttributeName();
        if(attributeName != null) {
            String classificationMethod = metadata.getClassificationMethod();
            int intervalsNumber = metadata.getIntervalsNumber();
            return super.updateStyle(layerName, styleTemplate, getStyleRules(layerName, attributeName, classificationMethod, intervalsNumber, metadata.getColourMap()), metadata);
        }
        return false;
    }

    private String getStyleRules(String layerName, String attributeName, String classificationMethod, int intervalsNumber, ColourMap colourMap) throws PublishException {
        List<ColourMapEntry> entries = colourMap.getColourMapEntries();
        
        String startColor = entries.get(0).getColor().getCSS().substring(1);
        String endColor = entries.get(1).getColor().getCSS().substring(1);
        
        try {
            return gsr.getClassifiedStyle(layerName, attributeName, classificationMethod, startColor, endColor, intervalsNumber);
        } catch (MalformedURLException e) {
            throw new PublishException("sldservice url misconfigured", e);
        } catch (IOException e) {
            throw new PublishException("Error retrieving new style from sldservice", e);
        }
    }

    /**
     * Returns the list of supported attributes for the given layerName.
     * 
     * @param layerName
     * @return
     * @throws PublishException 
     */
    @Override
    public List<String> getAttributes(String layerName) throws PublishException {
        List<String> attributes = new ArrayList<String>();
        RESTFeatureType layerInfo = gsr.getLayerInfo(layerName);
        if(layerInfo != null) {
            for(Attribute attributeInfo : layerInfo.getAttributes()) {
                try {
                    Class<?> binding = Class.forName(attributeInfo.getBinding());
                    if(Number.class.isAssignableFrom(binding)) {
                        attributes.add(attributeInfo.getName());
                    }
                } catch (ClassNotFoundException e) {
                    throw new PublishException("Invalid binding: " + attributeInfo.getBinding(), e);
                }
            }
            return attributes;
        } else {
            throw new PublishException("Layer " + layerName + " is not available on GeoServer");
        }
    }
    
    
    @Override
    protected List<ColourMapEntry> getColourMapEntries(ColourMap colourMap, int intervalsNumber) {
        List<ColourMapEntry> entries = colourMap.getColourMapEntries();
        List<ColourMapEntry> continuous = new ArrayList<ColourMapEntry>();
        ColourMapEntry minEntry = entries.get(0);
        ColourMapEntry maxEntry = entries.get(1);
        double min = minEntry.getValue();
        double max = maxEntry.getValue();
        
        int[] minColor = getColorArray(minEntry);
        int[] maxColor = getColorArray(maxEntry);
        
        double step = (max - min) / (double)intervalsNumber;
        double[] colorSteps = getColorSteps(minColor, maxColor, intervalsNumber);
        for(int i = 0; i < intervalsNumber; i++) {
            ColourMapEntry entry = new ColourMapEntry();
            entry.setId(i+1);
            entry.setFrom(min + i * step);
            entry.setTo(min + (i + 1) * step);
            
            entry.setAlpha((int)Math.round(minColor[0] + i * colorSteps[0]));
            entry.setRed((int)Math.round(minColor[1] + i * colorSteps[1]));
            entry.setGreen((int)Math.round(minColor[2] + i * colorSteps[2]));
            entry.setBlue((int)Math.round(minColor[3] + i * colorSteps[3]));
            
            continuous.add(entry);
        }
        return continuous;
    }

    private double[] getColorSteps(int[] minColor, int[] maxColor, int intervals) {
        double[] steps = new double[minColor.length];
        for(int i = 0; i<minColor.length; i++) {
            steps[i] = ((double)maxColor[i] - minColor[i]) / ((double)intervals); 
        }
        return steps;
    }

    private int[] getColorArray(ColourMapEntry minEntry) {
        return new int[] {minEntry.getAlpha(), minEntry.getRed(), minEntry.getGreen(), minEntry.getBlue()};
    }
    
    /**
     * Checks if the publisher support dynamic styling (to be applied only on 
     * already published data).
     * 
     * @return
     */
    public boolean supportsAdHocStyling() {
        return true;
    }
}