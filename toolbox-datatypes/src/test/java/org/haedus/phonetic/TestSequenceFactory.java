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
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 2/5/2015
 */
public class TestSequenceFactory {

	private final FeatureModel model;

	public TestSequenceFactory() throws IOException {
		Resource resource = new ClassPathResource("features.model");
		File file = resource.getFile();
		model = new FeatureModel(file);
	}

	@Test
	public void testGetSequence01() {
		String word = "avaːm";

		SequenceFactory factory = new SequenceFactory(model, FormatterMode.INTELLIGENT);

		Sequence sequence = factory.getSequence(word);
		assertTrue(!sequence.isEmpty());
	}
}
