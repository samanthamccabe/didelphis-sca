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

	private static final SequenceFactory FACTORY_NF = loadNumericalFeatureModel();
	private static final SequenceFactory FACTORY_AT = loadArticulatorTheoryModel();

	private static SequenceFactory loadArticulatorTheoryModel() {
		InputStream stream = SegmentTest.class.getClassLoader().getResourceAsStream("AT_hybrid.model");
		FormatterMode mode = FormatterMode.INTELLIGENT;
		return new SequenceFactory(new FeatureModel(stream, mode), mode);
	}

	private static SequenceFactory loadNumericalFeatureModel() {
		InputStream stream = SegmentTest.class.getClassLoader().getResourceAsStream("features.model");
		FormatterMode mode = FormatterMode.INTELLIGENT;
		return new SequenceFactory(new FeatureModel(stream, mode), mode);
	}

	@Test
	public void testUnderspecifiedSegment01() {
		String string = "[-continuant, release:1]";
		Segment received = FACTORY_NF.getSegment(string);
		FeatureModel model = FACTORY_NF.getFeatureModel();

		List<Double> array = model.getUnderspecifiedArray();
		array.set(1, -1.0);
		array.set(3, 1.0);

		Segment expected = FACTORY_NF.getSegment("[-continuant, release:1]");

		assertEquals(expected, received);
	}

	@Test
	public void testUnderspecifiedSegment02() {
		String string = "[son:3, +con, hgt:-1,+frn,-bck,-atr,glt:0]";
		Segment received = FACTORY_NF.getSegment(string);
		FeatureModel model = FACTORY_NF.getFeatureModel();

		List<Double> array = model.getUnderspecifiedArray();
		array.set(0, 3.0);
		array.set(1, 1.0);
		array.set(10, -1.0);
		array.set(11, 1.0);
		array.set(12, -1.0);
		array.set(13, -1.0);
		array.set(16, 0.0);

		Segment expected = new Segment(string, array, model);

		assertEquals(expected, received);
	}

	@Test
	public void testMatch01() {
		Segment segmentA = FACTORY_NF.getSegment("a");

		Segment segmentP = FACTORY_NF.getSegment("p");
		Segment segmentT = FACTORY_NF.getSegment("t");
		Segment segmentK = FACTORY_NF.getSegment("k");

		Segment received = FACTORY_NF.getSegment("[-continuant, release:1]");

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
		Segment a = FACTORY_NF.getSegment("a");
		Segment n = FACTORY_NF.getSegment("n");

		assertFalse("a matches n", a.matches(n));
		assertFalse("n matches a", n.matches(a));
	}

	@Test
	public void testMatch03() {
		Segment segment = FACTORY_NF.getSegment("[son:3, +con, hgt:-1,+frn,-bck,-atr,glt:0]");

		Segment a = FACTORY_NF.getSegment("a");

		assertTrue(a.matches(segment));
		assertTrue(segment.matches(a));
	}

	@Test
	public void testMatch04() {
		Segment x = FACTORY_NF.getSegment("x");
		Segment e = FACTORY_NF.getSegment("e");

		assertFalse(e.matches(x));
		assertFalse(x.matches(e));
	}

	@Test
	public void testCompareTo01() {
		Segment p = FACTORY_NF.getSegment("p");
		Segment b = FACTORY_NF.getSegment("b");

		// These differ only by voicing, where p is -2 and b is 0
		LOGGER.info("{} {}", p, p.getFeatures());
		LOGGER.info("{} {}", b, b.getFeatures());

		assertTrue(p.compareTo(b) == -1);
	}

	@Test
	public void testCompareTo02() {
		Segment p = FACTORY_NF.getSegment("p");
		Segment t = FACTORY_NF.getSegment("t");

		LOGGER.info("{} {}", p, p.getFeatures());
		LOGGER.info("{} {}", t, t.getFeatures());

		assertTrue(p.compareTo(t) == 1);
	}
}
