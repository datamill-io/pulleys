
package pulleys;

import java.util.Set;

/**
 * A "snapshot" representing the internal activation and history of a
 * StateMachine. A StateCookie is passed to a State Machine by a Stateful when
 * it is first "attached." That same cookie is mutated to a current snapshot and
 * returned when {@link StateMachine#getStateCookie()} is called. StateMachines
 * do not update their cookies "on the fly," nor do they cache them. Each call
 * to get a StateCookie will clear the cookie and create it anew. This behavior
 * is important to note.
 *
 * @author Matthew Mark Miller
 */
public interface StateCookie {
    /**
     * Indicates a newly generated cookie with no state set. Used to
     * differentiate between a "null" state and a "new" state. This is set false
     * after any call to setActive().
     *
     * @return true if no value has ever been set active on this cookie
     */
    boolean isNew();

    /**
     * Removes all active and history records from the cookie
     */
    void clear();

    /**
     * Returns the activation of a given State in this snapshot.
     *
     * @param state A State to test activation for
     *
     * @return true if the State was active in this Snapshot
     */
    boolean isActive(State state);

    /**
     * Returns the activation of a given State in this snapshot.
     *
     * @param stateName Path name of A State to test activation for
     *
     * @return true if the State was active in this Snapshot
     */
    boolean isActive(String stateName);

    /**
     * Gets the HistoryChild of a given Parent state
     *
     * @param parentState a State
     *
     * @return the historyChild, or null if none is set.
     */
    State getHistoryChild(State parentState);

    /**
     * Sets a state as historic
     *
     * @param childState a State that is not active
     */
    void setHistoryChild(State childState);

    /**
     * Sets the history states in this cookie to <code>states</code>. Any
     * previous history states are replaced. The client's history cookie ID is
     * updated.
     *
     * @param states set of fully qualified state path names
     */
    void setHistoryStates(Set<String> states);

    /**
     * Sets this state's path name active in the cookie
     *
     * @param state
     */
    void setActive(State state);

    /**
     * Sets this state path name active in the cookie
     *
     * @param stateName a path name
     */
    void setActive(String stateName);

    /**
     * Sets this state's active path names in the cookie
     *
     * @param statePathNames
     */
    void setActive(Set<String> statePathNames);

    /**
     * An unmodifiable set of the pathnames of the active states represented by
     * this cookie.
     *
     * <p>This operation may not return <code>null</code>, although it may
     * return an empty set.
     *
     * @return active state pathnames
     */
    Set getActiveStatePathNames();

    /**
     * An unmodifiable set of the pathnames of the history states represented by
     * this cookie.
     *
     * <p>This operation may not return <code>null</code>, although it may
     * return an empty set.
     *
     * @return history state pathnames
     */
    Set getHistoryStatePathNames();
}
