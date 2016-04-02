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
import org.haedus.exceptions.ParseException;
import org.haedus.phonetic.model.FeatureModel;
import org.haedus.phonetic.SequenceFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 2/28/2015
 */
public class StateMachineModelTest extends MachineTestBase{

	private static final transient Logger LOGGER = LoggerFactory.getLogger(StateMachineModelTest.class);

	@BeforeClass
	public static void loadModel() {
		InputStream stream = StateMachineModelTest.class.getClassLoader().getResourceAsStream("AT_hybrid.model");

		FormatterMode mode = FormatterMode.INTELLIGENT;
		FeatureModel model = new FeatureModel(stream, mode);

		FACTORY = new SequenceFactory(model, mode);
	}

	@Test(expected = ParseException.class)
	public void testBasicStateMachine00() {
		getMachine("[]");
	}

	@Test
	public void testDot() {
		Machine machine = getMachine(".");

		test(machine, "a");
		test(machine, "b");
		test(machine, "c");

		fail(machine, "");
	}

	@Test
	public void testBasicStateMachine01() {
		Machine machine = getMachine("[-con, +son, -hgh, +frn, -atr, +voice]");

		test(machine, "a");
		test(machine, "aa");

		fail(machine, "b");
		fail(machine, "c");
	}

	@Test
	public void testBasicStateMachine03() {
		Machine machine = getMachine("a[-con, +son, -hgh, +frn]+");

		fail(machine, "a");
		test(machine, "aa");
		test(machine, "aaa");
		test(machine, "aa̤");
		test(machine, "aa̤a");

		fail(machine, "b");
		fail(machine, "c");
	}

	@Test
	public void testBasicStateMachine02() {
		Machine machine = getMachine("aaa");

		test(machine, "aaa");

		fail(machine, "a");
		fail(machine, "aa");
		fail(machine, "b");
		fail(machine, "c");
	}

	@Test
	public void testStateMachineStar() {
		Machine machine = getMachine("aa*");

		test(machine, "a");
		test(machine, "aa");
		test(machine, "aaa");
		test(machine, "aaaa");
		test(machine, "aaaaa");
		test(machine, "aaaaaa");
	}

	@Test
	public void testComplex02() {
		Machine machine = getMachine("{r l}?{a e o ā ē ō}{i u}?{n m l r}?{pʰ tʰ kʰ ḱʰ}us");

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
		Machine machine = getMachine("a?{pʰ tʰ kʰ ḱʰ}us");

		test(machine, "pʰus");
		test(machine, "tʰus");
		test(machine, "kʰus");
		test(machine, "ḱʰus");
		test(machine, "aḱʰus");
	}

	@Test
	public void testComplex04() {
		Machine machine = getMachine("{a e o ā ē ō}{pʰ tʰ kʰ ḱʰ}us");

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
		Machine machine = getMachine("[-con, +voice, -creaky][-son, -voice, +vot]us");

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

		fail(machine, "a̰pʰus");
		fail(machine, "a̰tʰus");
		fail(machine, "a̰kʰus");
		fail(machine, "a̰ḱʰus");
	}
}
