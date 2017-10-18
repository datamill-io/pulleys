package pulleys;

public interface Stateful {
    /**
     * Tests whether a trigger and parameter are mapped and COULD cause a
     * transition. This method doesn't test or mutate state).
     *
     * @param trigger the trigger
     * @param param additional qualifying parameter for mapping trigger to
     *              transitions
     *
     * @return true if a transition would occur as a result of this operation.
     */
    boolean isSupported(Trigger trigger, Object param);

    /**
     * Evaluates the <code>trigger</code>. If the <code>trigger</code> evaluates
     * to <code>true</code>, the runtime type of the <code>trigger</code> and
     * the <code>param</code> are used to find every {@link Transition} that
     * qualifies for execution. These transitions are executed, in order if an
     * order was specified in the state machine definition.
     *
     * <p>Transitions, naturally, cause new states to become active, which in
     * turn expose new transitions. Optionally, the transition lookup and
     * execute process may be performed recursively until no transitions
     * qualify.
     *
     * @param trigger the trigger
     * @param param additional qualifying parameter for mapping trigger to
     *              transitions
     *
     * @return The Trigger's evaluation
     */
    boolean pullTrigger(Trigger trigger, Object param);

    /**
     * Gets a representation of state activation and history.
     *
     * @return sc a StateCookie. Should not be null, but may have its NewFlag
     *         set to true.
     */
    StateCookie getStateCookie();

    /**
     * Notifies the stateful of a potential new value for one of its properties.
     *
     * @param propertyName the name of a property
     * @param newValue the new value to set (usually a String)
     */
    void notifyPropertyChanged(String propertyName, Object newValue);

    /**
     * Tests the internal state of this Stateful.
     *
     * @param statePath The fully qualified (dotted) path name of a state to be
     *                  tested.
     *
     * @return <code>true</code> if the Stateful is "in" the passed state.
     */
    boolean isInState(String statePath);
} // interface
