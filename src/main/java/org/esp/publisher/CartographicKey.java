package org.esp.publisher;

import java.util.List;

import org.esp.domain.blueprint.ColourMapEntry;
import org.jrc.ui.SimpleHtmlLabel;

import com.google.common.base.Joiner;
import com.vaadin.shared.ui.colorpicker.Color;

public class CartographicKey extends SimpleHtmlLabel {

    private static String DIV_TEMPLATE = "<div style='position: relative;'><div style='%s'>&nbsp;</div>%s</div>";
    private static String STYLE_TEMPLATE = "position: absolute; background-image: -webkit-linear-gradient(bottom, %s); height:100px; width: 20px;";

    private List<ColourMapEntry> colours;

    private String RGB_TEMPLATE = "rgb(%s,%s,%s) %s";

    double min = Double.POSITIVE_INFINITY;
    double max = Double.NEGATIVE_INFINITY;

    private void update() {
        
        String finalStyle = getStyleString();

        setValue(String.format(DIV_TEMPLATE, finalStyle, "<div style='position: absolute; left: 30px;'>0%</div><div style='position: absolute; top:80px; left: 30px;'>100%</div>"));
    }
    
    private String getStyleString() {

        /*
         * Find min and max vals
         */
        for (ColourMapEntry cme : colours) {
            double value = cme.getValue();
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        /*
         * build RGB
         */
        String[] rgbStrings = new String[colours.size()];
        for (int i = 0; i < rgbStrings.length; i++) {
            ColourMapEntry cme = colours.get(i);
            Color c = cme.getColor();

            int percentage = getPercentage(cme.getValue());
            String pcString = percentage + "%";

            rgbStrings[i] = String.format(RGB_TEMPLATE, c.getRed(),
                    c.getGreen(), c.getBlue(), pcString);
        }

        String colourString = Joiner.on(",").join(rgbStrings);
        String finalStyle = String.format(STYLE_TEMPLATE, colourString);
        return finalStyle;

    }

    /**
     * Get the percentage
     * 
     * @param value
     * @return
     */
    private int getPercentage(Double value) {
        double diff = max - min;
        double relZero = value - min;
        
        if ((int) relZero == 0) {
            return 0;
        }
        return (int) ((relZero / diff) * 100);
    }

    public void setColours(List<ColourMapEntry> cmeList) {

        this.colours = cmeList;
        update();

    }

}
