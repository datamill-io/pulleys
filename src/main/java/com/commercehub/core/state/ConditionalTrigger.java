
/**
 *
 */
package com.commercehub.core.state;

import java.util.Set;

/**
 * Abstract Trigger for ConditionalTriggers.
 *
 * @author mmiller
 */
public abstract class ConditionalTrigger implements Trigger {
    /**
     * Evaluates a Condition for a set of observed Stateful objects.
     *
     * @param stateful a Stateful object (unused in this method)
     * @param param an Object
     * @param condition a Condition object, or null
     *
     * @return DOCUMENT ME!
     *
     * @see com.commercehub.core.state.Trigger#eval(com.commercehub.core.state.Stateful,
     *      java.lang.Object)
     */
    public final boolean eval(Stateful stateful, Object param,
                              Condition condition) {
        if (condition != null) {
            return condition.eval(getObserved(), 
            		new InAnyStateConditionEvaluator());
        }
        return false;
    }

    /**
     * Returns a Set of Stateful objects observed by this conditional trigger.
     *
     * @return a Set of Stateful objects
     */
    protected abstract Set getObserved();
}
