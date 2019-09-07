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

package org.didelphis.soundchange;

import org.didelphis.language.parsing.FormatterMode;
import org.didelphis.language.parsing.ParseException;
import org.didelphis.language.phonetic.SequenceFactory;
import org.didelphis.language.phonetic.features.IntegerFeature;
import org.didelphis.language.phonetic.model.FeatureModelLoader;
import org.didelphis.language.phonetic.sequences.Sequence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @since 0.0.0
 */
class ConditionTest {

	private static final FeatureModelLoader<Integer> EMPTY =
			IntegerFeature.INSTANCE.emptyLoader();

	private static final SequenceFactory<Integer> FACTORY =
			new SequenceFactory<>(EMPTY.getFeatureMapping(),
					FormatterMode.INTELLIGENT);

	// We just need to see that this parses correctly
	@DisplayName("Condition with underscore only")
	@Test
	void testEmptyCondition() {
		Condition<Integer> ignored = new Condition<>("_", FACTORY);
	}

	// We just need to see that this parses correctly
	@Test
	void testBadCondition() {
		assertThrows(ParseException.class,
				() -> new Condition<>("a_b_c", FACTORY));
	}

	@Test
	void testDoubleUnderscore() {
		assertThrows(ParseException.class,
				() -> new Condition<>("_ _", FACTORY));
	}

	@Test
	void testPreconditionMatchingSimple() {
		Condition<Integer> condition = new Condition<>("_x", FACTORY);
		Sequence<Integer> sequence = FACTORY.toSequence("bax");

		assertTrue(condition.isMatch(sequence, 1));
		assertFalse(condition.isMatch(sequence, 0));
		assertFalse(condition.isMatch(sequence, 2));
	}

	@Test
	void testPostconditionMatchingSimple() {
		Condition<Integer> condition = new Condition<>("b_", FACTORY);
		Sequence<Integer> sequence = FACTORY.toSequence("bax");

		assertTrue(condition.isMatch(sequence, 1));
		assertFalse(condition.isMatch(sequence, 0));
		assertFalse(condition.isMatch(sequence, 2));
	}

	@Test
	void testPostconditionMatching01() {
		Condition<Integer> condition = new Condition<>("a_x", FACTORY);
		Sequence<Integer> sequence = FACTORY.toSequence("balx");

		boolean match = condition.isMatch(sequence, 2);
		assertTrue(match, "");
	}

	@Test
	void testPostconditionMatching02() {
		Condition<Integer> condition = new Condition<>("b_#", FACTORY);
		Sequence<Integer> sequence = FACTORY.toSequence("aba");

		assertFalse(condition.isMatch(sequence, 0));
		assertFalse(condition.isMatch(sequence, 1));
		assertTrue(condition.isMatch(sequence, 2));
	}

	@Test
	void testPostconditionMatching03() {
		Condition<Integer> condition = new Condition<>("b_lx", FACTORY);
		Sequence<Integer> sequence = FACTORY.toSequence("balx");

		assertTrue(condition.isMatch(sequence, 1));
		assertFalse(condition.isMatch(sequence, 2));
		assertFalse(condition.isMatch(sequence, 3));
	}

	@Test
	void testPostconditionMatching04() {
		Condition<Integer> condition = new Condition<>("_lxpld", FACTORY);
		Sequence<Integer> sequence = FACTORY.toSequence("beralxpld");

		assertTrue(condition.isMatch(sequence, 3));
		assertFalse(condition.isMatch(sequence, 2));
	}

	@Test
	void testOptional01() {

		Condition<Integer> condition = new Condition<>("_a?(b?c?)d?b", FACTORY);

		String[] positive = {
				"xb",
				"xbb",
				"xcb",
				"xbcb",
				"xab",
				"xbab",
				"xdb",
				"xbdb",
				"xacb",
				"xbacb",
				"xadb",
				"xbadb",
				"xcdb",
				"xbcdb",
				"xabcb",
				"xbabcb"
		};

		assertAllMatch(condition, positive);
	}

