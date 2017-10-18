
package pulleys;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Mapping of {@linkplain Trigger triggers and their parameters} to {@linkplain
 * Transition transitions}. Mappings for individual triggers are established by
 * associating the class of the trigger object and an optional parameter with a
 * transition.
 *
 * <p>When a trigger is "pulled" by passing the trigger object and an optional
 * parameter to a state machine, the state machine first evaluates the trigger.
 * If the evaluation returns <code>true</code>, the state machine checks here
 * for a mapping of the trigger's class and the parameter to any transitions
 * that should be fired.</p>
 *
 * <p>We say that the parameter is "optional", but a parameter value of <code>
 * null</code> functions identically to any other value - <code>null</code>
 * uniquely conditions a trigger-transition mapping the same as would, say, a
 * string. The wildcard parameter "*" may be used in mappings to indicate that
 * any runtime parameter (even <code>null</code>) should qualify the transition.
 * It may also be used at runtime to indicate that the client wishes to access
 * any transition mapped to the runtime trigger class, regardless of additional
 * parameterization in the map.
 *
 * <p>
 * <p>This mapping maintains transitions in the order in which they were
 * added.</p>
 *
 * <p>Additional mapping functionality has been added to this class since its
 * initial design: a mapping of Conditions to trigger/transition pairs, a
 * mapping of transition entry states, and a lazily-created map of actions
 * viable in particular states.</p>
 *
 * @version 1.1 17-Aug-2005
 * @author Orr Bernstein, <a href="mailto:jpulley@commercehub.com">J. Pulley</a>
 */
public class TriggerTransitionMap {
    /** DOCUMENT ME! */
    public static final String WILDCARD_MATCH = "*";
    private static final int NO_MATCH_SCORE = 0;
    private static final int WILDCARD_MATCH_SCORE = 1;
    private static final int EXACT_MATCH_SCORE = 2;

    /**
     * Used to getTransitions TriggerTransitionKey : LinkedHashSet<Transition>
     */
    private Map<TriggerTransitionKey, Set<Transition>> keyTransitions;

    /**
     * Used to getTransitions Transition : LinkedHashSet<TriggerTransitionKey>
     */
    private Map<Transition,Set<TriggerParamPair>> transitionKeys;

    /** Used to getTransitionsExitingState State : Set<Transition)> */
    private Map<State, Set> exitTransitions;

    /**
     * Used to getConditions TriggerTransitionConditionKey :
     * LinkedHashSet<Condition>
     */
    private Map<TriggerTransitionConditionKey, Condition> transitionConditions;

    /** Used to getEntryStates Transition : Set <State> */
    private Map<Transition, Set> transitionEntryStates;

    public TriggerTransitionMap() {
        keyTransitions = new HashMap<TriggerTransitionKey, Set<Transition>>();
        transitionKeys = new HashMap<Transition, Set<TriggerParamPair>>();
        exitTransitions = new HashMap<State, Set>();
        transitionConditions = new HashMap<TriggerTransitionConditionKey, Condition>();
        transitionEntryStates = new HashMap<Transition, Set>();
    }

    /**
     * Add a transition to the mapping, under the key formed by the combined
     * trigger class and optional parameter. The <code>triggerClass</code> and
     * <code>transition</code> arguments may not be <code>null</code>.
     *
     * @param triggerClass class of a trigger implementation
     * @param param parameter object, may be <code>null</code>
     * @param transition transition to store under the combined key
     */
    public void addTriggerTransition(Class triggerClass, Object param,
                                     Transition transition) {
        setupTriggerTransitionMap(triggerClass, param, transition);

        setupTriggerSet(triggerClass, param, transition);

        setupStateTransitionMap(transition);

        //set up entrystates map
        Set<State> entryStates = transitionEntryStates.get(transition);
        if (entryStates == null) {
            entryStates = new HashSet<State>();
            transitionEntryStates.put(transition, entryStates);
        }

        State thisState = transition.getEntryState();
        recordActivatedChildStates(thisState, entryStates);
        thisState = thisState.getParent();
        while (thisState != null) {
            if (thisState instanceof ConcurrentState) {
                recordActivatedChildStates(thisState, entryStates);
            } else {
                entryStates.add(thisState);
            }
            thisState = thisState.getParent();
        }
    }

	private void setupStateTransitionMap(Transition transition) {
		State exitState = transition.getExitState();
        Set<Transition> transitions = exitTransitions.get(exitState);
        if (transitions == null) {
            transitions = new LinkedHashSet<Transition>();
            exitTransitions.put(exitState, transitions);
        }
        transitions.add(transition);
	}

