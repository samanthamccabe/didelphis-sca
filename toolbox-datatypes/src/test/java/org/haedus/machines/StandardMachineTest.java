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
import org.haedus.enums.ParseDirection;
import org.haedus.phonetic.Sequence;
import org.haedus.phonetic.SequenceFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 3/14/2015
 */
public class StandardMachineTest {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(StandardMachineTest.class);

	private static final SequenceFactory FACTORY = SequenceFactory.getEmptyFactory();

	@Test
	public void testBasicStateMachine01() {
		StandardMachine stateMachine = getStateMachine("a");

		test(stateMachine, "a");
		test(stateMachine, "aa");

		fail(stateMachine, "b");
		fail(stateMachine, "c");
	}

	@Test
	public void testBasicStateMachine02() {
		StandardMachine stateMachine = getStateMachine("aaa");

		test(stateMachine, "aaa");

		fail(stateMachine, "a");
		fail(stateMachine, "aa");
		fail(stateMachine, "b");
		fail(stateMachine, "c");
	}

	@Test
	public void testBasicStateMachine03() throws IOException {
		StandardMachine stateMachine = getStateMachine("aaa?");

		test(stateMachine, "aa");
		test(stateMachine, "aaa");

		fail(stateMachine, "a");
		fail(stateMachine, "b");
		fail(stateMachine, "c");
	}

	@Test
	public void testBasicStateMachine04() throws IOException {
		StandardMachine stateMachine = getStateMachine("ab*cd?ab");

		test(stateMachine, "acab");
		test(stateMachine, "abcab");
		test(stateMachine, "abbcab");
		test(stateMachine, "abbbcab");

		test(stateMachine, "acdab");
		test(stateMachine, "abcdab");
		test(stateMachine, "abbcdab");
		test(stateMachine, "abbbcdab");

		fail(stateMachine, "acddab");
		fail(stateMachine, "abcddab");
		fail(stateMachine, "abbcddab");
		fail(stateMachine, "abbbcddab");
	}

	@Test
	public void testStateMachineStar() throws IOException {
		StandardMachine stateMachine = getStateMachine("aa*");

		test(stateMachine, "a");
		test(stateMachine, "aa");
		test(stateMachine, "aaa");
		test(stateMachine, "aaaa");
		test(stateMachine, "aaaaa");
		test(stateMachine, "aaaaaa");
	}

	@Test
	public void testStateMachinePlus() throws IOException {
		StandardMachine stateMachine = getStateMachine("a+");

		test(stateMachine, "a");
		test(stateMachine, "aa");
		test(stateMachine, "aaa");
		test(stateMachine, "aaaa");
		test(stateMachine, "aaaaa");
		test(stateMachine, "aaaaaa");

		test(stateMachine, "ab");
	}

	@Test
	public void testGroups() {
		StandardMachine stateMachine = getStateMachine("(ab)(cd)(ef)");

		test(stateMachine, "abcdef");
		fail(stateMachine, "abcd");
		fail(stateMachine, "ab");
		fail(stateMachine, "bcdef");
	}

	@Test
	public void testGroupStar01() {
		StandardMachine stateMachine = getStateMachine("(ab)*(cd)(ef)");

		test(stateMachine, "abababcdef");
		test(stateMachine, "ababcdef");
		test(stateMachine, "abcdef");
		test(stateMachine, "cdef");

		fail(stateMachine, "abcd");
		fail(stateMachine, "ab");
		fail(stateMachine, "bcdef");
		fail(stateMachine, "abbcdef");
	}

