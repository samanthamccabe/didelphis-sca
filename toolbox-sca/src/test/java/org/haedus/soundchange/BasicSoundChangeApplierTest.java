/******************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe                                  *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *     http://www.apache.org/licenses/LICENSE-2.0                             *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 ******************************************************************************/

package org.haedus.soundchange;

import org.haedus.enums.FormatterMode;
import org.haedus.phonetic.Lexicon;
import org.haedus.phonetic.SequenceFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 1/19/2015
 */
public class BasicSoundChangeApplierTest {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(BasicSoundChangeApplierTest.class);

	public static final SequenceFactory FACTORY_NONE        = new SequenceFactory(FormatterMode.NONE);
	public static final SequenceFactory FACTORY_INTELLIGENT = new SequenceFactory(FormatterMode.INTELLIGENT);

	@Test
	public void testBreak() {
		String[] commands = {
				"x > y",
				"BREAK",
				"a > b"
		};

		String[] lexicon = {"x", "xxa", "a"};
		String[] exWords = {"y", "yya", "a"};

		SoundChangeScript sca = new BasicScript(commands, lexicon, FormatterMode.NONE);
		sca.process();
		Lexicon received = sca.getLexicon(BasicScript.DEFAULT_LEXICON);
		Lexicon expected = FACTORY_NONE.getLexiconFromSingleColumn(exWords);
		assertEquals(expected, received);
	}

	@Test
	public void testNormalizerDecomposition() {

		String[] lexicon = {
				"á", "ö", "ē", "ȕ"
		};
		SoundChangeScript sca = new BasicScript(new String[]{}, lexicon, FormatterMode.DECOMPOSITION);
		sca.process();
		Lexicon received = sca.getLexicon(BasicScript.DEFAULT_LEXICON);
		Lexicon expected = new SequenceFactory(FormatterMode.DECOMPOSITION).getLexiconFromSingleColumn(lexicon);

		assertEquals(expected, received);
	}

	@Test
	public void testNormalizerNFC() {
		String[] lexicon = {"á", "ā", "ï", "à", "ȍ", "ő"};

		SoundChangeScript sca = new BasicScript(new String[]{}, lexicon, FormatterMode.COMPOSITION);
		sca.process();
		Lexicon received = sca.getLexicon(BasicScript.DEFAULT_LEXICON);
		Lexicon expected = FACTORY_NONE.getLexiconFromSingleColumn(lexicon);
		assertEquals(expected, received);
	}

	@Test
	public void TestNormalizerNFCvsNFD() {

		String[] lexicon = {"á", "ā", "ï", "à", "ȍ", "ő"};
		SoundChangeScript scaComp = new BasicScript(new String[]{}, lexicon, FormatterMode.COMPOSITION);
		SoundChangeScript scaDeco = new BasicScript(new String[]{}, lexicon, FormatterMode.DECOMPOSITION);
		scaComp.process();
		scaDeco.process();
		Lexicon lexComp = scaComp.getLexicon(BasicScript.DEFAULT_LEXICON);
		Lexicon lexDeco = scaDeco.getLexicon(BasicScript.DEFAULT_LEXICON);
		assertNotEquals(lexComp, lexDeco);
	}

	@Test
	public void simpleRuleTest01() {
		String[] commands = {
				"a > e",
				"d > t / _#"
		};

		String[] lexicon = {"abad", "abada", "ad", "ado"};
		String[] exWords = {"ebet", "ebede", "et", "edo"};

		SoundChangeScript sca = new BasicScript(commands, lexicon, FormatterMode.NONE);
		sca.process();
		Lexicon received = sca.getLexicon(BasicScript.DEFAULT_LEXICON);
		Lexicon expected = FACTORY_NONE.getLexiconFromSingleColumn(exWords);
		assertEquals(expected, received);
	}

	@Test
	public void simpleRuleTest02() {
		String[] commands = {
				"ḱʰ ḱ ǵ > cʰ c ɟ",
				"cʰs cs ɟs > ks ks ks",
				"s > 0 / {cʰ  c  ɟ}_",
				"tk tʰkʰ ct ɟt ck  > ks ks ɕt ɕt ɕk",
				"tc dc tcʰ tʰcʰ > cc"
		};

		String[] lexicon = {"ruḱso", "tkeh", "oḱto", "artḱos"};
		String[] exWords = {"rukso", "kseh", "oɕto", "arccos"};

		SoundChangeScript sca = new BasicScript(commands, lexicon, FormatterMode.INTELLIGENT);
		sca.process();
		Lexicon received = sca.getLexicon(BasicScript.DEFAULT_LEXICON);
		Lexicon expected = FACTORY_NONE.getLexiconFromSingleColumn(exWords);
		assertEquals(expected, received);
	}

