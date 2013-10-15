package org.esp.publisher;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.esp.domain.publisher.ColourMapEntry;
import org.jrc.persist.Dao;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class GeoserverRestApi {

    private String workspace;

    private GeoServerRESTPublisher publisher;

    private Configuration configuration;

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
     * Deletes the store 
     * 
     * @return
     */
    public boolean removeRasterStore(String storeName) {
        return publisher.removeCoverageStore(workspace, storeName, true);
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
