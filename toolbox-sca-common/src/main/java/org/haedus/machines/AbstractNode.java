package org.haedus.machines;

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

	protected static int getIndex(List<String> list, String left, String right, int startIndex) {
		int count = 1;
		int endIndex = -1;

		boolean matched = false;
		for (int i = startIndex + 1; i < list.size() && !matched; i++) {
			String ch = list.get(i);
			if (ch.equals(right) && count == 1) {
				matched = true;
				endIndex = i;
			} else if (ch.equals(right)) {
				count++;
			} else if (ch.equals(left)) {
				count--;
			}
		}
		return endIndex;
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

	protected static List<Expression> parse(List<String> expression, ParseDirection direction) {
		List<Expression> list = new ArrayList<Expression>();
		if (!expression.isEmpty()) {

			Expression buffer = new Expression();
			for (int i = 0; i < expression.size();) {
				String symbol = expression.get(i);
				if (symbol.equals("*") || symbol.equals("?") || symbol.equals("+")) {
					buffer.setMetacharacter(symbol);
					buffer = updateBuffer(list, buffer);
					i++;
				} else if (symbol.equals("!")) {
					// first in an expression
					buffer = updateBuffer(list, buffer);
					buffer.setNegative(true);
					i++;
				} else {
					if (!buffer.getExpression().isEmpty()) {
						buffer = updateBuffer(list, buffer);
					}
					/*****/if(symbol.equals("{")) {
						int endIndex = getIndex(expression, "{", "}", i);
						List<String> expParam = expression.subList(i, endIndex+1);
						buffer.setExpression(expParam);
						i = endIndex + 1;
					} else if (symbol.equals("(")) {
						int endIndex = getIndex(expression, "(", ")", i);
						List<String> expParam = expression.subList(i, endIndex+1);
						buffer.setExpression(expParam);
						i = endIndex + 1;
					} else {
						buffer.setExpression(expression.subList(i, i+1));
						i++;
					}
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
				List<String> exp = thing.getExpression();
				if (exp.get(0).equals("{")) {
//					String internal = CURLY_BRACES_PATTERN.matcher(exp).replaceAll("$1");
					List<Expression> subExpression = expressions.subList(1, expressions.size()-1);
					current = NodeFactory.getParallelStateMachine(subExpression, factory, direction, false); // Never accepting?
//					exp = null; // Ideally, we shouldn't do this
				} else if (exp.get(0).equals("(")) {
//					String internal = PARENTHESIS_PATTERN.matcher(exp).replaceAll("$1");
					List<Expression> subExpression = expressions.subList(1, expressions.size()-1);
					current = NodeFactory.getStateMachine(subExpression, factory, direction, false); // Never accepting?
//					exp = null; // Ideally, we shouldn't do this
				} else {
					current = NodeFactory.getNode(factory, !it.hasNext());
				}
				// Construct nodes
				String meta = thing.getMetacharacter();
				if (exp == null) {
					previous = constructRecursiveNode(previous, current, meta, !it.hasNext());
				} else {
					previous = constructTerminalNode(previous, current, exp, meta);
				}
			}
		}
		return node;
	}

	protected Node<Sequence> constructTerminalNode(Node<Sequence> previousNode, Node<Sequence> currentNode, List<String> exp, String meta) {
		Node<Sequence> referenceNode;
		if (exp.size() > 1) {
			throw new IllegalArgumentException("Expression has unexpected size > 1 " + exp);
		}
		Sequence sequence = factory.getSequence(exp.get(0));
		if /****/ (meta.equals("?")) {
			previousNode.add(sequence, currentNode);
			previousNode.add(currentNode);
			referenceNode = currentNode;
		} else if (meta.equals("*")) {
			previousNode.add(currentNode);
			currentNode.add(sequence, previousNode);
			// Don't change "previous" to current
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

	/*
	protected List<Thing> parseExpression(String expressionParam) {

		Matcher matcher = ILLEGAL_START_PATTERN.matcher(expressionParam);
		if (matcher.lookingAt()) {
			throw new ParseException("Expression stared with an illegal character: " + matcher.group(1) + " in " + expressionParam);
		}

		List<Thing> list = new ArrayList<Thing>();
		Thing buffer = new Thing();
		for (int i = 0; i < expressionParam.length(); ) {
			char ch = expressionParam.charAt(i);
			if (ch == '*' || ch == '?' || ch == '+') {
				// Last in an expression
				buffer.setMetacharacter(ch);
				buffer = updateBuffer(list, buffer);
				i++;
			} else if (ch == '!') {
				// first in an expression
				buffer = updateBuffer(list, buffer);
				buffer.setNegative(true);
				i++;
			} else {
				if (!buffer.getExpression().isEmpty()) {
					buffer = updateBuffer(list, buffer);
				}
				String tail = expressionParam.substring(i);
				String best = factory.getBestMatch(tail);
				// Choose appropriate string
				if (best.isEmpty()) {
					if (tail.startsWith("{")) {
						int endIndex = getIndex(expressionParam, '{', '}', i);
						best = expressionParam.substring(i, endIndex + 1);
					} else if (tail.startsWith("(")) {
						int endIndex = getIndex(expressionParam, '(', ')', i);
						best = expressionParam.substring(i, endIndex + 1);
					} else {
						best = expressionParam.substring(i, i + 1);
					}
				}
				buffer.setExpression(best);
				i += best.length();
			}
		}
		if (!buffer.getExpression().isEmpty()) {
			list.add(buffer);
		}
		return list;
	}
*/

	private Node<Sequence> constructRecursiveNode(Node<Sequence> previousNode, Node<Sequence> machineNode, String meta, boolean b) {
		Node<Sequence> nextNode = NodeFactory.getNode(factory, b);
		// currentNode contains the machine
		if /****/ (meta.equals("?")) {
			// P --> M --> N
			previousNode.add(machineNode);
			machineNode.add(nextNode);
			// P --> N
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
