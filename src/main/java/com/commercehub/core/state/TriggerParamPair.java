
package com.commercehub.core.state;

/**
 * Represents a common tuple in this state machine implmenetation -- the Trigger
 * / Parameter pair.
 *
 * @author mmiller
 */
public class TriggerParamPair {
    private Class triggerClass;
    private Object triggerParameter;

    /**
     * Initializes a new TriggerParamPair object.
     *
     * @param triggerClass DOCUMENT ME!
     * @param triggerParameter DOCUMENT ME!
     */
    public TriggerParamPair(Class triggerClass, Object triggerParameter) {
        this.triggerClass = triggerClass;
        this.triggerParameter = triggerParameter;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Class getTriggerClass() {
        return triggerClass;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Object getTriggerParam() {
        return triggerParameter;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isConditional() {
        return ConditionalTrigger.class.isAssignableFrom(triggerClass);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        String shortName = triggerClass.getName();
        shortName = shortName.substring(shortName.lastIndexOf('.') + 1);
        return shortName + ": " + triggerParameter;
    }
}
