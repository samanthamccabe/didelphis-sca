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

package org.didelphis.soundchange;

import org.didelphis.language.enums.ParseDirection;
import org.didelphis.language.exceptions.ParseException;
import org.didelphis.language.machines.EmptyStateMachine;
import org.didelphis.language.machines.StandardStateMachine;
import org.didelphis.language.machines.interfaces.StateMachine;
import org.didelphis.language.machines.sequences.SequenceMatcher;
import org.didelphis.language.machines.sequences.SequenceParser;
import org.didelphis.language.phonetic.SequenceFactory;
import org.didelphis.language.phonetic.sequences.BasicSequence;
import org.didelphis.language.phonetic.sequences.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.regex.Pattern;

/**
 * User: Samantha Fiona Morrigan McCabe
 * Date: 4/28/13
 * Time: 2:28 PM
 */
public class Condition<T> {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(Condition.class);

	private static final Pattern WHITESPACE_PATTERN  = Pattern.compile("\\s+");
	private static final Pattern OPEN_BRACE_PATTERN  = Pattern.compile("([\\[{(]) ");
	private static final Pattern CLOSE_BRACE_PATTERN = Pattern.compile(" ([]})])");

	private final String  conditionText;
	private final StateMachine<Sequence<T>> preCondition;
	private final StateMachine<Sequence<T>> postCondition;

	public Condition(String condition, SequenceFactory<T> sequenceFactory) {
		conditionText = cleanup(condition);

		SequenceParser<T> parser = new SequenceParser<>(sequenceFactory);
		SequenceMatcher<T> matcher = new SequenceMatcher<>(parser);

		if (conditionText.contains("_")) {
			String[] conditions = conditionText.split("_", -1);
			if (conditions.length == 1) {
				preCondition  = StandardStateMachine.create("M", conditions[0], parser, matcher, ParseDirection.BACKWARD);
				postCondition = EmptyStateMachine.getInstance();
			} else if (conditions.length == 2) {
				preCondition  = StandardStateMachine.create("X", conditions[0], parser, matcher, ParseDirection.BACKWARD);
				postCondition = StandardStateMachine.create("Y", conditions[1], parser, matcher, ParseDirection.FORWARD);
			} else if (conditions.length == 0) {
				preCondition  = EmptyStateMachine.getInstance();
				postCondition = EmptyStateMachine.getInstance();
			} else {
				throw new ParseException("Malformed Condition, multiple _ characters", condition);
			}
		} else {
			throw new ParseException("Malformed Condition, no _ character", condition);
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

	public boolean isMatch(Sequence<T> word, int index) {
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
	public boolean isMatch(Sequence<T> word, int startIndex, int endIndex) {
		boolean preconditionMatch  = false;
		boolean postconditionMatch = false;

		if (endIndex <= word.size() && startIndex <= endIndex) {
			Sequence<T> head = word.subsequence(0, startIndex);
			Sequence<T> tail = word.subsequence(endIndex);
			Sequence<T> reverse = new BasicSequence<>(head);

			Collections.reverse(reverse);
			
			preconditionMatch  = !preCondition.getMatchIndices(0, reverse).isEmpty();
			postconditionMatch = !postCondition.getMatchIndices(0, tail).isEmpty();
		}
		return preconditionMatch && postconditionMatch;
	}

}
