
/**
 *
 */
package pulleys.trigger;

import pulleys.annotations.RefName;
import pulleys.trigger.ConditionalTrigger;

import java.util.Set;

/**
 * A Conditional Trigger which takes as an argument a Set of statefuls to be
 * observed.
 *
 * @author mmiller
 */
@RefName("conditional")
public class SimpleConditionalTrigger extends ConditionalTrigger {
    Set observables;

    public SimpleConditionalTrigger(Set observables) {
        this.observables = observables;
    }

    protected Set getObserved() {
        return observables;
    }
}
