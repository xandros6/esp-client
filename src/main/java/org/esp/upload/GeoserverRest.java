package org.esp.upload;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.esp.domain.blueprint.IndicatorSurface;
import org.esp.domain.publisher.ColourMapEntry;
import org.geotools.data.FeatureReader;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.referencing.CRS;
import org.jrc.persist.Dao;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

public class GeoserverRest extends TiffMetaEx {

    private String workspace = "esp";

    private GeoServerRESTPublisher publisher;

    private Configuration configuration;

    Dao dao;

    static {
        System.setProperty("com.sun.media.jai.disableMediaLib", "true");
    }

    @Inject
    public GeoserverRest(Configuration config,
            @Named("gs_rest_url") String restUrl,
            @Named("gs_user") String restUser,
            @Named("gs_password") String restPassword, Dao dao)
            throws MalformedURLException {

        super(dao);
        this.dao = dao;
        this.publisher = new GeoServerRESTPublisher(restUrl, restUser,
                restPassword);
        this.configuration = config;

    }

    public void extractShpMetadata(File file, IndicatorSurface surface)
            throws IOException, FactoryException, TransformException {

        ShapefileDataStore source = new ShapefileDataStore(file.toURI().toURL());

        FeatureReader<SimpleFeatureType, SimpleFeature> iterator = source
                .getFeatureReader();

        Envelope env = null;

        try {
            while (iterator.hasNext()) {

                SimpleFeature feature = iterator.next();

                Geometry geom = (Geometry) feature.getDefaultGeometry();

                if (env == null) {
                    env = geom.getEnvelopeInternal();
                } else {
                    env.expandToInclude(geom.getEnvelopeInternal());
                }

            }
        } finally {
            
            iterator.close();
            source.dispose();
        }

        // FIXME: issue with nodata
        surface.setMinVal(0d);
        surface.setMaxVal(0d);

        CoordinateReferenceSystem sourceCRS = source.getSchema()
                .getGeometryDescriptor().getCoordinateReferenceSystem();
        Integer epsgCode = CRS.lookupEpsgCode(sourceCRS, true);
        String srid = "EPSG:" + epsgCode;
        surface.setSrid(srid);

        Polygon wgs84Env = envelopeToWgs84(epsgCode, env);
        surface.setEnvelope(wgs84Env);

    }

    /**
     * Publish a geotiff in the defined workspace.
     * 
     * @param geotiff
     * @param srs
     * @param layerAndStoreName
     * @param styleName
     * @return
     * @throws IllegalArgumentException
     * @throws FileNotFoundException
     */
    public boolean publishTiff(File geotiff, String srs,
            String layerAndStoreName, String styleName)
            throws FileNotFoundException, IllegalArgumentException {

        return publisher.publishGeoTIFF(workspace, layerAndStoreName,
                layerAndStoreName, geotiff, srs, ProjectionPolicy.NONE,
                styleName, null);
    }

    public boolean publishShp(File zipFile, String srs,
            String layerAndStoreName, String styleName) {

        try {

            boolean x = publisher.publishShp(workspace, layerAndStoreName,
                    layerAndStoreName, zipFile, srs, styleName);
            return x;

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;

    }

    /**
     * Deletes the store and associated styles for this indicator surface.
     * 
     * @param indicatorSurface
     * @return
     */
    public boolean removeStore(IndicatorSurface indicatorSurface) {
        String layerName = indicatorSurface.getLayerName();
        if (layerName != null) {
            if (indicatorSurface.getSpatialDataType().getLabel()
                    .equals("Raster")) {
                publisher.removeStyle(layerName);
                return publisher
                        .removeCoverageStore(workspace, layerName, true);
            } else if (indicatorSurface.getSpatialDataType().getLabel()
                    .equals("Vector")) {
                return publisher.removeDatastore(workspace, layerName, true);
            }
        }
        return false;
    }
    
    

    @Deprecated
    public boolean publishStyle(String styleName, double min, double max)
            throws IOException {

        Template template = configuration.getTemplate("SldContinuous.ftl");

        Map<String, Object> root = new HashMap<String, Object>();

        List<OldCME> l = new ArrayList<OldCME>();
        OldCME cm0 = new OldCME();
        cm0.setColour("#000000");
        cm0.setValue(0);
        cm0.setOpacity(0);

        OldCME cme = new OldCME();
        cme.setColour("#ffff00");
        cme.setValue(0);
        cme.setOpacity(1);

        OldCME cme2 = new OldCME();
        cme2.setColour("#ff0000");
        cme2.setValue(max);
        cme2.setOpacity(1);

        l.add(cm0);
        l.add(cme);
        l.add(cme2);

        root.put("styleName", styleName);
        // root.put("noData", nodata);
        root.put("colourMapEntries", l);

        try {
            // PrintWriter pw = new PrintWr
            StringWriter sw = new StringWriter();

            template.process(root, sw);

            String sldBody = sw.toString();
            return publisher.publishStyle(sldBody);
        } catch (TemplateException e) {
            e.printStackTrace();
        }
        return false;

    }

    public boolean publishSLD(String styleName, List<ColourMapEntry> cmes)
            throws IOException {
    
        Template template = configuration.getTemplate("SldContinuous.ftl");
    
        Map<String, Object> root = new HashMap<String, Object>();
    
    
        root.put("styleName", styleName);
        root.put("colourMapEntries", cmes);
    
        try {
            // PrintWriter pw = new PrintWr
            StringWriter sw = new StringWriter();
    
            template.process(root, sw);
    
            String sldBody = sw.toString();
            return publisher.publishStyle(sldBody);
        } catch (TemplateException e) {
            e.printStackTrace();
        }
        return false;
    
    }

}
