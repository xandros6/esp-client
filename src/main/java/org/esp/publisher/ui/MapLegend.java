package org.esp.publisher.ui;

import it.jrc.ui.HtmlHeader;
import it.jrc.ui.HtmlLabel;

import java.util.List;

import org.esp.domain.blueprint.EcosystemServiceIndicator;
import org.esp.domain.blueprint.Study;
import org.esp.domain.publisher.ColourMap;
import org.esp.domain.publisher.ColourMapEntry;
import org.esp.publisher.colours.CartographicKey;

import com.vaadin.ui.CssLayout;

/**
 * The map legend used in {@link SearchView}
 * 
 * @author Will Temperley
 * 
 */
public class MapLegend extends CssLayout {

    private HtmlHeader header = new HtmlHeader();

    private HtmlLabel label = new HtmlLabel();

    private CartographicKey ck = new CartographicKey();

    public MapLegend() {
        addComponent(header);
        addComponent(label);
        addComponent(ck);
        ck.showValues(true);
        setSizeFull();
        addStyleName("map-legend");
    }

    public void setValue(EcosystemServiceIndicator entity) {
        header.setCaption(entity.getIndicator().getLabel());

        ColourMap colourMap = entity.getColourMap();
        if (colourMap != null) {
            ck.setVisible(true);
            List<ColourMapEntry> colourMapEntries = colourMap
                    .getColourMapEntries();

            if (colourMapEntries != null && colourMapEntries.size() == 2) {
                Double minVal = entity.getMinVal();
                if (minVal == null) {
                    minVal = 0d;
                }
                colourMapEntries.get(0).setValue(minVal);
                Double maxVal = entity.getMaxVal();
                if (maxVal == null) {
                    maxVal = 1d;
                }
                colourMapEntries.get(1).setValue(maxVal);
                ck.setColours(colourMap.getColourMapEntries());
            }

        } else {
            ck.setVisible(false);
        }

        label.setValue(getLink(entity.getStudy()));
        label.setWidth("100%");
    }

    private String getLink(Study s) {

        StringBuilder sb = new StringBuilder();
        
        sb.append("Study reference: <br/>");

        if (s.getUrl() != null) {
            sb.append("<a href='");
            sb.append(s.getUrl());
            sb.append("' target='_blank'>");
            sb.append(s.getStudyName());
            sb.append("</a>");
        } else {
            sb.append(s.getStudyName());
        }
        
        sb.append("<br/>");
        sb.append(s.getProjectReferences());

        return sb.toString();
    }

}
