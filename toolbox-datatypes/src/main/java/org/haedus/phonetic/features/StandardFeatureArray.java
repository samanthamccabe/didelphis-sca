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

package org.haedus.phonetic.features;

import java.util.ArrayList;
import java.util.List;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 3/26/2016
 */
public class StandardFeatureArray<T extends Comparable<T>> implements FeatureArray<T> {
	
	private final List<T> features;
	
	public StandardFeatureArray(List<T> list) {
		features = new ArrayList<T>(list);
	}
	
	public StandardFeatureArray(StandardFeatureArray<T> array) {
		features = new ArrayList<T>(array.features);
	}
	
	@Override
	public void set(int index, T value) {
		features.set(index, value);
	}

	@Override
	public T get(int index) {
		return features.get(index);
	}

	@Override
	public boolean matches(FeatureArray<T> array) {
		return false; // TODO:
	}

	@Override
	public FeatureArray<T> alter(FeatureArray<T> array) {
		return null; // TODO:
	}
	
	@Override
	public int compareTo(FeatureArray<T> o) {
		StandardFeatureArray<T> array = (StandardFeatureArray<T>) o;
		List<T> oFeatures = array.features;
		for (int i = 0; i < features.size(); i++) {
			T a = features.get(i);
			T b = oFeatures.get(i);

			int comparison = a.compareTo(b);
			if (comparison != 0) {
				return comparison;
			}
			// Else, do nothing; the loop will check the next value
		}
		// If we get to the end, then all values must be equal
		return 0;
	}
}
