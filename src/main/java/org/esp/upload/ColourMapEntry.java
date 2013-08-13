package org.esp.upload;

public class ColourMapEntry {

    private String colour;

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    private double value;

    public double getValue() {
        return value;
    }

    public void setValue(double min) {
        this.value = min;
    }

    private double opacity;

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

    private String label;

    public String getLabel() {
        if (label == null) {
            return "";
        }
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
