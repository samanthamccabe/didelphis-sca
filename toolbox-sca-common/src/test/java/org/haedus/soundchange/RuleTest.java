/*******************************************************************************
 * Copyright (c) 2014 Haedus - Fabrica Codicis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.haedus.soundchange;

import org.haedus.datatypes.SegmentationMode;
import org.haedus.datatypes.phonetic.FeatureModel;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.SequenceFactory;
import org.haedus.datatypes.phonetic.VariableStore;
import org.haedus.exceptions.ParseException;
import org.haedus.soundchange.command.Rule;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Created with IntelliJ IDEA.
 * User: Samantha Fiona Morrigan McCabe
 * Date: 6/22/13
 * Time: 3:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class RuleTest {

	private static SequenceFactory factory;

	@BeforeClass
	public static void init() {
		factory = new SequenceFactory();
	}

	@Test
	public void testMetathesis01() throws ParseException {
		VariableStore store = new VariableStore();
		store.add("C = p t k");
		store.add("N = m n");

		Rule rule = new Rule("CN > $2$1", new FeatureModel(), store, SegmentationMode.DEFAULT);

		testRule(rule, "pn","np");
		testRule(rule, "tn","nt");
		testRule(rule, "kn","nk");

		testRule(rule, "pm","mp");
		testRule(rule, "tm","mt");
		testRule(rule, "km","mk");

		testRule(rule, "pt","pt");
		testRule(rule, "tp","tp");
		testRule(rule, "kp","kp");
	}

	@Test
	public void testMetathesis02() throws ParseException {
		VariableStore store = new VariableStore();
		store.add("C = p t k");
		store.add("N = m n");
		store.add("V = a i u");

		Rule rule = new Rule("CVN > $3V$1", new FeatureModel(), store, SegmentationMode.DEFAULT);

		testRule(rule, "pan", "nap");
		testRule(rule, "tin", "nit");
		testRule(rule, "kun", "nuk");

		testRule(rule, "pam", "map");
		testRule(rule, "tim", "mit");
		testRule(rule, "kum", "muk");

		testRule(rule, "pat", "pat");
		testRule(rule, "tip", "tip");
		testRule(rule, "kup", "kup");
	}

	@Test
	public void testMetathesis03() throws ParseException {
		VariableStore store = new VariableStore();
		store.add("C = p t k");
		store.add("G = b d g");
		store.add("N = m n"  );

		Rule rule = new Rule("CN > $2$G1", new FeatureModel(), store, SegmentationMode.DEFAULT);

		testRule(rule, "pn", "nb");
		testRule(rule, "tn", "nd");
		testRule(rule, "kn", "ng");

		testRule(rule, "pm", "mb");
		testRule(rule, "tm", "md");
		testRule(rule, "km", "mg");

		testRule(rule, "pt", "pt");
		testRule(rule, "tp", "tp");
		testRule(rule, "kp", "kp");
	}

	@Test
	public void testDeletion01() throws ParseException {
		Rule rule = new Rule("∅ - > 0");
		testRule(rule, "∅-s-irentu-pʰen", "sirentupʰen");
	}

	@Test
	public void testDeletion02() throws ParseException {
		Rule rule = new Rule("a > 0");
		testRule(rule, "aaaabbba", "bbb");
	}

	@Test
	public void testDeletion03() throws ParseException {
		Rule rule = new Rule("a b > 0");
		testRule(rule, "aaaaccbbccbba", "cccc");
	}

	@Test
	public void testRule01() throws ParseException {
		Rule rule = new Rule("a > b");

		testRule(rule, "aaaaaaccca", "bbbbbbcccb");
	}

	@Test
	public void testRule02() throws ParseException {
		Rule rule = new Rule("a e > æ ɛ");

		testRule(rule, "ate", "ætɛ");
		testRule(rule, "atereyamane", "ætɛrɛyæmænɛ");
	}

	@Test
	public void testRule03() throws ParseException {
		Rule rule = new Rule("a b c d e f g > A B C D E F G");

		testRule(rule, "abcdefghijk", "ABCDEFGhijk");
	}

	@Test
	public void testConditional01() throws ParseException {
		Rule rule = new Rule("a > o / g_");

		testRule(rule, "ga", "go");
		testRule(rule, "adamagara", "adamagora");
	}

	@Test
	public void testConditional02() throws ParseException {
		Rule rule = new Rule("a > e / _c");
		testRule(rule, "abacaba", "abecaba");
		testRule(rule, "ababaca", "ababeca");
		testRule(rule, "acababa", "ecababa");
		testRule(rule, "acabaca", "ecabeca");
	}

	@Test
	public void testConditional03() throws ParseException {
		Rule rule = new Rule("a > e / _c+#");
		testRule(rule, "abac", "abec");
		testRule(rule, "abacc", "abecc");
		testRule(rule, "abaccc", "abeccc");
		testRule(rule, "abacccc", "abecccc");
		testRule(rule, "abaccccc", "abeccccc");
	}

	@Test
	public void testUnconditional04() throws ParseException {
		Rule rule = new Rule("eʔe aʔa eʔa aʔe > ē ā ā ē");
		testRule(rule, "keʔe", "kē");
		testRule(rule, "kaʔa", "kā");
		testRule(rule, "keʔa", "kā");
		testRule(rule, "kaʔe", "kē");
	}

	@Test
	public void testConditional05() throws ParseException {
		Rule rule = new Rule("rˌh lˌh > ər əl / _a");
		testRule(rule, "krˌha", "kəra");
		testRule(rule, "klˌha", "kəla");
		testRule(rule, "klˌhe", "klˌhe");
	}

	@Test
	public void testConditional06() throws ParseException {
		Rule rule = new Rule("pʰ tʰ kʰ ḱʰ > b d g ɟ / _{r l}?{a e o ā ē ō}{i u}?{n m l r}?{pʰ tʰ kʰ ḱʰ}");

		testRule(rule, "pʰāḱʰus", "bāḱʰus");
		testRule(rule, "pʰentʰros", "bentʰros");
		testRule(rule, "pʰlaḱʰmēn", "blaḱʰmēn");
		testRule(rule, "pʰoutʰéyet", "boutʰéyet");

		testRule(rule, "pʰɛḱʰus", "pʰɛḱʰus");
	}

	@Test
	public void testConditional07() throws ParseException {
		Rule rule = new Rule("pʰ tʰ kʰ ḱʰ > b d g ɟ / _{a e o}{pʰ tʰ kʰ ḱʰ}");

		testRule(rule, "pʰaḱʰus", "baḱʰus");
		testRule(rule, "pʰāḱʰus", "pʰāḱʰus");
	}

	@Test
	public void testConditional08() throws ParseException {
		Rule rule = new Rule("d > t / _#");

		testRule(rule, "abad", "abat");
		testRule(rule, "abada", "abada");
	}

	@Test
	public void testLoop01() throws ParseException {
		Rule rule = new Rule("q > qn");

		testRule(rule, "aqa", "aqna");
	}

	@Test
	public void testUnconditional() throws ParseException {
		Sequence word     = factory.getSequence("h₁óh₁es-");
		Sequence expected = factory.getSequence("ʔóʔes-");

		Rule rule = new Rule("h₁ h₂ h₃ h₄ > ʔ x ɣ ʕ");

		assertEquals(expected, rule.apply(word));
	}

	@Test
	public void testUnconditional02() throws ParseException {
		Sequence expected = factory.getSequence("telə");

		Rule rule = new Rule("eʔé > ê");

		assertEquals(expected, rule.apply(expected));
	}

	@Test
	public void testDebug01() throws ParseException {
		Sequence original = factory.getSequence("mlan");
		Sequence expected = factory.getSequence("blan");

		VariableStore vs = new VariableStore();
		vs.add("V = a e i o u");

		Rule rule = new Rule("ml > bl / #_V", vs, SegmentationMode.DEFAULT);

		assertEquals(expected, rule.apply(original));
	}

	@Test
	public void testUnconditional03() throws ParseException {
		Rule rule = new Rule("ox > l");

		testRule(rule, "oxoxoxox", "llll");
		testRule(rule, "moxmoxmoxmoxmox", "mlmlmlmlml");
		testRule(rule, "mmoxmmoxmmoxmmoxmmox", "mmlmmlmmlmmlmml");
	}

	// "trh₂-we"
	@Test
	public void testDebug02() throws ParseException {
		Sequence original = factory.getSequence("trh₂we");
		Sequence expected = factory.getSequence("tə̄rwe");

		VariableStore vs = new VariableStore();
		vs.add("X  = h₁  h₂ h₃ h₄" );
		vs.add("A  = r   l  m  n"  );
		vs.add("W  = y   w"        );
		vs.add("Q  = kʷʰ kʷ gʷ"    );
		vs.add("K  = kʰ  k  g");
		vs.add("KY = cʰ  c  ɟ");
		vs.add("T  = pʰ  p  b");
		vs.add("P  = tʰ  t  d");

		vs.add("[PLOSIVE] = P T K KY Q"     );
		vs.add("[OBSTRUENT] = [PLOSIVE] s"  );
		vs.add("C = [OBSTRUENT] A W"        );

		Rule rule1 = new Rule("rX lX nX mX > r̩X l̩X n̩X m̩X / [OBSTRUENT]_", vs, SegmentationMode.DEFAULT);
		Rule rule2 = new Rule("r l > r̩ l̩ / [OBSTRUENT]_{C #}"             , vs, SegmentationMode.DEFAULT);
		Rule rule3 = new Rule("r̩ l̩ > r l / C_N{C #}"                      , vs, SegmentationMode.DEFAULT);
		Rule rule4 = new Rule("r̩X l̩X > ə̄r ə̄l   / _{C #}"                , vs, SegmentationMode.DEFAULT);

		Sequence sequence = rule1.apply(original);

		sequence = rule2.apply(sequence);
		sequence = rule3.apply(sequence);
		sequence = rule4.apply(sequence);

		assertEquals(expected, sequence);
	}

	@Test
	public void testDebug03() throws ParseException {
		Sequence original = factory.getSequence("pʰabopa");
		Sequence expected = factory.getSequence("papoba");

		Rule rule = new Rule("pʰ p b > p b p");

		Sequence received = rule.apply(original);
		assertEquals(expected, received);
	}

	@Test
	public void testCompound01() throws ParseException {
		Rule rule = new Rule("a > b / x_ OR _y");

		testRule(rule, "axa", "axb");
		testRule(rule, "aya", "bya");
		testRule(rule, "ayxa", "byxb");
		testRule(rule, "axya", "axya");
	}

	@Test
	public void testCompound02() throws ParseException {
		Rule rule = new Rule("a > b / x_ NOT _y");

		testRule(rule, "axa",   "axb");
		testRule(rule, "axay",  "axay");
		testRule(rule, "xayxa", "xayxb");
	}

	@Test
	public void testCompound03() throws ParseException {
		VariableStore store = new VariableStore();
		store.add("C = x y z");

		Rule rule = new Rule("a > b / C_ NOT x_", new FeatureModel(), store, SegmentationMode.DEFAULT);

		testRule(rule, "axa",   "axa");
		testRule(rule, "aya",   "ayb");
		testRule(rule, "aza",   "azb");
	}

	/*======================================================================+
	 | Exception Tests                                                      |
	 +======================================================================*/
	@Test(expected = ParseException.class)
	public void testRuleException01() throws ParseException {
		new Rule(" > ");
	}

	@Test(expected = ParseException.class)
	public void testRuleException02() throws ParseException {
		new Rule("a > b /");
	}

	@Test(expected = ParseException.class)
	public void testRuleException03() throws ParseException {
		new Rule("a > / b");
	}

	@Test(expected = ParseException.class)
	public void testRuleException04() throws ParseException {
		new Rule(" > a / b");
	}

	@Test(expected = ParseException.class)
	public void testRuleException05() throws ParseException {
		new Rule(" > / b");
	}

	private void testRule(Rule rule, String seq, String exp) {
		Sequence sequence = factory.getSequence(seq);
		Sequence expected = factory.getSequence(exp);
		Sequence received = rule.apply(sequence);

		assertEquals(expected, received);
	}
}
