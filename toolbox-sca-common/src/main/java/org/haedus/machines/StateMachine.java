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

import org.haedus.datatypes.SegmentationMode;
import org.haedus.datatypes.phonetic.FeatureModel;
import org.haedus.datatypes.Segmenter;
import org.haedus.datatypes.phonetic.Segment;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.VariableStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Samantha Fiona Morrigan McCabe
 * 11/10/13.
 */
public class StateMachine {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(StateMachine.class);

	private final Node             startNode;
	private final VariableStore    variableStore;
	private final FeatureModel     featureModel;
	private final SegmentationMode segmentationMode;

	public StateMachine() {
		variableStore    = new VariableStore();
		startNode        = new Node(0);
		featureModel     = new FeatureModel();
		segmentationMode = SegmentationMode.DEFAULT;
	}

	public StateMachine(String expression, FeatureModel model, VariableStore store, SegmentationMode modeParam, boolean isForward) {
		variableStore    = store;
		featureModel     = model;
		segmentationMode = modeParam;
		
		startNode = getNodeFromExpression(expression, isForward);
	}

    // Determines if the Sequence is accepted by this machine
	public boolean matches(Sequence sequence) {

		sequence.add(new Segment("#", featureModel.getFeaturesNaN(), featureModel));
		// At the beginning of the process, we are in the start-state
		// so we find out what arcs leave the node.
		List<MatchState> states = new ArrayList<MatchState>();
		List<MatchState> swap   = new ArrayList<MatchState>();
		// Add an initial state at the beginning of the sequence
		states.add(new MatchState(0, startNode));
		// if the condition is empty, it will always match
		boolean match = startNode.isEmpty();
		while (!match && !states.isEmpty()) {
			for (MatchState state : states) {

				Node currentNode = state.getNode();
				int index = state.getIndex();

				if (!currentNode.isAccepting()) {
					updateSwapStates(sequence, swap, currentNode, index);
				} else {
					match = true;
					break;
				}
			}
			states = swap;
			swap = new LinkedList<MatchState>();
		}
		return match;
	}

	public boolean isEmpty() {
		return startNode.isEmpty();
	}

    private void updateSwapStates(Sequence testSequence, Collection<MatchState> swap, Node currentNode, int index) {
        Sequence tail = testSequence.getSubsequence(index);

        for (Sequence symbol : currentNode.getKeys()) {
            for (Node nextNode : currentNode.getNodes(symbol)) {
                if (variableStore.contains(symbol)) {
	                addStateFromVariable(swap, index, tail, symbol, nextNode);
                } else if (tail.startsWith(symbol)) {
                    swap.add(new MatchState(index + symbol.size(), nextNode));
                } else if (symbol.isEmpty()) {
                    swap.add(new MatchState(index, nextNode));
                }
            }
        }
    }

	// Checks of the tail starts with a symbol in the variable store
	private void addStateFromVariable(Collection<MatchState> swap, int index, Sequence tail, Sequence symbol, Node nextNode) {
		for (Sequence s : variableStore.get(symbol)) {
		    if (tail.startsWith(s)) {
		        swap.add(new MatchState(index + s.size(), nextNode));
		    }
		}
	}

	//
	private Node getNodeFromExpression(String string, boolean isForward) {
		Collection<String> keys = new ArrayList<String>();
		
		keys.addAll(variableStore.getKeys());
		keys.addAll(featureModel.getSymbols());
		
		List<String> list = Segmenter.getSegmentedString(string, keys, segmentationMode);

		Node root;
		if (list.isEmpty()) {
			root = NodeFactory.getEmptyNode();
		} else {
			root = NodeFactory.getNode();
			Expression ex   = new Expression(list);
			Node       last = parse(ex, root, isForward);
			last.setAccepting(true);
			LOGGER.trace(ExpressionUtil.getGML(ex));
		}
		return root;
	}

    //
	private Node parse(Expression expression, Node root, boolean forward) {
		Node current = root;

		if (expression.isParallel()) {
            Node tail = NodeFactory.getNode();
            if (expression.isNegative()) {
                for (Expression ex : expression.getSubExpressions(forward)) {
                    Node next = getNode(forward, current, ex);
                    next.add(tail);
                }
            } else {

                for (Expression ex : expression.getSubExpressions(forward)) {
                    Node next = getNode(forward, current, ex);
                    next.add(tail);
                }
                current = tail;
            }
		} else {
			for (Expression ex : expression.getSubExpressions(forward)) {
				current = getNode(forward, current, ex);
			}
		}
		return current;
	}

	/**
	 * Processes an Expression into a state machine
	 * @param forward determines if the Expression will be parsed forwards (left-to-right)
	 * @param start the starting node of the machine
	 * @param ex the Expression we wish to process
	 * @return the last node in the machine.
	 */
	private Node getNode(boolean forward, Node start, Expression ex) {

		if (ex.isTerminal()) {
			String element = ex.getString();
			Sequence sequence = Segmenter.getSequence(element, featureModel, variableStore, segmentationMode);

			if (ex.isOptional() && ex.isRepeatable()) {
				start.add(sequence, start);
			} else {
				Node next = NodeFactory.getNode();
				start.add(sequence, next);
				if (ex.isRepeatable())
					next.add(start);
				else if (ex.isOptional())
					start.add(next);
				start = next;
			}
		} else {
			// This provides the start and end states of our machine
			Node next = NodeFactory.getNode();
			Node last = parse(ex, next, forward);

			start.add(next);

			if (ex.isOptional() && ex.isRepeatable()) {
				last.add(start);
				Node alpha = NodeFactory.getNode();
				start.add(alpha);
				start = alpha;
			} else {
				if (ex.isRepeatable())
					last.add(start);
				else if (ex.isOptional())
					next.add(last);
				start = last;
			}
		}
		return start;
	}

	/**
	 * Utility class for matching strings
	 */
	private final class MatchState {

		private final Integer index; // Where in the sequence the cursor is
		private final Node    node;  // What node the cursor is currently on

		private MatchState(Integer i, Node n) {
			index = i;
			node  = n;
		}

		public int getIndex() {
			return index;
		}

		public Node getNode() {
			return node;
		}
		
		@Override
		public String toString() {
			return "<" + index + ", " + node.getId() + ">";
		}
		
		@Override
		public int hashCode() {
			return 13 * index.hashCode() * node.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)                  return false;
			if (obj.getClass() != getClass()) return false;

			MatchState other = (MatchState) obj;
			return index == other.getIndex() &&
			       node.equals(other.getNode());
		}
	}
}
