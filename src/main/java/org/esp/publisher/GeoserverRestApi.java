package org.esp.publisher;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.decoder.RESTFeatureType;
import it.geosolutions.geoserver.rest.decoder.RESTLayer;
import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;
import it.jrc.persist.Dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.esp.domain.publisher.ColourMapEntry;
import org.geotools.data.ows.HTTPResponse;
import org.geotools.data.ows.SimpleHttpClient;
import org.geotools.ows.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class GeoserverRestApi {

    private String workspace;

    private GeoServerRESTPublisher publisher;
    
    private GeoServerRESTReader reader;
    
    private Configuration configuration;
    
    private static Pattern searchAggregate = Pattern.compile("^.*<AggregationResults><.*?>([0-9.E\\-]+)</.*?></AggregationResults>.*$",java.util.regex.Pattern.DOTALL);
    
    String wpsUrl;
    
    String classifyUrl;
    
    private Logger logger = LoggerFactory.getLogger(GeoserverRestApi.class);
    
    private Pattern searchRules = Pattern.compile("^\\s*<Rules>\\s*(.*?)\\s*</Rules>\\s*$",Pattern.DOTALL);

    Dao dao;
    
    SimpleHttpClient httpClient = new SimpleHttpClient();

    static {
        System.setProperty("com.sun.media.jai.disableMediaLib", "true");
    }

    @Inject
    public GeoserverRestApi(Configuration config,
            @Named("gs_rest_url") String restUrl,
            @Named("gs_workspace") String workspace,
            @Named("gs_user") String restUser,
            @Named("gs_password") String restPassword,
            @Named("gs_wps_url") String wpsUrl,
            Dao dao)
            throws ServiceException, IOException {

        this.classifyUrl = restUrl
                + "/rest/sldservice/"
                + workspace
                + ":%s/classify.xml?attribute=%s&method=%s&intervals=%d&ramp=custom&startColor=0x%s&endColor=0x%s&open=true";
        
        this.dao = dao;
        this.publisher = new GeoServerRESTPublisher(restUrl, restUser,
                restPassword);
        this.reader = new GeoServerRESTReader(restUrl, restUser, restPassword);
        
        this.wpsUrl = wpsUrl;
        
        this.configuration = config;
        this.workspace = workspace;
        
        httpClient.setUser(restUser);
        httpClient.setPassword(restPassword);

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

    public boolean updateStyle(String styleName, String attributeName, String templateName, List<ColourMapEntry> cmes, String rules) {

        logger.info("Updating style: " + styleName);

        String sldBody = buildSLDBody(styleName, attributeName, templateName, cmes, rules);

        GSLayerEncoder layer = new GSLayerEncoder();
        layer.setWmsPath("newpath");
        boolean configured = publisher.configureLayer(workspace, styleName, layer);
        logger.info("Configured ok: " + configured);

        return publisher.updateStyle(sldBody, styleName);
        

    }
    
    public boolean publishStyle(String styleName, String attributeName, String templateName, List<ColourMapEntry> cmes, String rules) {

        logger.info("Publishing style: " + styleName);

        String sldBody = buildSLDBody(styleName, attributeName, templateName, cmes, rules);
        return publisher.publishStyle(sldBody);

    }
    

    private String buildSLDBody(String styleName, String attributeName, String templateName, List<ColourMapEntry> cmes, String rules) {

        try {

            Template template = configuration.getTemplate(templateName);

            Map<String, Object> root = new HashMap<String, Object>();

            root.put("styleName", styleName);
            root.put("colourMapEntries", cmes);
            root.put("attributeName", attributeName);
            root.put("rules", rules);

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

    public RESTFeatureType getLayerInfo(String layerName) {
        RESTLayer layer = reader.getLayer(layerName);
        if(layer != null) {
            return reader.getFeatureType(layer);
        }
        return null;
    }

    public double[] getExtrema(String layerName, String attributeName) {
        Template template;
        try {
            template = configuration.getTemplate("Aggregate.ftl");
       
        
            Map<String, Object> root = new HashMap<String, Object>();
    
            root.put("layerName", workspace+":"+layerName);
            root.put("attributeName", attributeName);
            root.put("functionName", "Max");
    
            StringWriter sw = new StringWriter();
            template.process(root, sw);
    
            String wpsRequest = sw.toString();
            SimpleHttpClient httpClient = new SimpleHttpClient();
            HTTPResponse response = httpClient.post(new URL(wpsUrl), IOUtils.toInputStream(wpsRequest), "application/xml");
            String wpsResponse = IOUtils.toString(response.getResponseStream());
            double max = 0.0;
            double min = 0.0;
            Matcher matcher = searchAggregate.matcher(wpsResponse);
            if(matcher.find()) {
                max = Double.parseDouble(matcher.group(1));
            }
            
            root.put("functionName", "Min");
            sw = new StringWriter();
            template.process(root, sw);
            wpsRequest = sw.toString();
            
            response = httpClient.post(new URL(wpsUrl), IOUtils.toInputStream(wpsRequest), "application/xml");
            wpsResponse = IOUtils.toString(response.getResponseStream());
            matcher = searchAggregate.matcher(wpsResponse);
            if(matcher.find()) {
                min = Double.parseDouble(matcher.group(1));
            }
            
            return new double[] {min, max};
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getClassifiedStyle(String layerName, String attributeName, String classificationMethod, String startColor,
            String endColor, int intervals) throws MalformedURLException, IOException {
        String sldServiceUrl = String.format(classifyUrl, layerName, attributeName,
                classificationMethod, intervals, startColor, endColor);
        HTTPResponse response = httpClient.get(new URL(sldServiceUrl));
        try {
            String rules = IOUtils.toString(response.getResponseStream());
            Matcher m = searchRules.matcher(rules);
            if(m.find()) {
                return m.group(1);
            }
            throw new IOException("sldservice response is in the wrong format");
        } finally {
            response.dispose();
        }
    }

    

}
