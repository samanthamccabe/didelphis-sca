/*******************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.haedus.phonetic.model;

import org.haedus.enums.FormatterMode;
import org.haedus.phonetic.Segment;
import org.haedus.phonetic.Segmenter;
import org.haedus.phonetic.features.FeatureArray;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Samantha Fiona Morrigan McCabe
 */
public class StandardFeatureModelTest extends ModelTestBase {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(StandardFeatureModelTest.class);

	private static final Double NAN = Double.NaN;
	private static final Double INF = Double.NEGATIVE_INFINITY;

	private static final FeatureModel MODEL = loadModel("AT_hybrid.model", FormatterMode.INTELLIGENT);


	@Test
	public void testLoad01() {
		// Ensure the MODEL loads correctly.
		assertFalse(MODEL.getFeatureMap().isEmpty());
		assertFalse(MODEL.getModifiers().isEmpty());
	}

	@Test
	public void testLoad02() {
		StandardFeatureModel model = loadModel("AT_hybrid.mapping", FormatterMode.INTELLIGENT);
		assertTrue(model.getSpecification().size() > 0);
	}

	@Test
	public void testLoad_AT_Hybrid() {
		FeatureModel model = loadModel("AT_hybrid.model", FormatterMode.INTELLIGENT);
		assertFalse(model.getFeatureMap().isEmpty());
		assertFalse(model.getModifiers().isEmpty());
	}
	
	@Test
	public void testGetStringFromFeatures01()  {
		testBestSymbol("g");
	}

	@Test
	public void testGetStringFromFeatures02()  {
		testBestSymbol("gʱ");
	}

	@Test
	public void testGetStringFromFeatures03()  {
		testBestSymbol("gʲ");
	}

	@Test
	public void testGetStringFromFeatures04()  {
		testBestSymbol("kʷʰ");
	}

	@Test
	public void testGetStringFromFeatures05()  {
		testBestSymbol("kːʷʰ");
	}

	private static void testNaN(double v) {
		assertTrue("Value was " + v + " not NaN", Double.isNaN(v));
	}
	
	private static void testBestSymbol(String string) {
		Segment segment = Segmenter.getSegment(string, MODEL, FormatterMode.INTELLIGENT);
		
		FeatureArray<Double> array = segment.getFeatures();
		String bestSymbol = MODEL.getBestSymbol(array);
		assertEquals(string, bestSymbol);
	}
}