	@Test
	public void simpleRuleTest03() {
		String[] commands = {
				"- > 0",
				"h₁ h₂ h₃ h₄ > ʔ x ɣ ʕ",
				"hₓ hₐ > ʔ ʕ",
				"b  d  ǵ  g  gʷ  > bʰ dʰ ǵʰ gʰ gʷʰ / _{ x ɣ }",
				"p  t  ḱ  k  kʷ  > pʰ tʰ ḱʰ kʰ kʷʰ / _{ x ɣ }",
				"bʰ dʰ ǵʰ gʰ gʷʰ > pʰ tʰ ḱʰ kʰ kʷʰ",
				"ḱ ḱʰ ǵ > c cʰ ɟ"
		};

		String[] lexicon = {
				"h₂oḱ-ri-", "bʰaḱehₐ-",
				"dʰh₁ilehₐ-", "ǵenh₁trihₐ-",
				"h₂rǵ-i-ḱuon-", "h₂wedh₂-",
				"dʰǵʰuhₓ", "dʰh₁ilehₐ-"};

		String[] exWords = {
				"xocri", "pʰaceʕ",
				"tʰʔileʕ", "ɟenʔtriʕ",
				"xrɟicuon", "xwetʰx",
				"tʰcʰuʔ", "tʰʔileʕ"};

		SoundChangeScript sca = new BasicScript(commands, lexicon, FormatterMode.INTELLIGENT);
		sca.process();
		Lexicon received = sca.getLexicon(BasicScript.DEFAULT_LEXICON);
		Lexicon expected = FACTORY_INTELLIGENT.getLexiconFromSingleColumn(exWords);
		assertEquals(expected, received);
	}

	@Test
	public void simpleRuleTest04() {
		String[] commands = {
				"e é ē ê > a á ā â / {x ʕ}_",
				"e é ē ê > a á ā â / _{x ʕ}",
				"e é ē ê > o ó ō ô / ɣ_",
				"e é ē ê > o ó ō ô / _ɣ",
				"ɣ ʕ > x ʔ",
				"x ʔ > 0 / _{x ʔ}",
				"x ʔ > 0 / _{i y í}",
				"ix iʔ ux uʔ > ī ī ū ū"
		};

		String[] lexicon = {
				"xocri", "pʰaceʕ", "tʰʔileʕ", "ɟenʔtriʕ",
				"xrɟicuon", "xwetʰx", "tʰcʰuʔ", "tʰʔileʕ"
		};

		String[] exWords = {
				"xocri", "pʰacaʔ", "tʰilaʔ", "ɟenʔtrī",
				"xrɟicuon", "xwetʰx", "tʰcʰū", "tʰilaʔ"
		};

		SoundChangeScript sca = new BasicScript(commands, lexicon, FormatterMode.INTELLIGENT);
		sca.process();
		Lexicon received = sca.getLexicon(BasicScript.DEFAULT_LEXICON);
		Lexicon expected = FACTORY_INTELLIGENT.getLexiconFromSingleColumn(exWords);
		assertEquals(expected, received);
	}

	@Test
	public void reassignmentTest01() {
		String[] commands = {
				"% Comment",
				"C = p t k pʰ tʰ kʰ n m r l",
				"e o > i u / _C",

				"C = p t k",
				"i u > a a / _C",
		};

		String[] lexicon = {"otetʰ"};
		String[] exWords = {"atitʰ"};

		SoundChangeScript sca = new BasicScript(commands, lexicon, FormatterMode.INTELLIGENT);
		sca.process();
		Lexicon received = sca.getLexicon(BasicScript.DEFAULT_LEXICON);
		Lexicon expected = FACTORY_INTELLIGENT.getLexiconFromSingleColumn(exWords);
		assertEquals(expected, received);
	}

	@Test
	public void ruleTest01() {
		String[] commands = {
				"% Comment",
				"C = p t k pʰ tʰ kʰ n m r l",
				"e o > i u / #_C",
		};

		String[] lexicon = {"epet", "epete", "et", "eto", "om", "elon", "tet"};
		String[] exWords = {"ipet", "ipete", "it", "ito", "um", "ilon", "tet"};

		SoundChangeScript sca = new BasicScript(commands, lexicon, FormatterMode.INTELLIGENT);
		sca.process();
		Lexicon received = sca.getLexicon(BasicScript.DEFAULT_LEXICON);
		Lexicon expected = FACTORY_INTELLIGENT.getLexiconFromSingleColumn(exWords);
		assertEquals(expected, received);
	}

