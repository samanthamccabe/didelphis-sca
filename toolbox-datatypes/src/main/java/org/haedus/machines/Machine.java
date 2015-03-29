/**
 * ***************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe                                  *
 * *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 * http://www.apache.org/licenses/LICENSE-2.0                             *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 * ****************************************************************************
 */

package org.haedus.machines;

import org.haedus.enums.ParseDirection;
import org.haedus.phonetic.Sequence;
import org.haedus.phonetic.SequenceFactory;
import org.haedus.utils.graph.DisplayEdge;
import org.haedus.utils.graph.DisplayGroup;
import org.haedus.utils.graph.DisplayNode;
import org.haedus.utils.graph.GraphBuilder;
import org.haedus.utils.graph.NodeShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

	private final String               machineId;
	private final String               startStateId;
	private final Set<String>          acceptingStates;
	private final TwoKeyMap            graph; // String (Node ID), Sequence (Arc) --> String (Node ID)
	private final Set<String>          nodes;
	private final Map<String, Machine> machinesMap;

	private Machine() {
		machineId = "EMPTY-MACHINE";
		factory = SequenceFactory.getEmptyFactory();
		startStateId = null;
		acceptingStates = null;
		graph = null;
		machinesMap = null;
		nodes = null;
	}

	// For use with NodeFactory only
	Machine(String id, String expressionParam, SequenceFactory factoryParam, ParseDirection direction) {
		factory = factoryParam;
		machineId = id;
		startStateId = machineId + ":S";

		machinesMap = new HashMap<String, Machine>();
		acceptingStates = new HashSet<String>();
		nodes = new HashSet<String>();
		graph = new TwoKeyMap();


		List<Expression> expressions = factory.getExpressions(expressionParam);

		// TODO:
		if (expressions.size() == 1) {

		} else {

		}

	}

	private void process(String starId, Expression expression, ParseDirection direction) {

	}

	private void process(String startId, List<Expression> expressions, ParseDirection direction) {
		nodes.add(startId);

		if (direction == ParseDirection.BACKWARD) { Collections.reverse(expressions); }

		int nodeId = 0;
		String previousNode = startId;

		for (Iterator<Expression> it = expressions.iterator(); it.hasNext(); ) {
			Expression expression = it.next();

			nodeId++;

			String expr = expression.getExpression();
			String meta = expression.getMetacharacter();

			String currentNode = machineId + ':' + nodeId;
			if (!it.hasNext()) {
				acceptingStates.add(currentNode);
			}
			nodes.add(currentNode);

			if (expr.startsWith("(") || expr.startsWith("{")) {

				Machine machine = new Machine(currentNode, expr, factory, direction);
				machinesMap.put(currentNode, machine);

				String nextNode = machineId + ':' + (nodeId + 1);
				nodes.add(nextNode);

				previousNode = constructRecursiveNode(nextNode, previousNode, currentNode, meta);
			} else {
				previousNode = constructTerminalNode(previousNode, currentNode, expr, meta);
			}
		}
	}

	private String constructRecursiveNode(String nextNode, String previousNode, String machineNode, String meta) {

		if /****/ (meta.equals("?")) {
			graph.put(previousNode, Sequence.EMPTY_SEQUENCE, machineNode);
			graph.put(machineNode, Sequence.EMPTY_SEQUENCE, nextNode);
			graph.put(previousNode, Sequence.EMPTY_SEQUENCE, nextNode);
		} else if (meta.equals("*")) {
			graph.put(previousNode, Sequence.EMPTY_SEQUENCE, machineNode);
			graph.put(machineNode, Sequence.EMPTY_SEQUENCE, previousNode);
			graph.put(previousNode, Sequence.EMPTY_SEQUENCE, nextNode);
		} else if (meta.equals("+")) {
			graph.put(previousNode, Sequence.EMPTY_SEQUENCE, machineNode);
			graph.put(machineNode, Sequence.EMPTY_SEQUENCE, nextNode);
			graph.put(nextNode, Sequence.EMPTY_SEQUENCE, previousNode);
		} else {
			graph.put(previousNode, Sequence.EMPTY_SEQUENCE, machineNode);
			graph.put(machineNode, Sequence.EMPTY_SEQUENCE, nextNode);
		}
		return nextNode;
	}

	private String constructTerminalNode(String previousNode, String currentNode, String exp, String meta) {
		String referenceNode;

		Sequence sequence = factory.getSequence(exp);
		if (meta.equals("?")) {
			graph.put(previousNode, sequence, currentNode);
			graph.put(previousNode, Sequence.EMPTY_SEQUENCE, currentNode);
			referenceNode = currentNode;
		} else if (meta.equals("*")) {
			graph.put(previousNode, sequence, previousNode);
			graph.put(previousNode, Sequence.EMPTY_SEQUENCE, currentNode);
			referenceNode = currentNode;
		} else if (meta.equals("+")) {
			graph.put(previousNode, sequence, currentNode);
			graph.put(currentNode, Sequence.EMPTY_SEQUENCE, previousNode);
			referenceNode = currentNode;
		} else {
			graph.put(previousNode, sequence, currentNode);
			referenceNode = currentNode;
		}
		return referenceNode;
	}

	private static Expression updateBuffer(Collection<Expression> list, Expression buffer) {
		list.add(buffer);
		return new Expression();
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
					if (machinesMap.containsKey(currentNode)) {
						matchIndices = machinesMap.get(currentNode).getMatchIndices(index, target);
					} else {
						matchIndices = new HashSet<Integer>();
						matchIndices.add(index);
					}

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

		Map<Sequence, Set<String>> sequenceSetMap = graph.get(currentNode);

		for (Map.Entry<Sequence, Set<String>> sequenceSetEntry : sequenceSetMap.entrySet()) {
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

	public DisplayGroup getGroup() {
		DisplayGroup group = new DisplayGroup(machineId, machineId);

		Set<DisplayNode> displayNodes = getDisplayNodes();
		Set<DisplayEdge> displayEdges = getDisplayEdges();

		group.addAllNodes(displayNodes);
		group.addAllEdges(displayEdges);

		return group;
	}

	public String getGraph() {
		GraphBuilder builder = new GraphBuilder();

		Set<DisplayNode> displayNodes = getDisplayNodes();
		Set<DisplayEdge> displayEdges = getDisplayEdges();

		builder.addAllNodes(displayNodes);
		builder.addAllEdges(displayEdges);

		return builder.generateGraphML();
	}

	private Set<DisplayNode> getDisplayNodes() {
		Set<DisplayNode> displayNodes = new HashSet<DisplayNode>();
		for (String nodeId : nodes) {

			if (machinesMap.containsKey(nodeId)) {
				displayNodes.add(machinesMap.get(nodeId).getGroup());
			} else {

				DisplayNode node = new DisplayNode(nodeId, nodeId);

				if (acceptingStates.contains(nodeId)) {
					node.setFillColor1("#40C0C0");
					node.setShape(NodeShape.DIAMOND);
				} else if (startStateId.equals(nodeId)) {
					node.setFillColor1("#F04040");
					node.setShape(NodeShape.ELLIPSE);
				}
				displayNodes.add(node);
			}
		}
		return displayNodes;
	}

	private Set<DisplayEdge> getDisplayEdges() {
		int arcId = 1;
		Set<DisplayEdge> displayEdges = new HashSet<DisplayEdge>();
		for (String nodeId : graph.getKeys()) {
			Map<Sequence, Set<String>> map = graph.get(nodeId);

			for (Map.Entry<Sequence, Set<String>> entry : map.entrySet()) {
				Sequence key = entry.getKey();
				Set<String> value = entry.getValue();

				for (String targetId : value) {
					DisplayEdge edge = new DisplayEdge(machineId + ":arc-" + arcId, key.toString(), nodeId, targetId);
					displayEdges.add(edge);
					arcId++;
				}
			}
		}
		return displayEdges;
	}

	public boolean matches(int startIndex, Sequence target) {
		return false;
	}

	private static class TwoKeyMap {
		private final Map<String, Map<Sequence, Set<String>>> map;

		private TwoKeyMap() {
			map = new HashMap<String, Map<Sequence, Set<String>>>();
		}

		private Set<String> getKeys() {
			return map.keySet();
		}

		private Map<Sequence, Set<String>> get(String k1) {
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

		private boolean contains(String k1, Sequence k2) {
			return map.containsKey(k1) && map.get(k1).containsKey(k2);
		}
	}

	private static final class MatchState {

		private final int    index; // Where in the sequence the cursor is
		private final String node;  // What node the cursor is currently on

		private MatchState(Integer i, String n) {
			index = i;
			node = n;
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
			if (obj == null) { return false; }
			if (obj.getClass() != getClass()) { return false; }

			MatchState other = (MatchState) obj;
			return index == other.index &&
				node.equals(other.node);
		}
	}

	private static int getIndex(CharSequence string, char left, char right, int startIndex) {
		int count = 1;
		int endIndex = -1;

		boolean matched = false;
		for (int i = startIndex + 1; i <= string.length() && !matched; i++) {
			char ch = string.charAt(i);
			if (ch == right && count == 1) {
				matched = true;
				endIndex = i;
			} else if (ch == right) {
				count++;
			} else if (ch == left) {
				count--;
			}
		}
		return endIndex;
	}

	private static Collection<String> parseSubExpressions(String expressionParam) {
		Collection<String> subExpressions = new ArrayList<String>();

		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < expressionParam.length(); i++) {
			char c = expressionParam.charAt(i);
			/*  */
			if (c == '{') {
				int index = getIndex(expressionParam, '{', '}', i);
				buffer.append(expressionParam.substring(i, index));
				i = index - 1;
			} else if (c == '(') {
				int index = getIndex(expressionParam, '(', ')', i);
				buffer.append(expressionParam.substring(i, index));
				i = index - 1;
			} else if (c != ' ') {
				buffer.append(c);
			} else if (buffer.length() > 0) { // No isEmpty() call available
				subExpressions.add(buffer.toString());
				buffer = new StringBuilder();
			}
		}

		if (buffer.length() > 0) {
			subExpressions.add(buffer.toString());
		}
		return subExpressions;
	}
}
