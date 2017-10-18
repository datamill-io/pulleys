
package pulleys;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * A state in which all child states are active when the state is active.
 * Concurrent states act as containers for independent sub-state machines.
 *
 * @author Orr Bernstein, <a href="mailto:jpulley@commercehub.com">J. Pulley</a>
 * @version 1.0
 * @version 1.1 6-Aug-2005
 */
public class ConcurrentState extends State {
    /**
     * Initializes a new concurrent state object.
     *
     * @param name name of this state
     * @param deepHistory whether this is a deepHistory state
     *
     * @throws IllegalArgumentException if the name is invalid (see {@link
     *                                  State#setName(String)}
     */
    public ConcurrentState(String name, boolean deepHistory) {
        super(name, deepHistory);
    }

    /**
     * This operation is invoked on the Nearest Active Ancestor (NAA) of the
     * entry state of a transition. It directs the NAA to perform any necessary
     * deactivations.
     *
     * <p>Concurrent states do nothing with this notification. The NCA remains
     * active throughout the transition, and since all children of a concurrent
     * state are active when the state itself is active, no deactivations are
     * performed.</p>
     */
    protected void naaDeactivateAsNeeded() {
    }

    /**
     * Deactivate this state. Deactivates all children, than executes any exit
     * actions, and finally marks itself inactive.
     */
    protected void deactivateSelf() {
        Iterator it = getChildren().iterator();
        while (it.hasNext()) {
            State nextChild = (State) it.next();
            nextChild.deactivateSelf();
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
        //copy the path, so it may be consumed.
        LinkedList tempPath = new LinkedList(path);

        State pathHead = (State) tempPath.removeFirst();
        pathHead.activateSelf(tempPath);
    }

    /**
     * Activate this state and the specified path of descendants. Activates
     * children as appropriate. <em>This operation consumes the head of the
     * <code>path</code> argument.</em>
     *
     * @param path path of descendants to activate
     */
    protected void activateSelf(LinkedList path) {
        if (!isActive()) {
            doActions(getEntryActions());
            setActive(true);
        }

        if (path.size() == 0) {
            activateChildren(null, isDeepHistory());
        } else {
            State pathHead = (State) path.removeFirst();
            activateChildren(pathHead, false);
            pathHead.activateSelf(path);
        }
    }

    /**
     * Activates this state. Activates children as appropriate.
     *
     * @param observeDeepHistory Informs all children to observe deep history.
     */
    protected void activateSelf(boolean observeDeepHistory) {
        if (!isActive()) {
            doActions(getEntryActions());
            setActive(true);
        }
        activateChildren(null, isDeepHistory() || observeDeepHistory);
    }

    /**
     * Loops through child states, activating children that aren't along the
     * entry branch of a transition
     *
     * @param toSkip a State representing the next leaf in a transition entry
     *               branch
     * @param observeDeepHistory Informs all children to observe deep history.
     */
    private void activateChildren(State toSkip, boolean observeDeepHistory) {
        Iterator it = getChildren().iterator();
        while (it.hasNext()) {
            State nextChild = (State) it.next();
            if (nextChild != toSkip) {
                nextChild.activateSelf(observeDeepHistory);
            }
        }
    }
}
