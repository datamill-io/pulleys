
package pulleys.trigger;

import pulleys.Stateful;
import pulleys.annotations.RefName;
import pulleys.trigger.ConditionalTrigger;

import java.util.Collections;
import java.util.Set;

/**
 * A CreationTrigger is used to jimmy a Stateful object into an initial state
 * based off the state of related statefuls. It is a ConditionalTrigger that
 * only evaluates a single stateful.
 *
 * @author mmiller
 */
@RefName("creation")
public class CreationTrigger extends ConditionalTrigger {
    Set singleStateful;

    public CreationTrigger(Stateful stateful) {
        singleStateful = Collections.singleton(stateful);
    }

    public Set getObserved() {
        return singleStateful;
    }
}
