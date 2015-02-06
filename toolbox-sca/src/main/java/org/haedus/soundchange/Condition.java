/*******************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.haedus.soundchange;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.haedus.phonetic.Sequence;
import org.haedus.phonetic.SequenceFactory;
import org.haedus.machines.Node;
import org.haedus.enums.ParseDirection;
import org.haedus.machines.NodeFactory;
import org.haedus.machines.StateMachine;
import org.haedus.soundchange.exceptions.RuleFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * User: Samantha Fiona Morrigan McCabe
 * Date: 4/28/13
 * Time: 2:28 PM
 */
public class Condition {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(Condition.class);

	private static final Pattern WHITESPACE_PATTERN  = Pattern.compile("\\s+");
	private static final Pattern OPEN_BRACE_PATTERN  = Pattern.compile("([\\[\\{\\(]) ");
	private static final Pattern CLOSE_BRACE_PATTERN = Pattern.compile(" ([\\]\\}\\)])");

	private final String         conditionText;
	private final Node<Sequence> preCondition;
	private final Node<Sequence> postCondition;

	// package-private: testing only
	Condition(String condition) {
		this(condition, SequenceFactory.getEmptyFactory());
	}

	public Condition(String condition, SequenceFactory factoryParam) {
		conditionText = cleanup(condition);
		if (conditionText.contains("_")) {
			String[] conditions = conditionText.split("_");
			if (conditions.length == 1) {
				preCondition  = NodeFactory.getStateMachine(conditions[0], factoryParam, ParseDirection.BACKWARD, true);
				postCondition = StateMachine.EMPTY_MACHINE;
			} else if (conditions.length == 2) {
				preCondition  = NodeFactory.getStateMachine(conditions[0], factoryParam, ParseDirection.BACKWARD, true);
				postCondition = NodeFactory.getStateMachine(conditions[1], factoryParam, ParseDirection.FORWARD,  true);
			} else if (conditions.length == 0) {
				preCondition  = StateMachine.EMPTY_MACHINE;
				postCondition = StateMachine.EMPTY_MACHINE;
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

	private static String cleanup(String s) {
		return CLOSE_BRACE_PATTERN.matcher(OPEN_BRACE_PATTERN.matcher(WHITESPACE_PATTERN.matcher(s).replaceAll(" ")).replaceAll("$1")).replaceAll("$1");
	}

	public boolean isMatch(Sequence word, int index) {
		return isMatch(word, index, index + 1);
	}

	/**
	 * Checks if this condition is applicable to the Sequence at the provided index
	 *
	 * @param word       the Sequence to check
	 * @param startIndex the first index of the targeted Sequence; cannot be negative
	 * @param endIndex   the last index of the targeted Sequence (exclusive); cannot be negative
	 * @return Returns true if the condition isMatch
	 */
	public boolean isMatch(Sequence word, int startIndex, int endIndex) {
		boolean preconditionMatch  = false;
		boolean postconditionMatch = false;

		if (endIndex <= word.size() && startIndex < endIndex) {
			Sequence head = word.getSubsequence(0, startIndex);
			Sequence tail = word.getSubsequence(endIndex);

			preconditionMatch  = preCondition.matches(0, head.getReverseSequence());
			postconditionMatch = postCondition.matches(0, tail);
		}
		return preconditionMatch && postconditionMatch;
	}

	public boolean isEmpty() {
		return preCondition.isTerminal() && postCondition.isTerminal();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		Condition rhs = (Condition) obj;
		return new EqualsBuilder()
				.append(preCondition, rhs.preCondition)
				.append(postCondition, rhs.postCondition)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(preCondition)
				.append(postCondition)
				.toHashCode();
	}
}




