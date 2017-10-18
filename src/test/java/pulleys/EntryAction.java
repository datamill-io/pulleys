
package pulleys;

import pulleys.annotations.RefName;

@RefName("EntryAction")
public class EntryAction implements StateAction {
    private static int executed = 0;

    public static int timesExecuted() {
        return executed;
    }

    public static void clear() {
        executed = 0;
    }

    public void execute(Stateful stateful, Object param) {
        System.out.println("Entered "
            + stateful.getClass().getName() + " (" + param + ")");
        executed++;
    }
}
