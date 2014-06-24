
package com.commercehub.core.state;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * A state in which at most one child state may be active.
 *
 * <p>In addition to the {@link State#isDeepHistory() deep history} mechanism
 * provided by {@link State all states}, an exclusive state supports {@link
 * #isHistory() shallow history}, or simply "history". When a history state is
 * activated, it activates its most recently active child state, but does not
 * propagate that activation through all descendants.</p>
 *
 * <p>The "history" and "deep history" properties of a state are controlled
 * independently; setting one does not clear the other. It is not superfluous to
 * set a state to observe history when an ancestor is already enforcing deep
 * history, as any state may be directly activated.</p>
 *
 * <p>An exclusive state may have a {@link #getDefaultChild() default child
 * state}, which the state activates when it is made active in the absence of
 * any history.</p>
 *
 * @author Orr Bernstein, <a href="mailto:jpulley@commercehub.com">J. Pulley</a>
 * @version 1.0
 * @version 1.1 6-Aug-2005
 */
public class ExclusiveState extends State {
    /**
     * Child state to activate when this state is activated, if no history
     * exists
     */
    private State defaultChild;

    /** Whether this state is a history state */
    private boolean history;

    /**
     * If this state has been previously active, the history child is the
     * previously active child. This value is maintained even if the state is
     * not a history state, since some ancestor may require deep history.
     */
    private State historyChild;

    /**
     * Initializes a new exclusive state object.
     *
     * @param name name of this state
     * @param history whether this is a history state
     * @param deepHistory whether this is a deepHistory state
     *
     * @throws IllegalArgumentException if the name is invalid (see {@link
     *                                  State#setName(String)}
     */
    public ExclusiveState(String name, boolean history,
                          boolean deepHistory) {
        super(name, deepHistory);
        this.history = history;
    }

    /**
     * Set the default child of this state. An exception is thrown if the
     * argument is not either already a child of this state or <code>
     * null</code>. Returns the previous default child state.
     *
     * @param child a child of this state
     *
     * @return previous default child state
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public State setDefaultChild(State child) {
        if (child != null && !getChildren().contains(child)) {
            String message =
                "Default child must actually be a child state, or null.";
            throw new IllegalArgumentException(message);
        }
        State oldValue = defaultChild;
        defaultChild = child;
        return oldValue;
    }

    /**
     * Returns the active child of this exclusive State or null if none is
     * active. This result is not cached.
     *
     * @return the Active Child state
     */
    public State getActiveChild() {
        State activeChild = null;
        Iterator it = getChildren().iterator();
        while (it.hasNext()) {
            State nextChild = (State) it.next();
            if (nextChild.isActive()) {
                activeChild = nextChild;
                break;
            }
        }
        return activeChild;
    }

    /**
     * Default child of this state. In the absence of history, the default child
     * is made active when this state is activated. It is not required that a
     * default child be defined, in which case this operation will return <code>
     * null</code>.
     *
     * @return default child state
     */
    public State getDefaultChild() {
        return defaultChild;
    }

    /**
     * Whether this is a history state. When activated, a history state
     * activates the child state that was active when the state was last active.
     *
     * @return whether this is a history state
     */
    public boolean isHistory() {
        return history;
    }

    /**
     * The active child state of this state, at the time this state was last
     * active. This value is available without regard for whether this is a
     * {@link #isHistory() history} state. If this state has never been active,
     * this operation will return <code>null</code>.
     *
     * @return last active child of this state
     */
    public State getHistoryChild() {
        return historyChild;
    }

    /**
     * Control whether this is a history state. Returns whether this was a
     * history state prior to invocation of this method.
     *
     * @param yn whether this is a history state
     *
     * @return previous value
     */
    public boolean setHistory(boolean yn) {
        boolean oldValue = history;
        history = yn;
        return oldValue;
    }

    /**
     * This operation is invoked on the Nearest Active Ancestor (NAA) of the
     * entry state of a transition. It directs the NAA to perform any necessary
     * deactivations.
     */
    protected void naaDeactivateAsNeeded() {
        State activeChild = getActiveChild();
        if (activeChild != null) {
            activeChild.deactivateSelf();
        }
    }

    /**
     * Deactivate this state. Deactivates its active child, then performs any
     * exit actions, and finally marks itself inactive.
     */
    protected void deactivateSelf() {
        State activeChild = getActiveChild();
        if (activeChild != null) {
            activeChild.deactivateSelf();
        }
        doActions(getExitActions());
        setActive(false);
    }

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
    protected void naaActivatePath(LinkedList path) {
        if (!path.isEmpty()) {
            //copy the path, so it may be consumed.
            LinkedList tempPath = new LinkedList(path);

            historyChild = (State) tempPath.removeFirst();
            historyChild.activateSelf(tempPath);
        }
    }

    /**
     * Activates this state and the specified path of descendants. <em>This
     * operation consumes the head of the <code>path</code> argument.</em>
     *
     * @param path path of descendants to activate
     */
    protected void activateSelf(LinkedList path) {
        if (!isActive()) {
        	setActive(true);
            doActions(getEntryActions());
        }

        if (path.size() == 0) {
            if ((history || isDeepHistory()) && historyChild != null) {
                historyChild.activateSelf(isDeepHistory());
            } else if (defaultChild != null) {
                historyChild = defaultChild;
                defaultChild.activateSelf(false);
            }
        } else {
            historyChild = (State) path.removeFirst();
            historyChild.activateSelf(path);
        }
    }

    /**
     * Activates this state and its default or history child as appropriate.
     *
     * @param observeDeepHistory Informs children to observe deep history
     */
    protected void activateSelf(boolean observeDeepHistory) {
        if (!isActive()) {
            doActions(getEntryActions());
            setActive(true);
        }
        observeDeepHistory |= isDeepHistory();

        if ((history || observeDeepHistory) && historyChild != null) {
            historyChild.activateSelf(observeDeepHistory);
        } else if (defaultChild != null) {
            historyChild = defaultChild;
            defaultChild.activateSelf(false);
        }
    }

    /**
     * Resets the activation of this state and its children. Exclusive states
     * need to reset historyChild as well.
     */
    protected void reset() {
        this.historyChild = null;
        super.reset();
    }

    /**
     * Initializes the activation of this state and its children from the values
     * stores in a loaded StateCookie. Exclusive states need to set historyChild
     * as well.
     *
     * @param cookie A StateCookie
     */
    protected void initFromCookie(StateCookie cookie) {
        if (!isActive()) { //enforces exclusivity between activation and history
            this.historyChild = cookie.getHistoryChild(this);
        }

        super.initFromCookie(cookie);
    }

    /**
     * Fills a cookie with the activation of this state and its children.
     * Exclusive states need to fill in their historyChild as well.
     *
     * @param cookie A StateCookie
     */
    protected void fillCookie(StateCookie cookie) {
        if (!isActive() && historyChild != null) {
            cookie.setHistoryChild(historyChild);
        }
        super.fillCookie(cookie);
    }

    /**
     * Fills a pair of Sets with the active path names of this state and its
     * children. Exclusive states need to fill in the history Set as well.
     *
     * @param activeSet a Set of active states
     * @param historySet a Set of history states
     */
    protected void fillStateSets(Set activeSet, Set historySet) {
        if (!isActive() && historyChild != null) {
            historySet.add(historyChild.getPathName());
        }
        super.fillStateSets(activeSet, historySet);
    }
}
