
package com.commercehub.core.state;

import org.xml.sax.SAXException;

/**
 * Represents an exception uncovered while attempting to parse a
 * StateMachine.xml file
 *
 * @author mmiller
 */
public class StateMachineConfigurationException extends SAXException {
    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 1399129126215487197L;

    /**
     * Initializes a new StateMachineConfigurationException object.
     */
    public StateMachineConfigurationException() {
        super("");
    }

    /**
     * Initializes a new StateMachineConfigurationException object.
     *
     * @param message String message representing the context of this execption
     */
    public StateMachineConfigurationException(String message) {
        super(message);
    }

    /**
     * Initializes a new StateMachineConfigurationException object.
     *
     * @param message String message representing the context of this execption
     * @param cause root cause of this exception
     */
    public StateMachineConfigurationException(String message,
                                              Throwable cause) {
        super(message);
        initCause(cause);
    }

    /**
     * Initializes a new StateMachineConfigurationException object.
     *
     * @param cause root cause of this exception
     */
    public StateMachineConfigurationException(Throwable cause) {
        super("");
        initCause(cause);
    }
}
