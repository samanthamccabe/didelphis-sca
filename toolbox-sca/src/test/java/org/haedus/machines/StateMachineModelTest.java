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

package org.haedus.machines;

import org.haedus.enums.FormatterMode;
import org.haedus.enums.ParseDirection;
import org.haedus.phonetic.FeatureModel;
import org.haedus.phonetic.Sequence;
import org.haedus.phonetic.SequenceFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 2/28/2015
 */
public class StateMachineModelTest {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(StateMachineModelTest.class);

	private static final SequenceFactory FACTORY = loadModel();

	@Test
	public void testBasicStateMachine01() {
		Node<Sequence> stateMachine = getMachine("[son:3, +con, hgt:-1, +frn, -bck, -atr, glt:0]");

		test(stateMachine, "a");
		test(stateMachine, "aa");

		fail(stateMachine, "b");
		fail(stateMachine, "c");
	}

	@Test
	public void testBasicStateMachine03() {
		Node<Sequence> stateMachine = getMachine("a[son:3, +con, hgt:-1, +frn, -bck, -atr]+");

		Node<Sequence> stateMachine1 = getMachine("a[son:3, +con, hgt:-1, +frn, -bck, -atr]+");
		Node<Sequence> stateMachine2 = getMachine("a[son:3, +con, hgt:-1, +frn, -bck, -atr]+");

//		fail(stateMachine, "a");
//		test(stateMachine, "aa");
//		test(stateMachine, "aaa");
//		test(stateMachine, "aa̤");
//		test(stateMachine, "aa̤a");

//		test(stateMachine1, "aa̤a");
//		test(stateMachine2, "aa̤");

		Sequence sequence1 = FACTORY.getSequence("aa̤a");
		Sequence sequence2 = FACTORY.getSequence("aa̤");

		Collection<Integer> matchIndices1a = stateMachine1.getMatchIndices(0, sequence1);
		Collection<Integer> matchIndices2a = stateMachine2.getMatchIndices(0, sequence2);
		Collection<Integer> matchIndices1b = stateMachine1.getMatchIndices(0, sequence1);
		Collection<Integer> matchIndices2b = stateMachine2.getMatchIndices(0, sequence2);

		assertEquals(stateMachine1, stateMachine);
		assertEquals(stateMachine2, stateMachine);

//		Collection<Integer> matchIndices = testMachine(stateMachine, target);
		assertFalse("Machine failed to accept input", matchIndices1a.isEmpty());
//		Collection<Integer> matchIndices = testMachine(stateMachine, target);
		assertFalse("Machine failed to accept input", matchIndices2a.isEmpty());

		fail(stateMachine, "b");
		fail(stateMachine, "c");
	}

	@Test
	public void testBasicStateMachine02() {
		Node<Sequence> stateMachine = getMachine("aaa");

		test(stateMachine, "aaa");

		fail(stateMachine, "a");
		fail(stateMachine, "aa");
		fail(stateMachine, "b");
		fail(stateMachine, "c");
	}

	@Test
	public void testStateMachineStar() {
		Node<Sequence> stateMachine = getMachine("aa*");

		test(stateMachine, "a");
		test(stateMachine, "aa");
		test(stateMachine, "aaa");
		test(stateMachine, "aaaa");
		test(stateMachine, "aaaaa");
		test(stateMachine, "aaaaaa");
	}

	private static void test(Node<Sequence> stateMachine, String target) {
		Collection<Integer> matchIndices = testMachine(stateMachine, target);
		assertFalse("Machine failed to accept input", matchIndices.isEmpty());
	}

	private static void fail(Node<Sequence> stateMachine, String target) {
		Collection<Integer> matchIndices = testMachine(stateMachine, target);
		assertTrue("Machine accepted input it should not have", matchIndices.isEmpty());
	}


	private static Node<Sequence> getMachine(String expression) {
		return NodeFactory.getStateMachine(expression, FACTORY, ParseDirection.FORWARD, true);
	}

	private static Collection<Integer> testMachine(Node<Sequence> stateMachine, String target) {
		Sequence sequence = FACTORY.getSequence(target);
		Collection<Integer> matchIndices = stateMachine.getMatchIndices(0, sequence);
		LOGGER.debug("{} ran against \"{}\" and produced output {}",stateMachine, sequence, matchIndices);
		return matchIndices;
	}

	private static SequenceFactory loadModel() {
		Resource resource = new ClassPathResource("features.model");
		FeatureModel model = null;
		try {
			model = new FeatureModel(resource.getFile());
		} catch (IOException e) {
			LOGGER.error("Failed to load file from {}", resource, e);
		}
		return new SequenceFactory(model, FormatterMode.INTELLIGENT);
	}
}
