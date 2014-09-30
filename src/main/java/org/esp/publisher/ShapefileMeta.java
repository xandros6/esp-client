package org.esp.publisher;

import java.util.List;


public class ShapefileMeta extends PublishedFileMeta {

    String attributeName;
    
    List<String> attributes;

    /**
     * Sets attributeName for simple themas.
     * 
     * @param attributeName
     */
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeName() {
        return attributeName;
    }
    
    public void setAttributes(List<String> attributes) {
        this.attributes = attributes;
    }

    public List<String> getAttributes() {
        return attributes;
    }
    
}
