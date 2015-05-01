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

package org.haedus.soundchange.command;

import org.haedus.enums.FormatterMode;
import org.haedus.phonetic.FeatureModel;
import org.haedus.phonetic.Sequence;
import org.haedus.phonetic.SequenceFactory;
import org.haedus.phonetic.VariableStore;
import org.haedus.soundchange.exceptions.RuleFormatException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: Samantha Fiona Morrigan McCabe
 * Date: 6/22/13
 * Time: 3:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class RuleModelTest {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(RuleModelTest.class);

	private static final Set<String>     EMPTY_SET   = new HashSet<String>();
	private static final FeatureModel    MODEL       = loadModel();
	private static final SequenceFactory FACTORY = new SequenceFactory(MODEL, FormatterMode.INTELLIGENT);

	@Test(expected = RuleFormatException.class)
	public void testFeatureTransformOutOfRange() {
		new Rule("a > g[hgt:1]", FACTORY);
	}

	@Test
	public void testFeatureTransform() {
		Rule rule = new Rule("[son:3, +con, hgt:-1, +frn, -bck, -atr, glt:0] > [hgt:1, +atr]", FACTORY);
		testRule(rule, "a", "i");
	}

	@Test
	public void testMetathesis01() {
		VariableStore store = new VariableStore();
		store.add("C = p t k");
		store.add("N = m n");

		SequenceFactory factory = new SequenceFactory(MODEL, store, EMPTY_SET, FormatterMode.INTELLIGENT);

		Rule rule = new Rule("CN > $2$1", factory);

		testRule(rule, "pn", "np");
		testRule(rule, "tn", "nt");
		testRule(rule, "kn", "nk");

		testRule(rule, "pm", "mp");
		testRule(rule, "tm", "mt");
		testRule(rule, "km", "mk");

		testRule(rule, "pt", "pt");
		testRule(rule, "tp", "tp");
		testRule(rule, "kp", "kp");
	}

	@Test
	public void testMetathesis02() {
		VariableStore store = new VariableStore();
		store.add("C = p t k");
		store.add("N = m n");
		store.add("V = a i u");

		SequenceFactory factory = new SequenceFactory(MODEL, store, EMPTY_SET, FormatterMode.INTELLIGENT);


		Rule rule = new Rule("CVN > $3V$1", factory);

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
	public void testMetathesis03() {
		VariableStore store = new VariableStore();
		store.add("C = p t k");
		store.add("G = b d g");
		store.add("N = m n");
		SequenceFactory factory = new SequenceFactory(MODEL, store, EMPTY_SET, FormatterMode.INTELLIGENT);

		Rule rule = new Rule("CN > $2$G1", factory);

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
	public void testDeletion01() {
		Rule rule = new Rule("∅ - > 0", FACTORY);
		testRule(rule, FACTORY, "∅-s-irentu-pʰen", "sirentupʰen");
	}

	@Test
	public void testDeletion02() {
		Rule rule = new Rule("a > 0", FACTORY);
		testRule(rule, FACTORY, "aaaabbba", "bbb");
	}

	@Test
	public void testDeletion03() {
		Rule rule = new Rule("a b > 0", FACTORY);
		testRule(rule, FACTORY, "aaaaccbbccbba", "cccc");
	}

	@Test
	public void testRule01() {
		Rule rule = new Rule("a > b", FACTORY);

		testRule(rule, FACTORY, "aaaaaaccca", "bbbbbbcccb");
	}

	@Test
	public void testRule02() {
		Rule rule = new Rule("a e > æ ɛ", FACTORY);

		testRule(rule, FACTORY, "ate", "ætɛ");
		testRule(rule, FACTORY, "atereyamane", "ætɛrɛyæmænɛ");
	}

	@Test
	public void testRule03() {
		Rule rule = new Rule("a b c d e f g > A B C D E F G", FACTORY);

		testRule(rule, FACTORY, "abcdefghijk", "ABCDEFGhijk");
	}

	@Test
	public void testConditional01() {
		Rule rule = new Rule("a > o / g_", FACTORY);

		testRule(rule, FACTORY, "ga", "go");
		testRule(rule, FACTORY, "adamagara", "adamagora");
	}

	@Test
	public void testConditional02() {
		Rule rule = new Rule("a > e / _c", FACTORY);
		testRule(rule, FACTORY, "abacaba", "abecaba");
		testRule(rule, FACTORY, "ababaca", "ababeca");
		testRule(rule, FACTORY, "acababa", "ecababa");
		testRule(rule, FACTORY, "acabaca", "ecabeca");
	}

	@Test
	public void testConditional03() {
		Rule rule = new Rule("a > e / _c+#", FACTORY);
		testRule(rule, FACTORY, "abac", "abec");
		testRule(rule, FACTORY, "abacc", "abecc");
		testRule(rule, FACTORY, "abaccc", "abeccc");
		testRule(rule, FACTORY, "abacccc", "abecccc");
		testRule(rule, FACTORY, "abaccccc", "abeccccc");
	}

	@Test
	public void testUnconditional04() {
		Rule rule = new Rule("eʔe aʔa eʔa aʔe > ē ā ā ē", FACTORY);
		testRule(rule, FACTORY, "keʔe", "kē");
		testRule(rule, FACTORY, "kaʔa", "kā");
		testRule(rule, FACTORY, "keʔa", "kā");
		testRule(rule, FACTORY, "kaʔe", "kē");
	}

	@Test
	public void testConditional05() {
		Rule rule = new Rule("rˌh lˌh > ər əl / _a", FACTORY);
		testRule(rule, FACTORY, "krˌha", "kəra");
		testRule(rule, FACTORY, "klˌha", "kəla");
		testRule(rule, FACTORY, "klˌhe", "klˌhe");
	}

	@Test
	public void testConditional06() {
		Rule rule = new Rule("pʰ tʰ kʰ ḱʰ > b d g ɟ / _{r l}?{a e o ā ē ō}{i u}?{n m l r}?{pʰ tʰ kʰ ḱʰ}", FACTORY);

		testRule(rule, FACTORY, "pʰāḱʰus", "bāḱʰus");
		testRule(rule, FACTORY, "pʰentʰros", "bentʰros");
		testRule(rule, FACTORY, "pʰlaḱʰmēn", "blaḱʰmēn");
		testRule(rule, FACTORY, "pʰoutʰéyet", "boutʰéyet");
		testRule(rule, FACTORY, "pʰɛḱʰus", "pʰɛḱʰus");
	}

	@Test
	public void testConditional07() {
		Rule rule = new Rule("pʰ tʰ kʰ ḱʰ > b d g ɟ / _{a e o}{pʰ tʰ kʰ ḱʰ}", FACTORY);

		testRule(rule, FACTORY, "pʰaḱʰus", "baḱʰus");
		testRule(rule, FACTORY, "pʰaːḱʰus", "pʰaːḱʰus");
	}

	@Test
	public void testConditional08() {
		Rule rule = new Rule("d > t / _#", FACTORY);

		testRule(rule, FACTORY, "abad", "abat");
		testRule(rule, FACTORY, "abada", "abada");
	}

	@Test
	public void testInsertion01() {
		Rule rule = new Rule("q > qn", FACTORY);

		testRule(rule, FACTORY, "aqa", "aqna");
	}

	@Test
	public void testUnconditional() {
		Sequence word = FACTORY.getSequence("h₁óh₁es-");
		Sequence expected = FACTORY.getSequence("ʔóʔes-");

		Rule rule = new Rule("h₁ h₂ h₃ h₄ > ʔ x ɣ ʕ", FACTORY);

		assertEquals(expected, rule.apply(word));
	}

	@Test
	public void testUnconditional02() {
		Sequence expected = FACTORY.getSequence("telə");
		Rule rule = new Rule("eʔé > ê", FACTORY);

		assertEquals(expected, rule.apply(expected));
	}

	@Test
	public void testDebug01() {

		VariableStore store = new VariableStore();
		store.add("V = a e i o u");

		SequenceFactory factory = new SequenceFactory(FeatureModel.EMPTY_MODEL, store, EMPTY_SET, FormatterMode.INTELLIGENT);

		Sequence original = factory.getSequence("mlan");
		Sequence expected = factory.getSequence("blan");

		Rule rule = new Rule("ml > bl / #_V", factory);

		assertEquals(expected, rule.apply(original));
	}

	@Test
	public void testUnconditional03() {
		Rule rule = new Rule("ox > l", FACTORY);

		testRule(rule, FACTORY, "oxoxoxox", "llll");
		testRule(rule, FACTORY, "moxmoxmoxmoxmox", "mlmlmlmlml");
		testRule(rule, FACTORY, "mmoxmmoxmmoxmmoxmmox", "mmlmmlmmlmmlmml");
	}

	@Test
	public void testDebug02() {
		VariableStore store = new VariableStore();
		store.add("X  = h₁  h₂ h₃ h₄");
		store.add("A  = r   l  m  n");
		store.add("W  = y   w");
		store.add("Q  = kʷʰ kʷ gʷ");
		store.add("K  = kʰ  k  g");
		store.add("KY = cʰ  c  ɟ");
		store.add("T  = pʰ  p  b");
		store.add("P  = tʰ  t  d");

		store.add("[PLOSIVE] = P T K KY Q");
		store.add("[OBSTRUENT] = [PLOSIVE] s");
		store.add("C = [OBSTRUENT] A W");

		SequenceFactory factory = new SequenceFactory(FeatureModel.EMPTY_MODEL, store, new HashSet<String>(), FormatterMode.INTELLIGENT);

		Sequence original = factory.getSequence("trh₂we");
		Sequence expected = factory.getSequence("tə̄rwe");

		Rule rule1 = new Rule("rX lX nX mX > r̩X l̩X n̩X m̩X / [OBSTRUENT]_", factory);
		Rule rule2 = new Rule("r l > r̩ l̩ / [OBSTRUENT]_{C #}", factory);
		Rule rule3 = new Rule("r̩ l̩ > r l / C_N{C #}", factory);
		Rule rule4 = new Rule("r̩X l̩X > ə̄r ə̄l   / _{C #}", factory);

		Sequence sequence = rule1.apply(original);

		sequence = rule2.apply(sequence);
		sequence = rule3.apply(sequence);
		sequence = rule4.apply(sequence);

		assertEquals(expected, sequence);
	}

	@Test
	public void testDebug03() {
		Sequence original = FACTORY.getSequence("pʰabopa");
		Sequence expected = FACTORY.getSequence("papoba");

		Rule rule = new Rule("pʰ p b > p b p", FACTORY);

		Sequence received = rule.apply(original);
		assertEquals(expected, received);
	}

	@Test
	public void testCompound01() {
		Rule rule = new Rule("a > b / x_ OR _y", FACTORY);

		testRule(rule, FACTORY, "axa", "axb");
		testRule(rule, FACTORY, "aya", "bya");
		testRule(rule, FACTORY, "ayxa", "byxb");
		testRule(rule, FACTORY, "axya", "axya");
	}

	@Test
	public void testCompound02() {
		Rule rule = new Rule("a > b / x_ NOT _y", FACTORY);

		testRule(rule, FACTORY, "axa", "axb");
		testRule(rule, FACTORY, "axay", "axay");
		testRule(rule, FACTORY, "xayxa", "xayxb");
	}

	@Test
	public void testCompound03() {
		VariableStore store = new VariableStore();
		store.add("C = x y z");

		SequenceFactory factory = new SequenceFactory(FeatureModel.EMPTY_MODEL, store, EMPTY_SET, FormatterMode.INTELLIGENT);

		Rule rule = new Rule("a > b / C_ NOT x_", factory);

		testRule(rule, factory, "axa", "axa");
		testRule(rule, factory, "aya", "ayb");
		testRule(rule, factory, "aza", "azb");
	}

	private static void testRule(Rule rule, String seq, String exp) {
		testRule(rule, FACTORY, seq, exp);
	}

	private static void testRule(Rule rule, SequenceFactory factory, String seq, String exp) {
		Sequence sequence = factory.getSequence(seq);
		Sequence expected = factory.getSequence(exp);
		Sequence received = rule.apply(sequence);

		assertEquals(expected, received);
	}

	private static FeatureModel loadModel() {
		Resource resource = new ClassPathResource("features.model");
		FeatureModel model = null;
		try {
			model = new FeatureModel(resource.getFile());
		} catch (IOException e) {
			LOGGER.error("Failed to load file from {}", resource, e);
		}
		return model;
	}
}
