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

package org.didelphis.phonetic;

import org.didelphis.enums.FormatterMode;
import org.didelphis.phonetic.model.FeatureModel;
import org.didelphis.phonetic.model.StandardFeatureModel;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
	
	private static final Pattern INFINITY_PATTERN = Pattern.compile("(-|\\+)?Infinity");
	private static final Pattern DECIMAL_PATTERN  = Pattern.compile("([^\\-])(\\d\\.\\d)");

	private static SequenceFactory loadArticulatorTheoryModel() {
		InputStream stream = SegmentTest.class.getClassLoader().getResourceAsStream("AT_hybrid.model");
		FormatterMode mode = FormatterMode.INTELLIGENT;
		return new SequenceFactory(new StandardFeatureModel(stream, mode), mode);
	}
	
	@Test
	public void testUnderspecifiedSegment01() {
		String string = "[-continuant, +release]";
		Segment received = FACTORY.getSegment(string);
		FeatureModel model = FACTORY.getFeatureModel();

		List<Double> array = new ArrayList<Double>();
		int size = model.getSpecification().size();
		for (int i = 0; i < size; i++) {
			array.add(null);
		}
		
		array.set(1, -1.0);
		array.set(3,  1.0);

		Segment expected = FACTORY.getSegment("[-continuant, +release]");

		assertEquals(expected, received);
	}
	
	@Test
	public void testSelectorAliasHigh() {
		Segment alias   = FACTORY.getSegment("[high]");
		Segment segment = FACTORY.getSegment("[+high]");
		
		assertTrue(alias.matches(segment));
	}

	@Test
	public void testSelectorAliasMid() {
		Segment alias   = FACTORY.getSegment("[mid]");
		Segment segment = FACTORY.getSegment("[0:high]");

		assertTrue(alias.matches(segment));
	}

	@Test
	public void testSelectorAliasLow() {
		Segment alias   = FACTORY.getSegment("[low]");
		Segment segment = FACTORY.getSegment("[-high]");

		assertTrue(alias.matches(segment));
	}

	@Test
	public void testSelectorAliasRetroflex() {
		Segment alias   = FACTORY.getSegment("[retroflex]");
		Segment segment = FACTORY.getSegment("[4:coronal, -distributed]");

		assertTrue(alias.matches(segment));
	}

	@Test
	public void testSelectorAliasPalatal() {
		Segment alias   = FACTORY.getSegment("[palatal]");
		Segment segment = FACTORY.getSegment("[4:coronal, +distributed]");

		assertTrue(alias.matches(segment));
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

		assertTrue(p.compareTo(b) == -1);
	}

	@Test
	public void testOrdering02() {
		Segment p = FACTORY.getSegment("p");
		Segment t = FACTORY.getSegment("t");

		assertTrue(p.compareTo(t) == 1);
	}

	@Test
	public void testConstraintLateralToNasal01() {
		Segment segment = FACTORY.getSegment("l");

		segment.setFeatureValue(6, 1.0);

		double received = segment.getFeatureValue(5);
		double expected = -1.0;

		assertEquals(expected, received, 0.00001);
	}

	@Test
	public void testConstraintLateralToNasal02() {
		Segment segment = FACTORY.getSegment("l");

		Segment pNas = FACTORY.getSegment("[+nas]");
		Segment nLat = FACTORY.getSegment("[-lat]");

		assertMatch(segment, pNas, nLat);
	}

	@Test
	public void testConstraintNasalToLateral01() {
		Segment segment = FACTORY.getSegment("n");

		segment.setFeatureValue(5, 1.0);

		double expected = -1.0;
		double received = segment.getFeatureValue(6);

		assertEquals(expected, received, 0.00001);
	}

	@Test
	public void testConstraintNasalToLateral02() {
		Segment segment = FACTORY.getSegment("n");

		Segment pLat = FACTORY.getSegment("[+lat]"); // i = 5
		Segment nNas = FACTORY.getSegment("[-nas]"); // i = 6

		assertMatch(segment, pLat, nNas);
	}

	@Test
	public void testConstaintSonorant() {
		Segment segment = FACTORY.getSegment("i");

		Segment nSon = FACTORY.getSegment("[-son]"); // i = 1
		Segment pCon = FACTORY.getSegment("[+con]"); // i = 0

		assertMatch(segment, nSon, pCon);
	}

	@Test
	public void testConstraintConsonant() {
		Segment segment = FACTORY.getSegment("s");

		Segment nCon = FACTORY.getSegment("[-con]"); // i = 0
		Segment pSon = FACTORY.getSegment("[+son]"); // i = 1

		assertMatch(segment, nCon, pSon);
	}

	@Test
	public void testConstraintConsonantalRelease() {
		Segment segment = FACTORY.getSegment("kx");

		Segment nCon = FACTORY.getSegment("[-con]"); // i = 0
		Segment nRel = FACTORY.getSegment("[-rel]"); // i = 1

		assertMatch(segment, nCon, nRel);
	}

	@Test
	public void testConstraintContinuantRelease() {
		Segment segment = FACTORY.getSegment("kx");

		Segment nCnt = FACTORY.getSegment("[+cnt]"); // i = 0
		Segment nRel = FACTORY.getSegment("[-rel]"); // i = 1

		assertMatch(segment, nCnt, nRel);
	}

	@Test
	public void testConstraintSonorantRelease() {
		Segment segment = FACTORY.getSegment("ts");

		Segment nCnt = FACTORY.getSegment("[+son]"); // i = 0
		Segment nRel = FACTORY.getSegment("[-rel]"); // i = 1

		assertMatch(segment, nCnt, nRel);
	}

	@Test
	public void testConstraintReleaseConsonantal() {
		Segment segment = FACTORY.getSegment("r");

		Segment pRel = FACTORY.getSegment("[+rel]"); // i = 0
		Segment pCon = FACTORY.getSegment("[+con]"); // i = 1

		assertMatch(segment, pRel, pCon);
	}

	private static void assertMatch(Segment segment, Segment modifier, Segment matching) {
		Segment alter = segment.alter(modifier);

		String message = "\n"+
				segment.getFeatures() +
				"\naltered by\n" +
				modifier.getFeatures() +
				"\nproduces\n" +
				alter.getFeatures() +
				"\nwhich does not match\n" +
				matching.getFeatures();
		
		message = INFINITY_PATTERN.matcher(message).replaceAll("____");
		message = DECIMAL_PATTERN.matcher(message).replaceAll("$1 $2");
		assertTrue(message,  alter.matches(matching));
	}
}
