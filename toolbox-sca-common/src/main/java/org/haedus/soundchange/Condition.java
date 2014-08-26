/*******************************************************************************
 * Copyright (c) 2014 Haedus - Fabrica Codicis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.haedus.soundchange;

import org.haedus.datatypes.phonetic.FeatureModel;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.VariableStore;
import org.haedus.machines.StateMachine;
import org.haedus.soundchange.exceptions.RuleFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: Samantha Fiona Morrigan McCabe
 * Date: 4/28/13
 * Time: 2:28 PM
 */
public class Condition {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(Condition.class);

	private final FeatureModel  featureModel;
	private final String        conditionText;
	private final StateMachine  preCondition;
	private final StateMachine  postCondition;
	private final VariableStore variableStore;

	public Condition() {
		conditionText = "";
		preCondition = new StateMachine();
		postCondition = new StateMachine();
		variableStore = new VariableStore();
		featureModel = new FeatureModel();
	}

	// package-private: testing only
	Condition(String condition) throws RuleFormatException {
		this(condition, new VariableStore());
	}

	public Condition(String condition, VariableStore variables, FeatureModel model) throws RuleFormatException {
		conditionText = cleanup(condition);
		variableStore = variables;
		featureModel = model;

		if (conditionText.contains("_")) {
			String[] conditions = conditionText.split("_");
			if (conditions.length == 1) {
				preCondition = new StateMachine(conditions[0], variableStore, false);
				postCondition = new StateMachine();
			} else if (conditions.length == 2) {
				preCondition = new StateMachine(conditions[0], variableStore, false);
				postCondition = new StateMachine(conditions[1], variableStore, true);
			} else if (conditions.length == 0) {
				postCondition = new StateMachine();
				preCondition = new StateMachine();
			} else {
				throw new RuleFormatException("Malformed Condition, multiple _ characters");
			}
		} else {
			throw new RuleFormatException("Malformed Condition, no _ character");
		}
	}

	public Condition(String condition, VariableStore variables) throws RuleFormatException {
		this(condition, variables, new FeatureModel());
	}


	public StateMachine getPostCondition() {
		return postCondition;
	}

	public StateMachine getPreCondition() {
		return preCondition;
	}

	@Override
	public String toString() {
		return conditionText;
	}

	private String cleanup(String s) {
		return s.replaceAll("\\s+", " ")
		        .replaceAll("([\\[\\{\\(]) ", "$1")
		        .replaceAll(" ([\\]\\}\\)])", "$1");
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

			preconditionMatch  = preCondition.matches(head.getReverseSequence());
			postconditionMatch = postCondition.matches(tail);
		}
		return preconditionMatch && postconditionMatch;
	}

	public boolean isEmpty() {
		return preCondition.isEmpty() && postCondition.isEmpty();
	}
}




