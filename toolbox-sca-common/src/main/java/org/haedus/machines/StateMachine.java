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
import org.haedus.exceptions.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Samantha Fiona Morrigan McCabe
 * 11/10/13.
 */
public class StateMachine extends AbstractNode {

	public static final StateMachine EMPTY_MACHINE = new StateMachine();

	private static final transient Logger LOGGER = LoggerFactory.getLogger(StateMachine.class);

	public static final  Pattern ILLEGAL_START_PATTERN = Pattern.compile("^([\\$\\^\\*\\?\\+\\)\\}\\]\\\\])");
	public static final  Pattern METACHARACTER_PATTERN = Pattern.compile("^[\\*\\?\\+]");
	private static final Pattern PARENTHESIS_PATTERN   = Pattern.compile("\\(\\s*(.*)\\s*\\)");
	private static final Pattern CURLY_BRACES_PATTERN  = Pattern.compile("\\{\\s*(.*)\\s*\\}");

	private final String inputExpression;
	private final Node<Sequence> startNode;
	private final boolean        isAccepting;

	private StateMachine() {
		super("S-EMPTY", null);
		isAccepting = true;
		startNode = null;
		inputExpression = "";
	}

	public StateMachine(String id, String expression, SequenceFactory factoryParam, ParseDirection direction) {
		super(id, factoryParam);

		isAccepting = false; // ok maybe i failed to understand how this works.
		startNode = parseExpression(expression, direction);
		inputExpression = expression;
	}

	private Node<Sequence> parseExpression(String expression, ParseDirection direction) {
		// Parse out each top-level expression
		int count = 0;
		Node<Sequence> node;
		if (expression == null || expression.isEmpty()) {
			node = null;
		} else {
			node = NodeFactory.getNode(factory, false);

			List<Thing> things = parseExpression(expression);
			if (direction == ParseDirection.BACKWARD) { Collections.reverse(things); }

			Node<Sequence> previousNode = node;
			for (Thing thing : things) {
				Node<Sequence> currentNode;
				String exp = thing.expression;
				if (exp.startsWith("{")) {
					String internal = CURLY_BRACES_PATTERN.matcher(exp).replaceAll("$1");
					currentNode = new ParallelStateMachine("P-" + count, internal, factory, direction);
				} else if (exp.startsWith("(")) {
					String internal = PARENTHESIS_PATTERN.matcher(exp).replaceAll("$1");
					currentNode = new StateMachine("S-" + count, internal, factory, direction);
				} else {
					currentNode = NodeFactory.getNode(factory);
				}

				char meta = thing.metacharacter;
				if /****/ (meta == '?') {
					previousNode.add(factory.getSequence(exp), currentNode);
					previousNode.add(currentNode);
					previousNode = currentNode;
				} else if (meta == '*') {
					previousNode.add(factory.getSequence(exp), currentNode);
					currentNode.add(previousNode);
					// Don't change "previous" to current
				} else if (meta == '+') {
					previousNode.add(factory.getSequence(exp), currentNode);
					currentNode.add(previousNode);
					previousNode = currentNode;
				} else {
					previousNode.add(factory.getSequence(exp), currentNode);
					previousNode = currentNode;
				}
			}
		}

		return node;
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

	private List<Thing> parseExpression(String expression) {

		Matcher matcher = ILLEGAL_START_PATTERN.matcher(expression);
		if (matcher.lookingAt()) {
			throw new ParseException("Expression stared with an illegal character: " + matcher.group(1) + " in " + expression);
		}

		List<Thing> list = new ArrayList<Thing>();
		Thing buffer = new Thing();
		for (int i = 0; i < expression.length(); ) {
			char ch = expression.charAt(i);
			if (ch == '*' || ch == '?' || ch == '+') {
				// Last in an expressio
				buffer.metacharacter = ch;
				buffer = updateBuffer(list, buffer);
				i++;
			} else if (ch == '!') {
				// first in an expression
				buffer = updateBuffer(list, buffer);
				buffer.negative = true;
				i++;
			} else {
				if (!buffer.expression.isEmpty() ) {
					buffer = updateBuffer(list, buffer);
				}

				String tail = expression.substring(i);
				String best = factory.getBestMatch(tail);
				if (best.isEmpty()) {
					if (tail.startsWith("{")) {
						int endIndex = getIndex(expression, '{', '}', i);
						best = expression.substring(i, endIndex + 1);
					} else if (tail.startsWith("(")) {
						int endIndex = getIndex(expression, '(', ')', i);
						best = expression.substring(i, endIndex + 1);
					} else {
						best = expression.substring(i, i + 1);
					}
				}
				buffer.expression = best;
				i += best.length();
			}
		}
		list.add(buffer);
		return list;
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
		return getId() + ' ' + inputExpression;
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
		states.add(new MatchState(0, startNode));
		// if the condition is empty, it will always match
		while (!states.isEmpty()) {
			for (MatchState state : states) {

				Node<Sequence> currentNode = state.getNode();
				int index = state.getIndex();

				if (!currentNode.isAccepting() && !currentNode.isEmpty()) {
					updateSwapStates(sequence, swap, currentNode, index);
				}
				else {
//					match = true;
//					break;
					indices.add(index);
				}
			}
			states = swap;
			swap = new ArrayList<MatchState>();
		}}
		return indices;
	}


