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

package org.haedus.phonetic;

import org.haedus.enums.FormatterMode;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Samantha Fiona Morrigan McCabe
 */
public class FeatureModelTest extends ModelTestBase {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(FeatureModelTest.class);

	private static final Double NAN = Double.NaN;
	private static final Double INF = Double.NEGATIVE_INFINITY;

	private static final List<Double> G_FEATURES    = new ArrayList<Double>();
	private static final List<Double> GH_FEATURES   = new ArrayList<Double>();
	private static final List<Double> GJ_FEATURES   = new ArrayList<Double>();
	private static final List<Double> KWH_FEATURES  = new ArrayList<Double>();
	private static final List<Double> KKWH_FEATURES = new ArrayList<Double>();

	private static final FeatureModel MODEL = initModel();

	public static FeatureModel initModel() {
		//                                  0     2    3    4    5    6    7    8   10    11   12   13   14    16   17
		//                                son   vot  rel  nas  lat  lab  rnd  lin  hgt   frn  bck  atr  rad   glt  len
		Collections.addAll(G_FEATURES,    0.0, -1.0, 1.0, NAN, NAN, NAN,-1.0, NAN, 1.0, -1.0, 1.0, NAN, NAN,  0.0, 0.0);
		Collections.addAll(GH_FEATURES,   0.0,  1.0, 1.0, NAN, NAN, NAN,-1.0, NAN, 1.0, -1.0, 1.0, NAN, NAN,  0.0, 0.0);
		Collections.addAll(GJ_FEATURES,   0.0, -1.0, 1.0, NAN, NAN, NAN,-1.0, NAN, 1.0,  1.0, 1.0, NAN, NAN,  0.0, 0.0);
		Collections.addAll(KWH_FEATURES,  0.0,  1.0, 1.0, NAN, NAN, NAN, 1.0, NAN, 1.0, -1.0, 1.0, NAN, NAN, -2.0, 0.0);
		Collections.addAll(KKWH_FEATURES, 0.0,  1.0, 1.0, NAN, NAN, NAN, 1.0, NAN, 1.0, -1.0, 1.0, NAN, NAN, -2.0, 1.0);

		return loadModel("reduced.model", FormatterMode.INTELLIGENT);
	}

	@Test
	public void testLoad01() {
		// Ensure the MODEL loads correctly.
		assertTrue(MODEL.getNumberOfFeatures() > 0);
	}

	@Test
	public void testLoad_AT_Hybrid() {
		// Ensure the MODEL loads correctly.
		FeatureModel model = loadModel("AT_hybrid.model", FormatterMode.INTELLIGENT);
		assertTrue(model.getNumberOfFeatures() > 0);
	}

	@Test
	public void testFeatureParse01() {
		List<Double> ex = new ArrayList<Double>();
		//                     son  vot  rel  nas  lat  lab  rnd  lin  hgt  frn  bck  atr  rad  glt  len
		// 					p  ___  ___  ___  ___  ___  ___  ___  ___  ___  ___  ___  ___  ___  ___  ___
		Collections.addAll(ex, 0.0, INF, 1.0, INF, INF, 1.0, INF, INF, INF, INF, INF, INF, INF,-3.0, 0.0);
		Segment expected = new Segment("p", ex, MODEL);
		Segment received = MODEL.getSegmentFromFeatures("[son:0,rel:1,lab:1,glt:-3,len:0]");
		assertEquals(expected.getFeatures(), received.getFeatures());
	}

	@Test
	public void testFeatureParse02() {
		List<Double> ex = new ArrayList<Double>();
		//                     son  vot  rel  nas  lat  lab  rnd  lin  hgt  frn  bck  atr  rad  glt  len
		// 					p  ___  ___  ___  ___  ___  ___  ___  ___  ___  ___  ___  ___  ___  ___  ___
		Collections.addAll(ex, 0.0, INF, 1.0, INF, INF, 1.0, INF, INF, INF, INF, INF, INF, INF,-3.0, 0.0);

		Segment expected = new Segment("p", ex, MODEL);
		Segment received = MODEL.getSegmentFromFeatures("[sonorance:0,release:1,labial:1,glottalstate:-3,length:0]");
		assertEquals(expected.getFeatures(), received.getFeatures());
	}

	@Test
	public void testConstructor01() {
		Segment received = Segmenter.getSegment("g", MODEL, FormatterMode.INTELLIGENT);
		Segment expected = new Segment("g", G_FEATURES, MODEL);

		assertEquals(expected, received);
	}

	@Test
	public void testGetStringFromFeatures01()  {
		String bestSymbol = MODEL.getBestSymbol(G_FEATURES);
		assertEquals("g", bestSymbol);
	}

	@Test
	public void testGetStringFromFeatures02()  {
		String bestSymbol = MODEL.getBestSymbol(GH_FEATURES);
		assertEquals("gʱ", bestSymbol);
	}

	@Test
	public void testGetStringFromFeatures03()  {
		String bestSymbol = MODEL.getBestSymbol(GJ_FEATURES);
		assertEquals("gʲ", bestSymbol);
	}

	@Test
	public void testGetStringFromFeatures04()  {
		String bestSymbol = MODEL.getBestSymbol(KWH_FEATURES);
		assertEquals("kʷʰ", bestSymbol);
	}

	@Test
	public void testGetStringFromFeatures05()  {
		String bestSymbol = MODEL.getBestSymbol(KKWH_FEATURES);
		assertEquals("kːʷʰ", bestSymbol);
	}

	private static void testNaN(double v) {
		assertTrue("Value was " + v + " not NaN", Double.isNaN(v));
	}
}
