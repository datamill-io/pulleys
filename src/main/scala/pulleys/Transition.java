
package pulleys;

import pulleys.action.ParametricAction;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * States are activated by transitions. Each transition has an {@link
 * #getExitState() exit state} and an {@link #getEntryState() entry state}. A
 * transition may be {@link #fire() fired} if its exit state is active. When
 * fired, it activates its entry state.
 *
 * <p>Note that it is not the responsibility of the transition to deactivate its
 * exit state. The exit state may be, and usually is, deactivated when a
 * transition fires, but only as a side effect of activation of the entry state.
 * {@link ExclusiveState Exclusive states} allow at most one active child; this
 * is the mechanism responsible for state deactivation.</p>
 *
 * <p>Transitions may have {@link #getActions() actions} associated with them. A
 * transition's actions are fired after its exit state is deactivated, if it is
 * to be deactivated, and before its entry state is activated.</p>
 *
 * @author Orr Bernstein, <a href="mailto:jpulley@commercehub.com">J. Pulley</a>
 * @version 1.0
 * @version 1.1 6-Aug-2005
 */
public class Transition {
    private State exit;
    private State entry;
    private List actions;
    private String name;
    private StateMachine machine;
    private Collection rankedStates;

    /**
     * Initializes a new transition.
     *
     * @param machine the StateMachine this Transition will run in.
     * @param exit exit state
     * @param entry entry state
     * @param name the transition name, as referenced by triggers
     *
     * @throws NullPointerException if either argument is <code>null</code>
     */
    public Transition(StateMachine machine, State exit, State entry,
                      String name) {
        this.machine = machine;
        this.exit = exit;
        this.entry = entry;
        this.name = name;
        actions = new LinkedList();
        rankedStates = new LinkedHashSet();
    }

    /**
     * This transition's exit state. This transition may be fired only if its
     * exit state is active.
     *
     * @return exit state
     */
    public State getExitState() {
        return exit;
    }

    /**
     * This transition's entry state. When fired, this transition activates its
     * entry state
     *
     * @return entry state
     */
    public State getEntryState() {
        return entry;
    }

    /**
     * An {@link Collections#unmodifiableList(List) unmodifiable List} of this
     * transition's actions. A transition executes its actions after the exit
     * state is deactivated, if it is to be deactivated, and before its entry
     * state is activated. The actions are executed in the order they appear in
     * the list.
     *
     * @return A List of transition ParametricActions
     */
    public List getActions() {
        return Collections.unmodifiableList(actions);
    }

    /**
     * Add <code>newAction</code> to the list of actions. The same action may
     * appear more than once in the list.
     *
     * @param newAction action to add
     * @param param parameter passed when this action is executed
     *
     * @throws NullPointerException if <code>newAction</code> is null
     */
    public void addAction(StateAction newAction, Object param) {
        if (newAction == null) {
            throw new NullPointerException("action may not be null");
        }
        actions.add(new ParametricAction(newAction, param));
    }

    /**
     * Add <code>newAction</code> to the list of actions, at the specified
     * <code>index</code>. The same action may appear more than once in the
     * list.
     *
     * @param index zero-based location in the list of actions
     * @param newAction action to add
     * @param param parameter passed when this action is executed
     *
     * @throws IndexOutOfBoundsException if <code>index</code> is less than zero
     *                                   or greater than the current number of
     *                                   actions
     * @throws NullPointerException if <code>newAction</code> is null
     */
    public void addAction(int index, StateAction newAction, Object param) {
        if (newAction == null) {
            throw new NullPointerException("action may not be null");
        }
        actions.add(index, new ParametricAction(newAction, param));
    }

    /**
     * Replace the action at <code>index</code> with <code>newAction</code>.
     * Returns the action previously at <code>index</code>.
     *
     * @param index zero-based location in the list of actions
     * @param newAction action to add
     * @param param parameter passed when this action is executed
     *
     * @return a ParametricAction previously at <code>index</code>
     *
     * @throws IndexOutOfBoundsException if <code>index</code> is less than zero
     *                                   or greater than or equal to the current
     *                                   number of actions
     * @throws NullPointerException if <code>newAction</code> is null
     */
    public ParametricAction setAction(int index, StateAction newAction,
                                      Object param) {
        if (newAction == null) {
            throw new NullPointerException("action may not be null");
        }
        return (ParametricAction) actions.set(index,
                new ParametricAction(newAction, param));
    }

    /**
     * Removes the action at <code>index</code> from the list of actions. The
     * removed action is returned.
     *
     * @param index zero-based location in the list of actions
     *
     * @return a ParametricAction previously at <code>index</code>
     *
     * @throws IndexOutOfBoundsException if <code>index</code> is less than zero
     *                                   or greater than or equal to the current
     *                                   number of actions
     */
    public ParametricAction removeAction(int index) {
        return (ParametricAction) actions.remove(index);
    }

    /**
     * Removes <code>action</code> from the list of actions. Unlike {@link
     * List#remove(Object)}, this operation removes all occurances from the
     * list. Returns whether the list was actually modified as a result of this
     * operation, i.e. whether <code>action</code> was originally in the list.
     *
     * @param action ParametricAction to remove
     *
     * @return whether <code>action</code> was originally in the list of actions
     */
    public boolean removeAction(ParametricAction action) {
        boolean removed = false;
        boolean again = true;
        while (again) {
            again = actions.remove(action);
            removed |= again;
        }
        return removed;
    }

    /**
     * Returns the Name of this transition
     *
     * @return A String indicating this transition's name
     */
    public String getName() {
        return name;
    }

    /**
     * Fires this transition. The entry state is activated, and is passed this
     * transition's list of actions to be fired.
     *
     * @throws IllegalStateException if the exit state is not active
     */
    public void fire() {
        entry.notifyTransitionFired(exit, getActions());
    }

    /**
     * Returns true if the exit State is active.
     *
     * @return true, if the exit State is active
     */
    public boolean canFire() {
        return exit.isActive();
    }

    /**
     * Returns the StateMachine that owns this Transition
     *
     * @return A StateMachine
     */
    public StateMachine getStateMachine() {
        return machine;
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        return getName();
    }

    /**
     * DOCUMENT ME!
     *
     * @param state DOCUMENT ME!
     */
    public void addRankedState(State state) {
        rankedStates.add(state);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Collection getRankedStates() {
        return Collections.unmodifiableCollection(rankedStates);
    }
}
