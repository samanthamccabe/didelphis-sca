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
import org.haedus.utils.graph.GraphBuilder;
import org.haedus.utils.graph.edge.DisplayEdge;
import org.haedus.utils.graph.node.DisplayGroup;
import org.haedus.utils.graph.node.DisplayNode;
import org.haedus.utils.graph.node.NodeStyleBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 4/7/2015
 */
public abstract class AbstractMachine implements Machine {

	protected final SequenceFactory factory;

	protected final String                       machineId;
	protected final String                       startStateId;
	protected final Set<String>                  acceptingStates;
	protected final Set<String>                  nodes;
	protected final Map<String, AbstractMachine> machinesMap;

	private final TwoKeyMap graph; // String (Node ID), Sequence (Arc) --> String (Node ID)

	// For use with NodeFactory only
	protected AbstractMachine(String id, SequenceFactory factoryParam, ParseDirection direction) {
		factory = factoryParam;
		machineId = id;
		startStateId = machineId + ":S";

		machinesMap = new HashMap<String, AbstractMachine>();
		acceptingStates = new HashSet<String>();
		nodes = new HashSet<String>();
		graph = new TwoKeyMap();
	}

	@Override
	public Collection<Integer> getMatchIndices(int startIndex, Sequence target) {
		Collection<Integer> indices = new HashSet<Integer>();

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
		return indices;
	}


	protected String constructRecursiveNode(String nextNode, String previousNode, String machineNode, String meta) {

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

	protected String constructTerminalNode(String previousNode, String currentNode, String exp, String meta) {
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


	public String getGraph() {
		GraphBuilder builder = new GraphBuilder();

		Set<DisplayNode>  displayNodes  = getDisplayNodes();
		Set<DisplayEdge>  displayEdges  = getDisplayEdges();
		Set<DisplayGroup> displayGroups = getDisplayGroups();

		builder.addAllNodes(displayNodes);
		builder.addAllGroups(displayGroups);
		builder.addAllEdges(displayEdges);


		return builder.generateGraphML();
	}

	private Set<DisplayNode> getDisplayNodes() {
		Set<DisplayNode> displayNodes = new HashSet<DisplayNode>();
		for (String nodeId : nodes) {
			if (!machinesMap.containsKey(nodeId)) {
				DisplayNode node = new DisplayNode(nodeId, nodeId);

				NodeStyleBuilder builder = new NodeStyleBuilder();
				if (acceptingStates.contains(nodeId)) {

//				node.setFillColor1("#40C0C0");
//				node.setShape(NodeShape.DIAMOND);
				} else if (startStateId.equals(nodeId)) {
//				node.setFillColor1("#F04040");
//				node.setShape(NodeShape.ELLIPSE);
				} else {

				}
				displayNodes.add(node);
			}
		}
		return displayNodes;
	}

	private Set<DisplayGroup> getDisplayGroups() {
		Set<DisplayGroup> groups = new HashSet<DisplayGroup>();
		for (Map.Entry<String, AbstractMachine> entry : machinesMap.entrySet()) {
			DisplayGroup group = entry.getValue().getGroup();
			groups.add(group);
		}
		return groups;
	}

	public DisplayGroup getGroup() {
		DisplayGroup group = new DisplayGroup(machineId, machineId);

		Set<DisplayNode>  displayNodes = getDisplayNodes();
		Set<DisplayEdge>  displayEdges = getDisplayEdges();
		Set<DisplayGroup> displayGroups = getDisplayGroups();

		// TODO: update this
		group.addAllNodes(displayNodes);
		group.addAllGroups(displayGroups);
		group.addAllEdges(displayEdges);

		return group;
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

	private  static final class MatchState {

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
}
