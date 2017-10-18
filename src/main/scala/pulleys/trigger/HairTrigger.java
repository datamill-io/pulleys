
package pulleys.trigger;

import pulleys.Condition;
import pulleys.Stateful;
import pulleys.Trigger;
import pulleys.annotations.RefName;

/**
 * A HairTrigger always evaluates true, regardless of conditions.
 *
 * <p>Shoot first, and ask questions later</p>
 *
 * @author Matthew Mark Miller
 */
@RefName("hair")
public class HairTrigger implements Trigger {
    public boolean eval(Stateful stateful, Object param, Condition cond) {
        return true;
    }
}
