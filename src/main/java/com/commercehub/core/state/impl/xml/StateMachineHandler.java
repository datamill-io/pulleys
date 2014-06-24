
package com.commercehub.core.state.impl.xml;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.commercehub.core.state.ConcurrentState;
import com.commercehub.core.state.Condition;
import com.commercehub.core.state.ExclusiveState;
import com.commercehub.core.state.PropertyValuePair;
import com.commercehub.core.state.SetPropertyStateAction;
import com.commercehub.core.state.State;
import com.commercehub.core.state.StateAction;
import com.commercehub.core.state.StateMachine;
import com.commercehub.core.state.StateMachineConfigurationException;
import com.commercehub.core.state.Stateful;
import com.commercehub.core.state.Transition;
import com.commercehub.core.state.Trigger;
import com.commercehub.core.state.TriggerTransitionMap;

/**
 * Handles the creation of StateMachines from a statemachine xml document.
 * Requires two xml documents -- a state-machine definition and a client-impl
 * definition -- which can be parsed in any order. Furthermore, the internal
 * references generated during parsing are not consumed in the production of a
 * State Machine. Meaning you can pass one client-impl, then each corresponding
 * state-machine, without needing to re-parse the client file and vica versa.
 */
public class StateMachineHandler extends DefaultHandler {
    private static StateAction a;

    private LinkedList<String> elementStack;

    /** Used for parsing a state-machine */
    private HashMap<String, Transition> transitionMap;

    private LinkedList<State> stateStack;
    private LinkedList<String> defaultStateStack;

    private LinkedList<TransDef> transitionDefList;
    private LinkedList<Ref> refList;
    private LinkedList<Ref> triggerMapRefList;
    private LinkedList<PropertyValuePair> defaultPropertyValues;

    private StateMachine stateMachine;
    private HashMap<String, LinkedList<String>> possiblePropertyValueMap;
    private LinkedList<String> currentPropertyList;

    private StringBuffer conditionString;

    private HashMap<TransDef, Collection<StateRankDef>> transitionStateRanks;

    /** Used for parsing a state-machine-client-impl */
    private HashMap<String, StateAction> actionMap;
    private HashMap<String, TriggerDef> triggerMap;
    private HashMap<String, StatefulDef> statefulMap;

    /**
     * Used during WireEvents to provide visibility of a triggertransition map
     * to private classes, enabling triggers to wire themselves.
     */
    private TriggerTransitionMap triggerTransMap;

    /** Used during a TriggerMap * */
    private String lastTriggerRef;
    private String lastTriggerParam;

    private ExpressionParser conditionParser;

    /**
     * Initializes a new StateMachineHandler object.
     */
    public StateMachineHandler() {
        conditionParser = new ExpressionParser();
        elementStack = new LinkedList<String>();
    }

