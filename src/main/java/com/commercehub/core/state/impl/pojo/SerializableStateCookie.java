package com.commercehub.core.state.impl.pojo;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.commercehub.core.state.State;
import com.commercehub.core.state.StateCookie;

/**
 * A state cookie using state sets, reducable to a state string in the model:
 * state.substate.concurrent-substate[onelane.active-state,nextlane.active-state]
 * 
 * @author m3
 *
 */
public class SerializableStateCookie implements StateCookie, Serializable{
	private static final long serialVersionUID = -7432399227832996727L;

    private Set<String> activeCookies; 
    private Set<String> historyCookies;

    public SerializableStateCookie() {
        activeCookies = new HashSet<String>();
        historyCookies = new HashSet<String>();
    }

    public boolean isActive(State state) {
        if (state == null) {
            return false;
        }
        return isActive(state.getPathName());
    }

    public boolean isActive(String stateName) {
        if (stateName == null) {
            return false;
        }
        return activeCookies.contains(stateName);
    }

    public void setActive(State state) {
        if (state == null) {
            return;
        }
        setActive(state.getPathName());
    }

    public void setActive(String stateName) {
        if (!activeCookies.contains(stateName)) {
            activeCookies.add(stateName);
        }
    }

    public void setActive(Set<String> statePathNames) {
        activeCookies = statePathNames;
    }

    /**
     * @see com.commercehub.core.state.StateCookie#toString()
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("");
        sb.append("Active States:");
        sb.append(this.activeCookies);

        sb.append("Historic States:");
        sb.append(this.historyCookies);

        return sb.toString();
    }

    /**
     * @see com.commercehub.core.state.StateCookie#clear()
     */
    public void clear() {
        activeCookies.clear();
        historyCookies.clear();
    }

    /**
     * @see com.commercehub.core.state.StateCookie#getHistoryChild(State)
     */
    public State getHistoryChild(State parentState) {
        for (State child: parentState.getChildren()) {
            if (historyCookies.contains(child.getPathName())) {
                return child;
            }
        }
        return null;
    }

    /**
     * @see com.commercehub.core.state.StateCookie#setHistoryChild(State)
     */
    public void setHistoryChild(State childState) {
        historyCookies.add(childState.getPathName());
    }

    /**
     * Sets the history states in this cookie to <code>states</code>. Any
     * previous history states are replaced. The client's history cookie ID is
     * updated.
     *
     * @param states set of fully qualified state path names
     */
    public void setHistoryStates(Set<String> states) {
        historyCookies = states;
    }

    public boolean isNew() {
        return activeCookies.isEmpty();
    }

    /**
     * An unmodifiable set of the pathnames of the history states represented by
     * this cookie.
     *
     * <p>This operation may not return <code>null</code>, although it may
     * return an empty set.
     *
     * @return history state pathnames
     */
    public Set<String> getHistoryStatePathNames() {
        return Collections.unmodifiableSet(historyCookies);
    }

    /**
     * An unmodifiable set of the pathnames of the active states represented by
     * this cookie.
     *
     * <p>This operation may not return <code>null</code>, although it may
     * return an empty set.
     *
     * @return active state pathnames
     */
    public Set<String> getActiveStatePathNames() {
        return Collections.unmodifiableSet(activeCookies);
    }

}
