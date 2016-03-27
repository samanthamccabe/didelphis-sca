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

import org.haedus.phonetic.model.FeatureModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 3/26/2016
 */
public class StandardFeatureArray<T extends Number & Comparable<T>>
		implements FeatureArray<T> {
	
	private final List<T> features;

	public StandardFeatureArray(int size) {
		features = new ArrayList<T>(size);
		for (int i = 0; i < size; i++) {
			features.add(null);
		}
	}

	public StandardFeatureArray(List<T> list) {
		features = new ArrayList<T>(list);
	}
	
	public StandardFeatureArray(StandardFeatureArray<T> array) {
		features = new ArrayList<T>(array.features);
	}

	public StandardFeatureArray(FeatureArray<T> array) {
		int size = array.size();
		features = new ArrayList<T>(size);
		for (int i = 0; i < size; i++) {
			features.add(array.get(i));
		}
	}

	@Override
	public int size() {
		return features.size();
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
		if (size() != array.size()) {
			throw new IllegalArgumentException(
					"Attempting to compare arrays of different lengths");
		}

		for (int i = 0; i < size(); i++) {
			if (!matches(array, i) && !array.matches(this, i)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean matches(FeatureArray<T> array, int index) {
		T a = this.get(index);
		T b = array.get(index);
		return  a == null ||
				b == null ||
				a.equals(FeatureModel.MASKING_VALUE) ||
				b.equals(FeatureModel.MASKING_VALUE) ||
				a.equals(b);
	}

	@Override
	public FeatureArray<T> alter(FeatureArray<T> array) {
		FeatureArray<T> featureArray = new StandardFeatureArray<T>(this);
		for (int i = 0; i < features.size(); i++) {
			T t = array.get(i);
			if (t != null) {
				featureArray.set(i, t);
			}
		}
		return featureArray;
	}

	@Override
	public boolean contains(T value) {
		return features.contains(value);
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;

		StandardFeatureArray<?> that = (StandardFeatureArray<?>) obj;

		return features.equals(that.features);
	}

	@Override
	public int hashCode() {
		return features.hashCode();
	}

	@Override
	public String toString() {
		return features.toString();
	}

	@Override
	public Iterator<T> iterator() {
		return features.iterator();
	}
}
