
package com.commercehub.core.state.impl.xml;

import com.commercehub.core.state.StateCookie;
import com.commercehub.core.state.StateMachine;
import com.commercehub.core.state.StateMachineConfigurationException;
import com.commercehub.core.state.Stateful;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * A Hub3 implementation of {@link
 * com.commercehub.core.state.StateMachineFactory} that creates new, fully
 * modelled and wired StateMachine objects from XML.
 *
 * @author jply
 * @author Matthew Mark Miller
 * @author pmogren
 */
public class XMLStateMachineFactory {
    public StateMachine getStateMachineFromInputStreams(InputStream stateMachineStream,
        InputStream clientImplStream) throws StateMachineConfigurationException, SAXException,
        IOException {
        StateMachineHandler handler = new StateMachineHandler();
        StateMachine stateMachine = null;

        try {
            SAXParserFactory pfactory = SAXParserFactory.newInstance();
            pfactory.setValidating(false);

            SAXParser parser = pfactory.newSAXParser();
            parser.parse(stateMachineStream, handler);
            parser.parse(clientImplStream, handler);
            stateMachine = handler.getWiredStateMachine();
        } catch (ParserConfigurationException pcx) {
            String msg = "Java-XML setup is incorrect on this machine";
            StateMachineConfigurationException smcx =
                    new StateMachineConfigurationException(msg, pcx);
            throw smcx;
        }

        return stateMachine;
    }
}