	@Test
	void testOptional02() {

		Condition<Integer> condition = new Condition<>("_d?ab", FACTORY);

		assertMatches(condition, "xab", 0);
		assertMatches(condition, "xdab", 0);

		assertFalse(condition.isMatch(FACTORY.toSequence("xadb"), 0));
		assertFalse(condition.isMatch(FACTORY.toSequence("xacb"), 0));
		assertFalse(condition.isMatch(FACTORY.toSequence("xdb"), 0));
	}

	@Test
	void testOptional03() {

		Condition<Integer> condition =
				new Condition<>("_a(l(hamb)?ra)?#", FACTORY);

		assertMatches(condition, "xalhambra", 0);
		assertMatches(condition, "xalra", 0);
		assertMatches(condition, "xa", 0);
		assertNoMatch(condition, "xalh", 0);
	}

	@Test
	void testOptional04() {

		Condition<Integer> condition = new Condition<>("_a(ba)?b", FACTORY);

		assertMatches(condition, "xab", 0);
		assertMatches(condition, "xabab", 0);
		assertNoMatch(condition, "xalh", 0);
	}

	@Test
	void testStar01() {

		Condition<Integer> condition = new Condition<>("_a*b", FACTORY);

		assertTrue(condition.isMatch(FACTORY.toSequence("xb"), 0));
		assertTrue(condition.isMatch(FACTORY.toSequence("xab"), 0));
		assertTrue(condition.isMatch(FACTORY.toSequence("xaab"), 0));
		assertTrue(condition.isMatch(FACTORY.toSequence("xaaab"), 0));
		assertTrue(condition.isMatch(FACTORY.toSequence("xaaaab"), 0));
		assertTrue(condition.isMatch(FACTORY.toSequence("xaaaaab"), 0));
		assertFalse(condition.isMatch(FACTORY.toSequence("xcaaaab"), 0));
	}

	@Test
	void testStar02() {

		Condition<Integer> condition = new Condition<>("_aa*b", FACTORY);

		assertFalse(condition.isMatch(FACTORY.toSequence("xb"), 0));
		assertTrue(condition.isMatch(FACTORY.toSequence("xab"), 0));
		assertTrue(condition.isMatch(FACTORY.toSequence("xaab"), 0));
		assertTrue(condition.isMatch(FACTORY.toSequence("xaaab"), 0));
		assertTrue(condition.isMatch(FACTORY.toSequence("xaaaab"), 0));
		assertTrue(condition.isMatch(FACTORY.toSequence("xaaaaab"), 0));
		assertFalse(condition.isMatch(FACTORY.toSequence("xcaaaab"), 0));
	}

	@Test
	void testStar03() {

		Condition<Integer> condition = new Condition<>("_da*b", FACTORY);

		assertTrue(condition.isMatch(FACTORY.toSequence("xdb"), 0));
		assertTrue(condition.isMatch(FACTORY.toSequence("xdab"), 0));
		assertTrue(condition.isMatch(FACTORY.toSequence("xdaab"), 0));
		assertTrue(condition.isMatch(FACTORY.toSequence("xdaaab"), 0));
		assertTrue(condition.isMatch(FACTORY.toSequence("xdaaaab"), 0));
		assertTrue(condition.isMatch(FACTORY.toSequence("xdaaaaab"), 0));
		assertFalse(condition.isMatch(FACTORY.toSequence("xdcaaaab"), 0));
	}

	@Test
	void testStar04() {

		Condition<Integer> condition = new Condition<>("_d(eo)*b", FACTORY);

		assertTrue(condition.isMatch(FACTORY.toSequence("xdb"), 0));
		assertTrue(condition.isMatch(FACTORY.toSequence("xdeob"), 0));
		assertTrue(condition.isMatch(FACTORY.toSequence("xdeoeob"), 0));
		assertTrue(condition.isMatch(FACTORY.toSequence("xdeoeoeob"), 0));
		assertFalse(condition.isMatch(FACTORY.toSequence("xdcaaaab"), 0));
	}

	@Test
	void testStar05() {

		Condition<Integer> condition = new Condition<>("_d(eo*)*b", FACTORY);

		String[] positive = {
				"xdb",
				"xdeob",
				"xdeb",
				"xdeooeob",
				"xdeeb",
				"xdeoooeob",
				"xdeeeb",
				"xdeoeoob",
				"xdeeeeb",
				"xdeoeooob",
				"xdeoeob",
				"xdeoob",
				"xdeoeoeob",
				"xdeooob",
				"xdeoeoeoeob",
				"xdeoooob",
		};
		assertAllMatch(condition, positive);
	}

