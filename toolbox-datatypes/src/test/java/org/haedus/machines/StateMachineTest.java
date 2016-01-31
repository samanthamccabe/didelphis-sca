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
import org.haedus.exceptions.ParseException;
import org.haedus.phonetic.Sequence;
import org.haedus.phonetic.SequenceFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 3/14/2015
 */
public class StateMachineTest {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(StateMachineTest.class);

	private static final SequenceFactory FACTORY = SequenceFactory.getEmptyFactory();

	@Test(expected = ParseException.class)
	public void testIllegalBoundary01() {
		getStateMachine("a#?");
	}

	@Test(expected = ParseException.class)
	public void testIllegalBoundary02() {
		getStateMachine("a#+");
	}

	@Test(expected = ParseException.class)
	public void testIllegalBoundary03() {
		getStateMachine("a#*");
	}

	@Test(expected = ParseException.class)
	public void testIllegalBoundary04() {
		getStateMachine("#*a");
	}

	@Test(expected = ParseException.class)
	public void testIllegalBoundary05() {
		getStateMachine("a#a");
	}

	@Test(expected = ParseException.class)
	public void testIllegalBoundary06() {
		getStateMachine("a(#a)a");
	}

	@Test
	public void testNegativeStateMachine01() {
		Machine machine = getStateMachine("!a");
		fail(machine, "a");

		test(machine, "b");
		test(machine, "c");
	}
	
	@Test
	public void testBasicStateMachine01() {
		Machine machine = getStateMachine("a");

		test(machine, "a");
		test(machine, "aa");

		fail(machine, "b");
		fail(machine, "c");
	}

	@Test
	public void testBasicStateMachine02() {
		Machine machine = getStateMachine("aaa");

		test(machine, "aaa");

		fail(machine, "a");
		fail(machine, "aa");
		fail(machine, "b");
		fail(machine, "c");
	}

	@Test
	public void testBasicStateMachine03() throws IOException {
		Machine machine = getStateMachine("aaa?");

		test(machine, "aa");
		test(machine, "aaa");

		fail(machine, "a");
		fail(machine, "b");
		fail(machine, "c");
	}

	@Test
	public void testBasicStateMachine04() throws IOException {
		Machine machine = getStateMachine("ab*cd?ab");

		test(machine, "acab");
		test(machine, "abcab");
		test(machine, "abbcab");
		test(machine, "abbbcab");

		test(machine, "acdab");
		test(machine, "abcdab");
		test(machine, "abbcdab");
		test(machine, "abbbcdab");

		fail(machine, "acddab");
		fail(machine, "abcddab");
		fail(machine, "abbcddab");
		fail(machine, "abbbcddab");
	}

	@Test
	public void testStateMachineStar() throws IOException {
		Machine machine = getStateMachine("aa*");

		test(machine, "a");
		test(machine, "aa");
		test(machine, "aaa");
		test(machine, "aaaa");
		test(machine, "aaaaa");
		test(machine, "aaaaaa");
	}

	@Test
	public void testStateMachinePlus() throws IOException {
		Machine machine = getStateMachine("a+");

		test(machine, "a");
		test(machine, "aa");
		test(machine, "aaa");
		test(machine, "aaaa");
		test(machine, "aaaaa");
		test(machine, "aaaaaa");

		test(machine, "ab");
	}

	@Test
	public void testGroups() {
		Machine machine = getStateMachine("(ab)(cd)(ef)");

		test(machine, "abcdef");
		fail(machine, "abcd");
		fail(machine, "ab");
		fail(machine, "bcdef");
	}

	@Test
	public void testGroupStar01() {
		Machine machine = getStateMachine("(ab)*(cd)(ef)");

		test(machine, "abababcdef");
		test(machine, "ababcdef");
		test(machine, "abcdef");
		test(machine, "cdef");

		fail(machine, "abcd");
		fail(machine, "ab");
		fail(machine, "bcdef");
		fail(machine, "abbcdef");
	}

	@Test
	public void testGroupStar02() throws IOException {
		Machine machine = getStateMachine("d(eo*)*b");

		test(machine, "db");
		test(machine, "deb");
		test(machine, "deeb");
		test(machine, "deob");
		test(machine, "deoob");
		test(machine, "deoeob");
		test(machine, "deoeoob");

		fail(machine, "abcd");
		fail(machine, "ab");
		fail(machine, "bcdef");
		fail(machine, "abbcdef");
	}

