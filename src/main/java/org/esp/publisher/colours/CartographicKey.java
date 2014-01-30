package org.esp.publisher.colours;

import java.util.List;

import org.esp.domain.publisher.ColourMapEntry;
import org.jrc.ui.SimpleHtmlLabel;

import com.google.common.base.Joiner;
import com.vaadin.shared.ui.colorpicker.Color;

public class CartographicKey extends SimpleHtmlLabel {

    private static String DIV_TEMPLATE = "<div style='width: 50px; height: 95px; padding-top: 10px;'><div style='position: relative;'><div style='%s'>&nbsp;</div>%s</div></div>";

    private List<ColourMapEntry> colours;

    private String RGB_TEMPLATE = "rgb(%s,%s,%s) %s";

    Double min;
    Double max;

    /*
     * Allow this as an option - may be useful if values are to be shown on a
     * key.
     */
    boolean showValues = false;

    private void update() {

        String finalStyle = getStyleString();

        StringBuilder sb = new StringBuilder();

        for (ColourMapEntry cme : colours) {
            Double val = cme.getValue();
            double pc = getPercentage(val);
            double height = 80;
            if (showValues) {
                sb.append("<div style='position: absolute; left: 30px; top:"
                        + (int) (height * pc) / 100 + "px;'>");
                sb.append(cme.getValue());
                sb.append("</div>");
            }
        }

        String labels = sb.toString();

        setValue(String.format(DIV_TEMPLATE, finalStyle, labels));
    }

    private String getStyleString() {

        min = Double.POSITIVE_INFINITY;
        max = Double.NEGATIVE_INFINITY;

        /*
         * Find min and max vals
         */
        for (ColourMapEntry cme : colours) {
            Double value = cme.getValue();
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

        String theStyle = String
                .format(
                        "filter: progid:DXImageTransform.Microsoft.gradient(startColorstr=#fffff, endColorstr=#000000); " + 
                          "background-image: -webkit-linear-gradient(bottom, %s); "
                        + "background-image: -moz-linear-gradient(bottom, %s); "
                        + "background-image: linear-gradient(to top, %s);",
                        colourString, colourString, colourString);

        String styleTemplate = "position: absolute; %s height:95px; width: 20px;";
        String finalStyle = String.format(styleTemplate, theStyle);
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
