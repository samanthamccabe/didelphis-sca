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
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Samantha Fiona McCabe
 * @date 2013-07-07
 * @since 0.0.0
 */
class ConditionTest {

	private static final FeatureModelLoader<Integer> EMPTY =
			IntegerFeature.emptyLoader();

	private static final SequenceFactory<Integer> FACTORY =
			new SequenceFactory<>(EMPTY.getFeatureMapping(),
					FormatterMode.INTELLIGENT);

	// We just need to see that this parses correctly
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

		testPositive(condition, positive);
	}

	@Test
	void testOptional02() {

		Condition<Integer> condition = new Condition<>("_d?ab", FACTORY);

		testTrue(condition, "xab", 0);
		testTrue(condition, "xdab", 0);

		assertFalse(condition.isMatch(FACTORY.toSequence("xadb"), 0));
		assertFalse(condition.isMatch(FACTORY.toSequence("xacb"), 0));
		assertFalse(condition.isMatch(FACTORY.toSequence("xdb"), 0));
	}

	@Test
	void testOptional03() {

		Condition<Integer> condition =
				new Condition<>("_a(l(hamb)?ra)?#", FACTORY);

		testTrue(condition, "xalhambra", 0);
		testTrue(condition, "xalra", 0);
		testTrue(condition, "xa", 0);
		testFalse(condition, "xalh", 0);
	}

	@Test
	void testOptional04() {

		Condition<Integer> condition = new Condition<>("_a(ba)?b", FACTORY);

		testTrue(condition, "xab", 0);
		testTrue(condition, "xabab", 0);
		testFalse(condition, "xalh", 0);
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
		testPositive(condition, positive);
	}

	@Test
	void testStar06() {
		Condition<Integer> condition = new Condition<>("_(ab)*#", FACTORY);
		String[] positive = {
				"x", "xababab", "xab", "xabababab", "xabab", "xababababab"
		};

		testPositive(condition, positive);

		String[] negative = {
				"xa", "xabababa", "xaba", "xababababa", "xababa", "xabababababa"
		};

		testNegative(condition, negative);
	}

	@Test
	void testPlus01() {

		Condition<Integer> condition = new Condition<>("_a+b", FACTORY);

		String[] positive = {
				"xab", "xaab", "xaaab", "xaaaab", "xaaaaab",
		};

		testPositive(condition, positive);

		testFalse(condition, "xb");
		testFalse(condition, "xcb");
		testFalse(condition, "xacb");
		testFalse(condition, "xaacb");
		testFalse(condition, "xaaacb");
		testFalse(condition, "xba");
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
		testPositive(condition, positive);
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

		testPositive(condition, positive);
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

		testPositive(condition, positive);
	}

	@Test
	void testGroups01() {
		Condition<Integer> condition =
				new Condition<>("_(ab)(cd)(ef)", FACTORY);

		testTrue(condition, "xabcdef", 0);
		testFalse(condition, "xabcd", 0);
		testFalse(condition, "xab", 0);
		testFalse(condition, "bcdef", 0);

	}

	@Test
	void testGroups02() {
		Condition<Integer> condition =
				new Condition<>("_(ab)*(cd)(ef)", FACTORY);
		testTrue(condition, "xcdef", 0);
		testTrue(condition, "xabcdef", 0);
		testTrue(condition, "xababcdef", 0);
		testTrue(condition, "xabababcdef", 0);

		testFalse(condition, "xabbcdef", 0);
		testFalse(condition, "xacdef", 0);
		testFalse(condition, "xabdef", 0);
		testFalse(condition, "xabcef", 0);
		testFalse(condition, "xabcdf", 0);
		testFalse(condition, "xabcde", 0);
	}

	@Test
	void testGroups03() {
		Condition<Integer> condition =
				new Condition<>("_(ab)(cd)*(ef)", FACTORY);
		testTrue(condition, "xabef", 0);
		testTrue(condition, "xabcdef", 0);
		testTrue(condition, "xabcdcdef", 0);
	}

	@Test
	void testGroups04() {
		Condition<Integer> condition =
				new Condition<>("_(ab)(cd)(ef)*", FACTORY);
		testTrue(condition, "xabcd", 0);
		testTrue(condition, "xabcdef", 0);
		testTrue(condition, "xabcdefef", 0);
	}

	@Test
	void testGroups05() {
		Condition<Integer> condition =
				new Condition<>("_(ab)?(cd)(ef)", FACTORY);
		testTrue(condition, "xabcdef", 0);
		testTrue(condition, "xcdef", 0);
	}

	@Test
	void testGroups06() {
		Condition<Integer> condition =
				new Condition<>("_(ab)(cd)?(ef)", FACTORY);
		testTrue(condition, "xabcdef", 0);
		testTrue(condition, "xabef", 0);
	}

	@Test
	void testGroups07() {
		Condition<Integer> condition =
				new Condition<>("_(ab)(cd)(ef)?", FACTORY);
		testTrue(condition, "xabcdef", 0);
		testTrue(condition, "xabcd", 0);
	}

	@Test
	void testGroups08() {
		Condition<Integer> condition =
				new Condition<>("_(ab)?(cd)?(ef)?", FACTORY);

		testTrue(condition, "xabcdef", 0);
		testTrue(condition, "x", 0);
		testTrue(condition, "xab", 0);
		testTrue(condition, "xcd", 0);
		testTrue(condition, "xef", 0);
		testTrue(condition, "xabef", 0);
		testTrue(condition, "xabcd", 0);
		testTrue(condition, "xcdef", 0);
	}

	@Test
	void testFullCondition() {
		Condition<Integer> condition =
				new Condition<>("(ab)?(cd)?(ef)?_(ab)?(cd)?(ef)?", FACTORY);
		testTrue(condition, "xabcdef", 0);
		testTrue(condition, "efxabcdef", 2);
		testTrue(condition, "cdefxabcdef", 4);
		testTrue(condition, "abcdefxabcdef", 6);
		testTrue(condition, "abcdefxabcd", 6);
		testTrue(condition, "abcdefxab", 6);
		testTrue(condition, "abcdefx", 6);

		testTrue(condition, "abx", 2);
		testTrue(condition, "cdx", 2);
		testTrue(condition, "efx", 2);

		testTrue(condition, "abefx", 4);
		testTrue(condition, "abcdx", 4);
		testTrue(condition, "cdefx", 4);
	}

	@Test
	void testSet01() {
		Condition<Integer> condition = new Condition<>("_{a b c}ds", FACTORY);
		testTrue(condition, "xads", 0);
		testTrue(condition, "xbds", 0);
		testTrue(condition, "xcds", 0);
		testFalse(condition, "xds", 0);
	}

	@Test
	void testSet02() {
		Condition<Integer> condition =
				new Condition<>("_{ab cd ef}tr", FACTORY);
		testTrue(condition, "xabtr", 0);
		testTrue(condition, "xcdtr", 0);
		testTrue(condition, "xeftr", 0);
		testFalse(condition, "xabcd", 0);
		testFalse(condition, "xtr", 0);
	}

	@Test
	void testSet04() {
		Condition<Integer> condition =
				new Condition<>("_{ab* cd+ ef}tr", FACTORY);

		testTrue(condition, "xabtr", 0);
		testTrue(condition, "xcdtr", 0);
		testTrue(condition, "xeftr", 0);

		testFalse(condition, "xacd", 0);
		testFalse(condition, "xabbcd", 0);
		testFalse(condition, "xabx", 0);
		testFalse(condition, "xabcd", 0);
		testFalse(condition, "xb", 0);
		testFalse(condition, "x", 0);
		testFalse(condition, "xc", 0);
		testFalse(condition, "xcdef", 0);
		testFalse(condition, "xtr", 0);
	}

	@Test
	void testSet05() {
		Condition<Integer> condition =
				new Condition<>("_{ab* (cd?)+ ((ae)*f)+}tr", FACTORY);

		testTrue(condition, "xabtr", 0);

		testTrue(condition, "xcdtr", 0);
		testTrue(condition, "xcctr", 0);
		testTrue(condition, "xccctr", 0);

		testTrue(condition, "xftr", 0);
		testTrue(condition, "xfftr", 0);
		testTrue(condition, "xaeftr", 0);
		testTrue(condition, "xaeaeftr", 0);
		testTrue(condition, "xaefaeftr", 0);
		testTrue(condition, "xaefffffaeftr", 0);

		testFalse(condition, "xabcd", 0);
		testFalse(condition, "xtr", 0);
	}

	@Test
	void testSet06() {
		Condition<Integer> condition =
				new Condition<>("_{ab {cd xy} ef}tr", FACTORY);
		testTrue(condition, "xabtr", 0);
		testTrue(condition, "xcdtr", 0);
		testTrue(condition, "xeftr", 0);
		testTrue(condition, "xxytr", 0);
		testFalse(condition, "xabcd", 0);
		testFalse(condition, "xtr", 0);
	}

	@Test
	void testSet07() {
		Condition<Integer> condition = new Condition<>("_{ x ɣ }", FACTORY);
		testTrue(condition, "pxi");
		testFalse(condition, "paxi");
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

		testTrue(factoryParam, condition, "pʰāḱʰus", 0);
		testTrue(factoryParam, condition, "pʰentʰros", 0);
		testTrue(factoryParam, condition, "pʰlaḱʰmēn", 0);
		testTrue(factoryParam, condition, "pʰoutʰéyet", 0);

		testFalse(factoryParam, condition, "pʰuǵos", 0);
	}

	@Test
	void testAdditional01() {
		testTrue(new Condition<>("_c+#", FACTORY), "abaccc", 2);
		testTrue(new Condition<>("_#", FACTORY), "abad", 3);
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

		testTrue(sequenceFactory, condition, "abaptk", 2);
		testTrue(sequenceFactory, condition, "abapppp", 2);
		testTrue(sequenceFactory, condition, "ababdg", 2);
		testTrue(sequenceFactory, condition, "abatʰkʰ", 2);

		testTrue(sequenceFactory, condition, "abaptk", 3);
		testTrue(sequenceFactory, condition, "abapppp", 3);
		testTrue(sequenceFactory, condition, "ababdg", 3);
		testTrue(sequenceFactory, condition, "abapʰtʰkʰ", 3);

		testFalse(sequenceFactory, condition, "abatʰkʰ", 1);
		testFalse(sequenceFactory, condition, "abatʰkʰ", 0);
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

		testTrue(sequenceFactory, condition, "abatʰkʰ", 2);
	}

	@Test
	void testNegative00() {
		Condition<Integer> condition = new Condition<>("_!a#", FACTORY);

		testTrue(condition, "zb", 0);
		testTrue(condition, "zc", 0);
		testTrue(condition, "zd", 0);
		testTrue(condition, "ze", 0);

		testFalse(condition, "za", 0); // Good
	}

	@Test
	void testNegative01() {
		Condition<Integer> condition = new Condition<>("_!(abc)#", FACTORY);

		testTrue(condition, "zbab", 0);
		testTrue(condition, "zcab", 0);
		testTrue(condition, "zdab", 0);
		testTrue(condition, "zeab", 0);

		testTrue(condition, "zaba", 0);
		testFalse(condition, "zabc", 0);
		testTrue(condition, "zcba", 0);
		testTrue(condition, "zaaa", 0);
	}

	@Test
	void testNegative02() {
		Condition<Integer> condition = new Condition<>("_!{a b c}#", FACTORY);

		testTrue(condition, "yz", 0);
		testTrue(condition, "ym", 0);
		testTrue(condition, "yr", 0);
		testTrue(condition, "yd", 0);

		testFalse(condition, "x!a", 0); // Good
		testFalse(condition, "xa", 0);
		testFalse(condition, "xb", 0);
		testFalse(condition, "xc", 0);
	}
	
	@Test
	void testUnknownFailure() {
		Condition<Integer> condition = new Condition<>("{s c y}{s c y}+_{o}", FACTORY);
		
		testTrue(condition, "tusscyos", 5);
	}

	private static void testTrue(
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

	private static void testTrue(Condition<Integer> condition,
			String testString, int index) {
		testTrue(FACTORY, condition, testString, index);
	}

	private static void testFalse(SequenceFactory<Integer> factory,
			Condition<Integer> condition, String testString, int index) {
		Sequence<Integer> word = factory.toSequence(testString);
		assertFalse(condition.isMatch(word, index),
				testString + " should not have matched " + condition + " but did.");
	}

	private static void testFalse(Condition<Integer> condition,
			String testString, int index) {
		testFalse(FACTORY, condition, testString, index);
	}

	private static void testFalse(Condition<Integer> condition,
			String testString) {
		testFalse(condition, testString, 0);
	}

	private static void testTrue(Condition<Integer> condition,
			String testString) {
		testTrue(condition, testString, 0);
	}

	private static void testPositive(Condition<Integer> condition,
			String... positive) {
		for (String p : positive) {
			testTrue(condition, p, 0);
		}
	}

	private static void testNegative(Condition<Integer> condition,
			String... negative) {
		for (String n : negative) {
			testFalse(condition, n, 0);
		}
	}
}


