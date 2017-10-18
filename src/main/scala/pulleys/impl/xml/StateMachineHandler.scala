package pulleys.impl.xml

import java.util.Collection
import java.util.HashMap
import java.util.HashSet
import java.util.Iterator
import java.util.LinkedList
import java.util.StringTokenizer

import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import pulleys.ConcurrentState
import pulleys.Condition
import pulleys.ExclusiveState
import pulleys.PropertyValuePair
import pulleys.action.SetPropertyStateAction
import pulleys.State
import pulleys.StateAction
import pulleys.StateMachine
import pulleys.StateMachineConfigurationException
import pulleys.Stateful
import pulleys.Transition
import pulleys.Trigger
import pulleys.TriggerTransitionMap
import pulleys.impl.xml.conditions.ExpressionParser

/**
  * Handles the creation of StateMachines from a statemachine xml document.
  * Requires two xml documents -- a state-machine definition and a client-impl
  * definition -- which can be parsed in any order. Furthermore, the internal
  * references generated during parsing are not consumed in the production of a
  * State Machine. Meaning you can pass one client-impl, then each corresponding
  * state-machine, without needing to re-parse the client file and vica versa.
  */
object StateMachineHandler {
  var a: StateAction = _
  var conditionParser = new ExpressionParser
  var elementStack = new LinkedList[String]
}

class StateMachineHandler() extends DefaultHandler {
  private var elementStack: LinkedList[String] = _
  /** Used for parsing a state-machine */
  private var transitionMap: HashMap[String, Transition] = _
  private var stateStack: LinkedList[State] = _
  private var defaultStateStack: LinkedList[String] = _
  private var transitionDefList: LinkedList[TransDef] = _
  private var refList: LinkedList[Ref] = _
  private var triggerMapRefList: LinkedList[Ref] = _
  private var defaultPropertyValues: LinkedList[PropertyValuePair] = _
  private var stateMachine: StateMachine = _
  private var possiblePropertyValueMap: HashMap[String, LinkedList[String]] = _
  private var currentPropertyList: LinkedList[String] = _
  private var conditionString: StringBuffer = _
  private var transitionStateRanks: HashMap[TransDef, Collection[StateRankDef]] = _
  /** Used for parsing a state-machine-client-impl */
  private var actionMap: HashMap[String, StateAction] = _
  private var triggerMap: HashMap[String, TriggerDef] = _
  private var statefulMap: HashMap[String, StatefulDef] = _
  /**
    * Used during WireEvents to provide visibility of a triggertransition map
    * to private classes, enabling triggers to wire themselves.
    */
  private var triggerTransMap: TriggerTransitionMap = _
  /** Used during a TriggerMap * */
  private var lastTriggerRef: String = _
  private var lastTriggerParam: String = _
  private var conditionParser: ExpressionParser = _

  /**
    * Receive notification of the beginning of an element. Dispatches these
    * notifications to startXXX and handleXXX methods for each element type.
    *
    * @param uri       namespace URI
    * @param localName local name (used with namespace)
    * @param qName     qualified XML 1.0 name
    * @param attrs     attributes attached to element, never <code>null</code>
    * @throws SAXException any SAX exception, possibly wrapping another
    *                      exception
    */
  @throws[SAXException]
  override def startElement(uri: String, localName: String, qName: String, attrs: Attributes) {
    elementStack.addLast(qName)
    try {
      if (qName == "entry-action-ref") {
        handleEntryActionRef(attrs)
      }
      else if (qName == "transition-action-ref") {
        handleTransitionActionRef(attrs)
      }
      else if (qName == "state-machine") {
        startStateMachine(attrs)
      }
      else if (qName == "state") {
        startState(attrs)
      }
      else if (qName == "trigger-ref") {
        handleTriggerRef(attrs)
      }
      else if (qName == "transition") {
        startTransition(attrs)
      }
      else if (qName == "exit-action-ref") {
        handleExitActionRef(attrs)
      }
      else if (qName == "transition-ref") {
        handleTransitionRef(attrs)
      }
      else if (qName == "trigger-map") {
        startTriggerMap(attrs)
      }
      else if (qName == "state-machine-client-impl") {
        startStateMachineClientImpl(attrs)
      }
      else if (qName == "trigger-defn") {
        handleTriggerDefn(attrs)
      }
      else if (qName == "action-defn") {
        handleActionDefn(attrs)
      }
      else if (qName == "stateful-defn") {
        handleStatefulDefn(attrs)
      }
      else if (qName == "property") {
        startProperty(attrs)
      }
      else if (qName == "value") {
        handleValue(attrs)
      }
      else if (qName == "transition-set-property") {
        handleTransitionSetProperty(attrs)
      }
      else if (qName == "entry-set-property") {
        handleEntrySetProperty(attrs)
      }
      else if (qName == "exit-set-property") {
        handleExitSetProperty(attrs)
      }
      else if (qName == "condition") {
        startCondition(attrs)
      }
      else if (qName == "state-rank") {
        handleStateRank(attrs)
      }
    }
    catch {
      case ex: Exception => {
        //            log.error("Start of element " + qName, ex, LOG_ANCHOR_5);
        throw new SAXException(ex)
      }
    }
  }

