
package pulleys;

import pulleys.annotations.RefName;

@RefName("HubActionTrigger")
public class HubActionTrigger implements Trigger {
    public boolean eval(Stateful stateful, Object param, Condition con) {
        return true;
    }
}
