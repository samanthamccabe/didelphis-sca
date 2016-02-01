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
import org.haedus.enums.ParseDirection;
import org.haedus.machines.Machine;
import org.haedus.machines.StateMachine;
import org.haedus.phonetic.Sequence;
import org.haedus.phonetic.SequenceFactory;
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
	private final Machine  preCondition;
	private final Machine postCondition;

	// package-private: testing only
	Condition(String condition) {
		this(condition, SequenceFactory.getEmptyFactory());
	}

	public Condition(String condition, SequenceFactory factoryParam) {
		conditionText = cleanup(condition);
		if (conditionText.contains("_")) {
			String[] conditions = conditionText.split("_");
			if (conditions.length == 1) {
				preCondition  = StateMachine.create("M", conditions[0], factoryParam, ParseDirection.BACKWARD);
				postCondition = StateMachine.EMPTY_MACHINE;
			} else if (conditions.length == 2) {
				preCondition  = StateMachine.create("X", conditions[0], factoryParam, ParseDirection.BACKWARD);
				postCondition = StateMachine.create("Y", conditions[1], factoryParam, ParseDirection.FORWARD);
			} else if (conditions.length == 0) {
				preCondition  = StateMachine.EMPTY_MACHINE;
				postCondition = StateMachine.EMPTY_MACHINE;
			} else {
				LOGGER.error("Malformed Condition, multiple _ characters in condition: {}", condition);
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

	private static String cleanup(String string) {
		string = WHITESPACE_PATTERN.matcher(string).replaceAll(" ");
		string = OPEN_BRACE_PATTERN.matcher(string).replaceAll("$1");
		return CLOSE_BRACE_PATTERN.matcher(string).replaceAll("$1");
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

		if (endIndex <= word.size() && startIndex <= endIndex) {
			Sequence head = word.getSubsequence(0, startIndex);
			Sequence tail = word.getSubsequence(endIndex);

			preconditionMatch  = !preCondition.getMatchIndices(0, head.getReverseSequence()).isEmpty();
			postconditionMatch = !postCondition.getMatchIndices(0, tail).isEmpty();
		}
		return preconditionMatch && postconditionMatch;
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




