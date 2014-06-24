
package com.commercehub.core.state;

/**
 * Notifies a Stateful object of a Property Set request.
 *
 * @author mmiller
 */
public class SetPropertyStateAction implements StateAction {
    /**
     * DOCUMENT ME!
     *
     * @see StateAction#execute(Stateful, Object)
     *
     * @param stateful A Stateful object
     * @param param A PropertyValuePair Object
     */
    public void execute(Stateful stateful, Object param) {
        PropertyValuePair pvp = (PropertyValuePair) param;
        stateful.notifyPropertyChanged(pvp.getProperty(), pvp.getValue());
    }
}