	@Test
	void testStar06() {
		Condition<Integer> condition = new Condition<>("_(ab)*#", FACTORY);
		String[] positive = {
				"x", "xababab", "xab", "xabababab", "xabab", "xababababab"
		};

		assertAllMatch(condition, positive);

		String[] negative = {
				"xa", "xabababa", "xaba", "xababababa", "xababa", "xabababababa"
		};

		assertNoneMatch(condition, negative);
	}

	@Test
	void testPlus01() {

		Condition<Integer> condition = new Condition<>("_a+b", FACTORY);

		String[] positive = {
				"xab", "xaab", "xaaab", "xaaaab", "xaaaaab",
		};

		assertAllMatch(condition, positive);

		assertNoMatch(condition, "xb");
		assertNoMatch(condition, "xcb");
		assertNoMatch(condition, "xacb");
		assertNoMatch(condition, "xaacb");
		assertNoMatch(condition, "xaaacb");
		assertNoMatch(condition, "xba");
	}

	@Test
	void testPlus02() {

		Condition<Integer> condition =
				new Condition<>("_a+l(ham+b)+ra", FACTORY);

		String[] positive = {
				"xalhambra",
				"xaalhambra",
				"xalhambhambhambra",
				"xalhammbra",
				"xaaalhambra",
				"xalhammbhammbra",
				"xalhammmbra",
				"xaaaalhambra",
				"xalhammbhambra",
				"xalhammmmbra",
				"xalhambhambra",
				"xalhammmmbhambra"
		};
		assertAllMatch(condition, positive);
	}

	@Test
	void testPlus03() {

		Condition<Integer> condition =
				new Condition<>("_(a+l(ham+b)*ra)+", FACTORY);
		String[] positive = {
				"xalhambra",
				"xaalhambra",
				"xalhammbra",
				"xaaalhambra",
				"xalhammmbra",
				"xaaaalhambra",
				"xalhammmmbra",
				"xalhambhambra",

				"xalhambhambhambra",
				"xalhambraalhambra",
				"xalhammbhammbra",
				"xalhammbraalhambra",
				"xalhammbhambra",
				"xalhammmbraalhambra",
				"xalhammmmbhambra",
				"xalhammmmbraalhambra",

				"xalhambraalhammbra",
				"xalhambraalhammmbra",
				"xalhammbraalhammbra",
				"xalhammbraalhammmbra",
				"xalhammmbraalhammbra",
				"xalhammmbraalhammmbra",
				"xalhammmmbraalhammbra",
				"xalhammmmbraalhammmbra",
		};

		assertAllMatch(condition, positive);
	}

	@Test
	void testStar07() {

		Condition<Integer> condition =
				new Condition<>("_(a+l(ham+b)+ra)*", FACTORY);

		String[] positive = {
				"xalhambra",
				"xalhammmmmbra",
				"xalhammbra",
				"xalhammmmmmbra",
				"xalhammmbra",
				"xalhammmmmmmbra",
				"xalhammmmbra",
				"xalhammmmmmmmbra",
				"xaalhambra",
				"xalhammbhambra",
				"xaaalhambra",
				"xalhammbhammbra",
				"xaaaalhambra",
				"xalhammbhambra",
				"xaaaaalhambra",
				"xalhammmmbhambra",
				"xalhambhambra",
				"xalhambraalhambra",
				"xalhambhambhambra",
				"xalhambraalhambraalhambra",
				"x"
		};

		assertAllMatch(condition, positive);
	}

	@Test
	void testGroups01() {
		Condition<Integer> condition =
				new Condition<>("_(ab)(cd)(ef)", FACTORY);

		assertMatches(condition, "xabcdef", 0);
		assertNoMatch(condition, "xabcd", 0);
		assertNoMatch(condition, "xab", 0);
		assertNoMatch(condition, "bcdef", 0);

	}

