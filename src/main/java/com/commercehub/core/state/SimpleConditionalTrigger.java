
/**
 *
 */
package com.commercehub.core.state;

import java.util.Set;

/**
 * A Conditional Trigger which takes as an argument a Set of statefuls to be
 * observed.
 *
 * @author mmiller
 */
public class SimpleConditionalTrigger extends ConditionalTrigger {
    /** DOCUMENT ME! */
    Set observables;

    /**
     * Initializes a new SimpleConditionalTrigger object.
     *
     * @param observables DOCUMENT ME!
     */
    public SimpleConditionalTrigger(Set observables) {
        this.observables = observables;
    }

    /**
     * {@inheritDoc}
     *
     * @return DOCUMENT ME!
     */
    protected Set getObserved() {
        return observables;
    }
}
