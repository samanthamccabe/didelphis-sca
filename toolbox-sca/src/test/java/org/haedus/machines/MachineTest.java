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

import org.haedus.enums.ParseDirection;
import org.haedus.phonetic.Sequence;
import org.haedus.phonetic.SequenceFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 3/14/2015
 */
public class MachineTest {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(MachineTest.class);

	private static final SequenceFactory FACTORY = SequenceFactory.getEmptyFactory();

	@Test
	public void testBasicStateMachine01() {
		Machine stateMachine = getStateMachine("a");

		test(stateMachine, "a");
		test(stateMachine, "aa");

		fail(stateMachine, "b");
		fail(stateMachine, "c");
	}

	@Test
	public void testBasicStateMachine02() {
		Machine stateMachine = getStateMachine("aaa");

		test(stateMachine, "aaa");

		fail(stateMachine, "a");
		fail(stateMachine, "aa");
		fail(stateMachine, "b");
		fail(stateMachine, "c");
	}

	@Test
	public void testBasicStateMachine03() {
		Machine stateMachine = getStateMachine("aaa?");

		test(stateMachine, "aa");
		test(stateMachine, "aaa");

		fail(stateMachine, "a");
		fail(stateMachine, "b");
		fail(stateMachine, "c");
	}

	@Test
	public void testStateMachineStar() {
		Machine stateMachine = getStateMachine("aa*");

		test(stateMachine, "a");
		test(stateMachine, "aa");
		test(stateMachine, "aaa");
		test(stateMachine, "aaaa");
		test(stateMachine, "aaaaa");
		test(stateMachine, "aaaaaa");
	}

	@Test
	public void testStateMachinePlus() {
		Machine stateMachine = getStateMachine("a+");

		test(stateMachine, "a");
		test(stateMachine, "aa");
		test(stateMachine, "aaa");
		test(stateMachine, "aaaa");
		test(stateMachine, "aaaaa");
		test(stateMachine, "aaaaaa");

		test(stateMachine, "ab");
	}

	@Ignore
	@Test
	public void testGroups() {
		Machine stateMachine = getStateMachine("(ab)(cd)(ef)");

		test(stateMachine, "abcdef");
		fail(stateMachine, "abcd");
		fail(stateMachine, "ab");
		fail(stateMachine, "bcdef");
	}

	@Ignore
	@Test
	public void testGroupStar01() {
		Machine stateMachine = getStateMachine("(ab)*(cd)(ef)");

		test(stateMachine, "abababcdef");
		test(stateMachine, "ababcdef");
		test(stateMachine, "abcdef");
		test(stateMachine, "cdef");

		fail(stateMachine, "abcd");
		fail(stateMachine, "ab");
		fail(stateMachine, "bcdef");
		fail(stateMachine, "abbcdef");
	}

	@Ignore
	@Test
	public void testGroupStar02() {
		Machine stateMachine = getStateMachine("d(eo*)*b");

		test(stateMachine, "db");
		test(stateMachine, "deb");
		test(stateMachine, "deeb");
		test(stateMachine, "deob");
		test(stateMachine, "deoob");
		test(stateMachine, "deoeob");
		test(stateMachine, "deoeoob");

		fail(stateMachine, "abcd");
		fail(stateMachine, "ab");
		fail(stateMachine, "bcdef");
		fail(stateMachine, "abbcdef");
	}

	@Ignore
	@Test
	public void testGroupOptional01() {
		Machine stateMachine = getStateMachine("(ab)?(cd)(ef)");

		test(stateMachine, "abcdef");
		test(stateMachine, "cdef");
	}

	@Ignore
	@Test
	public void testSets01() {
		Machine stateMachine = getStateMachine("{ x ɣ }");

		test(stateMachine, "x");
		test(stateMachine, "ɣ");
		fail(stateMachine, " ");
	}

	@Ignore
	@Test
	public void testSets02() {
		Machine stateMachine = getStateMachine("{ab {cd xy} ef}tr");

		test(stateMachine, "abtr");
		test(stateMachine, "cdtr");
		test(stateMachine, "xytr");
		test(stateMachine, "eftr");
		fail(stateMachine, " ");
	}

	@Ignore
	@Test
	public void testSetsExtraSpace01() {
		Machine machine = getStateMachine("{cʰ  c  ɟ}");

		test(machine, "cʰ");
		test(machine, "c");
		test(machine, "ɟ");
	}

	@Ignore
	@Test
	public void testGroupPlus01() {
		Machine machine = getStateMachine("(ab)+");

		test(machine, "ab");
		test(machine, "abab");
		test(machine, "ababab");

	}

	@Ignore
	@Test
	public void testComplexGroups01() {
		Machine machine = getStateMachine("(a+l(ham+b)*ra)+");

		test(machine, "alhambra");
	}

	@Ignore
	@Test
	public void testComplexGroups02() {
		Machine machine = getStateMachine("{ab* (cd?)+ ((ae)*f)+}tr");

		test(machine, "abtr");
		test(machine, "cdtr");
	}

	@Ignore
	@Test
	public void testComplex02() {
		Machine machine = getStateMachine("{r l}?{a e o ā ē ō}{i u}?{n m l r}?{pʰ tʰ kʰ ḱʰ}us");

		test(machine, "āḱʰus");
	}

	@Ignore
	@Test
	public void testComplex03() {
		Machine machine = getStateMachine("a?{pʰ tʰ kʰ ḱʰ}us");

		test(machine, "pʰus");
		test(machine, "tʰus");
		test(machine, "kʰus");
		test(machine, "ḱʰus");
		test(machine, "aḱʰus");
	}

	@Ignore
	@Test
	public void testComplex04() {
		Machine machine = getStateMachine("{a e o ā ē ō}{pʰ tʰ kʰ ḱʰ}us");

		test(machine, "apʰus");
		test(machine, "atʰus");
		test(machine, "akʰus");
		test(machine, "aḱʰus");
	}

	@Ignore
	@Test
	public void testComplex01() {
		Machine machine = getStateMachine("a?(b?c?)d?b");

		test(machine, "b");
		test(machine, "db");
		test(machine, "bcdb");
		test(machine, "acdb");
		test(machine, "abdb");
		test(machine, "abcb");
		test(machine, "abcdb");
	}

	private static Machine getStateMachine(String expression) {
		return NodeFactory.getMachine(expression, FACTORY, ParseDirection.FORWARD, true);
	}

	private static void test(Machine stateMachine, String target) {
		Collection<Integer> matchIndices = testMachine(stateMachine, target);
		assertFalse("Machine failed to accept input", matchIndices.isEmpty());
	}

	private static void fail(Machine stateMachine, String target) {
		Collection<Integer> matchIndices = testMachine(stateMachine, target);
		assertTrue("Machine accepted input it should not have", matchIndices.isEmpty());
	}

	private static Collection<Integer> testMachine(Machine stateMachine, String target) {
		Sequence sequence = FACTORY.getSequence(target);
		Collection<Integer> matchIndices = stateMachine.getMatchIndices(0, sequence);
		LOGGER.debug("{} ran against \"{}\" and produced output {}",stateMachine, sequence, matchIndices);
		return matchIndices;
	}

}