	@Test
	void testGroups02() {
		Condition<Integer> condition =
				new Condition<>("_(ab)*(cd)(ef)", FACTORY);
		assertMatches(condition, "xcdef", 0);
		assertMatches(condition, "xabcdef", 0);
		assertMatches(condition, "xababcdef", 0);
		assertMatches(condition, "xabababcdef", 0);

		assertNoMatch(condition, "xabbcdef", 0);
		assertNoMatch(condition, "xacdef", 0);
		assertNoMatch(condition, "xabdef", 0);
		assertNoMatch(condition, "xabcef", 0);
		assertNoMatch(condition, "xabcdf", 0);
		assertNoMatch(condition, "xabcde", 0);
	}

	@Test
	void testGroups03() {
		Condition<Integer> condition =
				new Condition<>("_(ab)(cd)*(ef)", FACTORY);
		assertMatches(condition, "xabef", 0);
		assertMatches(condition, "xabcdef", 0);
		assertMatches(condition, "xabcdcdef", 0);
	}

	@Test
	void testGroups04() {
		Condition<Integer> condition =
				new Condition<>("_(ab)(cd)(ef)*", FACTORY);
		assertMatches(condition, "xabcd", 0);
		assertMatches(condition, "xabcdef", 0);
		assertMatches(condition, "xabcdefef", 0);
	}

	@Test
	void testGroups05() {
		Condition<Integer> condition =
				new Condition<>("_(ab)?(cd)(ef)", FACTORY);
		assertMatches(condition, "xabcdef", 0);
		assertMatches(condition, "xcdef", 0);
	}

	@Test
	void testGroups06() {
		Condition<Integer> condition =
				new Condition<>("_(ab)(cd)?(ef)", FACTORY);
		assertMatches(condition, "xabcdef", 0);
		assertMatches(condition, "xabef", 0);
	}

	@Test
	void testGroups07() {
		Condition<Integer> condition =
				new Condition<>("_(ab)(cd)(ef)?", FACTORY);
		assertMatches(condition, "xabcdef", 0);
		assertMatches(condition, "xabcd", 0);
	}

	@Test
	void testGroups08() {
		Condition<Integer> condition =
				new Condition<>("_(ab)?(cd)?(ef)?", FACTORY);

		assertMatches(condition, "xabcdef", 0);
		assertMatches(condition, "x", 0);
		assertMatches(condition, "xab", 0);
		assertMatches(condition, "xcd", 0);
		assertMatches(condition, "xef", 0);
		assertMatches(condition, "xabef", 0);
		assertMatches(condition, "xabcd", 0);
		assertMatches(condition, "xcdef", 0);
	}

	@Test
	void testFullCondition() {
		Condition<Integer> condition =
				new Condition<>("(ab)?(cd)?(ef)?_(ab)?(cd)?(ef)?", FACTORY);
		assertMatches(condition, "xabcdef", 0);
		assertMatches(condition, "efxabcdef", 2);
		assertMatches(condition, "cdefxabcdef", 4);
		assertMatches(condition, "abcdefxabcdef", 6);
		assertMatches(condition, "abcdefxabcd", 6);
		assertMatches(condition, "abcdefxab", 6);
		assertMatches(condition, "abcdefx", 6);

		assertMatches(condition, "abx", 2);
		assertMatches(condition, "cdx", 2);
		assertMatches(condition, "efx", 2);

		assertMatches(condition, "abefx", 4);
		assertMatches(condition, "abcdx", 4);
		assertMatches(condition, "cdefx", 4);
	}

	@Test
	void testSet01() {
		Condition<Integer> condition = new Condition<>("_{a b c}ds", FACTORY);
		assertMatches(condition, "xads", 0);
		assertMatches(condition, "xbds", 0);
		assertMatches(condition, "xcds", 0);
		assertNoMatch(condition, "xds", 0);
	}

	@Test
	void testSet02() {
		Condition<Integer> condition =
				new Condition<>("_{ab cd ef}tr", FACTORY);
		assertMatches(condition, "xabtr", 0);
		assertMatches(condition, "xcdtr", 0);
		assertMatches(condition, "xeftr", 0);
		assertNoMatch(condition, "xabcd", 0);
		assertNoMatch(condition, "xtr", 0);
	}

