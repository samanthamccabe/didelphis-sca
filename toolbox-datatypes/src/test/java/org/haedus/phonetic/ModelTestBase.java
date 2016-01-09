package org.haedus.phonetic;

import org.haedus.enums.FormatterMode;

import java.io.InputStream;

/**
 * Created by samantha on 10/10/15.
 */
public class ModelTestBase {

	protected static SequenceFactory loadFactory(String resourceName, FormatterMode mode) {
		return new SequenceFactory(loadModel(resourceName, mode), mode);
	}

	protected static FeatureModel loadModel(String resourceName, FormatterMode mode) {
		InputStream stream = SegmentTest.class.getClassLoader().getResourceAsStream(resourceName);
		return new FeatureModel(stream, mode);
	}
}
