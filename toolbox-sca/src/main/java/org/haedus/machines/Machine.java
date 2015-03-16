/******************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe                                  *
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.attribute.HashDocAttributeSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 3/7/2015
 */
public class Machine {

	public static final Machine EMPTY_MACHINE = new Machine();

	private static final transient Logger LOGGER = LoggerFactory.getLogger(StateMachine.class);

	private final SequenceFactory factory;

	private final String      machineId;
	private final String      startStateId;
	private final Set<String> acceptingStates;
	private final TwoKeyMap   graph; // String (Node ID), Sequence (Arc) --> String (Node ID)
	private final Map<String, Machine> machinesMap;

	private Machine() {
		machineId = "EMPTY-MACHINE";
		factory = SequenceFactory.getEmptyFactory();
		startStateId = null;
		acceptingStates = null;
		graph = null;
		machinesMap = null;
	}

	// For use with NodeFactory only
	Machine(String id, String expressionParam, SequenceFactory factoryParam, ParseDirection direction) {
		factory = factoryParam;
		machineId = id;

		List<String> strings = factory.getSegmentedString(expressionParam);
		List<Expression> expressions = parse(strings);

		machinesMap = new HashMap<String, Machine>();
		acceptingStates = new HashSet<String>();
		graph = new TwoKeyMap();

		startStateId = process(expressions, direction);
	}

	private String process(List<Expression> expressions, ParseDirection direction) {
		int nodeId = 0;
		String startId;
		if (expressions == null || expressions.isEmpty()) {
			startId = null;
		} else {
			if (direction == ParseDirection.BACKWARD) { Collections.reverse(expressions); }

			startId = "N-" + nodeId;


			String previousNode = startId;


			for (Expression expression : expressions) {
				String exp = expression.getExpression();
				String meta = expression.getMetacharacter();

				nodeId++;
				String currentNode = "N-" + nodeId;

				if (exp.startsWith("{")) {
					String substring = exp.substring(1, exp.length() - 1).trim();
//					previousNode;
				} else if (exp.startsWith("(")) {
					String substring = exp.substring(1, exp.length() - 1);
//					previousNode;
				} else {
					previousNode = constructTerminalNode(previousNode, currentNode, exp, meta);
				}
			}
		}
		return startId;
	}

//	private String constructRecursiveNode(String previousNode, String machineNode, String meta, boolean b) {
//		Node<Sequence> nextNode = NodeFactory.getNode(factory, b);
//		// currentNode contains the machine
//		if /****/ (meta.equals("?")) {
//			previousNode.add(machineNode);
//			machineNode.add(nextNode);
//			previousNode.add(nextNode);
//		} else if (meta.equals("*")) {
//			previousNode.add(machineNode);
//			machineNode.add(previousNode);
//			previousNode.add(nextNode);
//		} else if (meta.equals("+")) {
//			previousNode.add(machineNode);
//			machineNode.add(nextNode);
//			nextNode.add(previousNode);
//		} else {
//			previousNode.add(machineNode);
//			machineNode.add(nextNode);
//		}
//		previousNode = nextNode;
//		return previousNode;
//	}

	private String constructTerminalNode(String previousNode, String currentNode, String exp, String meta) {
		String referenceNode;

		Sequence sequence = factory.getSequence(exp);
		if (meta.equals("?")) {
//			previousNode.add(sequence, currentNode);
//			previousNode.add(currentNode);
			graph.put(previousNode, sequence, currentNode);
			graph.put(previousNode, Sequence.EMPTY_SEQUENCE, currentNode);
			referenceNode = currentNode;
		} else if (meta.equals("*")) {
//			currentNode.add(sequence, previousNode);
//			previousNode.add(currentNode);
			graph.put(previousNode, sequence, previousNode);
			graph.put(previousNode, Sequence.EMPTY_SEQUENCE, currentNode);
			referenceNode = previousNode;
		} else if (meta.equals("+")) {
//			previousNode.add(sequence, currentNode);
//			currentNode.add(previousNode);
			graph.put(previousNode, sequence, currentNode);
			graph.put(currentNode, Sequence.EMPTY_SEQUENCE, previousNode);
			referenceNode = currentNode;
		} else {
//			previousNode.add(sequence, currentNode);
			graph.put(previousNode, sequence, currentNode);
			referenceNode = currentNode;
		}
		return referenceNode;
	}

	private static Expression updateBuffer(Collection<Expression> list, Expression buffer) {
		list.add(buffer);
		return new Expression();
	}

	private static List<Expression> parse(Collection<String> strings) {
		List<Expression> list = new ArrayList<Expression>();
		if (!strings.isEmpty()) {

			Expression buffer = new Expression();
			for (String symbol : strings) {
				if (symbol.equals("*") || symbol.equals("?") || symbol.equals("+")) {
					buffer.setMetacharacter(symbol);
					buffer = updateBuffer(list, buffer);
				} else if (symbol.equals("!")) {
					// first in an expression
					buffer = updateBuffer(list, buffer);
					buffer.setNegative(true);
				} else {
					if (!buffer.getExpression().isEmpty()) {
						buffer = updateBuffer(list, buffer);
					}
					buffer.setExpression(symbol);
				}
			}
			if (!buffer.getExpression().isEmpty()) {
				list.add(buffer);
			}
		}
		return list;
	}

