/******************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe                                  *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *     http://www.apache.org/licenses/LICENSE-2.0                             *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 ******************************************************************************/

package org.haedus.phonetic;

import org.haedus.enums.FormatterMode;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 2/14/2015
 */
public class SegmentTest {

	private static FeatureModel model;
	private static SequenceFactory factory;

	@BeforeClass
	public static void init() throws IOException {
		Resource resource = new ClassPathResource("features.model");
		model = new FeatureModel(resource.getFile());
		factory = new SequenceFactory(model, FormatterMode.INTELLIGENT);
	}

	@Test
	public void testMatch01() {
		Segment segmentP = factory.getSegment("p");
		Segment segmentT = factory.getSegment("t");
		Segment segmentK = factory.getSegment("k");

		Segment received = model.getSegmentFromFeatures("[-continuant, release:1]");

		assertTrue(segmentP.matches(received));
		assertTrue(segmentT.matches(received));
		assertTrue(segmentK.matches(received));

		assertFalse(received.matches(segmentP));
		assertFalse(received.matches(segmentT));
		assertFalse(received.matches(segmentK));
	}
}
