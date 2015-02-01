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

import org.haedus.phonetic.Sequence;
import org.haedus.phonetic.SequenceFactory;

import java.util.Collection;

/**
 * Created by samantha on 12/24/14.
 */
public class NegativeStateMachine extends AbstractNode {

	private final Node<Sequence> machine;

	public NegativeStateMachine(String id, String expression, SequenceFactory factoryParam, boolean accepting) {
		super(id, factoryParam, accepting);
		machine = null; // TODO: parse this
	}

	@Override
	public boolean matches(int startIndex, Sequence target) {
		return false;
	}

	@Override
	public boolean containsStateMachine() {
		return true;
	}

	@Override
	public Collection<Integer> getMatchIndices(int startIndex, Sequence target) {
		return null;
	}
}
