/*******************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.haedus.machines;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.haedus.datatypes.ParseDirection;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.SequenceFactory;

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
 * Created by samantha on 12/24/14.
 */
public abstract class AbstractNode implements Node<Sequence> {

	protected final SequenceFactory factory;

	private final String  nodeId;
	private final boolean accepting;

	private final Map<Sequence, Set<Node<Sequence>>> arcs;

	protected AbstractNode(String idParam, SequenceFactory factoryParam, boolean acceptingParam) {
		factory = factoryParam;
		nodeId = idParam;
		accepting = acceptingParam;
		arcs = new HashMap<Sequence, Set<Node<Sequence>>>();
	}

	protected static int getIndex(CharSequence string, char left, char right, int startIndex) {
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

	private static Expression updateBuffer(Collection<Expression> list, Expression buffer) {
		list.add(buffer);
		return new Expression();
	}

	@Override
	public String getId() {
		return nodeId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		AbstractNode that = (AbstractNode) o;
		return nodeId.equals(that.getId()) && arcs.equals(that.arcs);
	}

	@Override
	public int hashCode() {
		int result = nodeId.hashCode();
		result = 31 * result + (accepting ? 1 : 0);
		result = 31 * result + arcs.keySet().hashCode();
		return result;
	}

	@Override
	public void add(Sequence arcValue, Node<Sequence> node) {
		Set<Node<Sequence>> someNodes;
		if (arcs.containsKey(arcValue)) {
			someNodes = arcs.get(arcValue);
			if (!someNodes.contains(node)) {
				someNodes.add(node);
			}
		} else {
			someNodes = new HashSet<Node<Sequence>>();
			someNodes.add(node);
		}
		arcs.put(arcValue, someNodes);
	}

	@Override
	public boolean isTerminal() {
		return arcs.isEmpty();
	}

	@Override
	public void add(Node<Sequence> node) {
		add(null, node);
	}

	@Override
	public boolean hasArc(Sequence arcValue) {
		return arcs.containsKey(arcValue);
	}

	@Override
	public Collection<Node<Sequence>> getNodes(Sequence arcValue) {
		return arcs.get(arcValue);
	}

	@Override
	public Collection<Sequence> getKeys() {
		return arcs.keySet();
	}

	@Override
	public boolean isAccepting() {
		return accepting;
	}

	protected static List<Expression> parse(Collection<String> strings) {
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

	protected Node<Sequence> getStartNode(List<Expression> expressions, ParseDirection direction) {
		// Parse out each top-level expression
		Node<Sequence> node;
		if (expressions == null || expressions.isEmpty()) {
			node = null;
		} else {
			if (direction == ParseDirection.BACKWARD) { Collections.reverse(expressions); }

			node = NodeFactory.getNode(factory, false);
			Node<Sequence> previous = node;
			for (Iterator<Expression> it = expressions.iterator(); it.hasNext(); ) {
				Expression thing = it.next();
				Node<Sequence> current;
				String exp = thing.getExpression();
				String meta = thing.getMetacharacter();

				if (exp.startsWith("{")) {
					String substring = exp.substring(1, exp.length() - 1).trim();
					current = NodeFactory.getParallelStateMachine(substring, factory, direction, false); // Never accepting?
					previous = constructRecursiveNode(previous, current, meta, !it.hasNext());
				} else if (exp.startsWith("(")) {
					String substring = exp.substring(1, exp.length() - 1);
					current = NodeFactory.getStateMachine(substring, factory, direction, false); // Never accepting?
					previous = constructRecursiveNode(previous, current, meta, !it.hasNext());
				} else {
					current = NodeFactory.getNode(factory, !it.hasNext());
					previous = constructTerminalNode(previous, current, exp, meta);
				}
			}
		}
		return node;
	}

	protected Node<Sequence> constructTerminalNode(Node<Sequence> previousNode, Node<Sequence> currentNode, String exp, String meta) {
		Node<Sequence> referenceNode;

		Sequence sequence = factory.getSequence(exp);
		if (meta.equals("?")) {
			previousNode.add(sequence, currentNode);
			previousNode.add(currentNode);
			referenceNode = currentNode;
		} else if (meta.equals("*")) {
			previousNode.add(currentNode);
			currentNode.add(sequence, previousNode);
			referenceNode = previousNode;
		} else if (meta.equals("+")) {
			previousNode.add(sequence, currentNode);
			currentNode.add(previousNode);
			referenceNode = currentNode;
		} else {
			previousNode.add(sequence, currentNode);
			referenceNode = currentNode;
		}
		return referenceNode;
	}

	private Node<Sequence> constructRecursiveNode(Node<Sequence> previousNode, Node<Sequence> machineNode, String meta, boolean b) {
		Node<Sequence> nextNode = NodeFactory.getNode(factory, b);
		// currentNode contains the machine
		if /****/ (meta.equals("?")) {
			previousNode.add(machineNode);
			machineNode.add(nextNode);
			previousNode.add(nextNode);
		} else if (meta.equals("*")) {
			previousNode.add(machineNode);
			machineNode.add(previousNode);
			previousNode.add(nextNode);
		} else if (meta.equals("+")) {
			previousNode.add(machineNode);
			machineNode.add(nextNode);
			nextNode.add(previousNode);
		} else {
			previousNode.add(machineNode);
			machineNode.add(nextNode);
		}
		previousNode = nextNode;
		return previousNode;
	}
}
