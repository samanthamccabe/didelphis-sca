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

package org.haedus.phonetic;

import org.haedus.enums.FormatterMode;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SequenceTest {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(SequenceTest.class);

	private static final SequenceFactory FACTORY = new SequenceFactory(FormatterMode.INTELLIGENT);

	private static void testIndexOf(String e, Sequence sequence, int i) {
		assertEqual(i, sequence.indexOf(FACTORY.getSequence(e)));
	}

	private static void testNotStarstWith(Sequence sequence, String testString) {
		boolean doesStartWith = isStartWith(sequence, testString);
		assertFalse(doesStartWith);
	}

	private static boolean isStartWith(Sequence sequence, String testString) {
		Sequence testSequence = FACTORY.getSequence(testString);
		return sequence.startsWith(testSequence);
	}

	private static void assertEqual(int a, int b) {
		assertTrue(a == b);
	}

	private static void assertNotEqual(Object a, Object b) {
		assertFalse(a.equals(b));
	}

	@Test
	public void testMatches01() {
		Sequence sequence = FACTORY.getSequence("an");
		assertTrue(sequence.matches(FACTORY.getSequence("an")));
	}

	@Test
	public void testMatches02() {
		Sequence sequence = FACTORY.getSequence("a");
		assertFalse(sequence.matches(FACTORY.getSequence("n")));
	}

	@Test
	public void testGet() {
		Sequence received = FACTORY.getSequence("Sequences");

		assertEquals(FACTORY.getSegment("S"), received.get(0));
		assertEquals(FACTORY.getSegment("e"), received.get(1));
		assertEquals(FACTORY.getSegment("q"), received.get(2));
		assertEquals(FACTORY.getSegment("s"), received.get(8));
		assertEquals(FACTORY.getSegment("S"), received.getFirst());
		assertEquals(FACTORY.getSegment("s"), received.getLast());
	}

	@Test
	public void testAddSequence() {
		Sequence received = FACTORY.getSequence("Sequ");
		Sequence addition = FACTORY.getSequence("ence");
		Sequence expected = FACTORY.getSequence("Sequence");

		received.add(addition);

		assertEquals(expected, received);
	}

	@Test
	public void testAddArray() {

		Sequence received = FACTORY.getSequence("a");
		received.add(FACTORY.getSegment("w"));
		received.add(FACTORY.getSegment("o"));
		received.add(FACTORY.getSegment("r"));
		received.add(FACTORY.getSegment("d"));

		Sequence expected = FACTORY.getSequence("aword");

		assertEquals(expected, received);
	}

	@Test
	public void testEquals01() {
		assertEquals("sardo", "sardo");

		assertNotEqual("sardo", "sardox");
		assertNotEqual("sardo", "sārdo");
		assertNotEqual("sardo", "saardo");
		assertNotEqual("sardo", "sōrdo");
		assertNotEqual("sardo", "serdox");
		assertNotEqual("sardo", "ʃɛʁʔð");
	}

	@Test
	public void testEquals02() {
		assertEquals("pʰāḱʰus", "pʰāḱʰus");
		assertNotEqual("pʰāḱʰus", "bāḱʰus");
	}

	@Test
	public void testSubsequence01() {

		Sequence sequence = FACTORY.getSequence("expiated");
		Sequence expected = FACTORY.getSequence("iated");
		Sequence received = sequence.getSubsequence(3);

		assertEquals(expected, received);
	}

	@Test
	public void testSubsequence02() {

		Sequence sequence = FACTORY.getSequence("expiated");
		Sequence expected = FACTORY.getSequence("iat");
		Sequence received = sequence.getSubsequence(3, 6);

		assertEquals(expected, received);
	}

	@Test
	public void testSubsequence03() {

		Sequence sequence = FACTORY.getSequence("expiated");
		Sequence expected = FACTORY.getSequence("xpiat");
		Sequence received = sequence.getSubsequence(1, 6);

		assertEquals(expected, received);
	}

	@Test
	public void testIndexOf01() {
		Sequence sequence = FACTORY.getSequence("expiated");

		testIndexOf("e", sequence, 0);
		testIndexOf("ex", sequence, 0);
		testIndexOf("exp", sequence, 0);
		testIndexOf("expi", sequence, 0);
		testIndexOf("expia", sequence, 0);
		testIndexOf("expiat", sequence, 0);
		testIndexOf("expiate", sequence, 0);
		testIndexOf("expiated", sequence, 0);

		testIndexOf("x", sequence, 1);
		testIndexOf("xp", sequence, 1);
		testIndexOf("xpi", sequence, 1);
		testIndexOf("xpia", sequence, 1);
		testIndexOf("xpiat", sequence, 1);
		testIndexOf("xpiate", sequence, 1);
		testIndexOf("xpiated", sequence, 1);

		testIndexOf("p", sequence, 2);
		testIndexOf("pi", sequence, 2);
		testIndexOf("pia", sequence, 2);
		testIndexOf("piat", sequence, 2);
		testIndexOf("piate", sequence, 2);
		testIndexOf("piated", sequence, 2);

		testIndexOf("i", sequence, 3);
		testIndexOf("ia", sequence, 3);
		testIndexOf("iat", sequence, 3);
		testIndexOf("iate", sequence, 3);
		testIndexOf("iated", sequence, 3);

		testIndexOf("a", sequence, 4);
		testIndexOf("at", sequence, 4);
		testIndexOf("ate", sequence, 4);
		testIndexOf("ated", sequence, 4);

		testIndexOf("t", sequence, 5);
		testIndexOf("te", sequence, 5);
		testIndexOf("ted", sequence, 5);

		testIndexOf("d", sequence, 7);
	}

	@Test
	public void testIndexOf02() {
		//                                01234567
		Sequence sequence = FACTORY.getSequence("subverterunt");

		assertEqual(-1, sequence.indexOf(FACTORY.getSequence("s"), 2));
		assertEqual(0, sequence.indexOf(FACTORY.getSequence("s"), 0));
		assertEqual(4, sequence.indexOf(FACTORY.getSequence("er"), 4));
		assertEqual(7, sequence.indexOf(FACTORY.getSequence("er"), 7));
		assertEqual(11, sequence.indexOf(FACTORY.getSequence("t"), 7));
	}

	@Test
	public void testIndices03() {
		Sequence sequence = FACTORY.getSequence("subverterunt");

		List<Integer> expected = new ArrayList<Integer>();
		expected.add(4);
		expected.add(7);

		List<Integer> received = sequence.indicesOf(FACTORY.getSequence("er"));

		assertEquals(expected, received);
	}

	@Test
	public void testIndices04() {
		Sequence sequence = FACTORY.getSequence("aonaontada");

		List<Integer> expected = new ArrayList<Integer>();
		expected.add(0);
		expected.add(3);
		List<Integer> received = sequence.indicesOf(FACTORY.getSequence("ao"));

		assertEquals(expected, received);
	}

	@Test
	public void testRemove01() {
		Sequence sequence = FACTORY.getSequence("abcdefghijk");
		Sequence expected = FACTORY.getSequence("cdefghijk");

//		Sequence received = sequence.copy();
		Sequence received = new Sequence(sequence);
		Sequence removed = received.remove(0, 2);

		assertEquals(expected, received);
		assertEquals(removed, FACTORY.getSequence("ab"));
	}

	@Test
	public void testRemove02() {
		Sequence sequence = FACTORY.getSequence("abcdefghijk");
		Sequence expected = FACTORY.getSequence("defghijk");

		Sequence received = new Sequence(sequence);
		Sequence removed = received.remove(0, 3);

		assertEquals(expected, received);
		assertEquals(removed, FACTORY.getSequence("abc"));
	}

	@Test
	public void testRemove03() {
		Sequence sequence = FACTORY.getSequence("abcdefghijk");
		Sequence expected = FACTORY.getSequence("adefghijk");

		Sequence received = new Sequence(sequence);
		Sequence removed = received.remove(1, 3);

		assertEquals(expected, received);
		assertEquals(removed, FACTORY.getSequence("bc"));
	}

	@Test
	public void testRemove04() {
		Sequence sequence = FACTORY.getSequence("abcdefghijk");
		Sequence expected = FACTORY.getSequence("abcghijk");

		Sequence received = new Sequence(sequence);
		Sequence removed = received.remove(3, 6);

		assertEquals(expected, received);
		assertEquals(removed, FACTORY.getSequence("def"));
	}

	@Test
	public void testRemove05() {
		Sequence sequence = FACTORY.getSequence("abcdefghijk");
		Sequence expected = FACTORY.getSequence("abcdhijk");

		Sequence received = new Sequence(sequence);
		Sequence removed = received.remove(4, 7);

		assertEquals(expected, received);
		assertEquals(removed, FACTORY.getSequence("efg"));
	}

	@Test
	public void testReplaceAllSequences01() {
		Sequence sequence = FACTORY.getSequence("aoSaontada");
		Sequence expected = FACTORY.getSequence("ouSountada");
		Sequence received = sequence.replaceAll(FACTORY.getSequence("ao"), FACTORY.getSequence("ou"));

		assertEquals(expected, received);
	}

	@Test
	public void testReplaceAllSequences02() {
		Sequence sequence = FACTORY.getSequence("farcical");
		Sequence expected = FACTORY.getSequence("faarcicaal");
		Sequence received = sequence.replaceAll(FACTORY.getSequence("a"), FACTORY.getSequence("aa"));

		assertEquals(expected, received);
	}

	@Test
	public void testReplaceAllSequences03() {
		Sequence sequence = FACTORY.getSequence("farcical");
		Sequence expected = FACTORY.getSequence("faearcicaeal");
		Sequence received = sequence.replaceAll(FACTORY.getSequence("a"), FACTORY.getSequence("aea"));

		assertEquals(expected, received);
	}

	@Test
	public void testReplaceAllSequences04() {
		Sequence sequence = FACTORY.getSequence("onomotopaea");
		Sequence expected = FACTORY.getSequence("ɔɤnɔɤmɔɤtɔɤpaea");
		Sequence received = sequence.replaceAll(FACTORY.getSequence("o"), FACTORY.getSequence("ɔɤ"));

		assertEquals(expected, received);
	}

	@Test
	public void testStartsWith01() {
		Sequence sequence = FACTORY.getSequence("tekton");

		assertTrue(sequence.startsWith(FACTORY.getSequence("tekton")));
		assertTrue(sequence.startsWith(FACTORY.getSequence("tekto")));
		assertTrue(sequence.startsWith(FACTORY.getSequence("tekt")));
		assertTrue(sequence.startsWith(FACTORY.getSequence("tek")));
		assertTrue(sequence.startsWith(FACTORY.getSequence("te")));
	}

	@Test
	public void testStartsWith02() {
		Sequence sequence = FACTORY.getSequence("ton");

		assertTrue(sequence.startsWith(FACTORY.getSequence("t")));
		assertTrue(sequence.startsWith(FACTORY.getSequence("to")));
		assertTrue(sequence.startsWith(FACTORY.getSequence("ton")));

		testNotStarstWith(sequence, "tons");
		testNotStarstWith(sequence, "tekton");
		testNotStarstWith(sequence, "tekto");
		testNotStarstWith(sequence, "tekt");
		testNotStarstWith(sequence, "tek");
		testNotStarstWith(sequence, "te");
	}

	@Test
	public void testStartsWith03() {
		Sequence sequence = FACTORY.getSequence("elə");

		assertFalse(sequence.startsWith(FACTORY.getSequence("eʔé")));
	}
}

