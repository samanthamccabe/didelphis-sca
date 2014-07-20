package org.haedus.datatypes.phonetic;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * User: goats
 * Date: 4/5/13
 * Time: 11:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class SequenceTest {

	@Test
	public void testGet() {
		Sequence received = new Sequence("Sequences");

		assertEquals(new Segment("S"), received.get(0));
		assertEquals(new Segment("e"), received.get(1));
		assertEquals(new Segment("q"), received.get(2));
		assertEquals(new Segment("s"), received.get(8));

		assertEquals(new Segment("S"), received.getFirst());
		assertEquals(new Segment("s"), received.getLast());
	}

	@Test
	public void testAddSequence() {
		Sequence received = new Sequence("Sequ");
		Sequence addition = new Sequence("ence");

		received.add(addition);

		Sequence expected = new Sequence("Sequence");

		assertEquals(expected, received);
	}

	@Test
	public void testAddArray() {
		Segment[] segments = {
				new Segment("w"),
				new Segment("o"),
				new Segment("r"),
				new Segment("d"),
		};

		Sequence received = new Sequence("a");
		received.add(segments);

		Sequence expected = new Sequence("aword");

		assertEquals(expected, received);
	}


	@Test
	public void testEquals01() {
		testEquals("sardo", "sardo", true);
		testEquals("sardo", "sardox", false);
		testEquals("sardo", "sārdo", false);
		testEquals("sardo", "saardo", false);
		testEquals("sardo", "sōrdo", false);
		testEquals("sardo", "serdox", false);
		testEquals("sardo", "ʃɛʁʔð", false);
	}

	@Test
	public void testEquals02() {
		testEquals("pʰāḱʰus", "pʰāḱʰus", true);
		testEquals("pʰāḱʰus", "bāḱʰus", false);
	}

	@Test
	public void testSubsequence01() {

		Sequence sequence = new Sequence("expiated");
		Sequence expected = new Sequence("iated");
		Sequence received = sequence.getSubsequence(3);

		assertEquals(expected, received);
	}

	@Test
	public void testSubsequence02() {

		Sequence sequence = new Sequence("expiated");
		Sequence expected = new Sequence("iat");
		Sequence received = sequence.getSubsequence(3, 6);

		assertEquals(expected, received);
	}

	@Test
	public void testSubsequence03() {

		Sequence sequence = new Sequence("expiated");
		Sequence expected = new Sequence("xpiat");
		Sequence received = sequence.getSubsequence(1, 6);

		assertEquals(expected, received);
	}

	@Test
	public void testIndexOf01() {
		Sequence sequence = new Sequence("expiated");

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

	private void testIndexOf(String e, Sequence sequence, int i) {
		assertEquals(i, sequence.indexOf(new Sequence(e)));
	}


	@Test
	public void testIndexOf02() {
		//                                01234567
		Sequence sequence = new Sequence("subverterunt");

		assertEquals(-1, sequence.indexOf(new Sequence("s"), 2));
		assertEquals(0, sequence.indexOf(new Sequence("s"), 0));
		assertEquals(4, sequence.indexOf(new Sequence("er"), 4));
		assertEquals(7, sequence.indexOf(new Sequence("er"), 7));
		assertEquals(11, sequence.indexOf(new Sequence("t"), 7));
	}

	@Test
	public void testIndices03() {
		Sequence sequence = new Sequence("subverterunt");

		int[] expected = {4, 7};
		int[] received = sequence.indicesOf(new Sequence("er"));

		assertTrue(arrayToString(received), equals(expected, received));
	}

	@Test
	public void testIndices04() {
		Sequence sequence = new Sequence("aonaontada");

		int[] expected = {0, 3};
		int[] received = sequence.indicesOf(new Sequence("ao"));

		assertTrue(arrayToString(received), equals(expected, received));
	}

	@Test
	public void testRemove01() {
		Sequence sequence = new Sequence("abcdefghijk");
		Sequence expected = new Sequence("cdefghijk");

		Sequence received = sequence.copy();
		Sequence removed = received.remove(0, 2);

		assertEquals(expected, received);
		assertEquals(removed, new Sequence("ab"));
	}

	@Test
	public void testRemove02() {
		Sequence sequence = new Sequence("abcdefghijk");
		Sequence expected = new Sequence("defghijk");

		Sequence received = sequence.copy();
		Sequence removed = received.remove(0, 3);

		assertEquals(expected, received);
		assertEquals(removed, new Sequence("abc"));
	}

	@Test
	public void testRemove03() {
		Sequence sequence = new Sequence("abcdefghijk");
		Sequence expected = new Sequence("adefghijk");

		Sequence received = sequence.copy();
		Sequence removed = received.remove(1, 3);

		assertEquals(expected, received);
		assertEquals(removed, new Sequence("bc"));
	}

	@Test
	public void testRemove04() {
		Sequence sequence = new Sequence("abcdefghijk");
		Sequence expected = new Sequence("abcghijk");

		Sequence received = sequence.copy();
		Sequence removed = received.remove(3, 6);

		assertEquals(expected, received);
		assertEquals(removed, new Sequence("def"));
	}

	@Test
	public void testRemove05() {
		Sequence sequence = new Sequence("abcdefghijk");
		Sequence expected = new Sequence("abcdhijk");

		Sequence received = sequence.copy();
		Sequence removed = received.remove(4, 7);

		assertEquals(expected, received);
		assertEquals(removed, new Sequence("efg"));
	}

	@Test
	public void testReplaceAllSequences01() {
		Sequence sequence = new Sequence("aoSaontada");
		Sequence expected = new Sequence("ouSountada");
		Sequence received = sequence.replaceAll(new Sequence("ao"), new Sequence("ou"));

		assertEquals(expected, received);
	}

	@Test
	public void testReplaceAllSequences02() {
		Sequence sequence = new Sequence("farcical");
		Sequence expected = new Sequence("faarcicaal");
		Sequence received = sequence.replaceAll(new Sequence("a"), new Sequence("aa"));

		assertEquals(expected, received);
	}

	@Test
	public void testReplaceAllSequences03() {
		Sequence sequence = new Sequence("farcical");
		Sequence expected = new Sequence("faearcicaeal");
		Sequence received = sequence.replaceAll(new Sequence("a"), new Sequence("aea"));

		assertEquals(expected, received);
	}

	@Test
	public void testReplaceAllSequences04() {
		Sequence sequence = new Sequence("onomotopaea");
		Sequence expected = new Sequence("ɔɤnɔɤmɔɤtɔɤpaea");
		Sequence received = sequence.replaceAll(new Sequence("o"), new Sequence("ɔɤ"));

		assertEquals(expected, received);
	}

	@Test
	public void testStartsWith01() {
		Sequence sequence = new Sequence("tekton");

		assertTrue(sequence.startsWith(new Sequence("tekton")));
		assertTrue(sequence.startsWith(new Sequence("tekto")));
		assertTrue(sequence.startsWith(new Sequence("tekt")));
		assertTrue(sequence.startsWith(new Sequence("tek")));
		assertTrue(sequence.startsWith(new Sequence("te")));
	}

	@Test
	public void testStartsWith02() {
		Sequence sequence = new Sequence("ton");

		assertTrue(sequence.startsWith(new Sequence("t")));
		assertTrue(sequence.startsWith(new Sequence("to")));
		assertTrue(sequence.startsWith(new Sequence("ton")));

		testNotStarstWith(sequence, "tons");
		testNotStarstWith(sequence, "tekton");
		testNotStarstWith(sequence, "tekto");
		testNotStarstWith(sequence, "tekt");
		testNotStarstWith(sequence, "tek");
		testNotStarstWith(sequence, "te");
	}

	@Test
	public void testStartsWith03() {
		Sequence sequence = new Sequence("elə");

		assertFalse(sequence.startsWith(new Sequence("eʔé")));
	}

	private void testNotStarstWith(Sequence sequence, String testString) {
		boolean doesStartWith = isStartWith(sequence, testString);
		assertFalse(doesStartWith);
	}

	private void testStartsWith(Sequence sequence, String testString) {
		boolean doesStartWith = isStartWith(sequence, testString);
		assertTrue(doesStartWith);
	}

	private boolean isStartWith(Sequence sequence, String testString) {
		Sequence testSequence = new Sequence(testString);
		return sequence.startsWith(testSequence);
	}

	private boolean equals(int[] expected, int[] received) {
		boolean equal = (expected.length == received.length);
		if (equal) {
			for (int i = 0; i < expected.length; i++) {
				equal &= (expected[i] == received[i]);
			}
		}
		return equal;
	}

	private String arrayToString(int[] array) {
		String s = "[ ";
		for (int i : array) {
			s = s + i + " ";
		}
		return s + "]";
	}

	private void testEquals(String expected, String received, boolean equals) {
		Segment ex = new Segment(expected);
		Segment re = new Segment(received);

		boolean isEqual = ex.equals(re);

		if (equals)
			assertTrue(isEqual);
		else
			assertFalse(isEqual);
	}
}

