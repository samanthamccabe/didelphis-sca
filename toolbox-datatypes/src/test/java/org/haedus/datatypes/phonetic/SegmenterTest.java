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

package org.haedus.datatypes.phonetic;

import org.haedus.datatypes.FormatterMode;
import org.haedus.datatypes.NormalizerMode;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import org.haedus.datatypes.SegmentationMode;
import org.haedus.datatypes.Segmenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SegmenterTest
{
	private static FeatureModel model;

	@BeforeClass
	public static void init() throws IOException {
		Resource resource = new ClassPathResource("features.model");
		model = new FeatureModel(resource.getFile());
	}

	@Test
	public void testString() {
		String word = "t͜sʰ";

		List<String> segmentedString = Segmenter.getSegmentedString(word, new ArrayList<String>(), FormatterMode.INTELLIGENT);
		assertFalse("Nothing was returned by the Segmenter!",segmentedString.isEmpty());
	}

	@Test
	public void testSequence() {
		String word = "t͜sʰ";

		Sequence sequence = getSequence(word);
		assertTrue(!sequence.isEmpty());
	}

	@Test
	public void testSequence02() {
		String word = "aḱʰus";

		Sequence sequence = getSequence(word);
		assertTrue(sequence.size() == 4);
	}

	@Test
	public void testSegment() {
		Sequence sequence = getSequence("aː");

		assertTrue(!sequence.isEmpty());
	}

	private static Sequence getSequence(String word) {
		return Segmenter.getSequence(word, model, new ArrayList<String>(), FormatterMode.INTELLIGENT);
	}
}