	@Test
	void testSet04() {
		Condition<Integer> condition =
				new Condition<>("_{ab* cd+ ef}tr", FACTORY);

		assertMatches(condition, "xabtr", 0);
		assertMatches(condition, "xcdtr", 0);
		assertMatches(condition, "xeftr", 0);

		assertNoMatch(condition, "xacd", 0);
		assertNoMatch(condition, "xabbcd", 0);
		assertNoMatch(condition, "xabx", 0);
		assertNoMatch(condition, "xabcd", 0);
		assertNoMatch(condition, "xb", 0);
		assertNoMatch(condition, "x", 0);
		assertNoMatch(condition, "xc", 0);
		assertNoMatch(condition, "xcdef", 0);
		assertNoMatch(condition, "xtr", 0);
	}

	@Test
	void testSet05() {
		Condition<Integer> condition =
				new Condition<>("_{ab* (cd?)+ ((ae)*f)+}tr", FACTORY);

		assertMatches(condition, "xabtr", 0);

		assertMatches(condition, "xcdtr", 0);
		assertMatches(condition, "xcctr", 0);
		assertMatches(condition, "xccctr", 0);

		assertMatches(condition, "xftr", 0);
		assertMatches(condition, "xfftr", 0);
		assertMatches(condition, "xaeftr", 0);
		assertMatches(condition, "xaeaeftr", 0);
		assertMatches(condition, "xaefaeftr", 0);
		assertMatches(condition, "xaefffffaeftr", 0);

		assertNoMatch(condition, "xabcd", 0);
		assertNoMatch(condition, "xtr", 0);
	}

	@Test
	void testSet06() {
		Condition<Integer> condition =
				new Condition<>("_{ab {cd xy} ef}tr", FACTORY);
		assertMatches(condition, "xabtr", 0);
		assertMatches(condition, "xcdtr", 0);
		assertMatches(condition, "xeftr", 0);
		assertMatches(condition, "xxytr", 0);
		assertNoMatch(condition, "xabcd", 0);
		assertNoMatch(condition, "xtr", 0);
	}

	@Test
	void testSet07() {
		Condition<Integer> condition = new Condition<>("_{ x ɣ }", FACTORY);
		assertMatches(condition, "pxi");
		assertNoMatch(condition, "paxi");
	}

	@Test
	void testComplex01() {
		SequenceFactory<Integer> factoryParam = new SequenceFactory<>(
				EMPTY.getFeatureMapping(),
				FormatterMode.INTELLIGENT
		);
		Condition<Integer> condition = new Condition<>(
				"_{r l}?{a e o ā ē ō}{i u}?{n m l r}?{pʰ tʰ kʰ ḱʰ}",
				factoryParam
		);

		assertMatches(factoryParam, condition, "pʰāḱʰus", 0);
		assertMatches(factoryParam, condition, "pʰentʰros", 0);
		assertMatches(factoryParam, condition, "pʰlaḱʰmēn", 0);
		assertMatches(factoryParam, condition, "pʰoutʰéyet", 0);

		assertNoMatch(factoryParam, condition, "pʰuǵos", 0);
	}

	@Test
	void testAdditional01() {
		assertMatches(new Condition<>("_c+#", FACTORY), "abaccc", 2);
		assertMatches(new Condition<>("_#", FACTORY), "abad", 3);
	}

	@Test
	void testWithVariables01() {

		VariableStore store = new VariableStore(FormatterMode.INTELLIGENT);
		store.add("C = p t k b d g pʰ tʰ kʰ");

		SequenceFactory<Integer> sequenceFactory = new SequenceFactory<>(
				EMPTY.getFeatureMapping(),
				store.getKeys(),
				FormatterMode.INTELLIGENT
		);
		
		Condition<Integer> condition = new Condition<>(
				"_C+#",
				store,
				sequenceFactory
		);

		assertMatches(sequenceFactory, condition, "abaptk", 2);
		assertMatches(sequenceFactory, condition, "abapppp", 2);
		assertMatches(sequenceFactory, condition, "ababdg", 2);
		assertMatches(sequenceFactory, condition, "abatʰkʰ", 2);

		assertMatches(sequenceFactory, condition, "abaptk", 3);
		assertMatches(sequenceFactory, condition, "abapppp", 3);
		assertMatches(sequenceFactory, condition, "ababdg", 3);
		assertMatches(sequenceFactory, condition, "abapʰtʰkʰ", 3);

		assertNoMatch(sequenceFactory, condition, "abatʰkʰ", 1);
		assertNoMatch(sequenceFactory, condition, "abatʰkʰ", 0);
	}

