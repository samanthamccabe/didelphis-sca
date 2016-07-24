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
import org.haedus.phonetic.model.FeatureSpecification;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by samantha on 3/27/16.
 */
public final class SparseFeatureArray<T extends Number & Comparable<T>>
		implements FeatureArray<T> {

	private final FeatureSpecification specification;

	private final Map<Integer, T> features;

	public SparseFeatureArray(FeatureSpecification specification) {
		this.specification = specification;
		features = new HashMap<Integer, T>();
	}

	public SparseFeatureArray(List<T> list, FeatureSpecification specification) {
		this(specification);
		for (int i = 0; i < list.size(); i++) {
			features.put(i, list.get(i));
		}
	}

	public SparseFeatureArray(SparseFeatureArray<T> array) {
		specification = array.getSpecification();
		features = new HashMap<Integer, T>(array.features);
	}

	@Override
	public int size() {
		return specification.size();
	}

	@Override
	public void set(int index, T value) {
		indexCheck(index);
		features.put(index, value);
	}

	@Override
	public T get(int index) {
		indexCheck(index);
		return features.get(index);
	}

	@Override
	public boolean matches(FeatureArray<T> array) {
		if (size() != array.size()) {
			throw new IllegalArgumentException(
					"Attempting to compare arrays of different lengths");
		}

		for (Map.Entry<Integer, T> entry : features.entrySet()) {
			T a = entry.getValue();
			T b = array.get(entry.getKey());
			if (!(b == null || a.equals(b))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void alter(FeatureArray<T> array) {
		if (size() != array.size()) {
			throw new IllegalArgumentException(
					"Attempting to compare arrays of different lengths");
		}

		if (array instanceof SparseFeatureArray) {
			features.putAll(((SparseFeatureArray<T>) array).features);
		} else{
			for (int i = 0; i < size(); i++) {
				T t = array.get(i);
				if (t != null && !t.equals(FeatureModel.MASKING_VALUE)) {
					features.put(i, t);
				}
			}
		}
	}

	@Override
	public boolean contains(T value) {
		return features.containsValue(value);
	}

	@Override
	public int compareTo(FeatureArray<T> o) {
		if (size() != o.size()) {
			throw new IllegalArgumentException(
					"Attempting to compare arrays of different lengths");
		}

		int size = specification.size();

		// There should be a better way to do this than checking
		// every index, since usually only one value will be defined
		for (int i = 0; i < size; i++) {
			T a = get(i);
			T b = o.get(i);
			int comparison;
			if (a == null && b == null) {
				comparison = 0;
			} else if (a == null) {
				comparison = -1;
			}else if (b == null) {
				comparison = 1;
			} else {
				comparison = a.compareTo(b);
			}
			if (comparison != 0) {
				return comparison;
			}
			// Else, do nothing; the loop will check the next value
		}
		// If we get to the end, then all values must be equal
		return 0;
	}

	@Override
	public Iterator<T> iterator() {
		return features.values().iterator();
	}

	@Override
	public String toString() {
		return "SparseFeatureArray{" + ", features=" + features + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SparseFeatureArray<?> that = (SparseFeatureArray<?>) o;

		return specification.equals(specification) &&
				features.equals(that.features);
	}

	@Override
	public int hashCode() {
		int result = features.hashCode();
		result = 31 * result + features.hashCode();
		return result;
	}

	@Override
	public FeatureSpecification getSpecification() {
		return specification;
	}

	private void indexCheck(int index) {
		int size = specification.size();
		if (index >= size) {
			throw new IndexOutOfBoundsException("Provided index " + index +
					" is larger than defined size "+ size + " for specification " + specification);
		}
	}
}
