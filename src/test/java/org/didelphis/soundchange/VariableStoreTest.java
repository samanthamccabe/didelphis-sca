/*=============================================================================
 = Copyright (c) 2017. Samantha Fiona McCabe (Didelphis)
 =
 = Licensed under the Apache License, Version 2.0 (the "License");
 = you may not use this file except in compliance with the License.
 = You may obtain a copy of the License at
 =     http://www.apache.org/licenses/LICENSE-2.0
 = Unless required by applicable law or agreed to in writing, software
 = distributed under the License is distributed on an "AS IS" BASIS,
 = WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 = See the License for the specific language governing permissions and
 = limitations under the License.
 =============================================================================*/

package org.didelphis.soundchange;

import lombok.NonNull;
import org.didelphis.language.parsing.FormatterMode;
import org.didelphis.language.parsing.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VariableStoreTest {

	@Test
	void testVariableComplex01() {
		VariableStore vs = new VariableStore(FormatterMode.NONE);
		vs.add("C  = p t k");
		vs.add("HC = hC");

		assertTrue(vs.getKeys().contains("C"));
		assertTrue(vs.getKeys().contains("HC"));

		assertCorrect(vs, "C", "p", "t", "k");
		assertCorrect(vs, "HC", "hp", "ht", "hk");
	}

	@Test
	void testVariableComplex02() {
		VariableStore vs = new VariableStore(FormatterMode.NONE);
		vs.add("C  = p t ");
		vs.add("C2 = CC");

		assertTrue(vs.getKeys().contains("C"));
		assertTrue(vs.getKeys().contains("C2"));

		assertCorrect(vs, "C", "p", "t");
		assertCorrect(vs, "C2", "pp", "pt", "tp", "tt");
	}

	@Test
	void testVariableExpansion01() {
		VariableStore vs = new VariableStore(FormatterMode.NONE);

		vs.add("R = r l");
		vs.add("C = p t k R");

		assertTrue(vs.getKeys().contains("C"));
		assertTrue(vs.getKeys().contains("C2"));

		assertCorrect(vs, "C", "p", "t");
		assertCorrect(vs, "C2", "pp", "pt", "tp", "tt");
	}

	@Test
	void testVariableExpansion02() {
		VariableStore vs = new VariableStore(FormatterMode.NONE);

		vs.add("N = n m");
		vs.add("R = r l");
		vs.add("L = R w y");
		vs.add("C = p t k L N");

		Set<String> keys = vs.getKeys();
		assertTrue(keys.contains("N"));
		assertTrue(keys.contains("R"));
		assertTrue(keys.contains("L"));
		assertTrue(keys.contains("C"));

		assertCorrect(vs, "N", "n", "m");
		assertCorrect(vs, "R", "r", "l");
		assertCorrect(vs, "L", "r", "l", "w", "y");
		assertCorrect(vs, "C", "p", "t", "k", "r", "l", "w", "y", "n", "m");
	}

	@Test
	void testVariableExpansion03() {
		VariableStore vs = new VariableStore(FormatterMode.INTELLIGENT);

		vs.add("C = p t k");
		vs.add("H = x ɣ");
		vs.add("CH = pʰ tʰ kʰ");
		vs.add("[CONS] = CH C H");

		Set<String> keys = vs.getKeys();
		assertTrue(keys.contains("C"));
		assertTrue(keys.contains("H"));
		assertTrue(keys.contains("CH"));
		assertTrue(keys.contains("[CONS]"));

		assertCorrect(vs, "C", "p", "t", "k");
		assertCorrect(vs, "H", "x", "ɣ");
		assertCorrect(vs, "CH", "pʰ", "tʰ", "kʰ");
		assertCorrect(vs, "[CONS]", "pʰ", "tʰ", "kʰ", "p", "t", "k", "x", "ɣ");
	}

	@Test
	void testVariableExpansionComplex01() {
		VariableStore vs = new VariableStore(FormatterMode.NONE);
		
		vs.add("@Q  = kʷʰ kʷ gʷ");
		vs.add("@K  = kʰ  k  g");
		vs.add("@KY = cʰ  c  ɟ");
		vs.add("@P  = pʰ  p  b");
		vs.add("@T  = tʰ  t  d");
		vs.add("[PLOSIVE] = @P @T @KY @K @Q");

		Set<String> keys = vs.getKeys();
		assertTrue(keys.contains("@Q"));
		assertTrue(keys.contains("@K"));
		assertTrue(keys.contains("@KY"));
		assertTrue(keys.contains("@P"));
		assertTrue(keys.contains("@T"));
		assertTrue(keys.contains("[PLOSIVE]"));

		assertCorrect(vs, "@Q", "kʷʰ", "kʷ", "gʷ");
		assertCorrect(vs, "@K", "kʰ", "k", "g");
		assertCorrect(vs, "@KY", "cʰ", "c", "ɟ");
		assertCorrect(vs, "@P", "pʰ", "p", "b");
		assertCorrect(vs, "@T", "tʰ", "t", "d");
		assertCorrect(vs, "[PLOSIVE]", "pʰ", "p", "b", "tʰ", "t", "d", "cʰ", "c", "ɟ", "kʰ", "k", "g", "kʷʰ", "kʷ", "gʷ");
	}

	@Test
	void testMultipleEquals() {
		VariableStore vs = new VariableStore(FormatterMode.NONE);
		assertThrows(ParseException.class, () -> vs.add("A = B = C"));
	}

	private static List<String> toList(String... strings) {
		return new ArrayList<>(Arrays.asList(strings));
	}

	private static void assertCorrect(
			@NonNull VariableStore vs,
			@NonNull String key,
			@NonNull String... values
	) {
		assertEquals(toList(values), vs.get(key));
	}
}
