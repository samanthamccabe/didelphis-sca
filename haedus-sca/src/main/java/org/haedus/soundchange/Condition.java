package org.haedus.soundchange;

import org.haedus.datatypes.phonetic.Segment;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.VariableStore;
import org.haedus.machines.StateMachine;
import org.haedus.soundchange.exceptions.RuleFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: goats
 * Date: 4/28/13
 * Time: 2:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class Condition {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(Condition.class);

	private final String        conditionText;
	private final StateMachine  preCondition;
	private final StateMachine  postCondition;
	private final VariableStore variableStore;

	public Condition() {
		conditionText = "";
		preCondition  = new StateMachine();
		postCondition = new StateMachine();
		variableStore = new VariableStore();
	}

	public Condition(String condition) throws RuleFormatException
	{
		this(condition, new VariableStore());
	}

	public Condition(String condition, VariableStore variables) throws RuleFormatException {
		condition = cleanup(condition);
		conditionText = condition;
		variableStore = variables;

		if (condition.contains("_")) {
			String[] conditions = condition.split("_");
			if (conditions.length == 1) {
				preCondition  = new StateMachine(conditions[0], variableStore, false);
				postCondition = new StateMachine();
			} else if (conditions.length == 2) {
				preCondition  = new StateMachine(conditions[0], variableStore, false);
				postCondition = new StateMachine(conditions[1], variableStore, true);
			} else if (conditions.length == 0) {
				postCondition = new StateMachine();
				preCondition  = new StateMachine();
			} else {
				throw new RuleFormatException("Malformed Condition, multiple _ characters");
			}
		} else {
			throw new RuleFormatException("Malformed Condition, no _ character");
		}
	}

	@Override
	public String toString() {
		return conditionText;
	}

	private String cleanup(String s) {
		s = s.replaceAll("\\s+", " ");
		s = s.replaceAll("([\\[\\{\\(]) ", "$1");
		s = s.replaceAll(" ([\\]\\}\\)])", "$1");
		return s;
	}

	public boolean isMatch(Sequence word, int index) {
		return isMatch(word, index, index + 1);
	}

	/**
	 * Checks if this condition is applicable to the Sequence at the provided index
	 * @param word       the Sequence to check
	 * @param startIndex the first index of the targeted Sequence; cannot be negative
	 * @param endIndex   the last index of the targeted Sequence (exclusive); cannot be negative
	 * @return Returns true if the condition isMatch
	 */
	public boolean isMatch(Sequence word, int startIndex, int endIndex) {
		boolean preconditionMatch = false;
		boolean postconditionMatch = false;

		if (endIndex <= word.size() && startIndex < endIndex) {
			Sequence head = word.getSubsequence(0, startIndex);
			Sequence tail = word.getSubsequence(endIndex);

			head.addFirst(new Segment("#"));
			tail.add(new Segment("#"));

			preconditionMatch = preCondition.matches(head.getReverseSequence());
			postconditionMatch = postCondition.matches(tail);
		}
		return preconditionMatch && postconditionMatch;
	}

	public boolean isEmpty() {
		return preCondition.isEmpty() && postCondition.isEmpty();
	}
}




