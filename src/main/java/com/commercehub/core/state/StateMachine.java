
package com.commercehub.core.state;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;


/**
 * A StateMachine handles operations on a hierarchical StateMachine for a given
 * Stateful object. It is connected to a {@link Stateful} via {@link
 * #attachStateful(Stateful, StateCookie)}, can fire @li s via {@link
 * #pullTrigger(Trigger, Object, TransitionRecordFactory) and can  take a
 * snapshot of State activation and history via {@link  #getStateCookie()}.
 *
 * <p>StateMachines are generated and "wired" to Actions and Triggers by a
 * {@link StateMachineFactory}. They can be moderately expensive to create and
 * implementations of the Factory should pool machines. Objects implementing the
 * Stateful interface that are being removed from active duty should call {@link
 * #detachStateful()} which informs a StateMachine that it can return itself to
 * the pool.</p>
 *
 * <p>Convenience methods {@link #findByName(String)} and {@link
 * #getActiveStateString()} exist to return a String representing the current
 * internal state of this machine. These methods are not robust and do not take
 * into account history. Thus, they should only be used for informational or
 * logging purposes, and not used to manage or examine state. Use a StateCookie
 * for that.</p>
 *
 * @author Orr Bernstein, Matthew Mark Miller
 */
public class StateMachine {
    /** The rank given to parameter that is inapplicable in the current state */
    public static final int RANK_INAPPLICABLE_PARAMETER = Integer.MAX_VALUE;

    public static final int RANK_APPLICABLE_BUT_UNRANKED =
            Integer.MAX_VALUE - 1;
    private State rootState;
    private TriggerTransitionMap triggerTransitionMap;
    private Map<String, ? extends List<String>> possiblePropertyValueMap;
    private List<PropertyValuePair> defaultPropertyValues;
    private String description;

    private Stateful stateful;

    /**
     * Initializes a new StateMachine object. But you'll probably want to get
     * one from a StateMachineFactory instead, so that it will be properly
     * constructed and wired with Triggers, Transitions, Property Maps and
     * Actions.
     *
     * @param rootState the root State of this StateMachine.
     * @param description a description of the business process modelled in this
     *                    StateMachine
     * @param possiblePropertyValueMap
     * @param defaultPropertyValues
     */
    public StateMachine(State rootState, String description,
            Map<String, ? extends List<String>> possiblePropertyValueMap,
            List<PropertyValuePair> defaultPropertyValues) {
        this.rootState = rootState;
        this.description = description;
        this.possiblePropertyValueMap = possiblePropertyValueMap;
        this.defaultPropertyValues = defaultPropertyValues;
        rootState.setStateMachine(this);
    }

    /**
     * Sets a TriggerTransitionMap, which will be used to validate legal
     * Transitions when a Trigger is pulled on this StateMachine's Stateful.
     *
     * @param triggerTransitionMap
     *
     * @throws IllegalArgumentException
     */
    public void setTriggerTransitionMap(TriggerTransitionMap triggerTransitionMap) {
        if (triggerTransitionMap == null) {
            throw new IllegalArgumentException(
                    "Invalid argument to StateMachine - "
                            + System.getProperty("line.separator")
                            + "TriggerTransitionMap should be non-null.");
        }
        this.triggerTransitionMap = triggerTransitionMap;
    }

    /**
     * Returns a TriggerTransitionMap object, with functionality for
     * investigating the structure of the transitions in a StateMachine
     *
     * @return DOCUMENT ME!
     */
    public TriggerTransitionMap getTriggerTransitionMap() {
        return triggerTransitionMap;
    }

    /**
     * Returns the name of this StateMachine (also the name of its root state)
     *
     * @return a String
     */
    public String getName() {
        return rootState.getName();
    }

