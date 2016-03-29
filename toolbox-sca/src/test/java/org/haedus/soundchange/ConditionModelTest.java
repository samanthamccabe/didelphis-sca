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

package org.haedus.soundchange;

import org.haedus.enums.FormatterMode;
import org.haedus.phonetic.model.FeatureModel;
import org.haedus.phonetic.SequenceFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 4/19/2015
 */
public class ConditionModelTest {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(ConditionModelTest.class);

	private static final SequenceFactory FACTORY = loadModel();
	
	@Test
	public void testBasicStateMachine01() {
		Condition condition = new Condition("_a[+son, -hgh, +frn, -atr]+", FACTORY);

		fail(condition, "xa");
		test(condition, "xaa");
		test(condition, "xaaa");
		test(condition, "xaa̤");
		test(condition, "xaa̤a");

		test(condition, "xaa̤a");
		test(condition, "xaa̤");

		fail(condition, "xb");
		fail(condition, "xc");
	}

	@Test
	public void testComplex01() {
		Condition condition = new Condition("_[-con, +voice, -tense][-son, -voice, +vot]us", FACTORY);

		test(condition, "xapʰus");
		test(condition, "xatʰus");
		test(condition, "xakʰus");
		test(condition, "xaḱʰus");

		test(condition, "xepʰus");
		test(condition, "xetʰus");
		test(condition, "xekʰus");
		test(condition, "xeḱʰus");

		test(condition, "xopʰus");
		test(condition, "xotʰus");
		test(condition, "xokʰus");
		test(condition, "xoḱʰus");

		test(condition, "xāpʰus");
		test(condition, "xātʰus");
		test(condition, "xākʰus");
		test(condition, "xāḱʰus");

		test(condition, "xēpʰus");
		test(condition, "xētʰus");
		test(condition, "xēkʰus");
		test(condition, "xēḱʰus");

		test(condition, "xōpʰus");
		test(condition, "xōtʰus");
		test(condition, "xōkʰus");
		test(condition, "xōḱʰus");

		test(condition, "xipʰus");
		test(condition, "xitʰus");
		test(condition, "xikʰus");
		test(condition, "xiḱʰus");

		fail(condition, "xōpus");
		fail(condition, "xōtus");
		fail(condition, "xōkus");
		fail(condition, "xōḱus");

		fail(condition, "xa̰pʰus");
		fail(condition, "xa̰tʰus");
		fail(condition, "xa̰kʰus");
		fail(condition, "xa̰ḱʰus");
	}

	@Test
	public void testComplex02() {
		Condition condition = new Condition("_[-con][-son]us#", FACTORY);

		test(condition, "xapʰus");
		test(condition, "xatʰus");
		test(condition, "xakʰus");
		test(condition, "xaḱʰus");

		test(condition, "xepʰus");
		test(condition, "xetʰus");
		test(condition, "xekʰus");
		test(condition, "xeḱʰus");

		test(condition, "xāpʰus");
		test(condition, "xātʰus");
		test(condition, "xākʰus");
		test(condition, "xāḱʰus");

		test(condition, "xōpʰus");
		test(condition, "xōtʰus");
		test(condition, "xōkʰus");
		test(condition, "xōḱʰus");

		test(condition, "xōpus");
		test(condition, "xōtus");
		test(condition, "xōkus");
		test(condition, "xōḱus");

		test(condition, "xa̤pʰus");
		test(condition, "xa̤tʰus");
		test(condition, "xa̤kʰus");
		test(condition, "xa̤ḱʰus");

		fail(condition, "xpʰeus");
		fail(condition, "xtʰeus");
		fail(condition, "xkʰeus");
		fail(condition, "xḱʰeus");
	}

	private static void test(Condition condition, String target) {
		assertTrue(condition.isMatch(FACTORY.getSequence(target), 0));
	}

	private static void fail(Condition condition, String target) {
		assertFalse(condition.isMatch(FACTORY.getSequence(target), 0));
	}

	private static SequenceFactory loadModel() {
		InputStream stream = ConditionModelTest.class.getClassLoader().getResourceAsStream("AT_hybrid.model");
		FormatterMode mode = FormatterMode.INTELLIGENT;
		return new SequenceFactory(new FeatureModel(stream, mode),mode);
	}
}