	@Override
	public boolean isAccepting() {
		return false;
	}

	@Override
	public void setAccepting(boolean acceptingParam) {
		throw new UnsupportedOperationException("Attempt to set an immutable state-machine node as \"accepting\"!");
	}

    private void updateSwapStates(Sequence testSequence, Collection<MatchState> swap, Node<Sequence> currentNode, int index) {
        Sequence tail = testSequence.getSubsequence(index);

        for (Sequence symbol : currentNode.getKeys()) {
            for (Node<Sequence> nextNode : currentNode.getNodes(symbol)) {
	            if (symbol == null || symbol.isEmpty()) {
		            swap.add(new MatchState(index, nextNode));
	            } else if (factory.hasVariable(symbol.toString())) {
		            addStateFromVariable(swap, index, tail, symbol, nextNode);
	            } else if (tail.startsWith(symbol)) {
                    swap.add(new MatchState(index + symbol.size(), nextNode));
                }
            }
        }
    }

	// Checks of the tail starts with a symbol in the variable store
	private void addStateFromVariable(Collection<MatchState> swap, int index, Sequence tail, Sequence symbol, Node<Sequence> nextNode) {
		for (Sequence s : factory.getVariableValues(symbol.toString())) {
		    if (tail.startsWith(s)) {
		        swap.add(new MatchState(index + s.size(), nextNode));
		    }
		}
	}

//	private Node<Sequence> getNodeFromExpression(String string, ParseDirection direction) {
//		List<String> list = sequenceFactory.getSegmentedString(string);
//		Node<Sequence> root;
//		if (list.isEmpty()) {
//			root = NodeFactory.getEmptyNode();
//		} else {
//			root = NodeFactory.getNode();
//			Expression ex   = new Expression(list);
//			Node<Sequence> last = parse(ex, root, direction);
//			last.setAccepting(true);
//		}
//		return root;
//	}

    //
	private Node<Sequence> parse(Expression expression, Node<Sequence> root, ParseDirection direction) {
		Node<Sequence> current = root;

		if (expression.isParallel()) {
			Node<Sequence> tail = NodeFactory.getNode(factory);
            if (expression.isNegative()) {
                for (Expression ex : expression.getSubExpressions(direction)) {
                    Node<Sequence> next = getNode(current, ex, direction);
                    next.add(tail);
                }
            } else {

                for (Expression ex : expression.getSubExpressions(direction)) {
                    Node<Sequence> next = getNode(current, ex, direction);
                    next.add(tail);
                }
                current = tail;
            }
		} else {
			for (Expression ex : expression.getSubExpressions(direction)) {
				current = getNode(current, ex, direction);
			}
		}
		return current;
	}

	private static Thing updateBuffer(Collection<Thing> list, Thing buffer) {
		list.add(buffer);
		return new Thing();
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
			return '<' + index + ", " + node.getId() + '>';
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

	private static final class Thing {
		private String expression = "";
		private char    metacharacter;
		private boolean negative;

		@Override
		public String toString() {
			return (negative ? "!" : "") + expression + (metacharacter != 0 ? metacharacter : "");
		}
	}
}
