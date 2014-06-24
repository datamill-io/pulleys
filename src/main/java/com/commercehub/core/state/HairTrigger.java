
package com.commercehub.core.state;

/**
 * A HairTrigger always evaluates true, regardless of conditions.
 *
 * <p>Shoot first, and ask questions later</p>
 *
 * @author Matthew Mark Miller
 */
public class HairTrigger implements Trigger {
    public boolean eval(Stateful stateful, Object param, Condition cond) {
        return true;
    }
}