    /**
     * Returns a description of the business process modelled in this
     * StateMachine
     *
     * @return a String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Finds a State object given its dot separated path name.
     *
     * <p><b>This method should only be used for informational purposes! You
     * should never be checking activity, setting activity, or any other
     * management activity directly on a state.</b></p>
     *
     * @param dottedName Dotted path name of a State
     *
     * @return the State referenced by dottedName, or null if it can't be found
     */
    public State findByName(String dottedName) {
        StringTokenizer st =
                new StringTokenizer(dottedName,
                        String.valueOf(State.STATE_PATH_CHAR));

        State cursorState = rootState;
        while (st.hasMoreTokens()) {
            String currentToken = st.nextToken();
            boolean found = false;
            Iterator i = cursorState.getChildren().iterator();
            while (!found && i.hasNext()) {
                State childState = (State) i.next();
                if (childState.getName().equals(currentToken)) {
                    cursorState = childState;
                    found = true;
                }
            }
            if (!found) {
                return null;
            }
        }
        return cursorState;
    }

    /**
     * Returns the activation of a given State.
     *
     * @param stateName Path name of a State
     *
     * @return <code>true</code> if the State is active <code>false</code> if
     *         the State wasn't active, or wasn't found.
     */
    public boolean isInState(String stateName) {
        boolean inState = false;
        State testState = findByName(stateName);
        if (testState != null) {
            inState = testState.isActive();
        }
        return inState;
    }


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
     *              transitions (e.g. a Hub3 Action Label)
     * @param factory a TransitionRecordFactory that will be used to record any
     *                transitions fired by this trigger. If null, no records
     *                will be kept.
     *
     * @return true if any transitions fired.
     */
    public boolean pullTrigger(Trigger trigger, Object param,
            TransitionRecordFactory factory) {
        boolean transitionFired = false;
        StateCookie cookie = stateful.getStateCookie();
        Class<? extends Trigger> triggerClass = trigger.getClass();
        Set transitions =
                triggerTransitionMap.getTransitions(triggerClass, param);

        Iterator transitionsIter = transitions.iterator();
        while (transitionsIter.hasNext()) {
            Transition transition = (Transition) transitionsIter.next();
            if (transition.canFire()) {
                Condition condition =
                        triggerTransitionMap.getCondition(triggerClass, transition,
                                param);

                Stateful cachedStateful = stateful; // Trigger might detach the stateful.
                boolean evalResult = trigger.eval(stateful, param, condition);
                stateful = cachedStateful;

                if (evalResult) {
                    transition.fire();
                    if (factory != null) {
                        factory.newTransitionRecord(transition, stateful);
                    }
                    transitionFired = true;
                }
            }
        }

        if (transitionFired) {
            fillCookieWithSets(cookie);
        }

        return transitionFired;
    }

    /**
     * Tests whether a trigger and parameter pair is mapped to a transition in
     * this state machine. This method doesn't test or mutate state.
     *
     * @param triggerClass the class of a trigger (cannot be null)
     * @param param additional qualifying parameter for mapping trigger to
     *              transitions
     *
     * @return true if a trigger pair is supported
     */
    public boolean isSupported(Class<? extends Trigger> triggerClass, Object param) {
        Set transitions =
                triggerTransitionMap.getTransitions(
                        triggerClass, param);

        return !transitions.isEmpty();
    }

    /**
     * Returns true if this parameter is viable -- that is, if it can ever be
     * used to fire a transition in any state reachable by this machine, given
     * its current state and only testing transitions caused by this
     * triggerClass
     *
     * @param triggerClass
     * @param param
     *
     * @return true, if this class and parameter would ever be viable in this
     *         machine.
     */
    public boolean isParameterViable(Class triggerClass, Object param) {
        return triggerTransitionMap.isParameterViable(triggerClass, param,
                getActiveStates());
    }

    /**
     * Creates and returns a consumable Set of all the States currently active
     * in this machine.
     *
     * @return
     */
    public Set<State> getActiveStates() {
        Set<State> activeStates = new HashSet<State>();
        walkActiveStates(rootState, activeStates);
        return activeStates;
    }

    /**
     * DOCUMENT ME!
     *
     * @param stateToWalk DOCUMENT ME!
     * @param actives DOCUMENT ME!
     */
    private void walkActiveStates(State stateToWalk, Set<State> actives) {
        if (stateToWalk.isActive()) {
            actives.add(stateToWalk);
        }
        Iterator iterator = stateToWalk.getChildren().iterator();
        while (iterator.hasNext()) {
            walkActiveStates((State) iterator.next(), actives);
        }
    }

