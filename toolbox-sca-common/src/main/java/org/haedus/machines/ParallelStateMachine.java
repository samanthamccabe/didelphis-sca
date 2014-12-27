package org.haedus.machines;

import org.haedus.datatypes.ParseDirection;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.SequenceFactory;

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

	public ParallelStateMachine(String id, String expression, SequenceFactory factoryParam, ParseDirection direction) {
		super(id, factoryParam);
		machines = new HashSet<Node<Sequence>>();

		int i = 0;
		for (String subexpression : WHITESPACE.split(expression)) {
			// TODO: does not split nested sets correctly, not that we need to allow it
			machines.add(new StateMachine(id + '-' + i, subexpression, factoryParam, direction));
			i++;
		}
	}

	@Override
	public boolean matches(int startIndex, Sequence target) {
		return !getMatchIndices(startIndex, target).isEmpty();
	}

	@Override
	public boolean containsStateMachine() {
		return machines.isEmpty();
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
	public boolean isAccepting() {
		return false;
	}

	@Override
	public void setAccepting(boolean acceptingParam) {
		throw new UnsupportedOperationException("Cannot set \"accepting\" value on immutable node");
	}
}