	private void setupTriggerSet(Class triggerClass, Object param,
			Transition transition) {
		Set<TriggerParamPair> triggerSet = transitionKeys.get(transition);
        if (triggerSet == null) {
            triggerSet = new LinkedHashSet<TriggerParamPair>();
            transitionKeys.put(transition, triggerSet);
        }
        triggerSet.add(new TriggerParamPair(triggerClass, param));
	}

	private void setupTriggerTransitionMap(Class triggerClass, Object param,
			Transition transition) {
		TriggerTransitionKey key =
            new TriggerTransitionKey(triggerClass, param);
        Set<Transition> transitionSet = keyTransitions.get(key);
        if (transitionSet == null) {
            transitionSet = new LinkedHashSet<Transition>();
            keyTransitions.put(key, transitionSet);
        }
        transitionSet.add(transition);
	}

    /**
     * <p>Add a transition to the mapping, under the key formed by the combined
     * class of the trigger and optional parameter. The <code>trigger</code> and
     * <code>transition</code> arguments may not be <code>null</code>. This
     * operation is equivalent to {@link #addTriggerTransition(Class, Object,
     * Transition) addTriggerTransition(trigger.getClass(), param, transition)}.
     * </p>
     *
     * <p>Conditions are complex rule sets that are evaluated before their
     * transitions are returned when a ConditionalTrigger is passed.</p>
     *
     * @param triggerClass trigger object
     * @param param parameter object, may be <code>null</code>
     * @param transition transition to store under the combined key
     * @param condition a condition object, to be evaluated before this
     *                  transition
     */
    public void addTriggerTransition(Class triggerClass, Object param,
                                     Transition transition,
                                     Condition condition) {
        addTriggerTransition(triggerClass, param, transition);

        TriggerTransitionConditionKey ttcKey =
            new TriggerTransitionConditionKey(triggerClass, transition, param);

        transitionConditions.put(ttcKey, condition);
    }

    /**
     * Builds and returns a modifiable set containing the parameters for a given
     * trigger class. This operation will not return <code>null</code>, although
     * it may return an empty set.</p>
     *
     * @param triggerClass a trigger, possibly mapped to some transitions
     *
     * @return parameters
     */
    public Set<Object> getParameters(Class triggerClass) {
        Set<Object> allParameters = new LinkedHashSet<Object>();

        //add transitions to the set
        Iterator<TriggerTransitionKey> iter = keyTransitions.keySet().iterator();
        while (iter.hasNext()) {
            TriggerTransitionKey key = iter.next();
            if (key.triggerCls.isAssignableFrom(triggerClass)) {
                allParameters.add(key.parm);
            }
        }

        return allParameters;
    }


    /**
     * Get a modifiable set containing the parameters mapped to a given trigger
     * class.
     *
     * <p>An iterator over the returned set will produce the transitions in the
     * order in which they were added to this mapping. This operation will not
     * return <code>null</code>, although it may return an empty set.</p>
     *
     * @param triggerClass a trigger, possibly mapped to some transitions
     * @param param optional parameter which may further condition the mapping
     *
     * @return transitions to be fired
     */
    public Set getTransitions(Class triggerClass, Object param) {
        Set allTransitions = new LinkedHashSet();

        //add transitions to the set
        Iterator<TriggerTransitionKey> iter = keyTransitions.keySet().iterator();
        while (iter.hasNext()) {
            TriggerTransitionKey key = iter.next();
            int score = key.score(triggerClass, param);
            if (score > NO_MATCH_SCORE) {
                allTransitions.addAll(keyTransitions.get(key));
            }
        }

        return allTransitions;
    }

    /**
     * Returns an unmodifiable set of the transitions out of a particular state.
     *
     * @param exitState an exiting state
     *
     * @return an unmodifiable set of transitions
     */
    public Set getTransitionsExitingState(State exitState) {
        Set transitions = exitTransitions.get(exitState);
        if (transitions == null) {
            transitions = Collections.EMPTY_SET;
        }
        return Collections.unmodifiableSet(transitions);
    }

    /**
     * Returns an unmodifiable Set of TriggerParamPair objects that could cause
     * a transition.
     *
     * @param transition DOCUMENT ME!
     *
     * @return
     */
    public Set getTriggerParamPairsForTransition(Transition transition) {
        Set triggerPairs = transitionKeys.get(transition);
        if (triggerPairs == null) {
            triggerPairs = Collections.EMPTY_SET;
        }
        return Collections.unmodifiableSet(triggerPairs);
    }

    /**
     * Gets the Condition object (if any) mapped to a single
     * trigger-transition-parameter triplet.
     *
     * @param triggerClass a trigger instance
     * @param trans a Transition
     * @param param a Trigger mapping parameter
     *
     * @return a Condition, or null.
     */
    public Condition getCondition(Class triggerClass, Transition trans,
                                  Object param) {
        Condition condition = null;

        Iterator<TriggerTransitionConditionKey> iterator = transitionConditions.keySet().iterator();
        while (iterator.hasNext() && condition == null) {
            TriggerTransitionConditionKey ttcKey =
                iterator.next();
            int score = ttcKey.score(triggerClass, trans, param);
            if (score > NO_MATCH_SCORE) {
                condition = transitionConditions.get(ttcKey);
            }
        }

        return condition;
    }

