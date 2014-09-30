package org.esp.publisher;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;
import it.jrc.persist.Dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.esp.domain.blueprint.FileType;
import org.esp.domain.publisher.ColourMapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class GeoserverRestApi {

    private String workspace;

    private GeoServerRESTPublisher publisher;

    private Configuration configuration;
    
    private Logger logger = LoggerFactory.getLogger(GeoserverRestApi.class);

    Dao dao;

    static {
        System.setProperty("com.sun.media.jai.disableMediaLib", "true");
    }

    @Inject
    public GeoserverRestApi(Configuration config,
            @Named("gs_rest_url") String restUrl,
            @Named("gs_workspace") String workspace,
            @Named("gs_user") String restUser,
            @Named("gs_password") String restPassword, Dao dao)
            throws MalformedURLException {

        this.dao = dao;
        this.publisher = new GeoServerRESTPublisher(restUrl, restUser,
                restPassword);
        this.configuration = config;
        this.workspace = workspace;

    }

    /**
     * Publish a geotiff in the defined workspace.
     * 
     * @param geotiff
     * @param srs
     * @param layerAndStoreName
     * @param styleName TODO
     * @return
     * @throws IllegalArgumentException
     * @throws FileNotFoundException
     */
    public boolean publishTiff(File geotiff, String srs, String layerAndStoreName, String styleName)
            throws FileNotFoundException, IllegalArgumentException {

        logger.info("Publishing Geotiff");
        
        if (styleName == null) {
            styleName = layerAndStoreName;
        }

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
     * Deletes the store
     * 
     * @return
     */
    public boolean removeRasterStore(String storeName) {

        logger.info("Removing raster store");
        
        if (storeName == null) {
            return false;
        }

        return publisher.removeCoverageStore(workspace, storeName, true);

    }

    public boolean updateStyle(String styleName, String attributeName, String templateName, List<ColourMapEntry> cmes) {

        logger.info("Updating style: " + styleName);

        String sldBody = buildSLDBody(styleName, attributeName, templateName, cmes);

        GSLayerEncoder layer = new GSLayerEncoder();
        layer.setWmsPath("newpath");
        boolean configured = publisher.configureLayer(workspace, styleName, layer);
        logger.info("Configured ok: " + configured);

        return publisher.updateStyle(sldBody, styleName);
        

    }
    
    public boolean publishStyle(String styleName, String attributeName, String templateName, List<ColourMapEntry> cmes) {

        logger.info("Publishing style: " + styleName);

        String sldBody = buildSLDBody(styleName, attributeName, templateName, cmes);
        return publisher.publishStyle(sldBody);

    }
    

    private String buildSLDBody(String styleName, String attributeName, String templateName, List<ColourMapEntry> cmes) {

        try {

            Template template = configuration.getTemplate(templateName);

            Map<String, Object> root = new HashMap<String, Object>();

            root.put("styleName", styleName);
            root.put("colourMapEntries", cmes);
            root.put("attributeName", attributeName);

            StringWriter sw = new StringWriter();
            template.process(root, sw);

            String sldBody = sw.toString();
            return sldBody;

        } catch (TemplateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    

}
