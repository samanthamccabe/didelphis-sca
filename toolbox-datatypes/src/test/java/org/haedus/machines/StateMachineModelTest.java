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
import org.haedus.exceptions.ParseException;
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
import java.util.Collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 2/28/2015
 */
public class StateMachineModelTest {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(StateMachineModelTest.class);

	private static final SequenceFactory FACTORY = loadModel();

	@Test(expected = ParseException.class)
	public void testBasicStateMachine00() {
		getMachine("[]");
	}

	@Test
	public void testDot() {
		StateMachine stateMachine = getMachine(".");

		test(stateMachine, "a");
		test(stateMachine, "b");
		test(stateMachine, "c");

		fail(stateMachine, "");
	}

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

		fail(stateMachine, "a");
		test(stateMachine, "aa");
		test(stateMachine, "aaa");
		test(stateMachine, "aa̤");
		test(stateMachine, "aa̤a");

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

	@Test
	public void testComplex02() {
		StateMachine machine = getMachine("{r l}?{a e o ā ē ō}{i u}?{n m l r}?{pʰ tʰ kʰ ḱʰ}us");

		test(machine, "āḱʰus");
		test(machine, "rāḱʰus");
		test(machine, "lāḱʰus");

		test(machine, "aiḱʰus");
		test(machine, "raiḱʰus");
		test(machine, "laiḱʰus");

		test(machine, "ānḱʰus");
		test(machine, "rānḱʰus");
		test(machine, "lānḱʰus");

		test(machine, "ātʰus");
		test(machine, "rātʰus");
		test(machine, "lātʰus");

		test(machine, "aitʰus");
		test(machine, "raitʰus");
		test(machine, "laitʰus");

		test(machine, "āntʰus");
		test(machine, "rāntʰus");
		test(machine, "lāntʰus");

		fail(machine, "āntus");
		fail(machine, "rāntus");
		fail(machine, "lāntus");

		fail(machine, "intʰus");
		fail(machine, "rintʰus");
		fail(machine, "lintʰus");
	}

	@Test
	public void testComplex03() {
		StateMachine machine = getMachine("a?{pʰ tʰ kʰ ḱʰ}us");

		test(machine, "pʰus");
		test(machine, "tʰus");
		test(machine, "kʰus");
		test(machine, "ḱʰus");
		test(machine, "aḱʰus");
	}

	@Test
	public void testComplex04() {
		StateMachine machine = getMachine("{a e o ā ē ō}{pʰ tʰ kʰ ḱʰ}us");

		test(machine, "apʰus");
		test(machine, "atʰus");
		test(machine, "akʰus");
		test(machine, "aḱʰus");

		test(machine, "epʰus");
		test(machine, "etʰus");
		test(machine, "ekʰus");
		test(machine, "eḱʰus");

		test(machine, "opʰus");
		test(machine, "otʰus");
		test(machine, "okʰus");
		test(machine, "oḱʰus");

		test(machine, "āpʰus");
		test(machine, "ātʰus");
		test(machine, "ākʰus");
		test(machine, "āḱʰus");

		test(machine, "ēpʰus");
		test(machine, "ētʰus");
		test(machine, "ēkʰus");
		test(machine, "ēḱʰus");

		test(machine, "ōpʰus");
		test(machine, "ōtʰus");
		test(machine, "ōkʰus");
		test(machine, "ōḱʰus");

		fail(machine, "ōpus");
		fail(machine, "ōtus");
		fail(machine, "ōkus");
		fail(machine, "ōḱus");
	}

	@Test
	public void testComplex05() {
		StateMachine machine = getMachine("[son:3 glt:0][son:0 glt:-3 rel:1 +vot]us");

		test(machine, "apʰus");
		test(machine, "atʰus");
		test(machine, "akʰus");
		test(machine, "aḱʰus");

		test(machine, "epʰus");
		test(machine, "etʰus");
		test(machine, "ekʰus");
		test(machine, "eḱʰus");

		test(machine, "opʰus");
		test(machine, "otʰus");
		test(machine, "okʰus");
		test(machine, "oḱʰus");

		test(machine, "āpʰus");
		test(machine, "ātʰus");
		test(machine, "ākʰus");
		test(machine, "āḱʰus");

		test(machine, "ēpʰus");
		test(machine, "ētʰus");
		test(machine, "ēkʰus");
		test(machine, "ēḱʰus");

		test(machine, "ōpʰus");
		test(machine, "ōtʰus");
		test(machine, "ōkʰus");
		test(machine, "ōḱʰus");

		test(machine, "ipʰus");
		test(machine, "itʰus");
		test(machine, "ikʰus");
		test(machine, "iḱʰus");

		fail(machine, "ōpus");
		fail(machine, "ōtus");
		fail(machine, "ōkus");
		fail(machine, "ōḱus");

		fail(machine, "a̤pʰus");
		fail(machine, "a̤tʰus");
		fail(machine, "a̤kʰus");
		fail(machine, "a̤ḱʰus");
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