    /**
     * Records the child states that would be activated if the walkState became
     * active. This is used to populate a map of states that are guaranteed to
     * be active as the result of a transition for viability testing.
     *
     * @param walkState
     * @param transitions
     */
    private void recordActivatedChildStates(State walkState, Set<State> transitions) {
        //Walk defaults of exclusive states
        if (walkState instanceof ExclusiveState) {
            ExclusiveState excluShell = (ExclusiveState) walkState;
            State kid = excluShell.getDefaultChild();

            if (kid != null) {
                recordActivatedChildStates(kid, transitions);
            }
        } //Walk children-of-concurrents
        else if (walkState instanceof ConcurrentState) {
            ConcurrentState concShell = (ConcurrentState) walkState;
            Iterator iterator = concShell.getChildren().iterator();
            while (iterator.hasNext()) {
                State kid = (State) iterator.next();
                if (kid != null && !transitions.contains(kid)) {
                    recordActivatedChildStates(kid, transitions);
                }
            }
        }

        transitions.add(walkState);
    }

    /**
     * Returns a set of all the states guaranteed to be active as a result of a
     * particular transition.
     *
     * @param t a transition
     *
     * @return a Set of State objects. If this transition isn't mapped, an empty
     *         set is returned.
     */
    public Set getEntryStates(Transition t) {
        Set transitions = new HashSet();
        if (transitionEntryStates.containsKey(t)) {
            transitions.addAll(transitionEntryStates.get(t));
        }
        return transitions;
    }

    /**
     * Returns true if this parameter is viable -- that is, if it can ever be
     * used to fire a transition in any state reachable by this machine, given
     * its current state.
     *
     * @param triggerClass
     * @param param
     * @param activeStates DOCUMENT ME!
     *
     * @return
     */
    public boolean isParameterViable(Class triggerClass, Object param,
                                     Set activeStates) {
        boolean isViable = false;
        //find all the ones that match our trigger/param pair
        Set mappedTransitions = getTransitions(triggerClass, param);

        Set<Transition> walkedTransitions = new HashSet<Transition>();

        isViable =
            testTransitionReachability(activeStates,
                walkedTransitions, mappedTransitions, triggerClass);

        return isViable;
    }

    /**
     * Determines whether a transition in a set of tests is reachable from a set
     * of states, being careful not to test any transition twice. (States do in
     * fact get tested multiple times)
     *
     * @param states
     * @param walkedTransitions
     * @param testTransitions
     * @param restrictToClass
     *
     * @return
     */
    private boolean testTransitionReachability(Set states,
                                               Set<Transition> walkedTransitions,
                                               Set testTransitions,
                                               Class restrictToClass) {
        boolean anyViable = false;

        Iterator stateIter = states.iterator();
        while (!anyViable && stateIter.hasNext()) {
            Set transitionsForState =
                new HashSet(getTransitionsExitingState(
                        (State) stateIter.next()));

            transitionsForState.removeAll(walkedTransitions);

            Iterator transIter = transitionsForState.iterator();
            while (!anyViable && transIter.hasNext()) {
                Transition t = (Transition) transIter.next();

                if (testTransitions.contains(t)) {
                    anyViable = true;
                } else {
                    boolean couldBeCaused = false;
                    //don't walk transitions that could only be caused by
                    //trigger classes other than our restriction
                    Set pairs = getTriggerParamPairsForTransition(t);
                    for (Iterator it = pairs.iterator();
                            !couldBeCaused && it.hasNext();) {
                        TriggerParamPair key = (TriggerParamPair) it.next();
                        couldBeCaused |=
                            key.getTriggerClass().isAssignableFrom(restrictToClass);
                    }

                    if (couldBeCaused) {
                        walkedTransitions.add(t);
                        anyViable =
                            testTransitionReachability(
                                getEntryStates(t),
                                walkedTransitions, testTransitions,
                                restrictToClass);
                    }
                }
            }
        }

        return anyViable;
    }

    /**
     * Key for internal trigger-transition map.
     *
     * @version 1.1 17-Aug-2005
     * @author Orr Bernstein, <a href="mailto:jpulley@commercehub.com">J.
     *         Pulley</a>
     */
    private class TriggerTransitionKey {
        private Class triggerCls;
        private Object parm;
        private boolean wildcard;

        /**
         * Initializes a new TriggerTransitionKey object.
         *
         * @param triggerClass class of a trigger implementation
         * @param param optional additional key parameter
         */
        public TriggerTransitionKey(Class triggerClass, Object param) {
            triggerCls = triggerClass;
            parm = param;
            wildcard = WILDCARD_MATCH.equals(param);
        }

