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

package org.didelphis.machines;

import org.didelphis.enums.ParseDirection;
import org.didelphis.phonetic.Sequence;
import org.didelphis.phonetic.SequenceFactory;

import java.util.Collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 1/31/2016
 */
public class MachineTestBase {

	protected static SequenceFactory FACTORY;

	protected static Machine getMachine(String expression) {
		return StateMachine.create("M0", expression, FACTORY, ParseDirection.FORWARD);
	}

	protected static void test(Machine stateMachine, String target) {
		Collection<Integer> matchIndices = testMachine(stateMachine, target);
		assertFalse("Machine failed to accept input: " + target, matchIndices.isEmpty());
	}

	protected static void fail(Machine stateMachine, String target) {
		Collection<Integer> matchIndices = testMachine(stateMachine, target);
		assertTrue("Machine accepted input it should not have: " + target, matchIndices.isEmpty());
	}

	protected static Collection<Integer> testMachine(Machine stateMachine, String target) {
		Sequence sequence = FACTORY.getSequence(target);
		return stateMachine.getMatchIndices(0, sequence);
	}
}
