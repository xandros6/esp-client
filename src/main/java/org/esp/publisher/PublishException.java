package org.esp.publisher;

/**
 * Exception thrown when there are problems publishing a spatial data file.
 * 
 * @author mauro.bartolomeoli@geo-solutions.it
 *
 */
public class PublishException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public PublishException() {
        super();
    }

    public PublishException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public PublishException(String arg0) {
        super(arg0);
    }

    public PublishException(Throwable arg0) {
        super(arg0);
    }
    
    
}
