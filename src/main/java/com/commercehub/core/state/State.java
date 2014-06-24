
package com.commercehub.core.state;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A representation of a legitimate set of logical attribute values of a
 * business object.
 *
 * <p>Business messages often have <em>logical</em> attributes that may take one
 * of an enumerated set of distinct values as the messages evolve through their
 * life cycle in business processes. Further, there are often business
 * constraints that require these attributes to take values only in coordination
 * with one another, so we explicitly enumerate the legitimate sets of these
 * attributes values. The sets in this enumeration are the legal <em>states</em>
 * of the message in the business process.</p>
 *
 * <p>Note the emphasis on <em>logical</em> attributes. Usually, these
 * attributes are not implemented as member fields of the stateful object.
 * Rather, a single state is used to represent a meaningful combination of
 * logical attribute values. For instance, a purchase order may be delivered to
 * a vendor, accepted by the vendor, and shipped by the vendor. Rather than give
 * the purchase order object the boolean fields "delivered", "accepted", and
 * "shipped", we recognize a single state <em>open-shipped</em>. If the purchase
 * order is in that state, we infer that it has been delivered, accepted, and
 * shipped.</p>
 *
 * <p>A state may have any number of {@link #getChildren() child states}, so
 * that states are arranged in hierarchies of increasingly finer meaning. An
 * "open" purchase order may have been shipped or merely accepted (and not yet
 * shipped) by the vendor. To model this situation, we would construct an "open"
 * state with two child states, "accepted"and "shipped".</p>
 *
 * <p>A state has a {@link #getPathName() path name} that reflects its ancestry.
 * In the example above, the complete list of states is "open", "open.accepted",
 * and "open.shipped".</p>
 *
 * <p>A state is either {@link #isActive() active or inactive}. When a state is
 * active, it may be inferred that the relevant logical attributes of the
 * described business object have the values implied by that state. A state may
 * be active only if its parent is active.</p>
 *
 * <p>A state may enforce {@link #isDeepHistory() deep history}. When a deep
 * history state is made active, all its descendants which were active when the
 * state was last active are reactivated.</p>
 *
 * <p>A state may influence the object it describes, and through that object it
 * may influence the larger system. States may be assigned {@link StateAction
 * actions} to execute when the state is activated {@link #getEntryActions()
 * (entry actions)} or deactivated {@link #getExitActions() (exit actions)}.
 * Entry and exit actions are maintained in ordered lists, to give a predictable
 * order of execution.</p>
 *
 * <p>Entry actions are <em>not</em> executed when a transition enters an
 * already active state; they are only executed when a state changes from
 * inactive to active. Similarly, exit actions are not executed just because an
 * exiting transition was fired; the are only executed when a state changes from
 * active to inactive. These state machines are not a general-purpose
 * programming tool.</p>
 *
 * @author Orr Bernstein, <a href="mailto:jpulley@commercehub.com">J. Pulley</a>
 * @version 1.0
 * @version 1.1 6-Aug-2005
 */
public abstract class State {
    /**
     * Separator character for elements in a state path name. Symbolic constant
     * for the '.' character.
     */
    public static final char STATE_PATH_CHAR = '.';

    /**
     * Open character for concurrent elements in a state path. Symbolic constant
     * for the '[' character.
     */
    public static final char STATE_CHILD_OPEN = '[';

    /**
     * Close character for concurrent elements in a state path. Symbolic
     * constant for the ']' character.
     */
    public static final char STATE_CHILD_CLOSE = ']';

    /**
     * Separator character for concurrent elements in a state path. Symbolic
     * constant for the ',' character.
     */
    public static final char STATE_CHILD_SEPARATOR = ',';

    /** StateMachine object owning this state */
    protected StateMachine stateMachine;

    /** Name of this state */
    private String name;

    /** This state's parent */
    private State parent;

    /** This state's children. The elements are of type <code>State</code>. */
    private Set<State> children;

    /** Used to store the path name of this state * */
    private String pathName;

    /** Used to store this state's path * */
    private LinkedList<State> path;

    /** Whether this state is currently active */
    private boolean active;

    /** Whether this state is a deep history state */
    private boolean deepHistory;

    /**
     * This state's entry actions (run immediately after state activation). The
     * elements are of type {@link ParametricAction}.
     */
    private List<ParametricAction> entryActions;

    /**
     * This state's exit actions (run immediately before state deactivation).
     * The elements are of type {@link ParametricAction}.
     */
    private List<ParametricAction> exitActions;

    /**
     * Initializes a new state object.
     *
     * @param name name of this state
     * @param deepHistory whether this is a deepHistory state
     *
     * @throws IllegalArgumentException if the name is invalid (see {@link
     *                                  #setName(String)}
     */
    protected State(String name, boolean deepHistory) {
        if (!isLegalStateName(name)) {
            String message =
                "State names must be nonzero length and may "
                + "not contain whitespace or the '.' character.";
            throw new IllegalArgumentException(message);
        }
        this.name = name;
        this.deepHistory = deepHistory;
        children = new LinkedHashSet<State>();
        entryActions = new LinkedList<ParametricAction>();
        exitActions = new LinkedList<ParametricAction>();
    }

    /**
     * Tests the legality of a State name.
     *
     * @param toTest A string representing a proposed state name
     *
     * @return true if the state name contains no illegal or control characters
     */
    private boolean isLegalStateName(String toTest) {
        boolean legal = true;
        if (toTest == null) {
            legal = false;
        } else if (toTest.length() == 0) {
            legal = false;
        } else if (toTest.indexOf('.') != -1) {
            legal = false;
        } else if (toTest.indexOf(' ') != -1) {
            legal = false;
        } else if (toTest.indexOf('[') != -1) {
            legal = false;
        } else if (toTest.indexOf(']') != -1) {
            legal = false;
        } else if (toTest.indexOf('\t') != -1) {
            legal = false;
        } else if (toTest.indexOf('\n') != -1) {
            legal = false;
        }
        return legal;
    }

    /**
     * An {@link Collections#unmodifiableSet(Set) unmodifiable set} of this
     * state's child states.
     *
     * @return child states
     */
    public Set<State> getChildren() {
        return Collections.unmodifiableSet(children);
    }

    /**
     * Make <code>newChild</code> a child of this state. Returns <code>
     * false</code> if <code>newChild</code> was already a child of this state.
     *
     * @param newChild state to add as a child
     *
     * @return whether the set of children was actually altered as a result of
     *         this operation
     *
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public boolean addChild(State newChild) {
        if (newChild == null) {
            throw new NullPointerException("child may not be null");
        }
        boolean added = children.add(newChild);
        if (added) {
            newChild.setParent(this);
            newChild.setStateMachine(this.stateMachine);
        }
        return added;
    }

    /**
     * Removes <code>toRemove</code> from this state's children. Sets the parent
     * state of <code>toRemove</code> to <code>null</code>.
     *
     * @param toRemove child state to remove
     */
    public void removeChild(State toRemove) {
        if (toRemove != null) {
            toRemove.setParent(null);
        }
        children.remove(toRemove);
    }

    /**
     * An {@link Collections#unmodifiableList(List) unmodifiable List} of this
     * state's entry actions. A state executes its entry actions immediately
     * after it is activated. The actions are executed in the order they appear
     * in the list.
     *
     * @return entry ParametricActions
     */
    public List<ParametricAction> getEntryActions() {
        return Collections.unmodifiableList(entryActions);
    }

    /**
     * Add <code>newAction</code> to the list of entry actions. The same action
     * may appear more than once in the list of entry actions.
     *
     * @param newAction action to add
     * @param param a parameter that will be passed to the action on execution
     *
     * @throws NullPointerException if <code>newAction</code> is null
     */
    public void addEntryAction(StateAction newAction, Object param) {
        if (newAction == null) {
            throw new NullPointerException("action may not be null");
        }
        entryActions.add(new ParametricAction(newAction, param));
    }

    /**
     * Add <code>newAction</code> to the list of entry actions, at the specified
     * <code>index</code>. The same action may appear more than once in the list
     * of entry actions.
     *
     * @param index zero-based location in the list of entry actions
     * @param newAction action to add
     * @param param a parameter that will be passed to the action on execution
     *
     * @throws IndexOutOfBoundsException if <code>index</code> is less than zero
     *                                   or greater than the current number of
     *                                   entry actions
     * @throws NullPointerException if <code>newAction</code> is null
     */
    public void addEntryAction(int index, StateAction newAction, Object param) {
        if (newAction == null) {
            throw new NullPointerException("action may not be null");
        }
        entryActions.add(index, new ParametricAction(newAction, param));
    }

    /**
     * Replace the entry action at <code>index</code> with <code>
     * newAction</code>. Returns the action previously at <code>index</code>.
     *
     * @param index zero-based location in the list of entry actions
     * @param newAction action to add
     * @param param a parameter that will be passed to the action on execution
     *
     * @return ParametricAction previously at <code>index</code>
     *
     * @throws IndexOutOfBoundsException if <code>index</code> is less than zero
     *                                   or greater than or equal to the current
     *                                   number of entry actions
     * @throws NullPointerException if <code>newAction</code> is null
     */
    public ParametricAction setEntryAction(int index, StateAction newAction,
                                           Object param) {
        if (newAction == null) {
            throw new NullPointerException("action may not be null");
        }
        return entryActions.set(index,
                new ParametricAction(newAction, param));
    }

    /**
     * Removes the action at <code>index</code> from the list of entry actions.
     * The removed action is returned.
     *
     * @param index zero-based location in the list of entry actions
     *
     * @return action previously at <code>index</code>
     *
     * @throws IndexOutOfBoundsException if <code>index</code> is less than zero
     *                                   or greater than or equal to the current
     *                                   number of entry actions
     */
    public ParametricAction removeEntryAction(int index) {
        return entryActions.remove(index);
    }

    /**
     * Removes <code>action</code> from the list of entry actions. Unlike {@link
     * List#remove(Object)}, this operation removes all occurances from the
     * list. Returns whether the list was actually modified as a result of this
     * operation, i.e. whether <code>action</code> was originally in the list.
     *
     * @param action action to remove
     *
     * @return whether <code>action</code> was originally in the list of entry
     *         actions
     */
    public boolean removeEntryAction(ParametricAction action) {
        boolean removed = false;
        boolean again = true;
        while (again) {
            again = entryActions.remove(action);
            removed |= again;
        }
        return removed;
    }

    /**
     * An {@link Collections#unmodifiableList(List) unmodifiable List} of this
     * state's exit actions. A state executes its exit actions immediately
     * before it deactivates. The actions are executed in the order they appear
     * in the list.
     *
     * @return exit actions
     */
    public List<ParametricAction> getExitActions() {
        return Collections.unmodifiableList(exitActions);
    }

    /**
     * Add <code>newAction</code> to the list of exit actions. The same action
     * may appear more than once in the list of exit actions.
     *
     * @param newAction action to add
     * @param param a parameter that will be passed to the action on execution
     *
     * @throws NullPointerException if <code>newAction</code> is null
     */
    public void addExitAction(StateAction newAction, Object param) {
        if (newAction == null) {
            throw new NullPointerException("action may not be null");
        }
        exitActions.add(new ParametricAction(newAction, param));
    }

    /**
     * Add <code>newAction</code> to the list of exit actions, at the specified
     * <code>index</code>. The same action may appear more than once in the list
     * of exit actions.
     *
     * @param index zero-based location in the list of exit actions
     * @param newAction action to add
     * @param param a parameter that will be passed to the action on execution
     *
     * @throws IndexOutOfBoundsException if <code>index</code> is less than zero
     *                                   or greater than the current number of
     *                                   exit actions
     * @throws NullPointerException if <code>newAction</code> is null
     */
    public void addExitAction(int index, StateAction newAction, Object param) {
        if (newAction == null) {
            throw new NullPointerException("action may not be null");
        }
        exitActions.add(index, new ParametricAction(newAction, param));
    }

    /**
     * Replace the exit action at <code>index</code> with <code>
     * newAction</code>. Returns the action previously at <code>index</code>.
     *
     * @param index zero-based location in the list of exit actions
     * @param newAction action to add
     * @param param a parameter that will be passed to the action on execution
     *
     * @return action previously at <code>index</code>
     *
     * @throws IndexOutOfBoundsException if <code>index</code> is less than zero
     *                                   or greater than or equal to the current
     *                                   number of exit actions
     * @throws NullPointerException if <code>newAction</code> is null
     */
    public ParametricAction setExitAction(int index, StateAction newAction,
                                          Object param) {
        if (newAction == null) {
            throw new NullPointerException("action may not be null");
        }
        return exitActions.set(index,
                new ParametricAction(newAction, param));
    }

    /**
     * Removes the action at <code>index</code> from the list of exit actions.
     * The removed action is returned.
     *
     * @param index zero-based location in the list of exit actions
     *
     * @return action previously at <code>index</code>
     *
     * @throws IndexOutOfBoundsException if <code>index</code> is less than zero
     *                                   or greater than or equal to the current
     *                                   number of exit actions
     */
    public ParametricAction removeExitAction(int index) {
        return exitActions.remove(index);
    }

    /**
     * Removes <code>action</code> from the list of exit actions. Unlike {@link
     * List#remove(Object)}, this operation removes all occurances from the
     * list. Returns whether the list was actually modified as a result of this
     * operation, i.e. whether <code>action</code> was originally in the list.
     *
     * @param action action to remove
     *
     * @return whether <code>action</code> was originally in the list of exit
     *         actions
     */
    public boolean removeExitAction(ParametricAction action) {
        boolean removed = false;
        boolean again = true;
        while (again) {
            again = exitActions.remove(action);
            removed |= again;
        }
        return removed;
    }

    /**
     * Whether this state is active.
     *
     * @return whether this state is active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Set the "active" flag. Simple mutator, for subclasses only.
     *
     * @param yn new "active" value
     */
    protected void setActive(boolean yn) {
        active = yn;
    }

    /**
     * Name of this state.
     *
     * @return name of this state
     */
    public String getName() {
        return name;
    }

    /**
     * A list containing this state and all its ancestors, in order beginning
     * with the root state. This method caches the path, and thus should not be
     * called until the state has been assigned to its parent.
     *
     * @return path of this state from the root state
     */
    public LinkedList<State> getPath() {
        path = new LinkedList<State>();
        path.add(this);
        State nextParent = parent;
        while (nextParent != null) {
            path.addFirst(nextParent);
            nextParent = nextParent.parent;
        }
        return path;
    }

    /**
     * Path name of this state. The path name consists of the name of this state
     * with the path name of its parent prepended. Elements in the path name are
     * separated by the {@link #STATE_PATH_CHAR state path character}. This
     * method caches the path name, and thus should not be called until the
     * state has been assigned to its parent.
     *
     * @return path name of this state
     */
    public String getPathName() {
        if (pathName == null) {
            StringBuffer bfr = new StringBuffer();
            State nextState = this;
            while (nextState != null && nextState.getParent() != null) { //excludes root state
                if (nextState != this) {
                    bfr.insert(0, STATE_PATH_CHAR);
                }
                bfr.insert(0, nextState.getName());
                nextState = nextState.getParent();
            }
            pathName = bfr.toString();
        }
        return pathName;
    }

    /**
     * Stateful object described by the state machine that owns this state.
     *
     * @return stateful object
     */
    public Stateful getStateful() {
        return stateMachine.getStateful();
    }

    /**
     * Parent state of this state.
     *
     * @return parent state
     */
    public State getParent() {
        return parent;
    }

    /**
     * Set the parent state of this state. This operation does not add this
     * state to the parent state's children. Use the public operation {@link
     * #addChild(State)}, which calls this method.
     *
     * @param newParent new parent state
     */
    void setParent(State newParent) {
        parent = newParent;
    }

    /**
     * Whether this is a deep history state. When activated, a deep history
     * state activates all of its descendants that were active when the state
     * was last active.
     *
     * @return whether this state is designated as a deep history state
     */
    public boolean isDeepHistory() {
        return deepHistory;
    }

    /**
     * Notify this state that it is the entry state of a transition that has
     * fired. All activations occur by {@link Transition#fire() firing
     * transitions}, which is the only way this method should be invoked. The
     * transition must provide its <code>exitState</code> and list of transition
     * actions.
     *
     * @param exitState exit state of the transition activating this state
     * @param transitionActions actions to be performed on behalf of the
     *                          activating transition
     */
    void notifyTransitionFired(State exitState, List<ParametricAction> transitionActions) {
        if (!isActive()) {
            LinkedList<State> entryPath = getPath();
            State naa = null;
            do {
                naa = entryPath.removeFirst();
            } while (entryPath.size() > 0
                    && entryPath.getFirst().isActive());

            naa.naaDeactivateAsNeeded();

            //transition actions
            doActions(transitionActions);

            naa.naaActivatePath(entryPath);
        } else {
            //the entry state is already active.  The transition
            //has already been applied.
            doActions(transitionActions);
        }
    }

    /**
     * This operation is invoked on the Nearest Active Ancestor (NAA) of the
     * entry state of a transition. It directs the NAA to perform any necessary
     * deactivations.
     */
    protected abstract void naaDeactivateAsNeeded();

    /**
     * Deactivate this state. Deactivates children as necessary.
     */
    protected abstract void deactivateSelf();

    /**
     * This operation is invoked on the nearest active ancestor (NAA) of the
     * entry state of a transition. It directs the NAA to activate a descendant
     * and perform any other necessary activations.
     *
     * <p>The path argument is assumed to be relative to this state, i.e. the
     * path begins with a child of this state.</p>
     *
     * @param path path of states to activate
     */
    protected abstract void naaActivatePath(LinkedList<State> path);

    /**
     * Activate this state and the specified path of descendants. Activates
     * children as appropriate. <em>This operation consumes the head of the
     * <code>path</code> argument.</em>
     *
     * @param path path of descendants to activate
     */
    protected abstract void activateSelf(LinkedList<State> path);

    /**
     * Activates this state. Activates children as appropriate.
     *
     * @param observeDeepHistory whether a deep history node has been
     *                           encountered
     */
    protected abstract void activateSelf(boolean observeDeepHistory);


    /**
     * Utility to execute a list of {@link StateAction actions}.
     *
     * @param actions actions to execute
     */
    protected void doActions(List<ParametricAction> actions) {
        Stateful stateful = getStateful();
        if (stateful != null) {
            Iterator it = actions.iterator();
            while (it.hasNext()) {
                ParametricAction action = (ParametricAction) it.next();
                action.execute(stateful);
            }
        }
    }

    /**
     * Sets the parent state machine of this State. Useful for retreiving the
     * stateful the Machine is operating on, and for calling {@link
     * StateMachine#doActions(List)}.
     *
     * @param stateMachine a StateMachine object
     */
    void setStateMachine(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    /**
     * Resets the activation of this state and its children.
     */
    protected void reset() {
        this.setActive(false);
        Iterator<State> iterator = getChildren().iterator();
        while (iterator.hasNext()) {
            iterator.next().reset();
        }
    }

    /**
     * Initializes the activation of this state and its children from the values
     * stores in a loaded StateCookie.
     *
     * @param cookie A StateCookie
     */
    protected void initFromCookie(StateCookie cookie) {
        this.setActive(cookie.isActive(this) || this.getParent() == null);
        Iterator<State> iterator = getChildren().iterator();
        while (iterator.hasNext()) {
            iterator.next().initFromCookie(cookie);
        }
    }

    /**
     * Fills a cookie with the activation of this state and its children.
     * Exclusive states need to fill in their historyChild as well.
     *
     * @param cookie A StateCookie
     */
    protected void fillCookie(StateCookie cookie) {
        if (this.isActive() && this.getParent() != null) { //excludes root state
            cookie.setActive(this);
        }
        Iterator<State> iterator = getChildren().iterator();
        while (iterator.hasNext()) {
            iterator.next().fillCookie(cookie);
        }
    }

    /**
     * Fills a pair of Sets with the active path names of this state and its
     * children.
     *
     * @param activeSet a Set of active states
     * @param historySet a Set of history states
     */
    protected void fillStateSets(Set<String> activeSet, Set<String> historySet) {
        if (this.isActive() && this.getParent() != null) { //excludes root state
            activeSet.add(this.getPathName());
        }
        Iterator<State> iterator = getChildren().iterator();
        while (iterator.hasNext()) {
            iterator.next().fillStateSets(activeSet, historySet);
        }
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        return this.getName();
    }
}
