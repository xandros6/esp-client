package org.esp.publisher;

public class PublishException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public PublishException() {
        super();
    }

    public PublishException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
        super(arg0, arg1, arg2, arg3);
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