	@Test
	void testVariablesDebug01() {
		VariableStore store = new VariableStore(FormatterMode.INTELLIGENT);
		store.add("C = p t k b d g pʰ tʰ kʰ");
		Set<String> reserved = Collections.singleton("C");
		SequenceFactory<Integer> sequenceFactory = new SequenceFactory<>(
				EMPTY.getFeatureMapping(),
				reserved, 
				FormatterMode.INTELLIGENT
		);
		Condition<Integer> condition = new Condition<>("_C+#", store, sequenceFactory);

		assertMatches(sequenceFactory, condition, "abatʰkʰ", 2);
	}

	@Test
	void testNegative00() {
		Condition<Integer> condition = new Condition<>("_!a#", FACTORY);

		assertMatches(condition, "zb", 0);
		assertMatches(condition, "zc", 0);
		assertMatches(condition, "zd", 0);
		assertMatches(condition, "ze", 0);

		assertNoMatch(condition, "za", 0); // Good
	}

	@Test
	void testNegative01() {
		Condition<Integer> condition = new Condition<>("_!(abc)#", FACTORY);

		assertMatches(condition, "zbab", 0);
		assertMatches(condition, "zcab", 0);
		assertMatches(condition, "zdab", 0);
		assertMatches(condition, "zeab", 0);

		assertMatches(condition, "zaba", 0);
		assertNoMatch(condition, "zabc", 0);
		assertMatches(condition, "zcba", 0);
		assertMatches(condition, "zaaa", 0);
	}

	@Test
	void testNegative02() {
		Condition<Integer> condition = new Condition<>("_!{a b c}#", FACTORY);

		assertMatches(condition, "yz", 0);
		assertMatches(condition, "ym", 0);
		assertMatches(condition, "yr", 0);
		assertMatches(condition, "yd", 0);

		assertNoMatch(condition, "x!a", 0); // Good
		assertNoMatch(condition, "xa", 0);
		assertNoMatch(condition, "xb", 0);
		assertNoMatch(condition, "xc", 0);
	}
	
	@Test
	void testUnknownFailure() {
		Condition<Integer> condition = new Condition<>("{s c y}{s c y}+_{o}", FACTORY);
		
		assertMatches(condition, "tusscyos", 5);
	}

	private static void assertMatches(
			SequenceFactory<Integer> factory,
			Condition<Integer> condition,
			String testString,
			int index
	) {
		Sequence<Integer> word = factory.toSequence(testString);
		assertTrue(condition.isMatch(word, index),
				testString + " should have matched " + condition +
						" but did not."
		);
	}

	private static void assertMatches(Condition<Integer> condition,
			String testString, int index) {
		assertMatches(FACTORY, condition, testString, index);
	}

	private static void assertNoMatch(SequenceFactory<Integer> factory,
			Condition<Integer> condition, String testString, int index) {
		Sequence<Integer> word = factory.toSequence(testString);
		assertFalse(condition.isMatch(word, index),
				testString + " should not have matched " + condition + " but did.");
	}

	private static void assertNoMatch(Condition<Integer> condition,
			String testString, int index) {
		assertNoMatch(FACTORY, condition, testString, index);
	}

	private static void assertNoMatch(Condition<Integer> condition,
			String testString) {
		assertNoMatch(condition, testString, 0);
	}

	private static void assertMatches(Condition<Integer> condition,
			String testString) {
		assertMatches(condition, testString, 0);
	}

	private static void assertAllMatch(Condition<Integer> condition,
			String... positives) {
		for (String positive : positives) {
			assertMatches(condition, positive, 0);
		}
	}

	private static void assertNoneMatch(Condition<Integer> condition,
			String... negatives) {
		for (String negative : negatives) {
			assertNoMatch(condition, negative, 0);
		}
	}
}


