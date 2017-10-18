package pulleys.impl.xml;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;
import org.xml.sax.SAXException;

import pulleys.StateCookie;
import pulleys.StateMachine;
import pulleys.StateMachineConfigurationException;
import pulleys.Stateful;
import pulleys.Trigger;

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
	
	public class DummyStateful implements Stateful {

		public boolean isSupported(Trigger trigger, Object param) {

			return false;
		}

		public boolean pullTrigger(Trigger trigger, Object param) {

			return false;
		}

		public StateCookie getStateCookie() {

			return null;
		}

		public void notifyPropertyChanged(String propertyName, Object newValue) {

			
		}

		public boolean isInState(String statePath) {

			return false;
		}
		
	}
}
