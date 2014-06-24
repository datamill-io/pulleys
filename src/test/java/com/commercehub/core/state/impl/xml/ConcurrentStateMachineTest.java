
package com.commercehub.core.state.impl.xml;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Test;

import com.commercehub.core.state.EntryAction;
import com.commercehub.core.state.ExitAction;
import com.commercehub.core.state.HubActionTrigger;
import com.commercehub.core.state.StateCookie;
import com.commercehub.core.state.StateMachineConfigurationException;
import com.commercehub.core.state.TransitionAction;
import com.commercehub.core.state.impl.pojo.SerializableStateCookie;
import org.junit.After;

public class ConcurrentStateMachineTest {
    private Alphabetical a;

    @After
    public void tearDown() {
        clear();
    }
    
    private void clear() {
        ExitAction.clear();
        EntryAction.clear();
        TransitionAction.clear();
    }

    @Test
    public void testTransitionFromExclusiveToConcurrent()
        throws StateMachineConfigurationException {
        a = new Alphabetical("concurrent-alphabetical-statemachine.xml");
        assertEquals("Inital State fails", "B", a.getActiveStateString());
        assertEquals("Exit Actions fired improperly", 0,
                ExitAction.timesExecuted());
        assertEquals("Transition Actions fired improperly", 0,
                TransitionAction.timesExecuted());
        assertEquals("Entry Actions fired improperly", 0,
                EntryAction.timesExecuted());
        clear();

        a.pullTrigger(new HubActionTrigger(), "exc-to-conc");
        assertEquals("Post-sibling transition fails", "C[D.E,G.H]",
                a.getActiveStateString());
        assertEquals("Exit Actions fired improperly", 1,
                ExitAction.timesExecuted());
        assertEquals("Transition Actions fired improperly", 1,
                TransitionAction.timesExecuted());
        assertEquals("Entry Actions fired improperly", 5,
                EntryAction.timesExecuted());
    }

    @Test
    public void testTransitionFromConcurrentToExclusive()
        throws StateMachineConfigurationException {
        StateCookie sc = new SerializableStateCookie();
        sc.clear();
        sc.setActive("C");
        sc.setActive("C.D");
        sc.setActive("C.D.E");
        sc.setActive("C.G");
        sc.setActive("C.G.H");
        a = new Alphabetical(sc, "concurrent-alphabetical-statemachine.xml");

        assertEquals("Inital State fails", "C[D.E,G.H]",
                a.getActiveStateString());


        a.pullTrigger(new HubActionTrigger(), "conc-to-exc");
        assertEquals("Post-sibling transition fails", "B",
                a.getActiveStateString());
        assertEquals("Exit Actions fired improperly", 5,
                ExitAction.timesExecuted());
        assertEquals("Transition Actions fired improperly", 1,
                TransitionAction.timesExecuted());
        assertEquals("Entry Actions fired improperly", 1,
                EntryAction.timesExecuted());
    }

    @Test
    public void testTransitionInternalToConcurrent()
        throws StateMachineConfigurationException {
        StateCookie sc = new SerializableStateCookie();
        sc.clear();
        sc.setActive("C");
        sc.setActive("C.D");
        sc.setActive("C.D.E");
        sc.setActive("C.G");
        sc.setActive("C.G.H");
        a = new Alphabetical(sc, "concurrent-alphabetical-statemachine.xml");

        assertEquals("Inital State fails", "C[D.E,G.H]",
                a.getActiveStateString());

        a.pullTrigger(new HubActionTrigger(), "internal");
        assertEquals("Post-sibling transition fails", "C[D.E,G.I]",
                a.getActiveStateString());
        assertEquals("Exit Actions fired improperly", 1,
                ExitAction.timesExecuted());
        assertEquals("Transition Actions fired improperly", 1,
                TransitionAction.timesExecuted());
        assertEquals("Entry Actions fired improperly", 1,
                EntryAction.timesExecuted());
    }

    @Test
    public void testTransitionConcurrentToConcurrent()
        throws StateMachineConfigurationException {
        StateCookie sc = new SerializableStateCookie();
        sc.clear();
        sc.setActive("C");
        sc.setActive("C.D");
        sc.setActive("C.D.E");
        sc.setActive("C.G");
        sc.setActive("C.G.I");
        a = new Alphabetical(sc, "concurrent-alphabetical-statemachine.xml");
        assertEquals("Inital State fails", "C[D.E,G.I]",
                a.getActiveStateString());

        a.pullTrigger(new HubActionTrigger(), "conc-to-conc");
        assertEquals("Post-sibling transition fails", "J[K,L.M]",
                a.getActiveStateString());
        assertEquals("Exit Actions fired improperly", 5,
                ExitAction.timesExecuted());
        assertEquals("Transition Actions fired improperly", 1,
                TransitionAction.timesExecuted());
        assertEquals("Entry Actions fired improperly", 4,
                EntryAction.timesExecuted());
    }
}
