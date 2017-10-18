
package pulleys;

import pulleys.trigger.ConditionalTrigger;

/**
 * Represents a common tuple in this state machine implmenetation -- the Trigger
 * / Parameter pair.
 *
 * @author mmiller
 */
public class TriggerParamPair {
    private Class triggerClass;
    private Object triggerParameter;

    public TriggerParamPair(Class triggerClass, Object triggerParameter) {
        this.triggerClass = triggerClass;
        this.triggerParameter = triggerParameter;
    }

    public Class getTriggerClass() {
        return triggerClass;
    }

    public Object getTriggerParam() {
        return triggerParameter;
    }

    public boolean isConditional() {
        return ConditionalTrigger.class.isAssignableFrom(triggerClass);
    }


    public String toString() {
        String shortName = triggerClass.getName();
        shortName = shortName.substring(shortName.lastIndexOf('.') + 1);
        return shortName + ": " + triggerParameter;
    }
}