  private def handleStateRank(attrs: Attributes) {
    val rankDef: StateRankDef = new StateRankDef(attrs.getValue("ref"))
    val lastTrans: TransDef = this.transitionDefList.getLast
    var rankedStates: Collection[StateRankDef] = transitionStateRanks.get(lastTrans)
    if (rankedStates == null) {
      rankedStates = new LinkedList[StateRankDef]
      transitionStateRanks.put(lastTrans, rankedStates)
    }
    rankedStates.add(rankDef)
  }

  private def startCondition(attrs: Attributes) {
    conditionString = new StringBuffer
  }

  @throws[SAXException]
  override def characters(ch: Array[Char], start: Int, length: Int) {
    val qName: String = elementStack.getLast
    try {
      if (qName == "condition") {
        conditionString.append(ch, start, length)
      }
    }
    catch {
      case ex: Exception => {
        //            log.error("Reading contents of element " + elementStack, ex,
        //                LOG_ANCHOR_7);
        throw new SAXException(ex)
      }
    }
  }

  private def handleExitSetProperty(attrs: Attributes) {
    val name: String = attrs.getValue("name")
    val value: String = attrs.getValue("value")
    refList.add(new SetPropertyRef(stateStack.getLast, name, value, ActionRefTypes.STATE_EXIT))
  }

  private def handleTransitionSetProperty(attrs: Attributes) {
    val name: String = attrs.getValue("name")
    val value: String = attrs.getValue("value")
    val transDef: TransDef = transitionDefList.getLast
    refList.add(new SetPropertyRef(transDef.name, name, value, ActionRefTypes.TRANSITION))
  }

  private def handleEntrySetProperty(attrs: Attributes) {
    val name: String = attrs.getValue("name")
    val value: String = attrs.getValue("value")
    refList.add(new SetPropertyRef(stateStack.getLast, name, value, ActionRefTypes.STATE_ENTRY))
  }

  private def handleValue(attrs: Attributes) {
    val text: String = attrs.getValue("text")
    currentPropertyList.addLast(text)
  }

  private def startProperty(attrs: Attributes) {
    val name: String = attrs.getValue("name")
    val defaultValue: String = attrs.getValue("default-value")
    currentPropertyList = new LinkedList[String]
    possiblePropertyValueMap.put(name, currentPropertyList)
    if (defaultValue != null) {
      defaultPropertyValues.add(new PropertyValuePair(name, defaultValue))
    }
  }

  @throws[SAXException]
  override def endElement(uri: String, localName: String, qName: String) {
    try {
      if (qName == "state-machine") {
        endStateMachine()
      }
      else if (qName == "transition") {
        endTransition()
      }
      else if (qName == "trigger-map") {
        endTriggerMap()
      }
      else if (qName == "state") {
        endState()
      }
      else if (qName == "state-machine-client-impl") {
        endStateMachineClientImpl()
      }
      else if (qName == "property") {
        endProperty()
      }
      else if (qName == "condition") {
        endCondition()
      }
      elementStack.removeLast
    }
    catch {
      case ex: Exception => {
        //            log.error("Error at end of element " + qName, ex, LOG_ANCHOR_6);
      }
    }
  }

