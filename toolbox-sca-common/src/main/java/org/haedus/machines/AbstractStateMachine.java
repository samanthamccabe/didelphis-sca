package org.haedus.machines;

import org.haedus.datatypes.SegmentationMode;
import org.haedus.datatypes.phonetic.FeatureModel;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.SequenceFactory;
import org.haedus.datatypes.phonetic.VariableStore;

/**
 * Created by samantha on 12/24/14.
 */
public abstract class AbstractStateMachine implements Node<Sequence> {

	protected final SequenceFactory sequenceFactory;

	protected AbstractStateMachine(SequenceFactory factoryParam) {
		sequenceFactory = factoryParam;
	}

	protected AbstractStateMachine(FeatureModel model, VariableStore store, SegmentationMode modeParam) {
		sequenceFactory = new SequenceFactory(model, store, modeParam);
	}
}
