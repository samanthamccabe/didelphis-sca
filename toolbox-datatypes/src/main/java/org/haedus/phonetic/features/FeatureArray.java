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

import org.haedus.phonetic.ModelBearer;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 3/26/2016
 */
public interface FeatureArray<T extends Number & Comparable<T>>
		extends Comparable<FeatureArray<T>>, Iterable<T>, ModelBearer {

	/**
	 * Returns the number of elements in this list.
	 * @return the number of elements in this object
	 */
	int size();

	/**
	 * Replaces the element at the specified position in this list withthe specified element.
	 * @param index index of the element to replace
	 * @param value element to be stored at the specified position
	 * @throws IndexOutOfBoundsException - if the index is out of range (index < 0 || index >= size())
	 * @throws ClassCastException - if the class of the specified element prevents it from being added to this list
	 * @throws NullPointerException - if the specified element is null and this list does not permit null elements
	 */
	void set(int index, T value);

	/**
	 * Returns the element at the specified position in this object.
	 * @param index  index of the element to return
	 * @return the element at the specified position in this list; if there element, return null.
	 * @throws IndexOutOfBoundsException - if the index is out of range (index < 0 || index >= size())
	 */
	T get(int index);

	//TODO: this is non-trivial and needs to be explained
	boolean matches(FeatureArray<T> array);

	//TODO: this is non-trivial and needs to be explained
	void alter(FeatureArray<T> array);

	boolean contains(T value);
}