    /**
     * A trigger/parameter pair is applicable if it is mapped to a transition in
     * this state machine and at least one of the exit states of these
     * transitions is active.
     *
     * @param triggerClass
     * @param param
     *
     * @return if a trigger pair is applicable
     */
    public boolean isApplicable(Class<? extends Trigger> triggerClass, Object param) {
        boolean anyApplicable = false;
        Set transitions =
                triggerTransitionMap.getTransitions(
                        triggerClass, param);

        Iterator iterator = transitions.iterator();
        while (!anyApplicable && iterator.hasNext()) {
            anyApplicable |= ((Transition) iterator.next()).canFire();
        }

        return anyApplicable;
    }

    /**
     * Returns the Stateful we are currently driving.
     *
     * @return a Stateful, or null if one has not been set.
     */
    Stateful getStateful() {
        return stateful;
    }

    /**
     * Clears this StateMachine and returns it to a pooled state.
     */
    public void detachStateful() {
        this.stateful = null;
    }

    /**
     * Attaches a StateMachine to a Stateful and sets its inital state. If this
     * StateMachine is already attached to a Stateful, the old Stateful and its
     * state will be abandoned, so be careful and only call this method on
     * StateMachines newly returned by a Factory.
     *
     * @param stateful an object implementing the Stateful interface.
     */
    public void attachStateful(Stateful stateful) {
        this.stateful = null; //ensures that no actions are executed.
        StateCookie cookie = stateful.getStateCookie();
        boolean initialState = cookie.isNew();
        if (initialState) {
            //create default state
            rootState.reset();
            rootState.activateSelf(false);
            fillCookieWithSets(cookie);
        } else {
            rootState.initFromCookie(cookie);
        }

        this.stateful = stateful;


        if (initialState) {
            setDefaultProperties();
        }
    }

    /**
     * Fills the state cookie we were passed during {@link
     * #attachStateful(Stateful, StateCookie)} with a current snapshot of State
     * activation and history. This method creates Sets of state pathnames and
     * creates the cookie using these.
     *
     * @param cookie DOCUMENT ME!
     */
    private void fillCookieWithSets(StateCookie cookie) {
        HashSet<String> activeStates = new HashSet<String>();
        HashSet<String> historyStates = new HashSet<String>();

        rootState.fillStateSets(activeStates, historyStates);
        cookie.setActive(activeStates);
        cookie.setHistoryStates(historyStates);
    }

    /**
     * Returns the root state, for use within this package.
     *
     * @return a State
     */
    public State getRootState() {
        return rootState;
    }

    /**
     * Sets up the initial default properties, if any, for a currently connected
     * Stateful using a SetPropertyStateAction.
     */
    private void setDefaultProperties() {
        SetPropertyStateAction action = new SetPropertyStateAction();
        Iterator iterator = defaultPropertyValues.iterator();
        while (iterator.hasNext()) {
            action.execute(getStateful(), iterator.next());
        }
    }

    /**
     * Returns the possible values for a Property on this stateful, as set in a
     * statemachine.xml file. If a requested property is not set in the
     * statemachine definition, this method will return null.
     *
     * @param propertyName A property name.
     *
     * @return a List of Strings representing possible values.
     */
    public List<String> getPossibleValues(String propertyName) {
        List<String> possible = null;
        if (possiblePropertyValueMap != null
                && possiblePropertyValueMap.containsKey(propertyName)) {
            possible =
                    Collections.unmodifiableList((List<String>) possiblePropertyValueMap
                            .get(propertyName));
        }
        return possible;
    }

    /**
     * Returns a set of parameters that can cause a transition in this machine
     * for a given trigger class. This set will be empty if the trigger class is
     * not mapped.
     *
     * @param triggerClass a valid subclass of Trigger
     *
     * @return a Set of Object Parameters
     */
    public Set getSupportedParameters(Class<? extends Trigger> triggerClass) {
        Set transition = triggerTransitionMap.getParameters(triggerClass);

        return transition;
    }

