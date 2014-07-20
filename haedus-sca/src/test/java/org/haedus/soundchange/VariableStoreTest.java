package org.haedus.soundchange;

import org.haedus.datatypes.phonetic.VariableStore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by goats on 10/26/13.
 */
public class VariableStoreTest {

	@Test
	public void testVariableExpansion01() {
		VariableStore vs = new VariableStore();

		vs.add("R", "r", "l");
		vs.add("C", "p", "t", "k", "R");

		String expected = "C = p t k r l\nR = r l";

		assertEquals(expected, vs.toString());
	}

	@Test
	public void testVariableExpansion02() {
		VariableStore vs = new VariableStore();

		vs.add("N", "n", "m");
		vs.add("R", "r", "l");
		vs.add("L", "R", "w", "y");
		vs.add("C", "p", "t", "k", "L", "N");

		String expected = "" +
				"C = p t k r l w y n m\n" +
				"R = r l\n" +
				"L = r l w y\n" +
				"N = n m";

		assertEquals(expected, vs.toString());
	}

	@Test
	public void testVariableExpansion03() {
		VariableStore vs = new VariableStore();

		vs.add("C", "p", "t", "k");
		vs.add("H", "x", "ɣ");
		vs.add("CH", "pʰ", "tʰ", "kʰ");
		vs.add("[CONS]", "CH", "C", "H" );

		String expected = "" +
				"[CONS] = pʰ tʰ kʰ p t k x ɣ\n" +
				"CH = pʰ tʰ kʰ\n" +
				"C = p t k\n" +
				"H = x ɣ";

		assertEquals(expected, vs.toString());
	}
}
