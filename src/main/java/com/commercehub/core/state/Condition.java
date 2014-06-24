
package com.commercehub.core.state;

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
    /** DOCUMENT ME! */
    LinkedList conditionStack;

    /**
     * Initializes a new Condition object.
     */
    public Condition() {
        conditionStack = new LinkedList();
    }

    /**
     * DOCUMENT ME!
     *
     * @param statefuls DOCUMENT ME!
     * @param condEval TODO
     *
     * @return DOCUMENT ME!
     */
    public boolean eval(Set statefuls, ConditionEvaluator condEval) {
        boolean result = false;
        Iterator iterator = conditionStack.iterator();
        while (iterator.hasNext()) {
            Condition cond = (Condition) iterator.next();
            result |= cond.eval(statefuls, condEval);
        }
        return result;
    }

    /**
     * public String toString(){ String traverse = "Condition("; Iterator
     * iterator = conditionStack.iterator(); while (iterator.hasNext()){
     * Condition cond = (Condition) iterator.next(); traverse = traverse +
     * cond.toString(); if (cond != conditionStack.getLast()){ traverse =
     * traverse + ","; } } traverse = traverse + ")"; return traverse; }
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        return toString(0).toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param indent DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public StringBuffer toString(int indent) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < indent; i++) {
            buf.append(' ');
        }
        buf.append(getName());
        for (Iterator c = conditionStack.iterator(); c.hasNext();) {
            Condition child = (Condition) c.next();
            buf.append("\n");
            buf.append(child.toString(indent + 4));
        }
        return buf;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    protected String getName() {
        return "Condition";
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Condition addAndClause() {
        AndClause cond = new AndClause();
        conditionStack.add(cond);
        return cond;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Condition addOrClause() {
        OrClause cond = new OrClause();
        conditionStack.add(cond);
        return cond;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Condition addNotClause() {
        NotClause cond = new NotClause();
        conditionStack.add(cond);
        return cond;
    }

    /**
     * DOCUMENT ME!
     *
     * @param checkStates DOCUMENT ME!
     */
    public void addAnyClause(Set checkStates) {
        conditionStack.add(new AnyClause(checkStates));
    }

    /**
     * DOCUMENT ME!
     *
     * @param checkStates DOCUMENT ME!
     */
    public void addAllClause(Set checkStates) {
        conditionStack.add(new AllClause(checkStates));
    }

    /**
     * DOCUMENT ME!
     *
     * @param checkStates DOCUMENT ME!
     */
    public void addSomeClause(Set checkStates) {
        conditionStack.add(new SomeClause(checkStates));
    }

    /**
     * DOCUMENT ME!
     *
     * @param checkStates DOCUMENT ME!
     */
    public void addNoneClause(Set checkStates) {
        conditionStack.add(new NoneClause(checkStates));
    }

    /**
     * DOCUMENT ME!
     *
     * @author dcarr
     */
    class AndClause extends Condition {
        /**
         * DOCUMENT ME!
         *
         * @param statefuls DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public boolean eval(Set statefuls, ConditionEvaluator condEval) {
            boolean result = false;
            Iterator iterator = conditionStack.iterator();
            while (iterator.hasNext()) {
                result = true;
                Condition cond = (Condition) iterator.next();
                result &= cond.eval(statefuls, condEval);
                if (!result) {
                    break;
                }
            }
            return result;
        }

        /**
         * public String toString(){ String traverse = "And("; Iterator iterator
         * = conditionStack.iterator(); while (iterator.hasNext()){ Condition
         * cond = (Condition) iterator.next(); traverse = traverse +
         * cond.toString(); if (cond != conditionStack.getLast()){ traverse =
         * traverse + ","; } } traverse = traverse + ")"; return traverse; }
         *
         * @return DOCUMENT ME!
         */
        protected String getName() {
            return "AND";
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @author dcarr
     */
    class OrClause extends Condition {
        /**
         * DOCUMENT ME!
         *
         * @param statefuls DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public boolean eval(Set statefuls, ConditionEvaluator condEval) {
            boolean result = false;
            Iterator iterator = conditionStack.iterator();
            while (iterator.hasNext() && !result) {
                Condition cond = (Condition) iterator.next();
                result |= cond.eval(statefuls, condEval);
            }
            return result;
        }

        /**
         * public String toString(){ String traverse = "Or("; Iterator iterator
         * = conditionStack.iterator(); while (iterator.hasNext()){ Condition
         * cond = (Condition) iterator.next(); traverse = traverse +
         * cond.toString(); if (cond != conditionStack.getLast()){ traverse =
         * traverse + ","; } } traverse = traverse + ")"; return traverse; }
         *
         * @return DOCUMENT ME!
         */
        protected String getName() {
            return "OR";
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @author dcarr
     */
    class NotClause extends Condition {
        /**
         * DOCUMENT ME!
         *
         * @param statefuls DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public boolean eval(Set statefuls, ConditionEvaluator condEval) {
            Condition cond = (Condition) conditionStack.getLast();
            return !cond.eval(statefuls, condEval);
        }

        /**
         * public String toString(){ String traverse = "Not("; Iterator iterator
         * = conditionStack.iterator(); while (iterator.hasNext()){ Condition
         * cond = (Condition) iterator.next(); traverse = traverse +
         * cond.toString(); if (cond != conditionStack.getLast()){ traverse =
         * traverse + ","; } } traverse = traverse + ")"; return traverse; }
         *
         * @return DOCUMENT ME!
         */
        protected String getName() {
            return "NOT";
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @author dcarr
     */
    class AnyClause extends Condition {
        private Set inCondition;

        /**
         * Initializes a new AnyClause object.
         *
         * @param inCondition DOCUMENT ME!
         */
        AnyClause(Set inCondition) {
            this.inCondition = inCondition;
        }

        /**
         * DOCUMENT ME!
         *
         * @param statefuls DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public boolean eval(Set statefuls, ConditionEvaluator condEval) {
            boolean result = false;
            Iterator iterator = statefuls.iterator();
            while (iterator.hasNext() && result == false) {
                result = condEval.isInCondition(inCondition, iterator.next());
            }
            return result;
        }

        /**
         * public String toString(){ String traverse = "Any("; Iterator iterator
         * = inCondition.iterator(); while (iterator.hasNext()){ String state =
         * (String) iterator.next(); traverse = traverse + state; if
         * (iterator.hasNext()){ traverse = traverse + ","; } } traverse =
         * traverse + ")"; return traverse; }
         *
         * @return DOCUMENT ME!
         */
        protected String getName() {
            return "ANY " + inCondition;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @author dcarr
     */
    class AllClause extends Condition {
        private Set inCondition;

        /**
         * Initializes a new AllClause object.
         *
         * @param inCondition DOCUMENT ME!
         */
        AllClause(Set inCondition) {
            this.inCondition = inCondition;
        }

        /**
         * DOCUMENT ME!
         *
         * @param statefuls DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
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

        /**
         * public String toString(){ String traverse = "All("; Iterator iterator
         * = inCondition.iterator(); while (iterator.hasNext()){ String state =
         * (String) iterator.next(); traverse = traverse + state; if
         * (iterator.hasNext()){ traverse = traverse + ","; } } traverse =
         * traverse + ")"; return traverse; }
         *
         * @return DOCUMENT ME!
         */
        protected String getName() {
            return "ALL " + inCondition;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @author dcarr
     */
    class SomeClause extends Condition {
        private Set inCondition;

        /**
         * Initializes a new SomeClause object.
         *
         * @param inCondition DOCUMENT ME!
         */
        SomeClause(Set inCondition) {
            this.inCondition = inCondition;
        }

        /**
         * DOCUMENT ME!
         *
         * @param statefuls DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
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

        /**
         * public String toString(){ String traverse = "Some("; Iterator
         * iterator = inCondition.iterator(); while (iterator.hasNext()){ String
         * state = (String) iterator.next(); traverse = traverse + state; if
         * (iterator.hasNext()){ traverse = traverse + ","; } } traverse =
         * traverse + ")"; return traverse; }
         *
         * @return DOCUMENT ME!
         */
        protected String getName() {
            return "SOME " + inCondition;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @author dcarr
     */
    class NoneClause extends Condition {
        private Set inCondition;

        /**
         * Initializes a new NoneClause object.
         *
         * @param inCondition DOCUMENT ME!
         */
        NoneClause(Set inCondition) {
            this.inCondition = inCondition;
        }

        /**
         * DOCUMENT ME!
         *
         * @param statefuls DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public boolean eval(Set statefuls, ConditionEvaluator condEval) {
            boolean result = true;
            Iterator iterator = statefuls.iterator();
            while (iterator.hasNext() && result) {
                result = !condEval.isInCondition(inCondition, iterator.next());
            }
            return result;
        }

        /**
         * public String toString(){ String traverse = "None("; Iterator
         * iterator = inCondition.iterator(); while (iterator.hasNext()){ String
         * state = (String) iterator.next(); traverse = traverse + state; if
         * (iterator.hasNext()){ traverse = traverse + ","; } } traverse =
         * traverse + ")"; return traverse; }
         *
         * @return DOCUMENT ME!
         */
        protected String getName() {
            return "NONE " + inCondition;
        }
    }
}
