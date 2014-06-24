
package com.commercehub.core.state;

/**
 * Structure for passing a string property and an object value. Used by
 * StateMachine, SetPropertyStateAction and StateMachineHandler.
 *
 * @author mmiller
 */
public class PropertyValuePair {
    private String property;
    private Object value;

    /**
     * Initializes a new PropertyValuePair object.
     *
     * @param property a string Property
     * @param value an Object value
     */
    public PropertyValuePair(String property, Object value) {
        this.property = property;
        this.value = value;
    }

    /**
     * Gets the String in this structure
     *
     * @return a String
     */
    public String getProperty() {
        return property;
    }

    /**
     * Gets the Object in this structure
     *
     * @return an Object
     */
    public Object getValue() {
        return value;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        return this.property + ": " + this.value;
    }
}