  @throws[Exception]
  private def endCondition() {
    var lastRef: TriggerRef = null
    if (lastTriggerRef != null) {
      lastRef = triggerMapRefList.getLast.asInstanceOf[TriggerRef]
    }
    else {
      lastRef = refList.getLast.asInstanceOf[TriggerRef]
    }
    lastRef.setCondition(conditionString.toString)
  }

  private def endProperty() {
  }

  /**
    * Receive notification of the beginning of element "state-machine".
    *
    * @param attrs attributes attached to element, never <code>null</code>
    */
  def startStateMachine(attrs: Attributes) {
    //initialize StateMachine lists, stacks and maps
    stateStack = new LinkedList[State]
    defaultStateStack = new LinkedList[String]
    transitionDefList = new LinkedList[TransDef]
    refList = new LinkedList[Ref]
    triggerMapRefList = new LinkedList[Ref]
    val concurrent: String = attrs.getValue("concurrent")
    val defaultChild: String = attrs.getValue("default-child-ref")
    var rootState: State = if ("true" == concurrent) {
      new ConcurrentState(attrs.getValue("name"), false)
    } else {
      new ExclusiveState(attrs.getValue("name"), false, false)
    }
    possiblePropertyValueMap = new HashMap[String, LinkedList[String]]
    defaultPropertyValues = new LinkedList[PropertyValuePair]
    stateMachine = new StateMachine(rootState, attrs.getValue("description"), possiblePropertyValueMap, defaultPropertyValues)
    stateStack.addLast(rootState)
    defaultStateStack.addLast(defaultChild)
    transitionStateRanks = new HashMap[TransDef, Collection[StateRankDef]]
  }

