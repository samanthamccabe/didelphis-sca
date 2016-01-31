/******************************************************************************
 * Copyright (c) 2016. Samantha Fiona McCabe                                  *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *     http://www.apache.org/licenses/LICENSE-2.0                             *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 ******************************************************************************/

package org.haedus.machines;

import org.haedus.enums.ParseDirection;
import org.haedus.phonetic.Sequence;
import org.haedus.phonetic.SequenceFactory;

import java.util.Collection;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 1/30/2016
 */
public class NegativeStateMachine implements Machine {

	private final Machine negativeMachine;
	private final Machine positiveMachine;

	public static Machine create(String id, String expression, SequenceFactory factoryParam, ParseDirection direction) {
		// Create the actual branch, the one we don't want to match
		Machine negative = StateMachine.create(id + 'N', expression, factoryParam, direction);
		Machine positive = parsePositiveBranch(id + 'P', expression, factoryParam, direction);

		return new NegativeStateMachine(negative, positive);
	}

	private static Machine parsePositiveBranch(String id, String expression, SequenceFactory factoryParam, ParseDirection direction) {

		return null;
	}

	NegativeStateMachine(Machine negative, Machine positive) {
		positiveMachine = positive;
		negativeMachine = negative;
	}

	@Override
	public Collection<Integer> getMatchIndices(int startIndex, Sequence target) {

		Collection<Integer> positiveIndices = positiveMachine.getMatchIndices(startIndex, target);
		Collection<Integer> negativeIndices = negativeMachine.getMatchIndices(startIndex, target);

		// Complement --- remove negatives from positives
		positiveIndices.removeAll(negativeIndices);

		return positiveIndices;
	}
}