	@Test
	public void testGroupStar02() throws IOException {
		StandardMachine stateMachine = getStateMachine("d(eo*)*b");

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

	@Test
	public void testGroupOptional01() throws IOException {
		StandardMachine stateMachine = getStateMachine("(ab)?(cd)(ef)");

		test(stateMachine, "abcdef");
		test(stateMachine, "cdef");
	}

	
	@Test
	public void testSets01() throws IOException {
		StandardMachine stateMachine = getStateMachine("{ x ɣ }");

		test(stateMachine, "x");
		test(stateMachine, "ɣ");
		fail(stateMachine, " ");
	}

	@Test
	public void testSets02() throws IOException {
		StandardMachine stateMachine = getStateMachine("{ab {cd xy} ef}tr");

		test(stateMachine, "abtr");
		test(stateMachine, "cdtr");
		test(stateMachine, "xytr");
		test(stateMachine, "eftr");
		fail(stateMachine, " ");
	}

	@Test
	public void testSetsExtraSpace01() {
		StandardMachine machine = getStateMachine("{cʰ  c  ɟ}");

		test(machine, "cʰ");
		test(machine, "c");
		test(machine, "ɟ");
	}

	@Test
	public void testGroupPlus01() throws IOException {
		StandardMachine machine = getStateMachine("(ab)+");

		test(machine, "ab");
		test(machine, "abab");
		test(machine, "ababab");

	}

	@Test
	public void testComplexGroups01() throws IOException {
		StandardMachine machine = getStateMachine("(a+l(ham+b)*ra)+");

		test(machine, "alhambra");
	}

	@Test
	public void testComplex02() {
		StandardMachine machine = getStateMachine("{r l}?{a e o ā ē ō}{i u}?{n m l r}?{pʰ tʰ kʰ ḱʰ}us");

		test(machine, "āḱʰus");
	}

	@Test
	public void testComplex03() {
		StandardMachine machine = getStateMachine("a?{pʰ tʰ kʰ ḱʰ}us");

		test(machine, "pʰus");
		test(machine, "tʰus");
		test(machine, "kʰus");
		test(machine, "ḱʰus");
		test(machine, "aḱʰus");
	}

	@Test
	public void testComplex04() {
		StandardMachine machine = getStateMachine("{a e o ā ē ō}{pʰ tʰ kʰ ḱʰ}us");

		test(machine, "apʰus");
		test(machine, "atʰus");
		test(machine, "akʰus");
		test(machine, "aḱʰus");
	}

	@Test
	public void testComplex01() throws IOException {
		StandardMachine machine = getStateMachine("a?(b?c?)d?b");

		test(machine, "b");
		test(machine, "db");
		test(machine, "bcdb");
		test(machine, "acdb");
		test(machine, "abdb");
		test(machine, "abcb");
		test(machine, "abcdb");
	}

	@Test
	public void testComplex05() {
		StandardMachine machine = getStateMachine("{ab* (cd?)+ ((ae)*f)+}tr");

		test(machine, "abtr");
		test(machine, "cdtr");
		test(machine, "ftr");
		test(machine, "aeftr");
		test(machine, "aeaeftr");

		test(machine, "cctr");
		test(machine, "ccctr");
 		test(machine, "fftr");
		test(machine, "aefaeftr");
		test(machine, "aefffffaeftr");

		fail(machine, "abcd");
		fail(machine, "tr");
	}

//	@Test
//	public void testDot01() throws IOException {
//		StandardMachine machine = getStateMachine("..");
//
//		test(machine, "ab");
//		test(machine, "db");
//		test(machine, "bcdb");
//		test(machine, "acdb");
//		test(machine, "abdb");
//		test(machine, "abcb");
//		test(machine, "abcdb");
//
//		fail(machine, "a");
//		fail(machine, "b");
//		fail(machine, "c");
//		fail(machine, "d");
//		fail(machine, "e");
//	}

	private static StandardMachine getStateMachine(String expression) {
		StandardMachine stateMachine = StandardMachine.createStandardMachine("M0", expression, FACTORY, ParseDirection.FORWARD);
		String graphML = stateMachine.getGraph();
		try {
			FileUtils.write(new File("test.graphml"), graphML);
		} catch (IOException e) {
			LOGGER.error("failed to write graph", e);
		}
		return stateMachine;
	}

	private static void test(StandardMachine stateMachine, String target) {
		Collection<Integer> matchIndices = testMachine(stateMachine, target);
		assertFalse("Machine failed to accept input", matchIndices.isEmpty());
	}

	private static void fail(StandardMachine stateMachine, String target) {
		Collection<Integer> matchIndices = testMachine(stateMachine, target);
		assertTrue("Machine accepted input it should not have", matchIndices.isEmpty());
	}

	private static Collection<Integer> testMachine(StandardMachine stateMachine, String target) {
		Sequence sequence = FACTORY.getSequence(target);
		Collection<Integer> matchIndices = stateMachine.getMatchIndices(0, sequence);
		LOGGER.debug("{} ran against \"{}\" and produced output {}",stateMachine, sequence, matchIndices);
		return matchIndices;
	}

}
