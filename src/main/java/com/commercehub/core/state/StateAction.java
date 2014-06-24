
package com.commercehub.core.state;

/**
 * An Action is client code that is run in response to happenings in the state
 * machine - for example, activating or deactivating a state may cause the
 * execution of Actions. Also, Actions can be executed in the middle of a
 * transition.
 *
 * @author Orr Bernstein, Matthew Mark Miller
 */
public interface StateAction {
    /**
     * "Execute" this Action - the meaning of this is really defined by the
     * client code that implements this Action
     *
     * @param stateful The Stateful whose state is changing, and that is thus
     *                 indirectly responsible for the execution of this Action
     * @param param A paramater associated with this action in a state machine
     *              defintion, usually a string, can be null.
     */
    void execute(Stateful stateful, Object param);
} // end interface
