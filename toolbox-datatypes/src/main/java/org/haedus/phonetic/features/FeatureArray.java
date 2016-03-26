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

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 3/26/2016
 */
public interface FeatureArray<T> extends Comparable<FeatureArray<T>> {
	
	void set(int index, T value);
	
	T get(int index);
	
	boolean matches(FeatureArray<T> array);
	
	FeatureArray<T> alter(FeatureArray<T> array);
}
