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

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import org.didelphis.language.automata.expressions.Expression;
import org.didelphis.language.automata.matching.Match;
import org.didelphis.language.automata.parsing.SequenceParser;
import org.didelphis.language.automata.statemachines.StandardStateMachine;
import org.didelphis.language.automata.statemachines.StateMachine;
import org.didelphis.language.parsing.ParseDirection;
import org.didelphis.language.parsing.ParseException;
import org.didelphis.language.phonetic.SequenceFactory;
import org.didelphis.language.phonetic.sequences.Sequence;
import org.didelphis.structures.maps.GeneralMultiMap;
import org.didelphis.structures.maps.interfaces.MultiMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@EqualsAndHashCode
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Condition {

	String conditionText;
	StateMachine<Sequence> preCondition;
	StateMachine<Sequence> postCondition;

	public Condition(String condition, SequenceFactory factory) {
		this(condition, new VariableStore(), factory);
	}

	public Condition(String condition, VariableStore variables,
			SequenceFactory factory) {
		conditionText = condition;

		Map<String, Collection<Sequence>> map = new HashMap<>();
		for (String key : variables.getKeys()) {
			Collection<Sequence> collection = variables.get(key)
					.stream()
					.map(factory::toSequence)
					.collect(Collectors.toList());
			map.put(key, collection);
		}

		MultiMap<String, Sequence> multiMap =
				new GeneralMultiMap<>(HashMap.class, ArrayList.class, map);

		SequenceParser parser = new SequenceParser(factory, multiMap);

		// A condition must be entirely empty or contain exactly one _
		if (conditionText.contains("_") || condition.trim().isEmpty()) {
			String[] conditions = conditionText.split("_", -1);
			Expression empty = parser.parseExpression("");

			// note conditions should never have size 1 due to the behavior of
			// String::split when called with limit -1; so long as the condition
			// contains exactly one underscore character, the returned array
			// should be guaranteed to have a size of two
			if (conditions.length == 2) {
				Expression expression1 = parser.parseExpression(
						conditions[0],
						ParseDirection.BACKWARD
				);
				Expression expression2 = parser.parseExpression(
						conditions[1],
						ParseDirection.FORWARD
				);

				preCondition  = StandardStateMachine.create("X", expression1, parser);
				postCondition = StandardStateMachine.create("Y", expression2, parser);
			} else if (conditions.length == 0) {
				preCondition  = StandardStateMachine.create("M", empty, parser);
				postCondition = StandardStateMachine.create("M", empty, parser);
			} else {
				throw new ParseException("Condition with multiple _ characters");
			}
		} else {
			throw new ParseException("Condition without _");
		}
	}

	public boolean isMatch(Sequence word, int index) {
		return isMatch(word, index, index + 1);
	}

	/**
	 * Checks if this condition is applicable to the Sequence at the provided
	 * index
	 *
	 * @param word       the Sequence to check
	 * @param startIndex the first index of the targeted Sequence; cannot be
	 *                   negative
	 * @param endIndex   the last index of the targeted Sequence (exclusive);
	 *                   cannot be negative
	 *
	 * @return Returns true if the condition isMatch
	 */
	public boolean isMatch(Sequence word, int startIndex, int endIndex) {
		if (endIndex <= word.size() && startIndex <= endIndex) {
			Sequence sequence = word.getReverseSequence();
			int start = word.size() - startIndex;
			Match<Sequence> preMatch  = preCondition.match(sequence, start);
			Match<Sequence> postMatch = postCondition.match(word, endIndex);
			return preMatch.end() >= 0 && postMatch.end() >= 0;
		}
		return false;
	}

	@Override
	public String toString() {
		return conditionText;
	}

}
