
package com.commercehub.core.state.impl.xml;

/**
 * DOCUMENT ME!
 *
 * @author aalbu
 */
public class ParseException extends Exception {
    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 2354189049123902092L;

    /**
     * Initializes a new ParseException object.
     *
     * @param message DOCUMENT ME!
     */
    ParseException(String message) {
        super(message);
    }

    /**
     * Initializes a new ParseException object.
     *
     * @param message DOCUMENT ME!
     * @param cause DOCUMENT ME!
     */
    ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
