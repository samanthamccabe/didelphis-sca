package org.haedus.machines;

import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.SequenceFactory;

import java.util.Collection;

/**
 * Created by samantha on 12/24/14.
 */
public class NegativeStateMachine extends AbstractNode {

	private final Node<Sequence> machine;

	public NegativeStateMachine(String id, String expression, SequenceFactory factoryParam) {
		super(id, factoryParam);
		machine = null; // TODO: parse this
	}

	@Override
	public boolean matches(int startIndex, Sequence target) {
		return false;
	}

	@Override
	public boolean containsStateMachine() {
		return true;
	}

	@Override
	public Collection<Integer> getMatchIndices(int startIndex, Sequence target) {
		return null;
	}


	@Override
	public boolean isAccepting() {
		return false;
	}

	@Override
	public void setAccepting(boolean acceptingParam) {

	}
}
