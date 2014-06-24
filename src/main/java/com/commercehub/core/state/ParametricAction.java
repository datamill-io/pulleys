
package com.commercehub.core.state;

/**
 * An action with a parameter. Used by States and Transitions.
 *
 * @author Matthew Mark Miller
 */
public class ParametricAction {
    private StateAction action;
    private Object param;

    /**
     * Initializes a new ParametricAction object.
     *
     * @param action An instantiated Action
     * @param param A parameter, usually a string
     */
    public ParametricAction(StateAction action, Object param) {
        this.action = action;
        this.param = param;
    }

    /**
     * Executes the Action with a parameter
     *
     * @param stateful The stateful object to execute the Action on.
     */
    public void execute(Stateful stateful) {
        // TODO Auto-generated method stub
        action.execute(stateful, param);
    }

    /**
     * Returns the StateAction
     *
     * @return a StateAction
     */
    public StateAction getStateAction() {
        return action;
    }

    /**
     * Returns the Parameter
     *
     * @return a Parameter
     */
    public Object getParam() {
        return param;
    }
}
