
package pulleys;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * A Condition is an expression of state strings, to test against a set of
 * Stateful objects.
 *
 * @author mmiller
 */
public class Condition {
    private LinkedList<Condition> conditionStack;

    public Condition() {
        conditionStack = new LinkedList<Condition>();
    }

    public boolean eval(Set statefuls, ConditionEvaluator condEval) {
        boolean result = false;
        for (Condition cond: conditionStack){
            result |= cond.eval(statefuls, condEval);
        }
        return result;
    }

    public String toString() {
        return toString(0).toString();
    }

    public StringBuffer toString(int indent) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < indent; i++) {
            buf.append(' ');
        }
        buf.append(getName());
        for (Condition cond: conditionStack){
            buf.append("\n");
            buf.append(cond.toString(indent + 4));
        }
        return buf;
    }

    protected String getName() {
        return "Condition";
    }

    public Condition addAndClause() {
        AndClause cond = new AndClause();
        conditionStack.add(cond);
        return cond;
    }

    public Condition addOrClause() {
        OrClause cond = new OrClause();
        conditionStack.add(cond);
        return cond;
    }

    public Condition addNotClause() {
        NotClause cond = new NotClause();
        conditionStack.add(cond);
        return cond;
    }

    public void addAnyClause(Set checkStates) {
        conditionStack.add(new AnyClause(checkStates));
    }

    public void addAllClause(Set checkStates) {
        conditionStack.add(new AllClause(checkStates));
    }

    public void addSomeClause(Set checkStates) {
        conditionStack.add(new SomeClause(checkStates));
    }

    public void addNoneClause(Set checkStates) {
        conditionStack.add(new NoneClause(checkStates));
    }

    class AndClause extends Condition {
        public boolean eval(Set statefuls, ConditionEvaluator condEval) {
            boolean result = false;
            for (Condition cond: conditionStack){
                result = true;
                result &= cond.eval(statefuls, condEval);
                if (!result) {
                    break;
                }
            }
            return result;
        }

        protected String getName() {
            return "AND";
        }
    }

    class OrClause extends Condition {
        public boolean eval(Set statefuls, ConditionEvaluator condEval) {
            boolean result = false;
            for (Condition cond: conditionStack){
                result |= cond.eval(statefuls, condEval);
            }
            return result;
        }

        protected String getName() {
            return "OR";
        }
    }

    class NotClause extends Condition {
        public boolean eval(Set statefuls, ConditionEvaluator condEval) {
            Condition cond = (Condition) conditionStack.getLast();
            return !cond.eval(statefuls, condEval);
        }

        protected String getName() {
            return "NOT";
        }
    }

    class AnyClause extends Condition {
        private Set inCondition;

        AnyClause(Set inCondition) {
            this.inCondition = inCondition;
        }

        public boolean eval(Set statefuls, ConditionEvaluator condEval) {
            boolean result = false;
            Iterator iterator = statefuls.iterator();
            while (iterator.hasNext() && result == false) {
                result = condEval.isInCondition(inCondition, iterator.next());
            }
            return result;
        }

        protected String getName() {
            return "ANY " + inCondition;
        }
    }

    class AllClause extends Condition {
        private Set inCondition;

        AllClause(Set inCondition) {
            this.inCondition = inCondition;
        }

        public boolean eval(Set statefuls, ConditionEvaluator condEval) {
            boolean result = false;
            Iterator iterator = statefuls.iterator();
            while (iterator.hasNext()) {
                result = condEval.isInCondition(inCondition, iterator.next());
                if (result == false) {
                    break;
                }
            }
            return result;
        }

        protected String getName() {
            return "ALL " + inCondition;
        }
    }

    class SomeClause extends Condition {
        private Set inCondition;

        SomeClause(Set inCondition) {
            this.inCondition = inCondition;
        }

        public boolean eval(Set statefuls, ConditionEvaluator condEval) {
            boolean result = false;
            boolean onlySome = false;
            Iterator iterator = statefuls.iterator();
            while (iterator.hasNext() && !(result && onlySome)) {
                boolean isThisInState = 
                	condEval.isInCondition(inCondition, iterator.next());
                result |= isThisInState;
                onlySome |= !isThisInState;
            }
            return result && onlySome;
        }

        protected String getName() {
            return "SOME " + inCondition;
        }
    }

    class NoneClause extends Condition {
        private Set inCondition;

        NoneClause(Set inCondition) {
            this.inCondition = inCondition;
        }

        public boolean eval(Set statefuls, ConditionEvaluator condEval) {
            boolean result = true;
            Iterator iterator = statefuls.iterator();
            while (iterator.hasNext() && result) {
                result = !condEval.isInCondition(inCondition, iterator.next());
            }
            return result;
        }

        protected String getName() {
            return "NONE " + inCondition;
        }
    }
}
