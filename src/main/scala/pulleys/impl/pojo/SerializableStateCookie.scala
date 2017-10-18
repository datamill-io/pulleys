package pulleys.impl.pojo

import java.io.Serializable
import java.util.Collections
import java.util
import pulleys.State
import pulleys.StateCookie

/**
  * A state cookie using state sets, reducable to a state string in the model:
  * state.substate.concurrent-substate[onelane.active-state,nextlane.active-state]
  *
  * @author m3
  *
  */
@SerialVersionUID(-7432399227832996727L)
class SerializableStateCookie() extends StateCookie with Serializable {
  var activeCookies = new util.HashSet[String]
  var historyCookies = new util.HashSet[String]

  def isActive(state: State): Boolean = {
    if (state == null) {
      return false
    }
    return isActive(state.getPathName)
  }

  def isActive(stateName: String): Boolean = {
    if (stateName == null) {
      return false
    }
    return activeCookies.contains(stateName)
  }

  def setActive(state: State) {
    if (state == null) {
      return
    }
    setActive(state.getPathName)
  }

  def setActive(stateName: String) {
    if (!activeCookies.contains(stateName)) {
      activeCookies.add(stateName)
    }
  }

  def setActive(statePathNames: util.Set[String]) {
    activeCookies = new util.HashSet(statePathNames)
  }

  /**
    * @see StateCookie#toString()
    */
  override def toString: String = {
    val sb: StringBuffer = new StringBuffer("")
    sb.append("Active States:")
    sb.append(this.activeCookies)
    sb.append("Historic States:")
    sb.append(this.historyCookies)
    return sb.toString
  }

  /**
    * @see StateCookie#clear()
    */
  def clear() {
    activeCookies.clear()
    historyCookies.clear()
  }

  /**
    * @see StateCookie#getHistoryChild(State)
    */
  def getHistoryChild(parentState: State): State = {
    import scala.collection.JavaConversions._
    for (child <- parentState.getChildren) {
      if (historyCookies.contains(child.getPathName)) {
        return child
      }
    }
    return null
  }

  /**
    * @see StateCookie#setHistoryChild(State)
    */
  def setHistoryChild(childState: State) {
    historyCookies.add(childState.getPathName)
  }

  /**
    * Sets the history states in this cookie to <code>states</code>. Any
    * previous history states are replaced. The client's history cookie ID is
    * updated.
    *
    * @param states set of fully qualified state path names
    */
  def setHistoryStates(states: util.Set[String]) {
    historyCookies = new util.HashSet(states)
  }

  def isNew: Boolean = {
    return activeCookies.isEmpty
  }

  /**
    * An unmodifiable set of the pathnames of the history states represented by
    * this cookie.
    *
    * <p>This operation may not return <code>null</code>, although it may
    * return an empty set.
    *
    * @return history state pathnames
    */
  def getHistoryStatePathNames: util.Set[String] = {
    return Collections.unmodifiableSet(historyCookies)
  }

  /**
    * An unmodifiable set of the pathnames of the active states represented by
    * this cookie.
    *
    * <p>This operation may not return <code>null</code>, although it may
    * return an empty set.
    *
    * @return active state pathnames
    */
  def getActiveStatePathNames: util.Set[String] = {
    return Collections.unmodifiableSet(activeCookies)
  }
}