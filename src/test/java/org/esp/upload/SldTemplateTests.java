package org.esp.upload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.esp.domain.publisher.ColourMapEntry;
import org.junit.Before;
import org.junit.Test;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class SldTemplateTests {
    Configuration configuration;
    List<ColourMapEntry> entries = new ArrayList<ColourMapEntry>();
    
    @Before
    public void setUp() throws IOException {
        configuration = new Configuration();
        configuration.setDirectoryForTemplateLoading(new File("src/test/data"));
        configuration.setObjectWrapper(new DefaultObjectWrapper());
        
        addColourMapEntry(1, "min", 0, 0, 0, 1, 1012.3);
        addColourMapEntry(2, "max", 255, 0, 0, 1012.3, 2016.2);
    }
    private void addColourMapEntry(int id, String label, int red, int green, int blue, double from, double to) {
        ColourMapEntry entry = new ColourMapEntry();
        entry.setId(id);
        entry.setLabel(label);
        entry.setRed(red);
        entry.setGreen(green);
        entry.setBlue(blue);
        entry.setFrom(from);
        entry.setTo(to);
        entries.add(entry);
    }
    @Test
    public void testContinuousVector() throws TemplateException, IOException {
        Template template = configuration.getTemplate("SldVectorContinuous.ftl");

        Map<String, Object> root = new HashMap<String, Object>();

        root.put("styleName", "MyStyle");
        root.put("colourMapEntries", entries);
        root.put("attributeName", "MyAttribute");

        StringWriter sw = new StringWriter();
        template.process(root, sw);

        String sldBody = sw.toString();
        assertTrue(sldBody.contains("<ogc:PropertyName>MyAttribute</ogc:PropertyName>"));
        assertTrue(sldBody.contains("<ogc:Literal>1</ogc:Literal>"));
        assertTrue(sldBody.contains("<ogc:Literal>1012.3</ogc:Literal>"));
        assertTrue(sldBody.contains("<ogc:Literal>2016.2</ogc:Literal>"));
        assertTrue(sldBody.contains("<sld:CssParameter name=\"fill\">#000000</sld:CssParameter>"));
        assertTrue(sldBody.contains("<sld:CssParameter name=\"fill\">#ff0000</sld:CssParameter>"));
    }
}
