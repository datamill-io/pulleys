package com.commercehub.core.state.impl.xml;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.commercehub.core.state.StateCookie;
import com.commercehub.core.state.StateMachine;
import com.commercehub.core.state.StateMachineConfigurationException;
import com.commercehub.core.state.Stateful;
import com.commercehub.core.state.Trigger;

public class XMLStateMachineFactoryTest {

	String basicStateMachine = 
			"<state-machine name=\"root\">" +
					"<state name=\"a\"/>" +
					"<state name=\"b\"/>" +
			"</state-machine>";
	
	String basicClientImpl = 
			"<state-machine-client-impl>" +
			"</state-machine-client-impl>";
			
	
	@Test
	public void test() throws StateMachineConfigurationException, SAXException, IOException {
		XMLStateMachineFactory smFact = new XMLStateMachineFactory();
		StateMachine sm = smFact.getStateMachineFromInputStreams(
				new ByteArrayInputStream(basicStateMachine.getBytes()), 
				new ByteArrayInputStream(basicClientImpl.getBytes()));
		assertNotNull(sm.getRootState());
		assertEquals(2, sm.getRootState().getChildren().size());
		
	}
	
	public class DummyStateful implements Stateful{

		public boolean isSupported(Trigger trigger, Object param) {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean pullTrigger(Trigger trigger, Object param) {
			// TODO Auto-generated method stub
			return false;
		}

		public StateCookie getStateCookie() {
			// TODO Auto-generated method stub
			return null;
		}

		public void notifyPropertyChanged(String propertyName, Object newValue) {
			// TODO Auto-generated method stub
			
		}

		public boolean isInState(String statePath) {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
}
