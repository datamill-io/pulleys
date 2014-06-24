package com.commercehub.core.state;

import java.util.Iterator;
import java.util.Set;

/**
 * This ConditionEvaluator returns true if any of a set of conditions is met.
 * 
 * @author mmiller
 * 
 */
public class InAnyStateConditionEvaluator implements ConditionEvaluator {
	/**
	 * Returns true if any of a set of conditions is met.
	 */
	public boolean isInCondition(Set condition, Object toEval) {
		boolean anyInState = false;
		if (toEval instanceof Stateful) {
		    Stateful stateful = (Stateful) toEval;
	        Iterator iterator = condition.iterator();
	        while (iterator.hasNext() && !anyInState) {
	            String checkState = (String) iterator.next();
	            anyInState = stateful.isInState(checkState);
	        }
		} else {
			throw new IllegalStateException("Conditional " +
					"Evaluator is being used for " + toEval.getClass() + 
					" but only handles Stateful Objects");
		}
		return anyInState;
	}
}
