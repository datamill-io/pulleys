
package com.commercehub.core.state;

import com.commercehub.core.state.annotations.RefName;

@RefName("HubActionTrigger")
public class HubActionTrigger implements Trigger {
    public boolean eval(Stateful stateful, Object param, Condition con) {
        return true;
    }
}
