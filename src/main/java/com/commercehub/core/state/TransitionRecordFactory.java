
package com.commercehub.core.state;


/**
 * Used to create TransitionsRecords for auditing and logging purposes.
 *
 * @author Matthew Mark Miller
 */
public interface TransitionRecordFactory {
    /**
     * Creates a record of a transition on a stateful object.
     *
     * @param t The transition that was performed.
     * @param stateful The Stateful object the transition was performed on.
     *
     * @return a new TransitionRecord, or null if none was created
     */
    TransitionRecord newTransitionRecord(Transition t, Stateful stateful);
}