	@Test
	public void ruleTest02() {
		String[] commands = {
				"K  = p  t  k",
				"CH = pʰ tʰ kʰ",
				"R  = r l",
				"N  = n m",
				"C  = K CH N R",
				"G  = b d g",
				"V  = a e i o u ā ē ī ō ū",
				"CH > G / _R?VV?C*CH"
		};

		String[] lexicon = {"pʰapʰa"};
		String[] exWords = {"bapʰa"};

		SoundChangeScript sca = new BasicScript(commands, lexicon, FormatterMode.INTELLIGENT);
		sca.process();
		Lexicon received = sca.getLexicon(BasicScript.DEFAULT_LEXICON);
		Lexicon expected = FACTORY_INTELLIGENT.getLexiconFromSingleColumn(exWords);
		assertEquals(expected, received);
	}

	@Test
	public void testDebug002() {
		String[] commands = {
				"AT = î",
				"C  = þ s n",
				"IN = ĕ",
				"IN > 0 / ATC_#"};

		String[] lexicon = {"þîsĕ", "þîsnĕ"};
		String[] exWords = {"þîs", "þîsnĕ"};

		SoundChangeScript sca = new BasicScript(commands, lexicon, FormatterMode.INTELLIGENT);
		sca.process();
		Lexicon received = sca.getLexicon(BasicScript.DEFAULT_LEXICON);
		Lexicon expected = FACTORY_INTELLIGENT.getLexiconFromSingleColumn(exWords);
		assertEquals(expected, received);
	}

	@Test
	public void simpleNoSegmentation() {
		String[] commands = {
				"ḱʰ ḱ ǵ > cʰ c ɟ",
				"cʰs cs ɟs > ks ks ks",
				"s > 0 / {cʰ  c  ɟ}_",
				"tk tʰkʰ ct ɟt ck  > ks ks ɕt ɕt ɕk",
				"tc dc tcʰ tʰcʰ > cc"
		};

		String[] lexicon = {"ruḱso", "tkeh", "oḱto", "artḱos"};
		String[] exWords = {"rukso", "kseh", "oɕto", "arccos"};

		SoundChangeScript sca = new BasicScript(commands, lexicon, FormatterMode.NONE);
		sca.process();
		Lexicon received = sca.getLexicon(BasicScript.DEFAULT_LEXICON);
		Lexicon expected = FACTORY_NONE.getLexiconFromSingleColumn(exWords);
		assertEquals(expected, received);
	}

	@Test
	public void simpleNoSegmentation01() {
		String[] commands = {
				"ḱ  > ɟ",
				"ḱʰ > cʰ",
				"ǵ  > j"
		};

		String[] lexicon = {"ruḱo", "ḱʰeh", "oḱto", "arǵos"};
		String[] exWords = {"ruɟo", "ɟʰeh", "oɟto", "arjos"};

		SoundChangeScript sca = new BasicScript(commands, lexicon, FormatterMode.NONE);
		sca.process();
		Lexicon received = sca.getLexicon(BasicScript.DEFAULT_LEXICON);
		Lexicon expected = FACTORY_NONE.getLexiconFromSingleColumn(exWords);
		assertEquals(expected, received);
	}

	@Test
	public void reserveNaiveSegmentationTest() {
		String[] commands = {
				"ph th kh > f h x"
		};

		String[] lexicon = {"kho"};
		String[] exWords = {"xo"};

		SoundChangeScript sca = new BasicScript(commands, lexicon, FormatterMode.INTELLIGENT);
		sca.process();
		Lexicon received = sca.getLexicon(BasicScript.DEFAULT_LEXICON);
		Lexicon expected = FACTORY_NONE.getLexiconFromSingleColumn(exWords);
		assertEquals(expected, received);
	}

	@Test
	public void reserveDefaultSegmentationTest() {
		String[] commands = {
				"RESERVE ph th kh",
				"ph th kh > f h x"
		};

		String[] lexicon = {"rukho", "khek", "ophto", "arthos", "taphos"};
		String[] exWords = {"ruxo", "xek", "ofto", "arhos", "tafos"};

		SoundChangeScript sca = new BasicScript(commands, lexicon, FormatterMode.NONE);
		sca.process();
		Lexicon received = sca.getLexicon(BasicScript.DEFAULT_LEXICON);
		Lexicon expected = FACTORY_NONE.getLexiconFromSingleColumn(exWords);
		assertEquals(expected, received);
	}

	@Test
	public void testMetathesis01() {
		String[] commands = {
				"C = p t k",
				"N = m n",
				"Cn Cm > nC nC / _#"
		};

		String[] lexicon = {"atn"};
		String[] exWords = {"ant"};

		SoundChangeScript sca = new BasicScript(commands, lexicon, FormatterMode.NONE);
		sca.process();
		Lexicon received = sca.getLexicon(BasicScript.DEFAULT_LEXICON);
		Lexicon expected = FACTORY_NONE.getLexiconFromSingleColumn(exWords);
		assertEquals(expected, received);
	}

	private static void assertNotEquals(Object expected, Object received) {
		assertFalse(expected.equals(received));
	}
}