        /**
         * Scores a candidate key as an exact match, a wildcard match, or no
         * match at all.
         *
         * @param triggerClass class of a trigger implementation
         * @param param optional additional key parameter
         *
         * @return degree of match
         */
        public int score(Class triggerClass, Object param) {
            int score = NO_MATCH_SCORE;
            if (triggerCls.isAssignableFrom(triggerClass)) {
                if (parm == null) {
                    if (param == null) {
                        score = EXACT_MATCH_SCORE;
                    }
                } else if (parm.equals(param)) {
                    score = EXACT_MATCH_SCORE;
                } else if (wildcard) {
                    score = WILDCARD_MATCH_SCORE;
                } else if (WILDCARD_MATCH.equals(param)) {
                    score = WILDCARD_MATCH_SCORE;
                }
            }
            return score;
        }

        /**
         * Hash code, in accordance with definition of equality.
         *
         * @return hash code
         */
        public int hashCode() {
            return triggerCls.hashCode()
                + (parm == null ? 0 : parm.hashCode());
        }

        /**
         * Two TriggerTransitionKey objects are equal if the have the same
         * trigger implementation class and same optional parameter value.
         *
         * @param object another TriggerTransitionKey object
         *
         * @return whether this key and <code>object</code> are equal
         */
        public boolean equals(Object object) {
            boolean eqls = true;
            if (!(object instanceof TriggerTransitionKey)) {
                eqls = false;
            } else {
                TriggerTransitionKey other = (TriggerTransitionKey) object;
                if (other.triggerCls != triggerCls) {
                    eqls = false;
                } else if (parm == null) {
                    if (other.parm != null) {
                        eqls = false;
                    }
                } else if (!parm.equals(other.parm)) {
                    eqls = false;
                }
            }
            return eqls;
        }
    }

    /**
     * Key for internal triggertransition-condition map.
     *
     * @author Matthew M. Miller, <a href="mailto:jpulley@commercehub.com">J.
     *         Pulley</a>
     */
    private class TriggerTransitionConditionKey {
        private Class triggerCls;
        private Object parm;
        private Transition transition;
        private boolean wildcard;

        /**
         * Initializes a new TriggerTransitionConditionKey object.
         *
         * @param triggerClass class of a trigger implementation
         * @param trans a transition object
         * @param param optional additional key parameter
         */
        public TriggerTransitionConditionKey(Class triggerClass,
                                             Transition trans, Object param) {
            triggerCls = triggerClass;
            this.transition = trans;
            parm = param;
            wildcard = WILDCARD_MATCH.equals(param);
        }

        /**
         * Scores a candidate key as an exact match, a wildcard match, or no
         * match at all.
         *
         * @param triggerClass class of a trigger implementation
         * @param trans a Transition object
         * @param param optional additional key parameter
         *
         * @return degree of match
         */
        public int score(Class triggerClass, Transition trans, Object param) {
            int score = NO_MATCH_SCORE;
            if (triggerCls.isAssignableFrom(triggerClass)) {
                if (trans == this.transition) {
                    if (parm == null) {
                        if (param == null) {
                            score = EXACT_MATCH_SCORE;
                        }
                    } else if (parm.equals(param)) {
                        score = EXACT_MATCH_SCORE;
                    } else if (wildcard) {
                        score = WILDCARD_MATCH_SCORE;
                    } else if (WILDCARD_MATCH.equals(param)) {
                        score = WILDCARD_MATCH_SCORE;
                    }
                }
            }
            return score;
        }

        /**
         * Hash code, in accordance with definition of equality.
         *
         * @return hash code
         */
        public int hashCode() {
            return triggerCls.hashCode()
                + transition.hashCode()
                + (parm == null ? 0 : parm.hashCode());
        }

        /**
         * Two TriggerTransitionConditionKey objects are equal if the have the
         * same trigger implementation class, reference the same transition and
         * have the same optional parameter value.
         *
         * @param object another TriggerTransitionKey object
         *
         * @return whether this key and <code>object</code> are equal
         */
        public boolean equals(Object object) {
            boolean eqls = true;
            if (!(object instanceof TriggerTransitionConditionKey)) {
                eqls = false;
            } else {
                TriggerTransitionConditionKey other =
                    (TriggerTransitionConditionKey) object;
                if (other.triggerCls != triggerCls) {
                    eqls = false;
                } else if (other.transition != transition) {
                    eqls = false;
                } else if (parm == null) {
                    if (other.parm != null) {
                        eqls = false;
                    }
                } else if (!parm.equals(other.parm)) {
                    eqls = false;
                }
            }
            return eqls;
        }
    }
}
