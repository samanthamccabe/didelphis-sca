package org.haedus.machines;

import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.SequenceFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by samantha on 12/24/14.
 */
public class ParallelStateMachine extends AbstractStateMachine {

	private final Set<StateMachine> machines;

	public ParallelStateMachine(String expression, SequenceFactory factoryParam) {
		super(factoryParam);
		machines = new HashSet<StateMachine>();
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean matches(Sequence target) {
		return false;
	}

	@Override
	public void add(Node<Sequence> node) {

	}

	@Override
	public void add(Sequence arcValue, Node<Sequence> node) {

	}

	@Override
	public boolean hasArc(Sequence arcValue) {
		return false;
	}

	@Override
	public Collection<Node<Sequence>> getNodes(Sequence arcValue) {
		return null;
	}

	@Override
	public Collection<Sequence> getKeys() {
		return null;
	}

	@Override
	public boolean isAccepting() {
		return false;
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public void setAccepting(boolean acceptingParam) {

	}
}
