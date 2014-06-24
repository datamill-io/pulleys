package com.commercehub.core.state;

import java.util.Set;

/**
 * A ConditionEvaluator is a Strategy for evaluation a set of states against
 * an object 
 * 
 * @author mmiller
 *
 */
public interface ConditionEvaluator {

	/**
	 * Returns true if the condition (a set of Strings representing states)
	 * is true for this object.
	 * 
	 * These strings represent states, but do not necessarily deal with "state" 
	 * as defined in the State object.  They may be simply things that should be
	 * said to be true about an object for it to be in a given condition.  
	 * States only make sense when coupled with a given 
	 * 
	 * @param condition a Set of Strings representing states
	 * 
	 * @return true
	 */
	boolean isInCondition(Set<String> condition, Object toEval);
}