    /**
     * Returns a set of parameters that can cause a transition from the current
     * state for a given trigger.</p>
     *
     * This set will be empty if no parameters will cause a transition from this
     * state.
     *
     * @param triggerClass a valid subclass of Trigger
     *
     * @return a Set of Object parameters
     */
    public Set getApplicableParameters(Class<? extends Trigger> triggerClass) {
        Set supported = getSupportedParameters(triggerClass);

        Iterator iterator = supported.iterator();
        while (iterator.hasNext()) {
            if (!isApplicable(triggerClass, iterator.next())) {
                iterator.remove();
            }
        }

        return supported;
    }


    /**
     * Returns the rank of an applicable trigger in the trigger transition map.
     * </p>
     *
     * Rank is determined thusly: if a trigger is not applicable in the current
     * state, it is assigned the value INAPPLICABALE_PARAMETER_RANK.</p>
     *
     * if a trigger is applicable in the current state BUT no active states were
     * mapped to the resulting transition in a state-rank element, the value
     * RANK_APPLICABLE_BUT_UNRANKED is assigned.</p>
     *
     * Finally, if transition was ranked according to states, then the 0-based
     * position of the first active and mapped rank is returned.</p>
     *
     * @param triggerClass class of a trigger, should not be null
     * @param param optional parameter of a trigger, may be null
     *
     * @return an integer value
     */
    public int getRank(Class<? extends Trigger> triggerClass, Object param) {
        int rank = RANK_INAPPLICABLE_PARAMETER;
        Set allTransitions =
                triggerTransitionMap.getTransitions(triggerClass, param);
        Iterator transIter = allTransitions.iterator();
        while (transIter.hasNext()) {
            Transition t = (Transition) transIter.next();
            if (t.canFire()) {
                rank = Math.min(rank, RANK_APPLICABLE_BUT_UNRANKED);
                Iterator stateIter = t.getRankedStates().iterator();
                for (int i = 0; stateIter.hasNext(); i++) {
                    State checkState = (State) stateIter.next();
                    if (checkState.isActive()) {
                        rank = Math.min(rank, i);
                        break;
                    }
                }
            }
        }
        return rank;
    }
    
    /**
     * Returns a String indicating all active states in this machine
     *
     * <p></b> Only use this method for information and logging purposes! Do not
     * rely on it for management of states!</b></p>
     *
     * @return a String, never null
     */
    public String getActiveStateString() {
        return iterateAndConcatenate(rootState.getChildren(), false);
    }

    /**
     * Returns a String indicating ALL states in this machine, with no
     * indication of activation or history.
     *
     * <p><b>Only use this method for information and logging purposes! Do not
     * rely on it for management of states!</b></p>
     *
     * @return a String, never null
     */
    public String getAllStateStrings() {
        return iterateAndConcatenate(rootState.getChildren(), true);
    }

    /**
     * Iterates through a set of Children, building a string representation of
     * their names and their childrens' names.
     *
     * @param children a Set of States
     * @param includeInactiveStates if True, will return a list of ALL states
     *                              with no indication of activation. if False,
     *                              will return a list of only the Active states
     *
     * @return A String, never null
     */
    private String iterateAndConcatenate(Set children,
            boolean includeInactiveStates) {
        StringBuffer sb = new StringBuffer();

        Iterator i = children.iterator();
        while (i.hasNext()) {
            State child = (State) i.next();
            if (!child.isActive() && !includeInactiveStates) {
                continue;
            }
            sb.append(child.getName());
            Set grandChildren = child.getChildren();

            if (grandChildren.size() > 0) {
                boolean moreThanOne =
                        (child instanceof ConcurrentState
                                || includeInactiveStates)
                                && grandChildren.size() > 1;
                                String childrenString =
                                        iterateAndConcatenate(grandChildren, includeInactiveStates);

                                if (childrenString.length() > 0) {
                                    if (!moreThanOne) {
                                        sb.append(State.STATE_PATH_CHAR);
                                        sb.append(childrenString);
                                    } else {
                                        sb.append(State.STATE_CHILD_OPEN);
                                        sb.append(childrenString);
                                        sb.append(State.STATE_CHILD_CLOSE);
                                    }
                                }
            }
            if (i.hasNext()
                    && (child.getParent() instanceof ConcurrentState
                            || includeInactiveStates)) {
                sb.append(State.STATE_CHILD_SEPARATOR);
            }
        }

        return sb.toString();
    }
} // end class