	@Test
	public void testGroupOptional01() throws IOException {
		Machine machine = getStateMachine("(ab)?(cd)(ef)");

		test(machine, "abcdef");
		test(machine, "cdef");
	}

	
	@Test
	public void testSets01() throws IOException {
		Machine machine = getStateMachine("{ x ɣ }");

		test(machine, "x");
		test(machine, "ɣ");
		fail(machine, " ");
	}

	@Test
	public void testSets02() throws IOException {
		Machine machine = getStateMachine("{ab {cd xy} ef}tr");

		test(machine, "abtr");
		test(machine, "cdtr");
		test(machine, "xytr");
		test(machine, "eftr");
		fail(machine, " ");
	}

	@Test
	public void testSetsExtraSpace01() {
		Machine machine = getStateMachine("{cʰ  c  ɟ}");

		test(machine, "cʰ");
		test(machine, "c");
		test(machine, "ɟ");
	}

	@Test
	public void testGroupPlus01() throws IOException {
		Machine machine = getStateMachine("(ab)+");

		test(machine, "ab");
		test(machine, "abab");
		test(machine, "ababab");

	}

	@Test
	public void testComplexGroups01() throws IOException {
		Machine machine = getStateMachine("(a+l(ham+b)*ra)+");

		test(machine, "alhambra");
	}

	@Test
	public void testComplex02() {
		Machine machine = getStateMachine("{r l}?{a e o ā ē ō}{i u}?{n m l r}?{pʰ tʰ kʰ ḱʰ}us");

		test(machine, "āḱʰus");
	}

	@Test
	public void testComplex03() {
		Machine machine = getStateMachine("a?{pʰ tʰ kʰ ḱʰ}us");

		test(machine, "pʰus");
		test(machine, "tʰus");
		test(machine, "kʰus");
		test(machine, "ḱʰus");
		test(machine, "aḱʰus");
	}

	@Test
	public void testComplex04() {
		Machine machine = getStateMachine("{a e o ā ē ō}{pʰ tʰ kʰ ḱʰ}us");

		test(machine, "apʰus");
		test(machine, "atʰus");
		test(machine, "akʰus");
		test(machine, "aḱʰus");
	}

	@Test
	public void testComplex01() throws IOException {
		Machine machine = getStateMachine("a?(b?c?)d?b");

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
		Machine machine = getStateMachine("{ab* (cd?)+ ((ae)*f)+}tr");

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

	@Test
	public void testDot01() throws IOException {
		Machine machine = getStateMachine("..");

		test(machine, "ab");
		test(machine, "db");
		test(machine, "bcdb");
		test(machine, "acdb");
		test(machine, "abdb");
		test(machine, "abcb");
		test(machine, "abcdb");

		fail(machine, "a");
		fail(machine, "b");
		fail(machine, "c");
		fail(machine, "d");
		fail(machine, "e");
		fail(machine, "");
	}

	@Test
	public void testDot02() throws IOException {
		Machine machine = getStateMachine("a..");

		test(machine, "abb");
		test(machine, "acdb");
		test(machine, "abdb");
		test(machine, "abcb");
		test(machine, "abcdb");

		fail(machine, "");
		fail(machine, "a");
		fail(machine, "b");
		fail(machine, "c");
		fail(machine, "d");
		fail(machine, "e");
		fail(machine, "aa");
		fail(machine, "db");
		fail(machine, "bcdb");
	}

	@Test
	public void testGroupsDot() {
		Machine machine = getStateMachine(".*(cd)(ef)");

		test(machine, "cdef");
		test(machine, "bcdef");
		test(machine, "abcdef");
		test(machine, "xabcdef");
		test(machine, "xyabcdef");

		fail(machine, "abcd");
		fail(machine, "ab");
	}

	@Test
	public void testGroupsDotPlus() {
		Machine machine = getStateMachine(".+(cd)(ef)");

		test(machine, "bcdef");
		test(machine, "abcdef");
		test(machine, "xabcdef");
		test(machine, "xyabcdef");

		fail(machine, "cdef");
		fail(machine, "abcd");
		fail(machine, "ab");
	}

	@Test
	public void testGroupsDotStar() {
		Machine machine = getStateMachine("(a.)*cd#");

		test(machine, "cd");
		test(machine, "aXcd");
		test(machine, "aXaYcd");
		test(machine, "aXaYaZcd");

		fail(machine, "cdef");
		fail(machine, "bcd");
		fail(machine, "acd");
	}

	private static Machine getStateMachine(String expression) {
		return StateMachine.create("M0", expression, FACTORY, ParseDirection.FORWARD);
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
		return stateMachine.getMatchIndices(0, sequence);
	}

}
