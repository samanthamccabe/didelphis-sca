/*******************************************************************************
 * Copyright (c) 2014 Haedus - Fabrica Codicis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.haedus.datatypes.phonetic;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Samantha Fiona Morrigan McCabe
 */
public class FeatureModelTest {
	private static final transient Logger LOGGER = LoggerFactory.getLogger(FeatureModelTest.class);

	@Test
	public void testConstructor01() throws Exception {
		Resource resource = new ClassPathResource("featuremodel");
		FeatureModel model = new FeatureModel(resource.getFile());

		List<Integer> features = new ArrayList<Integer>();
		Collections.addAll(features,
				3, 0, 0, 0, 0, 0, 0, 0, -1, -1, 1, 2, 1, 0, 0, 0, 0);

		Segment received = model.get("g");
		Segment expected = new Segment("g", features);

		assertEquals(expected, received);
	}

	@Test
	public void testGetStringFromFeatures01() throws Exception {
		List<Integer> expected = new ArrayList<Integer>();
		Collections.addAll(expected,
				3, 0, 0, 0, 0, 0, 0, 0, -1, -1, 1, 2, 1, 0, 0, 0, 0);

		Resource resource = new ClassPathResource("featuremodel");
		FeatureModel model = new FeatureModel(resource.getFile());

		String bestSymbol = model.getBestSymbol(expected);
		LOGGER.info(bestSymbol);
		assertEquals("g", bestSymbol);
	}

	@Test
		 public void testGetStringFromFeatures02() throws Exception {
		List<Integer> expected = new ArrayList<Integer>();
		Collections.addAll(expected,
				3, 0, 1, 0, 0, 0, 0, 0, -1, -1, 1, 2, 1, 0, 0, 0, 0);

		Resource resource = new ClassPathResource("featuremodel");
		FeatureModel model = new FeatureModel(resource.getFile());

		String bestSymbol = model.getBestSymbol(expected);
		assertEquals("gʰ", bestSymbol);
	}

	@Test
	public void testGetStringFromFeatures03() throws Exception {
		List<Integer> expected = new ArrayList<Integer>();
		Collections.addAll(expected,
				3, 0, 1, 0, 0, 0, 0, 0, -1, -1, 1, 2, 1, 0, 0, 0, 0);

		Resource resource = new ClassPathResource("featuremodel");
		FeatureModel model = new FeatureModel(resource.getFile());

		String bestSymbol = model.getBestSymbol(expected);
		assertEquals("gʰ", bestSymbol);
	}
}
