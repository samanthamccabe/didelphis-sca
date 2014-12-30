package org.haedus.machines;

import org.haedus.datatypes.ParseDirection;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.SequenceFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by samantha on 12/24/14.
 */
public class ParallelStateMachine extends AbstractNode {

	private static final Pattern WHITESPACE = Pattern.compile("\\s+");

	private final Set<Node<Sequence>> machines;

	ParallelStateMachine(String id, String expressionParam, SequenceFactory factoryParam, ParseDirection direction, boolean acceptingParam) {
		super(id, factoryParam, acceptingParam);
		machines = new HashSet<Node<Sequence>>();

		Collection<String> subExpressions = parseSubExpressions(expressionParam);
		for (String subExpression : subExpressions) {
			machines.add(NodeFactory.getStateMachine(subExpression, factoryParam, direction, false));
		}
	}

	private static Collection<String> parseSubExpressions(String expressionParam) {
		Collection<String> subExpressions = new ArrayList<String>();

		StringBuilder buffer = new StringBuilder();
		for (int i =0; i < expressionParam.length();) {
			char c = expressionParam.charAt(i);
			/*  */ if (c == '{') {
				int index = getIndex(expressionParam, '{','}', i);
				buffer.append(expressionParam.substring(i, index));
			} else if (c == '(') {
				int index = getIndex(expressionParam, '(',')', i);
				buffer.append(expressionParam.substring(i, index));
			} else if (c != ' ') {
				buffer .append(c);
				i++;
			} else if (buffer.length() > 0) { // No isEmpty() call available
				subExpressions.add(buffer.toString());
				buffer = new StringBuilder();
				i++;
			}
		}

		if (buffer.length() > 0) {
			subExpressions.add(buffer.toString());
		}
		return subExpressions;
	}

	@Override
	public boolean matches(int startIndex, Sequence target) {
		return !getMatchIndices(startIndex, target).isEmpty();
	}

	@Override
	public boolean containsStateMachine() {
		return !machines.isEmpty();
	}

	@Override
	public Collection<Integer> getMatchIndices(int startIndex, Sequence target) {
		Collection<Integer> indices = new HashSet<Integer>();
		for (Node<Sequence> machine : machines) {
			indices.addAll(machine.getMatchIndices(startIndex, target));
		}
		return indices;
	}

	@Override
	public String toString() {
		return getId() + machines;
	}
}
