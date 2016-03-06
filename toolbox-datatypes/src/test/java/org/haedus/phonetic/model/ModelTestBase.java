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

package org.haedus.phonetic.model;

import org.haedus.enums.FormatterMode;
import org.haedus.phonetic.SegmentTest;
import org.haedus.phonetic.SequenceFactory;
import org.haedus.phonetic.model.FeatureModel;

import java.io.InputStream;

/**
 * Created by samantha on 10/10/15.
 */
public class ModelTestBase {

	protected static SequenceFactory loadFactory(String resourceName, FormatterMode mode) {
		return new SequenceFactory(loadModel(resourceName, mode), mode);
	}

	protected static FeatureModel loadModel(String resourceName, FormatterMode mode) {
		InputStream stream = ModelTestBase.class.getClassLoader().getResourceAsStream(resourceName);
		return new FeatureModel(stream, mode);
	}
}
