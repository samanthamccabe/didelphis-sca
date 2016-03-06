/******************************************************************************
 * Copyright (c) 2016. Samantha Fiona McCabe                                  *
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
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by samantha on 2/21/16.
 */
public class FeatureModelSelectorTest extends ModelTestBase {

	public static final FeatureModel MODEL = loadModel("AT_hybrid.model", FormatterMode.INTELLIGENT);
	
	@Test
	public void testWriteOutTEMP() {
		String selector = "[+con; -cnt]";
		Segment segment = MODEL.getSegmentFromFeatures(selector);

		Collection<Segment> segments = MODEL.getMatchingSegments(segment);

		for (Segment g : segments) {
			System.out.print(FormatterMode.COMPOSITION.normalize(g.toString()) + ' ');
		}
	}

	@Test
	public void testWritexOutTEMP2() {
		String selector = "[-consonantal]";
		Segment segment = MODEL.getSegmentFromFeatures(selector);

		Collection<Segment> segments = MODEL.getMatchingSegments(segment);

		for (Segment g : segments) {
			System.out.println(g + "\t" + FeatureModel.formatFeatures(g.getFeatures()));
		}
	}

	@Test
	public void testConsonantal() {
		String selector = "[-consonantal]";
		Segment segment = MODEL.getSegmentFromFeatures(selector);

		Collection<Segment> segments = MODEL.getMatchingSegments(segment);

		assertFalse(segments.isEmpty());
		assertEquals(75,segments.size());
	}

	@Test
	public void testNonConsonantal() {
		String selector = "[-consonantal]";
		Segment segment = MODEL.getSegmentFromFeatures(selector);

		Collection<Segment> segments = MODEL.getMatchingSegments(segment);

		assertFalse(segments.isEmpty());
		assertEquals(31, segments.size());
	}

	@Test
	public void testObstruent() {
		String selector = "[-sonorant]";
		Segment segment = MODEL.getSegmentFromFeatures(selector);

		Collection<Segment> segments = MODEL.getMatchingSegments(segment);

		assertFalse(segments.isEmpty());
		assertEquals(59, segments.size());
	}

	@Test
	public void testObstruentEquivalence() {

		Segment segment1 = MODEL.getSegmentFromFeatures("[-sonorant]");
		Segment segment2 = MODEL.getSegmentFromFeatures("[+consonantal; -sonorant]");

		Collection<Segment> segments1 = MODEL.getMatchingSegments(segment1);
		Collection<Segment> segments2 = MODEL.getMatchingSegments(segment2);

		assertEquals(segments1, segments2);
	}

	@Test
	public void testNasal() {
		String selector = "[+nasal]";
		Segment segment = MODEL.getSegmentFromFeatures(selector);

		Collection<Segment> segments = MODEL.getMatchingSegments(segment);

		assertFalse(segments.isEmpty());
		assertEquals(8, segments.size());
	}



}
