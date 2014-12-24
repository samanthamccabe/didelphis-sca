package org.haedus.machines;

import org.haedus.datatypes.SegmentationMode;
import org.haedus.datatypes.phonetic.FeatureModel;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.SequenceFactory;
import org.haedus.datatypes.phonetic.VariableStore;

import java.util.Collection;

/**
 * Created by samantha on 12/24/14.
 */
public class NegativeStateMachine extends AbstractStateMachine {

	private final StateMachine machine;

	public NegativeStateMachine(StateMachine machineParam, SequenceFactory factoryParam) {
		super(factoryParam);
		machine = machineParam;
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
