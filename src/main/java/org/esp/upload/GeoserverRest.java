package org.esp.upload;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.jai.RenderedOp;
import javax.media.jai.operator.ExtremaDescriptor;
import javax.persistence.Query;

import org.esp.domain.blueprint.IndicatorSurface;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.FeatureReader;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.jrc.persist.Dao;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.google.common.primitives.Doubles;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class GeoserverRest {

    private String workspace = "esp";

    private GeoServerRESTPublisher publisher;

    private Configuration configuration;

    private Dao dao;

    static {
        System.setProperty("com.sun.media.jai.disableMediaLib", "true");
    }

    @Inject
    public GeoserverRest(Configuration config,
            @Named("gs_rest_url") String restUrl,
            @Named("gs_user") String restUser,
            @Named("gs_password") String restPassword, Dao dao)
            throws MalformedURLException {

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

    public boolean extractTiffMetadata(File tifFile, IndicatorSurface surface)
            throws FactoryException, IOException, TransformException, UnknownCRSException {

        GeoTiffReader gtr = new GeoTiffReader(tifFile);

        try {

            CoordinateReferenceSystem crs = gtr.getCrs();
            Integer epsgCode = CRS.lookupEpsgCode(crs, true);
            
            if (epsgCode == null) {
                ReferenceIdentifier name = crs.getName();
                String crsName = "Unknown";
                if (name != null) {
                    crsName = name.toString();
                }
                throw new UnknownCRSException(crsName);
            }

            String srid = "EPSG:" + epsgCode;

            GridCoverage2D coverage = gtr.read(null);

            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;

            RenderedImage img = coverage.getRenderedImage();

            RenderedOp extremaOp = ExtremaDescriptor.create(img, null, 5, 5,
                    false, 1, null);
            double[] allMins = (double[]) extremaOp.getProperty("minimum");
            min = Doubles.min(allMins);

            double[] allMaxs = (double[]) extremaOp.getProperty("maximum");
            max = Doubles.max(allMaxs);

            surface.setSrid(srid);
            surface.setMaxVal(max);
            surface.setMinVal(min);

            /*
             * Build the envelope and set to WGS84
             */
            GeneralEnvelope origEnv = gtr.getOriginalEnvelope();
            DirectPosition ll = origEnv.getLowerCorner();
            DirectPosition ur = origEnv.getUpperCorner();

            Envelope e = new Envelope();
            e.expandToInclude(ll.getOrdinate(0), ll.getOrdinate(1));
            e.expandToInclude(ur.getOrdinate(0), ur.getOrdinate(1));

            Geometry poly = envelopeToWgs84(epsgCode, e);

            if (poly instanceof Polygon) {
                surface.setEnvelope((Polygon) poly);
            }

            /*
             * Figure out the pixel size
             */
            double pixelSizeX = e.getWidth() / img.getWidth();
            double pixelSizeY = e.getHeight() / img.getHeight();
            surface.setPixelSizeX(pixelSizeX);
            surface.setPixelSizeY(pixelSizeY);

        } finally {

            gtr.dispose();
            
        }

        return true;
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
    protected boolean publishTiff(File geotiff, String srs,
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

    public boolean publishStyle(String styleName, double min, double max)
            throws IOException {

        Template template = configuration.getTemplate("SldContinuous.ftl");

        Map<String, Object> root = new HashMap<String, Object>();

        List<ColourMapEntry> l = new ArrayList<ColourMapEntry>();
        ColourMapEntry cm0 = new ColourMapEntry();
        cm0.setColour("#000000");
        cm0.setValue(0);
        cm0.setOpacity(0);

        ColourMapEntry cme = new ColourMapEntry();
        cme.setColour("#ffff00");
        cme.setValue(0);
        cme.setOpacity(1);

        ColourMapEntry cme2 = new ColourMapEntry();
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

    private Polygon envelopeToWgs84(Integer epsgCode, Envelope e)
            throws FactoryException, TransformException {

        Query q = dao
                .getEntityManager()
                .createNativeQuery(
                        "SELECT st_astext(st_transform(st_setsrid(st_makebox2d(st_point(?1,?2), st_point(?3,?4)), ?5), 4326));");
        q.setParameter(1, e.getMinX());
        q.setParameter(2, e.getMinY());
        q.setParameter(3, e.getMaxX());
        q.setParameter(4, e.getMaxY());
        q.setParameter(5, epsgCode);

        String wkt = (String) q.getSingleResult();

        WKTReader w = new WKTReader();
        Geometry poly;
        try {
            poly = w.read(wkt);
            poly.setSRID(4326);
            return (Polygon) poly;
        } catch (ParseException e1) {
            return null;
        }
        // // Integer epsg = CRS.lookupEpsgCode(crs, true);
        // CoordinateReferenceSystem ncrs = CRS.decode(srid);
        //
        // MathTransform transform = CRS.findMathTransform(ncrs,
        // DefaultGeographicCRS.WGS84);
        // Envelope wgs84Env = JTS.transform(e, transform);
        //
        // PrecisionModel p = new PrecisionModel();
        // GeometryFactory gf = new GeometryFactory(p, 4326);
        // Polygon poly = (Polygon) gf.toGeometry(wgs84Env);
    }

}
