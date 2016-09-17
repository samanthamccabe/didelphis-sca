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

package org.didelphis.machines;

import org.didelphis.phonetic.Sequence;

import java.util.Collection;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 4/7/2015
 */
public interface Machine {

	/**
	 * Returns the indices
	 * @param startIndex
	 * @param target
	 * @return
	 */
	Collection<Integer> getMatchIndices(int startIndex, Sequence target);
}