  /**
    * Receive notification of the end of element "state-machine".
    */
  def endStateMachine() {
    stateStack.removeLast
    defaultStateStack.removeLast
    //Build Transitions
    transitionMap = new HashMap[String, Transition]
    while (!transitionDefList.isEmpty) {
      {
        try {
          val newTrans: TransDef = transitionDefList.removeFirst
          val t: Transition = newTrans.make
          transitionMap.put(newTrans.name, t)
          val stateRanks: Collection[StateRankDef] = this.transitionStateRanks.get(newTrans)
          if (stateRanks != null) {
            import scala.collection.JavaConversions._
            for (srd <- stateRanks) {
              srd.make(t)
            }
          }
        }
        catch {
          case smce: StateMachineConfigurationException => {
            //                log.error(smce, LOG_ANCHOR_1);
          }
        }
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
  def startState(attrs: Attributes) {
    val history: String = attrs.getValue("history")
    val concurrent: String = attrs.getValue("concurrent")
    val name: String = attrs.getValue("name")
    val defaultChildRef: String = attrs.getValue("default-child-ref")
    var newState: State = null
    //create a new state.
    if ("true" == concurrent) {
      newState = new ConcurrentState(name, "deep" == history)
    }
    else {
      newState = new ExclusiveState(name, "self" == history, "deep" == history)
    }
    val parent: State = stateStack.getLast
    parent.addChild(newState)
    if (parent.isInstanceOf[ExclusiveState]) {
      val exclusiveParent: ExclusiveState = parent.asInstanceOf[ExclusiveState]
      if (name == defaultStateStack.getLast) {
        exclusiveParent.setDefaultChild(newState)
      }
    }
    //add self to the stack.
    stateStack.addLast(newState)
    defaultStateStack.addLast(defaultChildRef)
  }

  /**
    * Receive notification of the end of element "state". "Pops" the state
    * we're leaving and its default off the stack.
    */
  def endState() {
    stateStack.removeLast
    defaultStateStack.removeLast
  }

  /**
    * Receive notification of the beginning of element "transition". Makes a
    * list of transition definition records, which are used to create
    * Transition objects at the end of the current state-machine element.
    * References the last State placed on the stateStack.
    *
    * @param attrs attributes attached to element, never <code>null</code>
    */
  def startTransition(attrs: Attributes) {
    val entryRef: String = attrs.getValue("entry")
    val name: String = attrs.getValue("name")
    val exitState: State = stateStack.getLast
    transitionDefList.add(new TransDef(exitState, entryRef, name))
  }

  /**
    * Receive notification of the end of element "transition". Currently a
    * no-op
    */
  def endTransition() {
  }

  /**
    * Receive notification of the empty element "trigger-ref".
    *
    * @param attrs attributes attached to element, never <code>null</code>
    */
  def handleTriggerRef(attrs: Attributes) {
    val triggerRef: String = attrs.getValue("ref")
    val param: String = attrs.getValue("param")
    val transRef: String = transitionDefList.getLast.name
    refList.add(new TriggerRef(transRef, triggerRef, param))
  }

  /**
    * Receive notification of the empty element "transition-action-ref".
    *
    * @param attrs attributes attached to element, never <code>null</code>
    */
  def handleTransitionActionRef(attrs: Attributes) {
    val ref: String = attrs.getValue("ref")
    val param: String = attrs.getValue("param")
    val transDef: TransDef = transitionDefList.getLast
    refList.add(new ActionRef(transDef.name, ref, param, ActionRefTypes.TRANSITION))
  }

  /**
    * Receive notification of the empty element "entry-action-ref".
    *
    * @param attrs attributes attached to element, never <code>null</code>
    */
  def handleEntryActionRef(attrs: Attributes) {
    val ref: String = attrs.getValue("ref")
    val param: String = attrs.getValue("param")
    refList.add(new ActionRef(stateStack.getLast, ref, param, ActionRefTypes.STATE_ENTRY))
  }

  /**
    * Receive notification of the empty element "exit-action-ref".
    *
    * @param attrs attributes attached to element, never <code>null</code>
    */
  def handleExitActionRef(attrs: Attributes) {
    val ref: String = attrs.getValue("ref")
    val param: String = attrs.getValue("param")
    refList.add(new ActionRef(stateStack.getLast, ref, param, ActionRefTypes.STATE_EXIT))
  }

  /**
    * Receive notification of the beginning of element "trigger-map".
    *
    * @param attrs attributes attached to element, never <code>null</code>
    */
  def startTriggerMap(attrs: Attributes) {
    lastTriggerParam = attrs.getValue("param")
    lastTriggerRef = attrs.getValue("ref")
  }

  /**
    * Receive notification of the end of element "trigger-map".
    */
  def endTriggerMap() {
    lastTriggerParam = null
    lastTriggerRef = null
  }

  /**
    * Receive notification of the empty element "transition-ref".
    *
    * @param attrs attributes attached to element, never <code>null</code>
    */
  def handleTransitionRef(attrs: Attributes) {
    val transRef: String = attrs.getValue("ref")
    this.triggerMapRefList.add(new TriggerRef(transRef, lastTriggerRef, lastTriggerParam))
  }

  /**
    * Clears the private trigger, action and stateful maps in preparation for a
    * new state-machine-client definition.
    *
    * @param attrs
    */
  def startStateMachineClientImpl(attrs: Attributes) {
    triggerMap = new HashMap[String, TriggerDef]
    actionMap = new HashMap[String, StateAction]
    statefulMap = new HashMap[String, StatefulDef]
  }

  def endStateMachineClientImpl() {
  }

  /**
    * Handles Trigger-def elements, creating triggerdef skeletongs and adding
    * them to a triggerMap keyed by name.
    *
    * @param attrs
    */
  def handleTriggerDefn(attrs: Attributes) {
    val name: String = attrs.getValue("name")
    val classic: String = attrs.getValue("class")
    try {
      triggerMap.put(name, new TriggerDef(name, Class.forName(classic, false, getClass.getClassLoader).asInstanceOf[Class[_ <: Trigger]]))
    }
    catch {
      case cnfe: ClassNotFoundException => {
        //            log.error("Cannot create Trigger defintion " + name
        //                + ", could not find " + classic, cnfe, LOG_ANCHOR_4);
      }
    }
  }

  /**
    * Handles action-def elements, creating them and adding them to an
    * actionMap keyed by namefor later access by {@link ActionRef#wire()}
    *
    * @param attrs
    */
  def handleActionDefn(attrs: Attributes) {
    val name: String = attrs.getValue("name")
    val classic: String = attrs.getValue("class")
    try {
      @SuppressWarnings(Array("unchecked")) val actionClass: Class[StateAction] = Class.forName(classic).asInstanceOf[Class[StateAction]]
      actionMap.put(name, actionClass.newInstance)
    }
    catch {
      case iae: IllegalAccessException => {
        //            log.error("Creating Action " + name + " of class " + classic, iae,
        //                LOG_ANCHOR_3);
      }
      case ie: InstantiationException => {
        //            log.error("Creating Action " + name + " of class " + classic, ie,
        //                LOG_ANCHOR_8);
      }
      case cnfe: ClassNotFoundException => {
        //            log.error("Creating Action " + name + " of class " + classic, cnfe,
        //                LOG_ANCHOR_9);
      }
    }
  }

  /**
    * handles Stateful Definitions, creating StatefulDef skeletons and adding
    * them to a statefulMap
    *
    * @param attrs
    */
  def handleStatefulDefn(attrs: Attributes) {
    try {
      val name: String = attrs.getValue("name")
      val classic: String = attrs.getValue("class")
      statefulMap.put(name, new StatefulDef(name, Class.forName(classic, false, getClass.getClassLoader).asInstanceOf[Class[_ <: Stateful]]))
    }
    catch {
      case cnfe: ClassNotFoundException => {
        //            log.error("Cannot create Stateful defintion", cnfe, LOG_ANCHOR_2);
      }
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
  private def wireEvents(eventRefList: LinkedList[Ref]) {
    val iterator: Iterator[Ref] = eventRefList.iterator
    while (iterator.hasNext) {
      {
        try {
          val r: Ref = iterator.next
          r.wire()
        }
        catch {
          case ex: Exception => {
            //                log.error("Problem wiring a reference.", ex, LOG_ANCHOR_10);
          }
        }
      }
    }
  }

  /**
    * Returns a fully wired (Trigger and Actions mapped to their corresponding
    * States and Transitions) StateMachine. This handler should have been used
    * to parse both a state-machine and a state-machine-impl class by then.
    *
    * @return A fully wired State Machine
    * @throws StateMachineConfigurationException A configuration error
    *                                            indicating this state machine
    *                                            handler has been called
    *                                            improperly
    */
  @throws[StateMachineConfigurationException]
  def getWiredStateMachine: StateMachine = {
    if (stateMachine == null) {
      throw new StateMachineConfigurationException("The State Machine " + "Handler has not yet parsed a state-machine XML file.  No " + "state machine can be returned.")
    }
    if (statefulMap == null) {
      throw new StateMachineConfigurationException("The State Machine " + "Handler has not parsed a state-machine-client-impl XML " + "file.  No state machine can be returned.")
    }
    triggerTransMap = new TriggerTransitionMap
    if (!triggerMapRefList.isEmpty) {
      wireEvents(triggerMapRefList)
    }
    if (!refList.isEmpty) {
      wireEvents(refList)
    }
    stateMachine.setTriggerTransitionMap(this.triggerTransMap)
    return stateMachine
  }

  private class TransDef (var exitState: State, var entryRef: String, var name: String){
    @throws[StateMachineConfigurationException]
    def make: Transition = {
      val entryState: State = stateMachine.findByName(entryRef)
      if (entryState == null) {
        throw new StateMachineConfigurationException("Error handling " + stateMachine.getName + ".  Transition " + name + " is looking for nonexistant State " + entryRef + ".")
      }
      val realTrans: Transition = new Transition(stateMachine, exitState, entryState, name)
      return realTrans
    }
  }

  private class TriggerDef private[xml](/** Name of a Trigger */
                                        var name: String,

                                        /** Class of a Trigger */
                                        var triggerClass: Class[_ <: Trigger])

  private class StatefulDef private[xml](/** Name of a Stateful */
                                         var name: String,

                                         /** Class of a Stateful */
                                         var statefulClass: Class[_ <: Stateful])


  private class TriggerRef private[xml](var transRef: String, var triggerRef: String, //a String or Set of Strings
                                        var param: Any) extends Ref {
    //parse the parameter
    if (param.isInstanceOf[String]) {
      val paramStr: String = param.asInstanceOf[String]
      if (paramStr.startsWith("{") && paramStr.endsWith("}")) {
        val st: StringTokenizer = new StringTokenizer(paramStr.substring(1, paramStr.length - 1), ",")
        val hs: HashSet[String] = new HashSet[String]
        while (st.hasMoreTokens) {
          {
            hs.add(st.nextToken)
          }
        }
        this.param = hs
      }
    }
    private var condition: Condition = _

    @throws[Exception]
    private[xml] def setCondition(conditionExpression: String) {
      condition = conditionParser.parse(conditionExpression)
    }

    @throws[InstantiationException]
    @throws[IllegalAccessException]
    @throws[StateMachineConfigurationException]
    def wire() {
      val trans: Transition = transitionMap.get(transRef)
      val td: TriggerDef = triggerMap.get(triggerRef)
      if (td == null) {
        throw new StateMachineConfigurationException("Trigger " + triggerRef + " isn't defined in a clientimpl file.")
      }
      if (condition == null) {
        triggerTransMap.addTriggerTransition(td.triggerClass, param, trans)
      }
      else {
        triggerTransMap.addTriggerTransition(td.triggerClass, param, trans, condition)
      }
    }
  }

  private class SetPropertyRef private[xml](/**
                                              * For STATE RefTypes, this is a State. For TRANSITION RefTypes, this is
                                              * the name of a Transition
                                              */
                                            var actOn: Any, val property: String, val value: String, var actionRefType: Int)
    extends Ref {
    val pvp = new PropertyValuePair(property, value)

    /**
      * @see Ref#wire()
      */
    @throws[InstantiationException]
    @throws[IllegalAccessException]
    def wire() {
      if (StateMachineHandler.a == null) {
        StateMachineHandler.a = new SetPropertyStateAction
      }
      actionRefType match {
        case ActionRefTypes.TRANSITION =>
          val trans: Transition = transitionMap.get(actOn)
          trans.addAction(StateMachineHandler.a, pvp)
        case ActionRefTypes.STATE_ENTRY =>
          (actOn.asInstanceOf[State]).addEntryAction(StateMachineHandler.a, pvp)
        case ActionRefTypes.STATE_EXIT =>
          (actOn.asInstanceOf[State]).addExitAction(StateMachineHandler.a, pvp)
      }
    }
  }

  private class ActionRef private[xml](/**
                                         * For STATE RefTypes, this is a State. For TRANSITION RefTypes, this is
                                         * the name of a Transition
                                         */
                                       var actOn: Any, var actionRef: String, var param: String, var actionRefType: Int)
    extends Ref {
    /**
      * @see Ref#wire()
      */
    @throws[InstantiationException]
    @throws[IllegalAccessException]
    @throws[StateMachineConfigurationException]
    def wire() {
      val a: StateAction = actionMap.get(actionRef).asInstanceOf[StateAction]
      if (a == null) {
        throw new StateMachineConfigurationException("State Action " + actionRef + " is not defined in a clientimpl file.")
      }
      actionRefType match {
        case ActionRefTypes.TRANSITION =>
          val trans: Transition = transitionMap.get(actOn)
          trans.addAction(a, param)
        case ActionRefTypes.STATE_ENTRY =>
          (actOn.asInstanceOf[State]).addEntryAction(a, param)
        case ActionRefTypes.STATE_EXIT =>
          (actOn.asInstanceOf[State]).addExitAction(a, param)
      }
    }
  }

  private class StateRankDef private[xml](var stateRef: String) {
    @throws[StateMachineConfigurationException]
    private[xml] def make(trans: Transition) {
      val state: State = stateMachine.findByName(stateRef)
      if (state != null) {
        trans.addRankedState(state)
      }
      else {
        throw new StateMachineConfigurationException("Cannot find a state " + stateRef + " to rank for transition " + trans)
      }
    }
  }

  /**
    * Enumerates types of Action References
    *
    * @author Matthew Mark Miller
    */
  private object ActionRefTypes {
    /** A Transition Action */
    val TRANSITION: Int = 0
    /** A State Entry Action */
    val STATE_ENTRY: Int = 1
    /** A State Exit Action */
    val STATE_EXIT: Int = 2
  }

  trait Ref {
    /**
      * Wires a reference into a set of maps retreived from a client impl
      *
      * @throws InstantiationException
      * @throws IllegalAccessException
      * @throws StateMachineConfigurationException DOCUMENT ME!
      */
    @throws[InstantiationException]
    @throws[IllegalAccessException]
    @throws[StateMachineConfigurationException]
    def wire()
  }
}