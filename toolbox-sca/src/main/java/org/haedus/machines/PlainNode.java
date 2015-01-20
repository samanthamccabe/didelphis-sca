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

package org.haedus.machines;

import java.util.Collection;
import java.util.HashSet;

import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.SequenceFactory;

/**
 * Created by Samantha F M McCabe on 12/21/14.
 */
public class PlainNode extends AbstractNode {

	// package-access only, for factory instantiation
	PlainNode(String idParam, SequenceFactory factoryParam, boolean accepting) {
		super(idParam, factoryParam, accepting);
	}

	@Override
	public boolean matches(int startIndex, Sequence target) {
		// Because the terminal node does not contain a state machine
		// it will trivially match any input
		return true;
	}

	@Override
	public boolean containsStateMachine() {
		return false;
	}

	@Override
	public Collection<Integer> getMatchIndices(int startIndex, Sequence target) {
		Collection<Integer> integers = new HashSet<Integer>();
		integers.add(startIndex);
		return integers;
	}

	@Override
	public String toString() {
		return getId() + ": " + getKeys();
	}

}
