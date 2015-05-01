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

package org.haedus.soundchange;

import org.haedus.phonetic.VariableStore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Samantha Fiona Morrigan McCabe
 * 10/26/13.
 */
public class VariableStoreTest {

	@Test
	public void testVariableComplex01() {
		VariableStore vs = new VariableStore();
		vs.add("C  = p t k");
		vs.add("HC = hC");

		String expected =
				"C = p t k\n" +
				"HC = hp ht hk";
		assertEquals(expected, vs.toString());
	}

	@Test
	public void testVariableComplex02() {
		VariableStore vs = new VariableStore();
		vs.add("C  = p t ");
		vs.add("C2 = CC");

		String expected =
				"C = p t\n" +
				"C2 = pp pt tp tt";
		assertEquals(expected, vs.toString());
	}

	@Test
	public void testVariableExpansion01() {
		VariableStore vs = new VariableStore();

		vs.add("R = r l");
		vs.add("C = p t k R");

		String expected =
				"R = r l\n" +
				"C = p t k r l";
		assertEquals(expected, vs.toString());
	}

	@Test
	public void testVariableExpansion02()  {
		VariableStore vs = new VariableStore();

		vs.add("N = n m");
		vs.add("R = r l");
		vs.add("L = R w y");
		vs.add("C = p t k L N");

		String expected = "" +
				"N = n m\n" +
				"R = r l\n" +
				"L = r l w y\n" +
				"C = p t k r l w y n m";
		assertEquals(expected, vs.toString());
	}

	@Test
	public void testVariableExpansion03() {
		VariableStore vs = new VariableStore();

		vs.add("C = p t k");
		vs.add("H = x ɣ");
		vs.add("CH = pʰ tʰ kʰ");
		vs.add("[CONS] = CH C H");

		String expected = "" +
				"C = p t k\n" +
				"H = x ɣ\n" +
				"CH = pʰ tʰ kʰ\n" +
				"[CONS] = pʰ tʰ kʰ p t k x ɣ";
		assertEquals(expected, vs.toString());
	}
}
