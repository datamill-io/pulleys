
package com.commercehub.core.state.impl.xml;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.commercehub.core.state.EntryAction;
import com.commercehub.core.state.ExitAction;
import com.commercehub.core.state.HubActionTrigger;
import com.commercehub.core.state.StateCookie;
import com.commercehub.core.state.StateMachineConfigurationException;
import com.commercehub.core.state.TransitionAction;
import com.commercehub.core.state.impl.pojo.SerializableStateCookie;

public class ExclusiveStateMachineTest extends TestCase {
    private Alphabetical a;

    /**
     * private Hub3StateMachineFactory smFactory;
     *
     * @param name DOCUMENT ME!
     */
    public ExclusiveStateMachineTest(String name) {
        super(name);
    }

    /**
     * Sets up the test fixture. (Called before every test case method.)
     */
    protected void setUp() {
    }

    /**
     * Tears down the test fixture. (Called after every test case method.)
     */
    protected void tearDown() {
        clear();
        if (a != null) {
            a.releaseMachine();
        }
    }

    private void clear() {
        ExitAction.clear();
        EntryAction.clear();
        TransitionAction.clear();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Exclusive States");
        suite.addTest(new ExclusiveStateMachineTest(
                "testTransitionBetweenSiblings"));
        suite.addTest(new ExclusiveStateMachineTest(
                "testTransitionFromAncestorToDescendent"));
        suite.addTest(new ExclusiveStateMachineTest(
                "testTransitionFromDescendentToAncestor"));
        suite.addTest(new ExclusiveStateMachineTest(
                "testTransitionBetweenAncestors"));
        suite.addTest(new ExclusiveStateMachineTest("testSelfTransition"));
        return suite;
    }

    public void testTransitionBetweenSiblings()
        throws StateMachineConfigurationException {
        a = new Alphabetical("exclusive-alphabetical-statemachine.xml");
        super.assertEquals("Exit Actions fired improperly", 0,
            ExitAction.timesExecuted());
        super.assertEquals("Transition Actions fired improperly", 0,
            TransitionAction.timesExecuted());
        super.assertEquals("Entry Actions fired improperly", 0,
            EntryAction.timesExecuted());
        super.assertEquals("Inital State fails", "C.F",
            a.getActiveStateString());
        clear();

        System.out.println(a.getValidParameters(new HubActionTrigger()));
        a.pullTrigger(new HubActionTrigger(), "siblings");
        super.assertEquals("Post-sibling transition fails", "C.G",
            a.getActiveStateString());
        super.assertEquals("Exit Actions fired improperly", 1,
            ExitAction.timesExecuted());
        super.assertEquals("Transition Actions fired improperly", 1,
            TransitionAction.timesExecuted());
        super.assertEquals("Entry Actions fired improperly", 1,
            EntryAction.timesExecuted());
    }

    public void testTransitionFromAncestorToDescendent()
        throws StateMachineConfigurationException {
        StateCookie sc = new SerializableStateCookie();
        sc.setActive("C");
        sc.setActive("C.G");
        a = new Alphabetical(sc, "exclusive-alphabetical-statemachine.xml");

        super.assertEquals("Inital State fails", "C.G",
            a.getActiveStateString());

        //(Should be a no-change-op, only firing a Transition Action)
        a.pullTrigger(new HubActionTrigger(), "ancestor-to-descendent");
        super.assertEquals("Post-sibling transition fails", "C.G.J.K",
            a.getActiveStateString());
        super.assertEquals("Exit Actions fired improperly", 0,
            ExitAction.timesExecuted());
        super.assertEquals("Transition Actions fired improperly", 1,
            TransitionAction.timesExecuted());
        super.assertEquals("Entry Actions fired improperly", 2,
            EntryAction.timesExecuted());
    }

    public void testTransitionFromDescendentToAncestor()
        throws StateMachineConfigurationException {
        StateCookie sc = new SerializableStateCookie();
        sc.setActive("C");
        sc.setActive("C.G");
        a = new Alphabetical(sc, "exclusive-alphabetical-statemachine.xml");
        super.assertEquals("Inital State fails", "C.G",
            a.getActiveStateString());

        //(Should be a no-change-op, only firing a Transition Action)
        a.pullTrigger(new HubActionTrigger(), "descendent-to-ancestor");
        super.assertEquals("Post-sibling transition fails", "C.G",
            a.getActiveStateString());
        super.assertEquals("Exit Actions fired improperly", 0,
            ExitAction.timesExecuted());
        super.assertEquals("Transition Actions fired improperly", 1,
            TransitionAction.timesExecuted());
        super.assertEquals("Entry Actions fired improperly", 0,
            EntryAction.timesExecuted());
    }

    public void testTransitionBetweenAncestors()
        throws StateMachineConfigurationException {
        a = new Alphabetical("exclusive-alphabetical-statemachine.xml");
        super.assertEquals("Inital State fails", "C.F",
            a.getActiveStateString());
        clear();

        a.pullTrigger(new HubActionTrigger(), "ancestors");
        super.assertEquals("Post-sibling transition fails", "B",
            a.getActiveStateString());
        super.assertEquals("Exit Actions fired improperly", 2,
            ExitAction.timesExecuted());
        super.assertEquals("Transition Actions fired improperly", 1,
            TransitionAction.timesExecuted());
        super.assertEquals("Entry Actions fired improperly", 1,
            EntryAction.timesExecuted());
    }

    public void testSelfTransition() throws StateMachineConfigurationException {
        StateCookie sc = new SerializableStateCookie();
        sc.setActive("B");
        a = new Alphabetical(sc, "exclusive-alphabetical-statemachine.xml");
        super.assertEquals("Inital State fails", "B", a.getActiveStateString());

        //(Should be a no-change-op only firing a Transition Action)
        a.pullTrigger(new HubActionTrigger(), "self");
        super.assertEquals("Post-sibling transition fails", "B",
            a.getActiveStateString());
        super.assertEquals("Exit Actions fired improperly", 0,
            ExitAction.timesExecuted());
        super.assertEquals("Transition Actions fired improperly", 1,
            TransitionAction.timesExecuted());
        super.assertEquals("Entry Actions fired improperly", 0,
            EntryAction.timesExecuted());
    }

}
