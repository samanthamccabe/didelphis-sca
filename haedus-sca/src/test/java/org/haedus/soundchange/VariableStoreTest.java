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

		vs.put("R", "r l".split(" +"));
		vs.put("C", "p t k R".split(" +"));

		String expected = "C = p t k r l\n" +
		                  "R = r l";

		assertEquals(expected, vs.toString());
	}

	@Test
	public void testVariableExpansion02() {
		VariableStore vs = new VariableStore();

		vs.put("N", "n m".split(" +"));
		vs.put("R", "r l".split(" +"));
		vs.put("L", "R w y".split(" +"));
		vs.put("C", "p t k L N".split(" +"));

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

		vs.put("C", "p t k".split(" +"));
		vs.put("H", "x ɣ".split(" +"));
		vs.put("CH", "pʰ tʰ kʰ".split(" +"));
		vs.put("[CONS]", "CH C H".split(" +"));

		String expected = "" +
				"[CONS] = pʰ tʰ kʰ p t k x ɣ\n" +
				"CH = pʰ tʰ kʰ\n" +
				"C = p t k\n" +
				"H = x ɣ";

		assertEquals(expected, vs.toString());
	}
}
