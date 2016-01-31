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
import java.util.Map;
import java.util.Set;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 1/30/2016
 */
public class NegativeStateMachine implements Machine {

	private final SequenceFactory factory;

	private final String      machineId;
	private final String      startStateId;

	private final StateMachine negativeMachine;
	private final StateMachine positiveMachine;

	public static Machine create(String id, String expression, SequenceFactory factory, ParseDirection direction) {
		// Create the actual branch, the one we don't want to match
		StateMachine negative = StateMachine.create(id + 'N', expression, factory, direction);
		StateMachine positive = StateMachine.create(id + 'P', expression, factory, direction);

		// This is less elagant that I'd prefer, but bear with me:
		// We will extract the graph and id-machine map
		// and then the graph for *each* machine recursively.
		// We do this in order to replace each literal terminal
		// symbol with the literal dot (.) character
		buildPositiveBranch(factory, positive);

		return new NegativeStateMachine(id, negative, positive, factory);
	}

	private static void buildPositiveBranch(SequenceFactory factory, StateMachine positive) {
		Graph graph = positive.getGraph();
		Graph copy  = new Graph(graph);

		graph.clear();
		for (String key : copy.getKeys()) {
			for (Map.Entry<Sequence, Set<String>> entry : copy.get(key).entrySet()) {
				Sequence sequence = entry.getKey();
				Set<String> value = entry.getValue();
				// lambda / epsilon transition
				if(sequence == Sequence.EMPTY_SEQUENCE || sequence.equals(factory.getBorderSequence())) {
					graph.put(key, sequence, value);
				} else {
					graph.put(key, factory.getDotSequence(), value);
				}
			}
		}

		for (Machine machine : positive.getMachinesMap().values()) {
			if (machine instanceof StateMachine) {
				buildPositiveBranch(factory, (StateMachine) machine);
			} else if (machine instanceof NegativeStateMachine) {
				// Unclear if this is allowed to happen
				// or if this is the desired behavior
				buildPositiveBranch(factory, ((NegativeStateMachine) machine).negativeMachine);
			}
		}
	}

	private static Machine parsePositiveBranch(String id, String expression, SequenceFactory factoryParam, ParseDirection direction) {

		return null;
	}

	NegativeStateMachine(String id, StateMachine negative, StateMachine positive, SequenceFactory factoryParam) {
		//
		factory = factoryParam;
		machineId = id;
		startStateId = machineId + ":S";

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

	@Override
	public String toString() {
		return "NegativeStateMachine{" +
				"negativeMachine=" + negativeMachine +
				", positiveMachine=" + positiveMachine +
				'}';
	}
}
