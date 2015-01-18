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

import org.haedus.datatypes.FormatterMode;
import org.haedus.datatypes.SegmentationMode;
import org.haedus.datatypes.phonetic.FeatureModel;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.SequenceFactory;
import org.haedus.datatypes.phonetic.VariableStore;
import org.haedus.soundchange.exceptions.RuleFormatException;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: Samantha Fiona Morrigan McCabe
 * Date: 7/7/13
 * Time: 11:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConditionTest {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(ConditionTest.class);

	private static final SequenceFactory FACTORY = SequenceFactory.getEmptyFactory();

	// We just need to see that this parses correctly
	@Test
	public void testEmptyCondition() {
		new Condition("_");
	}

	// We just need to see that this parses correctly
	@Test(expected = RuleFormatException.class)
	public void testBadCondition() {
		new Condition("a_b_c");
	}

	@Test
	public void testPostconditionMatching01() {
		Condition condition = new Condition("a_x");
		Sequence sequence = FACTORY.getSequence("balx");

		assertTrue("", condition.isMatch(sequence, 2));
	}

	@Test
	public void testPostconditionMatching02() {
		Condition condition = new Condition("b_#");
		Sequence sequence = FACTORY.getSequence("aba");

		assertFalse("0", condition.isMatch(sequence, 0));
		assertFalse("1", condition.isMatch(sequence, 1));
		assertTrue("2", condition.isMatch(sequence, 2));
	}

	@Test
	public void testPostconditionMatching03() {
		Condition condition = new Condition("b_lx");
		Sequence sequence = FACTORY.getSequence("balx");

		assertTrue("1", condition.isMatch(sequence, 1));
		assertFalse("2", condition.isMatch(sequence, 2));
		assertFalse("3", condition.isMatch(sequence, 3));
	}

	@Test
	public void testPostconditionMatching04() {
		Condition condition = new Condition("_lxpld");
		Sequence sequence = FACTORY.getSequence("beralxpld");

		assertTrue("T", condition.isMatch(sequence, 3));
		assertFalse("F", condition.isMatch(sequence, 2));
	}

	@Test
	public void testOptional01() {

		Condition condition = new Condition("_a?(b?c?)d?b");

		String[] positive = {
				"xb", "xbb",
				"xcb",    "xbcb",
				"xab",    "xbab",
				"xdb",    "xbdb",
				"xacb",   "xbacb",
				"xadb",   "xbadb",
				"xcdb",   "xbcdb",
				"xabcb",  "xbabcb"
		};

		testPositive(condition, positive);
	}

	@Test
	public void testOptional02() {

		Condition condition = new Condition("_d?ab");

		testTrue(condition,"xab",0);
		testTrue(condition,"xdab",0);

		assertFalse("xadb", condition.isMatch(FACTORY.getSequence("xadb"), 0));
		assertFalse("xacb", condition.isMatch(FACTORY.getSequence("xacb"), 0));
		assertFalse("xdb",  condition.isMatch(FACTORY.getSequence("xdb"), 0));
	}

	@Test
	public void testOptional03() {

		Condition condition = new Condition("_a(l(hamb)?ra)?#");

		testTrue(condition,  "xalhambra", 0);
		testTrue(condition,  "xalra",     0);
		testTrue(condition,  "xa",        0);
		testFalse(condition, "xalh",      0);
	}

	@Test
	public void testOptional04() {

		Condition condition = new Condition("_a(ba)?b");

		testTrue(condition,  "xab",   0);
		testTrue(condition,  "xabab", 0);
		testFalse(condition, "xalh",  0);
	}

	@Test
	public void testStar01() {

		Condition condition = new Condition("_a*b");

		assertTrue("xb", condition.isMatch(FACTORY.getSequence("xb"), 0));
		assertTrue("xab	", condition.isMatch(FACTORY.getSequence("xab"), 0));
		assertTrue("xaab", condition.isMatch(FACTORY.getSequence("xaab"), 0));
		assertTrue("xaaab", condition.isMatch(FACTORY.getSequence("xaaab"), 0));
		assertTrue("xaaaab", condition.isMatch(FACTORY.getSequence("xaaaab"), 0));
		assertTrue("xaaaaab", condition.isMatch(FACTORY.getSequence("xaaaaab"), 0));
		assertFalse("xcaaaab", condition.isMatch(FACTORY.getSequence("xcaaaab"), 0));
	}

	@Test
	public void testStar02() {

		Condition condition = new Condition("_aa*b");

		assertFalse("xb", condition.isMatch(FACTORY.getSequence("xb"), 0));
		assertTrue("xab	", condition.isMatch(FACTORY.getSequence("xab"), 0));
		assertTrue("xaab", condition.isMatch(FACTORY.getSequence("xaab"), 0));
		assertTrue("xaaab", condition.isMatch(FACTORY.getSequence("xaaab"), 0));
		assertTrue("xaaaab", condition.isMatch(FACTORY.getSequence("xaaaab"), 0));
		assertTrue("xaaaaab", condition.isMatch(FACTORY.getSequence("xaaaaab"), 0));
		assertFalse("xcaaaab", condition.isMatch(FACTORY.getSequence("xcaaaab"), 0));
	}

	@Test
	public void testStar03() {

		Condition condition = new Condition("_da*b");

		assertTrue("xdb", condition.isMatch(FACTORY.getSequence("xdb"), 0));
		assertTrue("xdab", condition.isMatch(FACTORY.getSequence("xdab"), 0));
		assertTrue("xdaab", condition.isMatch(FACTORY.getSequence("xdaab"), 0));
		assertTrue("xdaaab", condition.isMatch(FACTORY.getSequence("xdaaab"), 0));
		assertTrue("xdaaaab", condition.isMatch(FACTORY.getSequence("xdaaaab"), 0));
		assertTrue("xdaaaaab", condition.isMatch(FACTORY.getSequence("xdaaaaab"), 0));
		assertFalse("xdcaaaab", condition.isMatch(FACTORY.getSequence("xdcaaaab"), 0));
	}

	@Test
	public void testStar04() {

		Condition condition = new Condition("_d(eo)*b");

		assertTrue("xdb", condition.isMatch(FACTORY.getSequence("xdb"), 0));
		assertTrue("xdeob", condition.isMatch(FACTORY.getSequence("xdeob"), 0));
		assertTrue("xdeoeob", condition.isMatch(FACTORY.getSequence("xdeoeob"), 0));
		assertTrue("xdeoeoeob", condition.isMatch(FACTORY.getSequence("xdeoeoeob"), 0));
		assertFalse("xdcaaaab", condition.isMatch(FACTORY.getSequence("xdcaaaab"), 0));
	}

	@Test
	public void testStar05() {

		Condition condition = new Condition("_d(eo*)*b");

		String[] positive = {
				"xdb",         "xdeob",
				"xdeb",        "xdeooeob",
				"xdeeb",       "xdeoooeob",
				"xdeeeb",      "xdeoeoob",
				"xdeeeeb",     "xdeoeooob",
				"xdeoeob",     "xdeoob",
				"xdeoeoeob",   "xdeooob",
				"xdeoeoeoeob", "xdeoooob",
		};
		testPositive(condition, positive);
	}

	@Test
	public void testStar06() {

		Condition condition = new Condition("_(ab)*#");
		String[] positive = {
				"x",     "xababab",
				"xab",   "xabababab",
				"xabab", "xababababab"
		};

		testPositive(condition, positive);

		String[] negative = {
				"xa",
				"xabababa",
				"xaba",   "xababababa",
				"xababa", "xabababababa"
		};

		testNegative(condition, negative);
	}

	@Test
	public void testPlus01() {

		Condition condition = new Condition("_a+b");

		String[] positive = {
				"xab",
				"xaab",
				"xaaab",
				"xaaaab",
				"xaaaaab",
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
	public void testPlus02() {

		Condition condition = new Condition("_a+l(ham+b)+ra");

		String[] positive = {
				"xalhambra",    "xaalhambra",    "xalhambhambhambra",
				"xalhammbra",   "xaaalhambra",   "xalhammbhammbra",
				"xalhammmbra",  "xaaaalhambra",  "xalhammbhambra",
				"xalhammmmbra", "xalhambhambra", "xalhammmmbhambra"
		};
		testPositive(condition, positive);
	}

	@Test
	public void testPlus03() {

		Condition condition = new Condition("_(a+l(ham+b)*ra)+");
		String [] positive = {
				"xalhambra",             "xaalhambra",
				"xalhammbra",            "xaaalhambra",
				"xalhammmbra",           "xaaaalhambra",
				"xalhammmmbra",          "xalhambhambra",

				"xalhambhambhambra",     "xalhambraalhambra",
				"xalhammbhammbra",       "xalhammbraalhambra",
				"xalhammbhambra",        "xalhammmbraalhambra",
				"xalhammmmbhambra",      "xalhammmmbraalhambra",

				"xalhambraalhammbra",    "xalhambraalhammmbra",
				"xalhammbraalhammbra",   "xalhammbraalhammmbra",
				"xalhammmbraalhammbra",  "xalhammmbraalhammmbra",
				"xalhammmmbraalhammbra", "xalhammmmbraalhammmbra",
		};

		testPositive(condition, positive);
	}

	@Test
	public void testStar07() {

		Condition condition = new Condition("_(a+l(ham+b)+ra)*");

		String[] positive = {
				"xalhambra",         "xalhammmmmbra",
				"xalhammbra",        "xalhammmmmmbra",
				"xalhammmbra",       "xalhammmmmmmbra",
				"xalhammmmbra",      "xalhammmmmmmmbra",
				"xaalhambra",        "xalhammbhambra",
				"xaaalhambra",       "xalhammbhammbra",
				"xaaaalhambra",      "xalhammbhambra",
				"xaaaaalhambra",     "xalhammmmbhambra",
				"xalhambhambra",     "xalhambraalhambra",
				"xalhambhambhambra", "xalhambraalhambraalhambra", "x"
		};

		testPositive(condition, positive);
	}

	@Test
		 public void testGroups01() {
		Condition condition = new Condition("_(ab)(cd)(ef)");

		testTrue(condition,"xabcdef",0);
		testFalse(condition,"xabcd",0);
		testFalse(condition,"xab",0);
		testFalse(condition,"bcdef",0);

	}

	@Test
	public void testGroups02() {
		Condition condition = new Condition("_(ab)*(cd)(ef)");
		testTrue(condition, "xcdef",0);
		testTrue(condition, "xabcdef",0);
		testTrue(condition, "xababcdef",0);
		testTrue(condition, "xabababcdef",0);

		testFalse(condition, "xabbcdef",0);
		testFalse(condition, "xacdef",0);
		testFalse(condition, "xabdef",0);
		testFalse(condition, "xabcef",0);
		testFalse(condition, "xabcdf",0);
		testFalse(condition, "xabcde",0);
	}

	@Test
	public void testGroups03() {
		Condition condition = new Condition("_(ab)(cd)*(ef)");
		testTrue(condition, "xabef",0);
		testTrue(condition, "xabcdef",0);
		testTrue(condition, "xabcdcdef",0);
	}

	@Test
	public void testGroups04() {
		Condition condition = new Condition("_(ab)(cd)(ef)*");
		testTrue(condition, "xabcd",0);
		testTrue(condition, "xabcdef",0);
		testTrue(condition, "xabcdefef",0);
	}

	@Test
	public void testGroups05() {
		Condition condition = new Condition("_(ab)?(cd)(ef)");
		testTrue(condition, "xabcdef",0);
		testTrue(condition, "xcdef",0);
	}

	@Test
	public void testGroups06() {
		Condition condition = new Condition("_(ab)(cd)?(ef)");
		testTrue(condition, "xabcdef",0);
		testTrue(condition, "xabef",0);
	}

	@Test
	public void testGroups07() {
		Condition condition = new Condition("_(ab)(cd)(ef)?");
		testTrue(condition, "xabcdef",0);
		testTrue(condition, "xabcd",0);
	}

	@Test
	public void testGroups08() {
		Condition condition = new Condition("_(ab)?(cd)?(ef)?");

		testTrue(condition, "xabcdef",0);
		testTrue(condition, "x",0);
		testTrue(condition, "xab",0);
		testTrue(condition, "xcd",0);
		testTrue(condition, "xef",0);
		testTrue(condition, "xabef",0);
		testTrue(condition, "xabcd",0);
		testTrue(condition, "xcdef",0);
	}

	@Test
	public void testFullCondition()  {
		Condition condition = new Condition("(ab)?(cd)?(ef)?_(ab)?(cd)?(ef)?");
		testTrue(condition, "xabcdef",0);
		testTrue(condition, "efxabcdef",2);
		testTrue(condition, "cdefxabcdef",4);
		testTrue(condition, "abcdefxabcdef",6);
		testTrue(condition, "abcdefxabcd",6);
		testTrue(condition, "abcdefxab",6);
		testTrue(condition, "abcdefx",6);

		testTrue(condition, "abx",2);
		testTrue(condition, "cdx",2);
		testTrue(condition, "efx",2);

		testTrue(condition, "abefx",4);
		testTrue(condition, "abcdx",4);
		testTrue(condition, "cdefx",4);
	}

	@Test
	public void testSet01()  {
		Condition condition = new Condition("_{a b c}ds");
		testTrue(condition,  "xads",0);
		testTrue(condition,  "xbds",0);
		testTrue(condition,  "xcds",0);
		testFalse(condition, "xds", 0);
	}

	@Test
	public void testSet02()  {
		Condition condition = new Condition("_{ab cd ef}tr");
		testTrue(condition,  "xabtr",0);
		testTrue(condition,  "xcdtr",0);
		testTrue(condition,  "xeftr",0);
		testFalse(condition, "xabcd", 0);
		testFalse(condition, "xtr",0);
	}

	@Test
	public void testSet04()  {
		Condition condition = new Condition("_{ab* cd+ ef}tr");

		testTrue(condition,  "xabtr", 0);
		testTrue(condition,  "xcdtr", 0);
		testTrue(condition,  "xeftr", 0);

		testFalse(condition, "xacd", 0);
		testFalse(condition, "xabbcd", 0);
		testFalse(condition, "xabx", 0);
		testFalse(condition, "xabcd", 0);
		testFalse(condition, "xb", 0);
		testFalse(condition, "x", 0);
		testFalse(condition, "xc", 0);
		testFalse(condition, "xcdef", 0);
		testFalse(condition, "xtr",   0);
	}

	@Test
	public void testSet05()  {
		Condition condition = new Condition("_{ab* (cd?)+ ((ae)*f)+}tr");

		testTrue(condition,  "xabtr",0);

		testTrue(condition,  "xcdtr",0);
		testTrue(condition,  "xcctr",0);
		testTrue(condition,  "xccctr",0);

		testTrue(condition,  "xftr",0);
		testTrue(condition,  "xfftr",0);
		testTrue(condition,  "xaeftr",0);
		testTrue(condition,  "xaeaeftr",0);
		testTrue(condition,  "xaefaeftr",0);
		testTrue(condition,  "xaefffffaeftr",0);

		testFalse(condition, "xabcd",0);
		testFalse(condition, "xtr",0);
	}

	@Test
	public void testSet06()  {
		Condition condition = new Condition("_{ab {cd xy} ef}tr");
		testTrue(condition,  "xabtr",0);
		testTrue(condition,  "xcdtr",0);
		testTrue(condition,  "xeftr",0);
		testTrue(condition,  "xxytr",0);
		testFalse(condition, "xabcd",0);
		testFalse(condition, "xtr",0);
	}

	@Test
	public void testSet07() {
		Condition condition = new Condition("_{ x ɣ }");
		testTrue(condition,  "pxi");
		testFalse(condition, "paxi");
	}

	@Test
	public void testComplex01() {
		SequenceFactory factoryParam = new SequenceFactory(FormatterMode.INTELLIGENT);
		Condition condition = new Condition("_{r l}?{a e o ā ē ō}{i u}?{n m l r}?{pʰ tʰ kʰ ḱʰ}", factoryParam);

		testTrue(factoryParam, condition, "pʰāḱʰus", 0);
		testTrue(factoryParam, condition, "pʰentʰros", 0);
		testTrue(factoryParam, condition, "pʰlaḱʰmēn", 0);
		testTrue(factoryParam, condition, "pʰoutʰéyet", 0);

		testFalse(factoryParam, condition, "pʰuǵos",0);
	}

	@Test
	public void testAdditional01() {

		testTrue(new Condition("_c+#"),  "abaccc", 2);
		testTrue(new Condition("_#"),    "abad",   3);
	}

	@Test
	public void testWithVariables01(){

		VariableStore store = new VariableStore();
		store.add("C = p t k b d g pʰ tʰ kʰ");

		SequenceFactory sequenceFactory = new SequenceFactory(FeatureModel.EMPTY_MODEL, store, new HashSet<String>(), FormatterMode.INTELLIGENT);
		Condition condition = new Condition("_C+#", sequenceFactory);

		testTrue(sequenceFactory, condition,  "abaptk",  2);
		testTrue(sequenceFactory, condition,  "abapppp", 2);
		testTrue(sequenceFactory, condition,  "ababdg",  2);
		testTrue(sequenceFactory, condition,  "abatʰkʰ", 2);

		testTrue(sequenceFactory, condition,  "abaptk",  3);
		testTrue(sequenceFactory, condition,  "abapppp", 3);
		testTrue(sequenceFactory, condition,  "ababdg",  3);
		testTrue(sequenceFactory, condition,  "abapʰtʰkʰ", 3);

		testFalse(sequenceFactory, condition,  "abatʰkʰ", 1);
		testFalse(sequenceFactory, condition,  "abatʰkʰ", 0);
	}
	
	@Test
	public void testVariablesDebug01(){
		VariableStore store = new VariableStore();
		store.add("C = p t k b d g pʰ tʰ kʰ");

		SequenceFactory sequenceFactory = new SequenceFactory(FeatureModel.EMPTY_MODEL, store, new HashSet<String>(),FormatterMode.INTELLIGENT);
		Condition condition = new Condition("_C+#", sequenceFactory);
		
		testTrue(sequenceFactory, condition,  "abatʰkʰ", 2);
	}

	@Ignore
	@Test
	public void testNegative00() {
		Condition condition = new Condition("_!a#");

		testTrue(condition,  "zb", 0);
		testTrue(condition,  "zc", 0);
		testTrue(condition,  "zd", 0);
		testTrue(condition,  "ze", 0);

		testFalse(condition, "za", 0); // Good
	}

	@Ignore
	@Test
	public void testNegative01() {
		Condition condition = new Condition("_!(abc)#");

		testTrue(condition,  "zbab", 0);
		testTrue(condition,  "zcab", 0);
		testTrue(condition,  "zdab", 0);
		testTrue(condition,  "zeab", 0);

		testTrue(condition,  "zaba", 0);
		testFalse(condition, "zabc", 0);
		testTrue(condition,  "zcba", 0);
		testTrue(condition,  "zaaa", 0);
	}

	@Ignore
	@Test
	public void testNegative02() {
		Condition condition = new Condition("_!{a b c}#");

		testTrue(condition, "yz", 0);
		testTrue(condition, "ym", 0);
		testTrue(condition, "yr", 0);
		testTrue(condition, "yd", 0);

		testFalse(condition, "x!a", 0); // Good
		testFalse(condition, "xa",  0);
		testFalse(condition, "xb",  0);
		testFalse(condition, "xc",  0);
	}

	private static void testTrue(SequenceFactory factory, Condition condition, String testString, int index) {
		Sequence word = factory.getSequence(testString);
		assertTrue(testString, condition.isMatch(word, index));
	}

	private static void testTrue(Condition condition, String testString, int index) {
		testTrue(FACTORY, condition, testString, index);
	}

	private static void testFalse(SequenceFactory factory, Condition condition, String testString, int index) {
		Sequence word = factory.getSequence(testString);
		assertFalse(testString, condition.isMatch(word, index));
	}

	private static void testFalse(Condition condition, String testString, int index) {
		testFalse(FACTORY, condition, testString, index);
	}

	private static void testFalse(Condition condition, String testString) {
		testFalse(condition, testString, 0);
	}

	private static void testTrue(Condition condition, String testString) {
		testTrue(condition, testString, 0);
	}

	private static void testPositive(Condition condition, String[] positive) {
		for (String p : positive) {
			testTrue(condition, p, 0);
		}
	}

	private static void testNegative(Condition condition, String[] negative) {
		for (String n : negative) {
			testFalse(condition, n, 0);
		}
	}
}


