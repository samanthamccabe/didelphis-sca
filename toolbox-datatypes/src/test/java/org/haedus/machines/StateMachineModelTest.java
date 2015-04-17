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

import org.apache.commons.io.FileUtils;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		StateMachine stateMachine = getMachine("[son:3, +con, hgt:-1, +frn, -bck, -atr, glt:0]");

		test(stateMachine, "a");
		test(stateMachine, "aa");

		fail(stateMachine, "b");
		fail(stateMachine, "c");
	}

	@Test
	public void testBasicStateMachine03() {
		StateMachine stateMachine  = getMachine("a[son:3, +con, hgt:-1, +frn, -bck, -atr]+");
		StateMachine stateMachine1 = getMachine("a[son:3, +con, hgt:-1, +frn, -bck, -atr]+");
		StateMachine stateMachine2 = getMachine("a[son:3, +con, hgt:-1, +frn, -bck, -atr]+");

		fail(stateMachine, "a");
		test(stateMachine, "aa");
		test(stateMachine, "aaa");
		test(stateMachine, "aa̤");
		test(stateMachine, "aa̤a");

		test(stateMachine1, "aa̤a");
		test(stateMachine2, "aa̤");

		fail(stateMachine, "b");
		fail(stateMachine, "c");
	}

	@Test
	public void testBasicStateMachine02() {
		StateMachine stateMachine = getMachine("aaa");

		test(stateMachine, "aaa");

		fail(stateMachine, "a");
		fail(stateMachine, "aa");
		fail(stateMachine, "b");
		fail(stateMachine, "c");
	}

	@Test
	public void testStateMachineStar() {
		StateMachine stateMachine = getMachine("aa*");

		test(stateMachine, "a");
		test(stateMachine, "aa");
		test(stateMachine, "aaa");
		test(stateMachine, "aaaa");
		test(stateMachine, "aaaaa");
		test(stateMachine, "aaaaaa");
	}

	private static void test(StateMachine stateMachine, String target) {
		Collection<Integer> matchIndices = testMachine(stateMachine, target);
		assertFalse("Machine failed to accept input", matchIndices.isEmpty());
	}

	private static void fail(StateMachine stateMachine, String target) {
		Collection<Integer> matchIndices = testMachine(stateMachine, target);
		assertTrue("Machine accepted input it should not have", matchIndices.isEmpty());
	}


	private static StateMachine getMachine(String expression) {
		StateMachine stateMachine = StateMachine.createStandardMachine("M0", expression, FACTORY, ParseDirection.FORWARD);
		String graphML = stateMachine.getGraph();
		try {
			FileUtils.write(new File("test.graphml"), graphML, "UTF-8");
		} catch (IOException e) {
			LOGGER.error("failed to write graph", e);
		}
		return stateMachine;
	}

	private static Collection<Integer> testMachine(StateMachine stateMachine, String target) {
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
