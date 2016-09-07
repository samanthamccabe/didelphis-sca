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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 1/30/2016
 */
public class NegativeStateMachine implements Machine {

	private final StateMachine negativeMachine;
	private final StateMachine positiveMachine;

	public static Machine create(String id, String expression, SequenceFactory factory, ParseDirection direction) {
		// Create the actual branch, the one we don't want to match
		StateMachine negative = StateMachine.create(id + 'N', expression, factory, direction);
		StateMachine positive = StateMachine.create(id + 'P', expression, factory, direction);

		// This is less elegant that I'd prefer, but bear with me:
		// We will extract the graph and id-machine map
		// and then the graph for *each* machine recursively.
		// We do this in order to replace each literal terminal
		// symbol with the literal dot (.) character
		buildPositiveBranch(factory, positive);

		return new NegativeStateMachine(negative, positive);
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
				} else if (factory.hasVariable(sequence.toString())) {
					Collection<Integer> lengths = new HashSet<Integer>();
					for (Sequence segments : factory.getVariableValues(sequence.toString())) {
						lengths.add(segments.size());
					}
					Sequence dot = factory.getDotSequence();
					for (Integer length : lengths) {
						buildDotChain(graph, key, value, length, dot);
					}
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

	NegativeStateMachine(StateMachine negative, StateMachine positive) {
		positiveMachine = positive;
		negativeMachine = negative;
	}

	@Override
	public Collection<Integer> getMatchIndices(int startIndex, Sequence target) {

		TreeSet<Integer> positiveIndices = positiveMachine.getMatchIndices(startIndex, target);
		TreeSet<Integer> negativeIndices = negativeMachine.getMatchIndices(startIndex, target);

		if (!negativeIndices.isEmpty()) {
			int positive = positiveIndices.last();
			int negative = negativeIndices.last();
			return positive != negative ? positiveIndices : new HashSet<Integer>();
		} else if (!positiveIndices.isEmpty()) {
			return positiveIndices;
		} else {
			return new HashSet<Integer>();
		}

		/* This is left here as reference; not used because this method
		 * is not greedy

		// Complement --- remove negatives from positives
		positiveIndices.removeAll(negativeIndices);
		return positiveIndices;

		*/
	}

	@Override
	public String toString() {
		return "NegativeStateMachine{" +
				"negativeMachine=" + negativeMachine +
				", positiveMachine=" + positiveMachine +
				'}';
	}

	private static void buildDotChain(Graph graph, String key, Set<String> endValues, int length, Sequence dot) {
		String thisState = key;
		for (int i = 0; i < length - 1; i++) {
			String nextState = key + '-' + i;
			graph.put(thisState, dot, nextState);
			thisState = nextState;
		}
		graph.put(thisState, dot, endValues);
	}
}
