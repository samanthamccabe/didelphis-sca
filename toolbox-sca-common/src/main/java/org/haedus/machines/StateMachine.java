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

package org.haedus.machines;

import org.haedus.datatypes.ParseDirection;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.SequenceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Samantha Fiona Morrigan McCabe
 * 11/10/13.
 */
public class StateMachine extends AbstractNode {

	public static final StateMachine EMPTY_MACHINE = new StateMachine();

	private static final transient Logger LOGGER = LoggerFactory.getLogger(StateMachine.class);

	private final String         expression;
	private final Node<Sequence> startNode;

	private StateMachine() {
		super("S-EMPTY", null, true); // degenerate machines are always accepting
		startNode  = null;
		expression = "";
	}

	// For use with NodeFactory only
	StateMachine(String id, String expressionParam, SequenceFactory factoryParam, ParseDirection direction, boolean isAccepting) {
		super(id, factoryParam, isAccepting);
		startNode  = getStartNode(expressionParam, direction);
		expression = expressionParam;
	}

	/**
	 * Processes an Expression into a state machine
	 * @param start the starting node of the machine
	 * @param ex the Expression we wish to process
	 * @param direction determines if the Expression will be parsed forwards (left-to-right)
	 * @return the last node in the machine.
	 */
	private Node<Sequence> getNode(Node<Sequence> start, Expression ex, ParseDirection direction) {

		if (ex.isTerminal()) {
			String element = ex.getString();
			Sequence sequence = factory.getSequence(element);

			if (ex.isOptional() && ex.isRepeatable()) {
				start.add(sequence, start);
			} else {
				Node<Sequence> next = NodeFactory.getNode(factory);
				start.add(sequence, next);
				if (ex.isRepeatable()) {
					next.add(start);
				} else if (ex.isOptional()) {
					start.add(next);
				}
				start = next;
			}
		} else {
			// This provides the start and end states of our machine
			Node<Sequence> next = NodeFactory.getNode(factory);
			Node<Sequence> last = parse(ex, next, direction);

			start.add(next);

			if (ex.isOptional() && ex.isRepeatable()) {
				last.add(start);
				Node<Sequence> alpha = NodeFactory.getNode(factory);
				start.add(alpha);
				start = alpha;
			} else {
				if (ex.isRepeatable()) {
					last.add(start);
				} else if (ex.isOptional()) {
					next.add(last);
				}
				start = last;
			}
		}
		return start;
	}

	// Determines if the Sequence is accepted by this machine
	@Override
	public boolean matches(int startIndex, Sequence sequence) {
		return !getMatchIndices(startIndex, sequence).isEmpty();
	}

	@Override
	public boolean containsStateMachine() {
		return startNode != null;
	}

	@Override
	public String toString() {
		return getId() + ' ' + expression;
	}

	@Override
	public Collection<Integer> getMatchIndices(int startIndex, Sequence target) {

		Collection<Integer> indices = new HashSet<Integer>();
		if (startNode == null) {
			indices.add(startIndex);
		} else {
			Sequence sequence = new Sequence(target);
			sequence.add(factory.getBoundarySegment());
			// At the beginning of the process, we are in the start-state
			// so we find out what arcs leave the node.
			List<MatchState> states = new ArrayList<MatchState>();
			List<MatchState> swap   = new ArrayList<MatchState>();
			// Add an initial state at the beginning of the sequence
			states.add(new MatchState(startIndex, startNode));
			// if the condition is empty, it will always match
			while (!states.isEmpty()) {
				for (MatchState state : states) {

					Node<Sequence> currentNode = state.getNode();
					int index = state.getIndex();
						// Check internal state machines
						Collection<Integer> matchIndices;
						if (currentNode.containsStateMachine()) {
							matchIndices = currentNode.getMatchIndices(index, target);
						} else {
							matchIndices = new HashSet<Integer>();
							matchIndices.add(index);
						}

						if(currentNode.isAccepting() || currentNode.isTerminal()) {
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

    private Collection<MatchState> updateSwapStates(Sequence testSequence, Node<Sequence> currentNode, int index) {
        Sequence tail = testSequence.getSubsequence(index);

		Collection<MatchState> states = new HashSet<MatchState>();

        for (Sequence symbol : currentNode.getKeys()) {
            for (Node<Sequence> nextNode : currentNode.getNodes(symbol)) {
	            if (symbol == null || symbol.isEmpty()) {
		            states.add(new MatchState(index, nextNode));
	            } else if (factory.hasVariable(symbol.toString())) {
					for (Sequence s : factory.getVariableValues(symbol.toString())) {
						if (tail.startsWith(s)) {
							states.add(new MatchState(index + s.size(), nextNode));
						}
					}
				} else if (tail.startsWith(symbol)) {
                    states.add(new MatchState(index + symbol.size(), nextNode));
                }
            }
        }
		return states;
    }

    //
	private Node<Sequence> parse(Expression expressionParam, Node<Sequence> root, ParseDirection direction) {
		Node<Sequence> current = root;

		if (expressionParam.isParallel()) {
			Node<Sequence> tail = NodeFactory.getNode(factory);
            if (expressionParam.isNegative()) {
                for (Expression ex : expressionParam.getSubExpressions(direction)) {
                    Node<Sequence> next = getNode(current, ex, direction);
                    next.add(tail);
                }
            } else {

                for (Expression ex : expressionParam.getSubExpressions(direction)) {
                    Node<Sequence> next = getNode(current, ex, direction);
                    next.add(tail);
                }
                current = tail;
            }
		} else {
			for (Expression ex : expressionParam.getSubExpressions(direction)) {
				current = getNode(current, ex, direction);
			}
		}
		return current;
	}

	private static final class MatchState {

		private final int            index; // Where in the sequence the cursor is
		private final Node<Sequence> node;  // What node the cursor is currently on

		private MatchState(Integer i, Node<Sequence> n) {
			index = i;
			node  = n;
		}

		public int getIndex() {
			return index;
		}

		public Node<Sequence> getNode() {
			return node;
		}

		@Override
		public String toString() {
			return "<<" + index + ", " + node.toString() + ">>";
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
