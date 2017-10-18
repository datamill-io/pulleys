package pulleys.action

import pulleys.StateAction
import pulleys.Stateful

/**
  * An action with a parameter. Used by States and Transitions.
  *
  * @author Matthew Mark Miller
  */
class ParametricAction(val action: StateAction, val param: Any){
  def execute(stateful: Stateful) = action.execute(stateful, param)

  def getStateAction: StateAction = action

  def getParam: Any = param
}