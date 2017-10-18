package pulleys.action

import pulleys.PropertyValuePair
import pulleys.StateAction
import pulleys.Stateful

/**
  * Notifies a Stateful object of a Property Set request.
  *
  * @author mmiller
  */
class SetPropertyStateAction extends StateAction {
  /**
    * DOCUMENT ME!
    *
    * @see StateAction#execute(Stateful, Object)
    * @param stateful A Stateful object
    * @param param    A PropertyValuePair Object
    */
  def execute(stateful: Stateful, param: Any) {
    val pvp: PropertyValuePair = param.asInstanceOf[PropertyValuePair]
    stateful.notifyPropertyChanged(pvp.getProperty, pvp.getValue)
  }
}