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
import java.util.List;

import static org.junit.Assert.assertEquals;
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
		FormatterMode mode = FormatterMode.INTELLIGENT;
		model = new FeatureModel(resource.getFile(), mode);
		factory = new SequenceFactory(model, mode);
	}

	@Test
	public void testUnderspecifiedSegment01() {
		Segment received = factory.getSegment("[-continuant, release:1]");

		List<Double> array = model.getUnderspecifiedArray();
		array.set(1, -1.0);
		array.set(3,  1.0);

		Segment expected = new Segment("[-continuant, release:1]", array, model);

		assertEquals(expected, received);
	}

	@Test
	public void testUnderspecifiedSegment02() {
		String string = "[son:3, +con, hgt:-1,+frn,-bck,-atr,glt:0]";
		Segment received = factory.getSegment(string);

		List<Double> array = model.getUnderspecifiedArray();
		array.set(0,   3.0);
		array.set(1,   1.0);
		array.set(10, -1.0);
		array.set(11,  1.0);
		array.set(12, -1.0);
		array.set(13, -1.0);
		array.set(16,  0.0);

		Segment expected = new Segment(string, array, model);

		assertEquals(expected, received);
	}

	@Test
	public void testMatch01() {
		Segment segmentA = factory.getSegment("a");

		Segment segmentP = factory.getSegment("p");
		Segment segmentT = factory.getSegment("t");
		Segment segmentK = factory.getSegment("k");

		Segment received = model.getSegmentFromFeatures("[-continuant, release:1]");

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
		Segment a = factory.getSegment("a");
		Segment n = factory.getSegment("n");

		assertFalse("a matches n", a.matches(n));
		assertFalse("n matches a", n.matches(a));
	}

	@Test
	public void testMatch03() {
		Segment segment = factory.getSegment("[son:3, +con, hgt:-1,+frn,-bck,-atr,glt:0]");

		Segment a = factory.getSegment("a");
		Segment n = factory.getSegment("n");

		assertTrue(a.matches(segment));
		assertTrue(segment.matches(a));
	}

	@Test
	public void testMatch04() {
		Segment segment = factory.getSegment("x");

		Segment e = factory.getSegment("e");
		Segment n = factory.getSegment("n");

		assertFalse(e.matches(segment));
		assertFalse(segment.matches(e));
	}
}
