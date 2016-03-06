/******************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe                                  *
 * *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 * http://www.apache.org/licenses/LICENSE-2.0                             *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 ******************************************************************************/

package org.haedus.phonetic;

import org.haedus.enums.FormatterMode;
import org.haedus.phonetic.model.FeatureModel;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 2/14/2015
 */
public class SegmentTest {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(SequenceTest.class);

	private static final SequenceFactory FACTORY = loadArticulatorTheoryModel();

	private static SequenceFactory loadArticulatorTheoryModel() {
		InputStream stream = SegmentTest.class.getClassLoader().getResourceAsStream("AT_hybrid.model");
		FormatterMode mode = FormatterMode.INTELLIGENT;
		return new SequenceFactory(new FeatureModel(stream, mode), mode);
	}
	
	@Test
	public void testUnderspecifiedSegment01() {
		String string = "[-continuant, release:1]";
		Segment received = FACTORY.getSegment(string);
		FeatureModel model = FACTORY.getFeatureModel();

		List<Double> array = model.getUnderspecifiedArray();
		array.set(1, -1.0);
		array.set(3, 1.0);

		Segment expected = FACTORY.getSegment("[-continuant, release:1]");

		assertEquals(expected, received);
	}

	@Test
	public void testMatch01() {
		Segment segmentA = FACTORY.getSegment("a");

		Segment segmentP = FACTORY.getSegment("p");
		Segment segmentT = FACTORY.getSegment("t");
		Segment segmentK = FACTORY.getSegment("k");

		Segment received = FACTORY.getSegment("[-continuant, -son]");

		assertTrue(segmentP.matches(received));
		assertTrue(segmentT.matches(received));
		assertTrue(segmentK.matches(received));

		assertTrue(received.matches(segmentP));
		assertTrue(received.matches(segmentT));
		assertTrue(received.matches(segmentK));

		assertFalse(segmentA.matches(received));
		assertFalse(received.matches(segmentA));
	}

	@Test
	public void testMatch02() {
		Segment a = FACTORY.getSegment("a");
		Segment n = FACTORY.getSegment("n");

		assertFalse("a matches n", a.matches(n));
		assertFalse("n matches a", n.matches(a));
	}

	@Test
	public void testMatch03() {
		Segment segment = FACTORY.getSegment("[-con, -hgh, +frn, -atr, +voice]");

		Segment a = FACTORY.getSegment("a");

		assertTrue(a.matches(segment));
		assertTrue(segment.matches(a));
	}

	@Test
	public void testMatch04() {
		Segment x = FACTORY.getSegment("x");
		Segment e = FACTORY.getSegment("e");

		assertFalse(e.matches(x));
		assertFalse(x.matches(e));
	}

	@Test
	public void testOrdering01() {
		Segment p = FACTORY.getSegment("p");
		Segment b = FACTORY.getSegment("b");

		// These differ only by voicing, where p is -2 and b is 0
		LOGGER.info("{} {}", p, p.getFeatures());
		LOGGER.info("{} {}", b, b.getFeatures());

		assertTrue(p.compareTo(b) == -1);
	}

	@Test
	public void testOrdering02() {
		Segment p = FACTORY.getSegment("p");
		Segment t = FACTORY.getSegment("t");

		LOGGER.info("{} {}", p, p.getFeatures());
		LOGGER.info("{} {}", t, t.getFeatures());

		assertTrue(p.compareTo(t) == 1);
	}
}
