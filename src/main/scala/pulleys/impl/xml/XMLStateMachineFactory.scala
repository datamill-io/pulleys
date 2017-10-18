package pulleys.impl.xml

import pulleys.StateMachine
import pulleys.StateMachineConfigurationException
import org.xml.sax.SAXException
import java.io.IOException
import java.io.InputStream
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory

/**
  * An implementation of {@link
  * pulleys.StateMachineFactory} that creates new, fully
  * modelled and wired StateMachine objects from XML.
  *
  * @author jply
  * @author Matthew Mark Miller
  * @author pmogren
  */
class XMLStateMachineFactory {
  @throws[StateMachineConfigurationException]
  @throws[SAXException]
  @throws[IOException]
  def getStateMachineFromInputStreams(stateMachineStream: InputStream, clientImplStream: InputStream): StateMachine = {
    val handler: StateMachineHandler = new StateMachineHandler
    var stateMachine: StateMachine = null
    try {
      val pfactory: SAXParserFactory = SAXParserFactory.newInstance
      pfactory.setValidating(false)
      val parser: SAXParser = pfactory.newSAXParser
      parser.parse(stateMachineStream, handler)
      parser.parse(clientImplStream, handler)
      stateMachine = handler.getWiredStateMachine
    }
    catch {
      case pcx: ParserConfigurationException => {
        val msg: String = "Java-XML setup is incorrect on this machine"
        val smcx: StateMachineConfigurationException = new StateMachineConfigurationException(msg, pcx)
        throw smcx
      }
    }
    return stateMachine
  }
}