    /**
     * Receive notification of the beginning of an element. Dispatches these
     * notifications to startXXX and handleXXX methods for each element type.
     *
     * @param uri namespace URI
     * @param localName local name (used with namespace)
     * @param qName qualified XML 1.0 name
     * @param attrs attributes attached to element, never <code>null</code>
     *
     * @throws SAXException any SAX exception, possibly wrapping another
     *                      exception
     */
    public void startElement(String uri, String localName, String qName,
                             Attributes attrs) throws SAXException {
        elementStack.addLast(qName);
        try {
            if (qName.equals("entry-action-ref")) {
                handleEntryActionRef(attrs);
            } else if (qName.equals("transition-action-ref")) {
                handleTransitionActionRef(attrs);
            } else if (qName.equals("state-machine")) {
                startStateMachine(attrs);
            } else if (qName.equals("state")) {
                startState(attrs);
            } else if (qName.equals("trigger-ref")) {
                handleTriggerRef(attrs);
            } else if (qName.equals("transition")) {
                startTransition(attrs);
            } else if (qName.equals("exit-action-ref")) {
                handleExitActionRef(attrs);
            } else if (qName.equals("transition-ref")) {
                handleTransitionRef(attrs);
            } else if (qName.equals("trigger-map")) {
                startTriggerMap(attrs);
            } else if (qName.equals("state-machine-client-impl")) {
                startStateMachineClientImpl(attrs);
            } else if (qName.equals("trigger-defn")) {
                handleTriggerDefn(attrs);
            } else if (qName.equals("action-defn")) {
                handleActionDefn(attrs);
            } else if (qName.equals("stateful-defn")) {
                handleStatefulDefn(attrs);
            } else if (qName.equals("property")) {
                startProperty(attrs);
            } else if (qName.equals("value")) {
                handleValue(attrs);
            } else if (qName.equals("transition-set-property")) {
                handleTransitionSetProperty(attrs);
            } else if (qName.equals("entry-set-property")) {
                handleEntrySetProperty(attrs);
            } else if (qName.equals("exit-set-property")) {
                handleExitSetProperty(attrs);
            } else if (qName.equals("condition")) {
                startCondition(attrs);
            } else if (qName.equals("state-rank")) {
                handleStateRank(attrs);
            }
        } catch (Exception ex) {
//            log.error("Start of element " + qName, ex, LOG_ANCHOR_5);
            throw new SAXException(ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param attrs DOCUMENT ME!
     */
    private void handleStateRank(Attributes attrs) {
        StateRankDef rankDef = new StateRankDef(attrs.getValue("ref"));
        TransDef lastTrans = this.transitionDefList.getLast();
        Collection<StateRankDef> rankedStates =
            transitionStateRanks.get(lastTrans);
        if (rankedStates == null) {
            rankedStates = new LinkedList<StateRankDef>();
            transitionStateRanks.put(lastTrans, rankedStates);
        }
        rankedStates.add(rankDef);
    }

    /**
     * DOCUMENT ME!
     *
     * @param attrs DOCUMENT ME!
     */
    private void startCondition(Attributes attrs) {
        conditionString = new StringBuffer();
    }

    /**
     * DOCUMENT ME!
     *
     * @param ch DOCUMENT ME!
     * @param start DOCUMENT ME!
     * @param length DOCUMENT ME!
     *
     * @throws SAXException DOCUMENT ME!
     */
    public void characters(char[] ch, int start, int length)
                    throws SAXException {
        String qName = elementStack.getLast();
        try {
            if (qName.equals("condition")) {
                conditionString.append(ch, start, length);
            }
        } catch (Exception ex) {
//            log.error("Reading contents of element " + elementStack, ex,
//                LOG_ANCHOR_7);
            throw new SAXException(ex);
        }
    }

    /**
     * Handes exit-set-property elements.
     *
     * @param attrs
     */
    private void handleExitSetProperty(Attributes attrs) {
        String name = attrs.getValue("name");
        String value = attrs.getValue("value");
        refList.add(new SetPropertyRef(stateStack.getLast(), name, value,
                ActionRefTypes.STATE_EXIT));
    }

    /**
     * Handes transition-set-property elements.
     *
     * @param attrs
     */
    private void handleTransitionSetProperty(Attributes attrs) {
        String name = attrs.getValue("name");
        String value = attrs.getValue("value");
        TransDef transDef = transitionDefList.getLast();
        refList.add(new SetPropertyRef(transDef.name, name, value,
                ActionRefTypes.TRANSITION));
    }

    /**
     * Handes entry-set-property elements.
     *
     * @param attrs
     */
    private void handleEntrySetProperty(Attributes attrs) {
        String name = attrs.getValue("name");
        String value = attrs.getValue("value");
        refList.add(new SetPropertyRef(stateStack.getLast(), name, value,
                ActionRefTypes.STATE_ENTRY));
    }

    /**
     * Handles a value element.
     *
     * @param attrs
     */
    private void handleValue(Attributes attrs) {
        String text = attrs.getValue("text");
        currentPropertyList.addLast(text);
    }

    /**
     * Handles the start of a Property section; creates a new property entry in
     * the map and creates a default, if any.
     *
     * @param attrs
     */
    private void startProperty(Attributes attrs) {
        String name = attrs.getValue("name");
        String defaultValue = attrs.getValue("default-value");
        currentPropertyList = new LinkedList<String>();
        possiblePropertyValueMap.put(name, currentPropertyList);
        if (defaultValue != null) {
            defaultPropertyValues.add(new PropertyValuePair(name,
                    defaultValue));
        }
    }

    /**
     * Receive notification of the end of an element and dispatches it to one or
     * more endXXX methods
     *
     * @param uri namespace URI
     * @param localName local name (used with namespace)
     * @param qName qualified XML 1.0 name
     *
     * @throws SAXException any SAX exception, possibly wrapping another
     *                      exception
     */
    public void endElement(String uri, String localName, String qName)
                    throws SAXException {
        try {
            if (qName.equals("state-machine")) {
                endStateMachine();
            } else if (qName.equals("transition")) {
                endTransition();
            } else if (qName.equals("trigger-map")) {
                endTriggerMap();
            } else if (qName.equals("state")) {
                endState();
            } else if (qName.equals("state-machine-client-impl")) {
                endStateMachineClientImpl();
            } else if (qName.equals("property")) {
                endProperty();
            } else if (qName.equals("condition")) {
                endCondition();
            }

            elementStack.removeLast();
        } catch (Exception ex) {
//            log.error("Error at end of element " + qName, ex, LOG_ANCHOR_6);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void endCondition() throws Exception {
        TriggerRef lastRef;
        if (lastTriggerRef != null) {
            lastRef = (TriggerRef) triggerMapRefList.getLast();
        } else {
            lastRef = (TriggerRef) refList.getLast();
        }
        lastRef.setCondition(conditionString.toString());
    }

    /**
     * DOCUMENT ME!
     */
    private void endProperty() {
    }

    /**
     * Receive notification of the beginning of element "state-machine".
     *
     * @param attrs attributes attached to element, never <code>null</code>
     */
    public void startStateMachine(Attributes attrs) {
        //initialize StateMachine lists, stacks and maps
        stateStack = new LinkedList<State>();
        defaultStateStack = new LinkedList<String>();

        transitionDefList = new LinkedList<TransDef>();
        refList = new LinkedList<Ref>();
        triggerMapRefList = new LinkedList<Ref>();

        String concurrent = attrs.getValue("concurrent");
        String defaultChild = attrs.getValue("default-child-ref");

        State rootState;
        if ("true".equals(concurrent)) {
            rootState = new ConcurrentState(attrs.getValue("name"), false);
        } else {
            rootState =
                new ExclusiveState(attrs.getValue("name"), false, false);
        }
        possiblePropertyValueMap = new HashMap<String, LinkedList<String>>();
        defaultPropertyValues = new LinkedList<PropertyValuePair>();

        stateMachine =
            new StateMachine(rootState, attrs.getValue("description"),
                possiblePropertyValueMap, defaultPropertyValues);

        stateStack.addLast(rootState);
        defaultStateStack.addLast(defaultChild);
        transitionStateRanks = new HashMap<TransDef, Collection<StateRankDef>>();
    }

    /**
     * Receive notification of the end of element "state-machine".
     */
    public void endStateMachine() {
        stateStack.removeLast();
        defaultStateStack.removeLast();

        //Build Transitions
        transitionMap = new HashMap<String, Transition>();
        while (!transitionDefList.isEmpty()) {
            try {
                TransDef newTrans = transitionDefList.removeFirst();
                Transition t = newTrans.make();
                transitionMap.put(newTrans.name, t);

                Collection<StateRankDef> stateRanks =
                    this.transitionStateRanks.get(newTrans);
                if (stateRanks != null) {
                    for (StateRankDef srd: stateRanks) {
                        srd.make(t);
                    }
                }
            } catch (StateMachineConfigurationException smce) {
//                log.error(smce, LOG_ANCHOR_1);
            }
        }
    }

    /**
     * Receive notification of the beginning of element "state". Creates a new
     * State object of the proper type and places it on the stateStack. If a
     * default-child-ref is defined, this reference is placed on a
     * defaultStateStack. This stack is checked by each new State and used to
     * set the default child of the parent.
     *
     * @param attrs attributes attached to element, never <code>null</code>
     */
    public void startState(Attributes attrs) {
        String history = attrs.getValue("history");
        String concurrent = attrs.getValue("concurrent");
        String name = attrs.getValue("name");
        String defaultChildRef = attrs.getValue("default-child-ref");

        State newState = null;
        //create a new state.
        if ("true".equals(concurrent)) {
            newState = new ConcurrentState(name, "deep".equals(history));
        } else {
            newState =
                new ExclusiveState(name, "self".equals(history),
                    "deep".equals(history));
        }

        State parent = stateStack.getLast();
        parent.addChild(newState);

        if (parent instanceof ExclusiveState) {
            ExclusiveState exclusiveParent = (ExclusiveState) parent;
            if (name.equals(defaultStateStack.getLast())) {
                exclusiveParent.setDefaultChild(newState);
            }
        }

        //add self to the stack.
        stateStack.addLast(newState);
        defaultStateStack.addLast(defaultChildRef);
    }

    /**
     * Receive notification of the end of element "state". "Pops" the state
     * we're leaving and its default off the stack.
     */
    public void endState() {
        stateStack.removeLast();
        defaultStateStack.removeLast();
    }

    /**
     * Receive notification of the beginning of element "transition". Makes a
     * list of transition definition records, which are used to create
     * Transition objects at the end of the current state-machine element.
     * References the last State placed on the stateStack.
     *
     * @param attrs attributes attached to element, never <code>null</code>
     */
    public void startTransition(Attributes attrs) {
        String entryRef = attrs.getValue("entry");
        String name = attrs.getValue("name");

        State exitState = stateStack.getLast();
        transitionDefList.add(new TransDef(exitState, entryRef, name));
    }

    /**
     * Receive notification of the end of element "transition". Currently a
     * no-op
     */
    public void endTransition() {
    }

    /**
     * Receive notification of the empty element "trigger-ref".
     *
     * @param attrs attributes attached to element, never <code>null</code>
     */
    public void handleTriggerRef(Attributes attrs) {
        String triggerRef = attrs.getValue("ref");
        String param = attrs.getValue("param");
        String transRef = transitionDefList.getLast().name;
        refList.add(new TriggerRef(transRef, triggerRef, param));
    }

    /**
     * Receive notification of the empty element "transition-action-ref".
     *
     * @param attrs attributes attached to element, never <code>null</code>
     */
    public void handleTransitionActionRef(Attributes attrs) {
        String ref = attrs.getValue("ref");
        String param = attrs.getValue("param");
        TransDef transDef = transitionDefList.getLast();
        refList.add(new ActionRef(transDef.name, ref, param,
                ActionRefTypes.TRANSITION));
    }

    /**
     * Receive notification of the empty element "entry-action-ref".
     *
     * @param attrs attributes attached to element, never <code>null</code>
     */
    public void handleEntryActionRef(Attributes attrs) {
        String ref = attrs.getValue("ref");
        String param = attrs.getValue("param");
        refList.add(new ActionRef(stateStack.getLast(), ref, param,
                ActionRefTypes.STATE_ENTRY));
    }

    /**
     * Receive notification of the empty element "exit-action-ref".
     *
     * @param attrs attributes attached to element, never <code>null</code>
     */
    public void handleExitActionRef(Attributes attrs) {
        String ref = attrs.getValue("ref");
        String param = attrs.getValue("param");
        refList.add(new ActionRef(stateStack.getLast(), ref, param,
                ActionRefTypes.STATE_EXIT));
    }

    /**
     * Receive notification of the beginning of element "trigger-map".
     *
     * @param attrs attributes attached to element, never <code>null</code>
     */
    public void startTriggerMap(Attributes attrs) {
        lastTriggerParam = attrs.getValue("param");
        lastTriggerRef = attrs.getValue("ref");
    }

    /**
     * Receive notification of the end of element "trigger-map".
     */
    public void endTriggerMap() {
        lastTriggerParam = lastTriggerRef = null;
    }

    /**
     * Receive notification of the empty element "transition-ref".
     *
     * @param attrs attributes attached to element, never <code>null</code>
     */
    public void handleTransitionRef(Attributes attrs) {
        String transRef = attrs.getValue("ref");
        this.triggerMapRefList.add(new TriggerRef(transRef, lastTriggerRef,
                lastTriggerParam));
    }

    /**
     * Clears the private trigger, action and stateful maps in preparation for a
     * new state-machine-client definition.
     *
     * @param attrs
     */
    public void startStateMachineClientImpl(Attributes attrs) {
        triggerMap = new HashMap<String, TriggerDef>();
        actionMap = new HashMap<String, StateAction>();
        statefulMap = new HashMap<String, StatefulDef>();
    }

    public void endStateMachineClientImpl() {
    }

    /**
     * Handles Trigger-def elements, creating triggerdef skeletongs and adding
     * them to a triggerMap keyed by name.
     *
     * @param attrs
     */
    public void handleTriggerDefn(Attributes attrs) {
        String name = attrs.getValue("name");
        String classic = attrs.getValue("class");
        try {
            triggerMap.put(name,
                new TriggerDef(name,
                    (Class<? extends Trigger>) Class.forName(classic, false,
                        getClass().getClassLoader())));
        } catch (ClassNotFoundException cnfe) {
//            log.error("Cannot create Trigger defintion " + name
//                + ", could not find " + classic, cnfe, LOG_ANCHOR_4);
        }
    }

    /**
     * Handles action-def elements, creating them and adding them to an
     * actionMap keyed by namefor later access by {@link ActionRef#wire()}
     *
     * @param attrs
     */
    public void handleActionDefn(Attributes attrs) {
        String name = attrs.getValue("name");
        String classic = attrs.getValue("class");
        try {
        	@SuppressWarnings("unchecked")
			Class<StateAction> actionClass = (Class<StateAction>) Class.forName(classic);
            actionMap.put(name, actionClass.newInstance());
        } catch (IllegalAccessException iae) {
//            log.error("Creating Action " + name + " of class " + classic, iae,
//                LOG_ANCHOR_3);
        } catch (InstantiationException ie) {
//            log.error("Creating Action " + name + " of class " + classic, ie,
//                LOG_ANCHOR_8);
        } catch (ClassNotFoundException cnfe) {
//            log.error("Creating Action " + name + " of class " + classic, cnfe,
//                LOG_ANCHOR_9);
        }
    }

    /**
     * handles Stateful Definitions, creating StatefulDef skeletons and adding
     * them to a statefulMap
     *
     * @param attrs
     */
    public void handleStatefulDefn(Attributes attrs) {
        try {
            String name = attrs.getValue("name");
            String classic = attrs.getValue("class");
            statefulMap.put(name,
                new StatefulDef(name,
                    (Class<? extends Stateful>) Class.forName(classic, false,getClass().getClassLoader())));
        } catch (ClassNotFoundException cnfe) {
//            log.error("Cannot create Stateful defintion", cnfe, LOG_ANCHOR_2);
        }
    }


    /**
     * Run after parsing one or more XML statemachine definition documents, this
     * method wires Actions and Triggers (our externally fired and internally
     * handled Events). References handle their own wiring. They know more about
     * themselves than we do, after all.
     *
     * @param eventRefList
     */
    private void wireEvents(LinkedList<Ref> eventRefList) {
        Iterator<Ref> iterator = eventRefList.iterator();
        while (iterator.hasNext()) {
            try {
                Ref r = iterator.next();
                r.wire();
            } catch (Exception ex) {
//                log.error("Problem wiring a reference.", ex, LOG_ANCHOR_10);
            }
        }
    }

    /**
     * Returns a fully wired (Trigger and Actions mapped to their corresponding
     * States and Transitions) StateMachine. This handler should have been used
     * to parse both a state-machine and a state-machine-impl class by then.
     *
     * @return A fully wired State Machine
     *
     * @throws StateMachineConfigurationException A configuration error
     *                                            indicating this state machine
     *                                            handler has been called
     *                                            improperly
     */
    public StateMachine getWiredStateMachine()
                                      throws StateMachineConfigurationException {
        if (stateMachine == null) {
            throw new StateMachineConfigurationException("The State Machine "
                + "Handler has not yet parsed a state-machine XML file.  No "
                + "state machine can be returned.");
        }
        if (statefulMap == null) {
            throw new StateMachineConfigurationException("The State Machine "
                + "Handler has not parsed a state-machine-client-impl XML "
                + "file.  No state machine can be returned.");
        }

        triggerTransMap = new TriggerTransitionMap();

        if (!triggerMapRefList.isEmpty()) {
            wireEvents(triggerMapRefList);
        }
        if (!refList.isEmpty()) {
            wireEvents(refList);
        }
        stateMachine.setTriggerTransitionMap(this.triggerTransMap);

        return stateMachine;
    }

    /**
     * Interface for Reference skellingtons A Reference crosses
     *
     * @author M3
     */
    private interface Ref {
        /**
         * Wires a reference into a set of maps retreived from a client impl
         *
         * @throws InstantiationException
         * @throws IllegalAccessException
         * @throws StateMachineConfigurationException DOCUMENT ME!
         */
        void wire() throws InstantiationException, IllegalAccessException,
                           StateMachineConfigurationException;
    }

    /**
     * A Transition skeleton. Used to hold on to parsed data until all States
     * has been parsed and loaded for a given state-machine element.
     *
     * @author mmiller
     */
    private class TransDef {
        private State exitState;
        private String entryRef;
        private String name;

        /**
         * Initializes a new TransDef object.
         *
         * @param exitState
         * @param entryRef
         * @param name
         */
        TransDef(State exitState, String entryRef, String name) {
            this.exitState = exitState;
            this.entryRef = entryRef;
            this.name = name;
        }

        /**
         * Makes a real transition out of our little skeleton
         *
         * @return A Transition wired to two States and a StateMachine
         *
         * @throws StateMachineConfigurationException
         */
        Transition make() throws StateMachineConfigurationException {
            State entryState = stateMachine.findByName(entryRef);
            if (entryState == null) {
                throw new StateMachineConfigurationException("Error handling "
                    + stateMachine.getName() + ".  Transition " + name
                    + " is looking for nonexistant State " + entryRef + ".");
            }
            Transition realTrans =
                new Transition(stateMachine, exitState, entryState, name);
            return realTrans;
        }
    }

    /**
     * Trigger Definition Skeleton
     *
     * @author mmiller
     */
    private class TriggerDef {
        /** Name of a Trigger */
        String name;

        /** Class of a Trigger */
        Class<? extends Trigger> triggerClass;

        /**
         * Initializes a new TriggerDef object.
         *
         * @param name
         * @param triggerClass
         */
        TriggerDef(String name, Class<? extends Trigger> triggerClass) {
            this.name = name;
            this.triggerClass = triggerClass;
        }
    }

    /**
     * Stateful Definition Skeleton
     *
     * @author mmiller
     */
    private class StatefulDef {
        /** Name of a Stateful */
        String name;

        /** Class of a Stateful */
        Class<? extends Stateful> statefulClass;

        /**
         * Initializes a new StatefulDef object.
         *
         * @param name
         * @param statefulClass
         */
        StatefulDef(String name, Class<? extends Stateful> statefulClass) {
            this.name = name;
            this.statefulClass = statefulClass;
        }
    }

    /**
     * Trigger Reference Skeleton
     *
     * @author mmiller
     */
    private class TriggerRef implements Ref {
        private String transRef;
        private String triggerRef;
        //a String or Set of Strings
        private Object param;
        private Condition condition;

        /**
         * Initializes a new TriggerRef object.
         *
         * @param transRef DOCUMENT ME!
         * @param triggerRef DOCUMENT ME!
         * @param param DOCUMENT ME!
         */
        TriggerRef(String transRef, String triggerRef, Object param) {
            this.transRef = transRef;
            this.triggerRef = triggerRef;
            //parse the parameter
            this.param = param;
            if (param instanceof String) {
                String paramStr = (String) param;
                if (paramStr.startsWith("{") && paramStr.endsWith("}")) {
                    StringTokenizer st =
                        new StringTokenizer(paramStr.substring(1,
                                paramStr.length() - 1), ",");

                    HashSet<String> hs = new HashSet<String>();
                    while (st.hasMoreTokens()) {
                        hs.add(st.nextToken());
                    }
                    this.param = hs;
                }
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param conditionExpression DOCUMENT ME!
         *
         * @throws Exception DOCUMENT ME!
         */
        void setCondition(String conditionExpression) throws Exception {
            condition = conditionParser.parse(conditionExpression);
        }

        /**
         * DOCUMENT ME!
         *
         * @see Ref#wire()
         *
         * @throws InstantiationException
         * @throws IllegalAccessException
         * @throws StateMachineConfigurationException DOCUMENT ME!
         */
        public void wire() throws InstantiationException,
                                  IllegalAccessException,
                                  StateMachineConfigurationException {
            Transition trans = transitionMap.get(transRef);
            TriggerDef td = triggerMap.get(triggerRef);
            if (td == null) {
                throw new StateMachineConfigurationException(
                    "Trigger " + triggerRef
                    + " isn't defined in a clientimpl file.");
            }
            if (condition == null) {
                triggerTransMap.addTriggerTransition(td.triggerClass, param,
                    trans);
            } else {
                triggerTransMap.addTriggerTransition(td.triggerClass, param,
                    trans, condition);
            }
        }
    }

    /**
     * SetProperty Reference Skeleton.
     *
     * @author mmiller
     */
    private class SetPropertyRef implements Ref {
        /**
         * For STATE RefTypes, this is a State. For TRANSITION RefTypes, this is
         * the name of a Transition
         */
        private Object actOn;
        private PropertyValuePair pvp;
        private int actionRefType;

        /**
         * Initializes a new ActionRef object.
         *
         * @param actOn
         * @param property
         * @param value
         * @param actionRefType
         */
        SetPropertyRef(Object actOn, String property, String value,
                       int actionRefType) {
            this.actOn = actOn;
            pvp = new PropertyValuePair(property, value);
            this.actionRefType = actionRefType;
        }

        /**
         * @see Ref#wire()
         */
        public void wire() throws InstantiationException,
                                  IllegalAccessException {
            if (a == null) {
                a = new SetPropertyStateAction();
            }
            switch (actionRefType) {
                case ActionRefTypes.TRANSITION:
                    Transition trans = transitionMap.get(actOn);
                    trans.addAction(a, pvp);
                    break;
                case ActionRefTypes.STATE_ENTRY:
                    ((State) actOn).addEntryAction(a, pvp);
                    break;
                case ActionRefTypes.STATE_EXIT:
                    ((State) actOn).addExitAction(a, pvp);
                    break;
            }
        }
    }

    /**
     * Action Reference Skeleton
     *
     * @author mmiller
     */
    private class ActionRef implements Ref {
        /**
         * For STATE RefTypes, this is a State. For TRANSITION RefTypes, this is
         * the name of a Transition
         */
        private Object actOn;
        private String actionRef;
        private String param;
        private int actionRefType;

        /**
         * Initializes a new ActionRef object.
         *
         * @param actOn
         * @param actionRef
         * @param param
         * @param actionRefType
         */
        ActionRef(Object actOn, String actionRef, String param,
                  int actionRefType) {
            this.actOn = actOn;
            this.actionRef = actionRef;
            this.actionRefType = actionRefType;
            this.param = param;
        }

        /**
         * @see Ref#wire()
         */
        public void wire() throws InstantiationException,
                                  IllegalAccessException,
                                  StateMachineConfigurationException {
            StateAction a = (StateAction) actionMap.get(actionRef);
            if (a == null) {
                throw new StateMachineConfigurationException(
                    "State Action " + actionRef
                    + " is not defined in a clientimpl file.");
            }
            switch (actionRefType) {
                case ActionRefTypes.TRANSITION:
                    Transition trans = transitionMap.get(actOn);
                    trans.addAction(a, param);
                    break;
                case ActionRefTypes.STATE_ENTRY:
                    ((State) actOn).addEntryAction(a, param);
                    break;
                case ActionRefTypes.STATE_EXIT:
                    ((State) actOn).addExitAction(a, param);
                    break;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @author dcarr
     */
    private class StateRankDef {
        private String stateRef;

        /**
         * Initializes a new StateRankDef object.
         *
         * @param stateRef DOCUMENT ME!
         */
        StateRankDef(String stateRef) {
            this.stateRef = stateRef;
        }

        /**
         * DOCUMENT ME!
         *
         * @param trans DOCUMENT ME!
         *
         * @throws StateMachineConfigurationException DOCUMENT ME!
         */
        void make(Transition trans) throws StateMachineConfigurationException {
            State state = stateMachine.findByName(stateRef);
            if (state != null) {
                trans.addRankedState(state);
            } else {
                throw new StateMachineConfigurationException(
                    "Cannot find a state " + stateRef
                    + " to rank for transition " + trans);
            }
        }
    }

    /**
     * Enumerates types of Action References
     *
     * @author Matthew Mark Miller
     */
    private final class ActionRefTypes {
        /** A Transition Action */
        static final int TRANSITION = 0;

        /** A State Entry Action */
        static final int STATE_ENTRY = 1;

        /** A State Exit Action */
        static final int STATE_EXIT = 2;
    }
}