	public Collection<Integer> getMatchIndices(int startIndex, Sequence target) {
		Collection<Integer> indices = new HashSet<Integer>();

		if (startStateId == null) {
			indices.add(startIndex);
		} else {
			Sequence sequence = new Sequence(target);
			sequence.add(factory.getUnspecifiedSegment());
			// At the beginning of the process, we are in the start-state
			// so we find out what arcs leave the node.
			List<MatchState> states = new ArrayList<MatchState>();
			List<MatchState> swap = new ArrayList<MatchState>();
			// Add an initial state at the beginning of the sequence
			states.add(new MatchState(startIndex, startStateId));
			// if the condition is empty, it will always match
			while (!states.isEmpty()) {
				for (MatchState state : states) {

					String currentNode = state.getNode();
					int index = state.getIndex();
					// Check internal state machines
					Collection<Integer> matchIndices;
//					if (currentNode.containsStateMachine()) {
					if (machinesMap.containsKey(currentNode)) {
//						matchIndices = currentNode.getMatchIndices(index, target);
						matchIndices = new HashSet<Integer>();
					} else {
						matchIndices = new HashSet<Integer>();
						matchIndices.add(index);
					}

//					if (currentNode.isAccepting() || currentNode.isTerminal()) {
					if (acceptingStates.contains(currentNode) || !graph.contains(currentNode)) {
						indices.addAll(matchIndices);
					} else {
					for (Integer matchIndex : matchIndices) {
						Collection<MatchState> matchStates = updateSwapStates(sequence, currentNode, matchIndex);
						swap.addAll(matchStates);
					}
					}
				}
				states = swap;
				swap = new ArrayList<MatchState>();
			}
		}
		return indices;
	}

	private Collection<MatchState> updateSwapStates(Sequence testSequence, String currentNode, int index) {
		Sequence tail = testSequence.getSubsequence(index);

		Collection<MatchState> states = new HashSet<MatchState>();
//		graph.get(currentNode)

		Map<Sequence, Set<String>> sequenceSetMap = graph.get(currentNode);

		for (Map.Entry<Sequence, Set<String>> sequenceSetEntry : sequenceSetMap.entrySet()) {
//		for (Sequence symbol : graph.get(currentNode)) {
			Set<String> value = sequenceSetEntry.getValue();
			for (String nextNode : value) {

				Sequence key = sequenceSetEntry.getKey();
				if (key == Sequence.EMPTY_SEQUENCE) {
					states.add(new MatchState(index, nextNode));
				} else if (factory.hasVariable(key.toString())) {
					for (Sequence s : factory.getVariableValues(key.toString())) {
						if (tail.startsWith(s)) {
							states.add(new MatchState(index + s.size(), nextNode));
						}
					}
				} else if (tail.startsWith(key)) {
					states.add(new MatchState(index + key.size(), nextNode));
				}
				// Else: the pattern fails to match
			}
		}
		return states;
	}

	public boolean matches(int startIndex, Sequence target) {
		return false;
	}

	private static class TwoKeyMap {
		private final Map<String, Map<Sequence, Set<String>>> map;

		private TwoKeyMap() {
			map = new HashMap<String, Map<Sequence, Set<String>>>();
		}

		private Map<Sequence,Set<String>> get(String k1) {
			return map.get(k1);
		}

		private Set<String> get(String k1, Sequence k2) {
			return map.get(k1).get(k2);
		}

		private boolean contains(String k1) {
			return map.containsKey(k1);
		}

		private Set<Map.Entry<Sequence, Set<String>>> getEntries(String k1) {
			return map.get(k1).entrySet();
		}


		private Set<Sequence> getKeys(String k1) {
			return map.get(k1).keySet();
		}

		private void put(String k1, Sequence k2, String value) {
			Map<Sequence, Set<String>> innerMap;
			if (map.containsKey(k1)) {
				innerMap = map.get(k1);
			} else {
				innerMap = new HashMap<Sequence, Set<String>>();
			}

			Set<String> set;
			if (innerMap.containsKey(k2)) {
				set = innerMap.get(k2);
			} else {
				set = new HashSet<String>();
			}
			set.add(value);
			innerMap.put(k2, set);
			map.put(k1, innerMap);
		}

		private boolean contains (String k1, Sequence k2) {
			return map.containsKey(k1) && map.get(k1).containsKey(k2);
		}
	}

	private static final class MatchState {

		private final int    index; // Where in the sequence the cursor is
		private final String node;  // What node the cursor is currently on

		private MatchState(Integer i, String n) {
			index = i;
			node  = n;
		}

		public int getIndex() {
			return index;
		}

		public String getNode() {
			return node;
		}

		@Override
		public String toString() {
			return "[" + index + ',' + node + ']';
		}

		@Override
		public int hashCode() {
			return (13 + index) * (32 + node.hashCode());
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)                  { return false; }
			if (obj.getClass() != getClass()) { return false; }

			MatchState other = (MatchState) obj;
			return index == other.index &&
				node.equals(other.node);
		}
	}
}
