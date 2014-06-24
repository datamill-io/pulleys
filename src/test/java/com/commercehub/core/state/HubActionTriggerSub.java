
/*
 * HubActionTriggerSub.java
 *
 * Created on October 31, 2003, 10:37 AM
 */
package com.commercehub.core.state;

public class HubActionTriggerSub extends HubActionTrigger {
    public boolean eval(Stateful stateful, Object param) {
        return true;
    }
}
