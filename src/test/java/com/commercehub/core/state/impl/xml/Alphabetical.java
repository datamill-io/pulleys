
package com.commercehub.core.state.impl.xml;

import java.util.Set;


import com.commercehub.core.state.StateCookie;
import com.commercehub.core.state.StateMachine;
import com.commercehub.core.state.StateMachineConfigurationException;
import com.commercehub.core.state.Stateful;
import com.commercehub.core.state.Trigger;
import com.commercehub.core.state.impl.pojo.SerializableStateCookie;
import com.commercehub.core.state.impl.xml.XMLStateMachineFactory;

public class Alphabetical implements Stateful {
    StateMachine sm;
    StateCookie sc;

    private int activeSwimLaneCount = 0;

    public Alphabetical(String machineDefinition)
                 throws StateMachineConfigurationException {
    	this(new SerializableStateCookie(), machineDefinition);
    }

    public Alphabetical(StateCookie initialState, String machineDefinition)
                 throws StateMachineConfigurationException {
    	try {
	        sc = initialState;
	        
	        sm = new
	            XMLStateMachineFactory().getStateMachineFromInputStreams(
	            		getClass().getResourceAsStream(machineDefinition), 
	            		getClass().getResourceAsStream("clientimpl.xml"));
	        sm.attachStateful(this);
     	} catch (Exception e) {
     		e.printStackTrace();
    		throw new StateMachineConfigurationException("Could not make an Alphabetical.", e);
    	}
    }
    public boolean pullTrigger(Trigger trigger, Object param) {
        sm.pullTrigger(trigger, param, null);
        return true;
    }
    
    public StateCookie getStateCookie() {
        return sc;
    }
    
    public void releaseMachine() {
    }

    public void notifyPropertyChanged(String propertyName, Object newValue) {
        if (propertyName.equals("swimlanes")) {
            activeSwimLaneCount = Integer.parseInt((String) newValue);
        }
    }

    public int getActiveSwimLaneCount() {
        return activeSwimLaneCount;
    }

    public Set getValidParameters(Trigger trig) {
        return sm.getApplicableParameters(trig.getClass());
    }

    public boolean isSupported(Trigger trigger, Object param) {
        return sm.isSupported(trigger.getClass(), param);
    }
    
    public boolean isInState(String statePath) {
        return sm.isInState(statePath);
    }

	public String getActiveStateString() {
		return sm.getActiveStateString();
	}
}
