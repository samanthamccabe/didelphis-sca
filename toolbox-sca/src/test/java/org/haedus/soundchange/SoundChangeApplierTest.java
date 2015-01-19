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
import org.haedus.datatypes.phonetic.Lexicon;
import org.haedus.datatypes.phonetic.SequenceFactory;
import org.haedus.exceptions.ParseException;
import org.haedus.io.ClassPathFileHandler;
import org.haedus.io.MockFileHandler;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: Samantha Fiona Morrigan McCabe
 * Date: 9/19/13
 * Time: 10:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class SoundChangeApplierTest {

	public static final            ClassPathFileHandler CLASSPATH_HANDLER   = new ClassPathFileHandler();
	public static final            SequenceFactory      FACTORY_NONE        = new SequenceFactory(FormatterMode.NONE);
	public static final            SequenceFactory      FACTORY_INTELLIGENT = new SequenceFactory(FormatterMode.INTELLIGENT);
	private static final transient Logger               LOGGER              = LoggerFactory.getLogger(SoundChangeApplierTest.class);

	private static void assertNotEquals(Object expected, Object received) {
		assertFalse(expected.equals(received));
	}

	@Test(expected = ParseException.class)
	public void testNormalizerBadMode() {
		new StandardScript("NORMALIZER:XXX");
	}

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
	public void testRuleLarge01() throws Exception {

		String[] exWords = {
				"xocri", "pʰacā",
				"tʰilā", "ɟentrī",
				"ərɟicwon", "əwetʰə",
				"ccū", "ccemen",
				"cʰesəlccomtʰə", "byôuyom",
				"tusciyos", "tə̄rwe",
				"tou", "telə",
				"somoɟəyos", "sēm",
				"ôwes", "blan"
		};

		SoundChangeScript sca = new StandardScript("IMPORT 'testRuleLarge01.txt'", CLASSPATH_HANDLER);
		sca.process();
		Lexicon received = sca.getLexicon("LEXICON");
		Lexicon expected = FACTORY_INTELLIGENT.getLexiconFromSingleColumn(exWords);
		assertEquals(expected, received);
	}

	@Test(timeout = 2000)
	public void testLoop01() {
		String[] commands = {
				"P = pw p t k",
				"B = bw b d g",
				"V = a o",
				"P > B / V_V",
				"P = p t k",
				"B = b d g",
				"B > 0 / #_c"
		};

		new StandardScript(commands);
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
	public void reserveTest() {
		String[] commands = {
				"SEGMENTATION: FALSE",
				"RESERVE ph th kh"
		};
		StandardScript sca = new StandardScript(commands);
		sca.process();
		Collection<String> received = sca.getReservedSymbols();
		Collection<String> expected = new HashSet<String>();
		expected.add("ph");
		expected.add("th");
		expected.add("kh");
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

	@Test
	public void testOpen01() {

		String[] lexicon = {
				"apat",
				"takan",
				"kepak",
				"pik",
				"ket"
		};

		SoundChangeScript sca = new StandardScript("OPEN \'testLexicon.lex\' as TEST", CLASSPATH_HANDLER);
		sca.process();
		assertTrue("Lexicon 'TEST' not found.", sca.hasLexicon("TEST"));
		Lexicon expected = FACTORY_NONE.getLexiconFromSingleColumn(lexicon);
		Lexicon received = sca.getLexicon("TEST");
		assertEquals(expected, received);
	}

	@Test
	public void testOpen02() {
		String lexicon = "" +
				"apat\n" +
				"takan\n" +
				"kepak\n" +
				"pik\n" +
				"ket";

		Map<String, String> map = new HashMap<String, String>();
		map.put("test.lex", lexicon);

		SoundChangeScript sca = new StandardScript("OPEN 'test.lex' as TEST", new MockFileHandler(map));
		sca.process();

		Lexicon expected = FACTORY_NONE.getLexiconFromSingleColumn(lexicon.split("\\n\\r?"));
		Lexicon received = sca.getLexicon("TEST");
		assertEquals(expected, received);
	}

	@Test
	public void testWrite01() {
		String lexicon = "" +
				"apat\n" +
				"takan\n" +
				"kepak\n" +
				"pik\n" +
				"ket";

		Map<String, String> map = new HashMap<String, String>();
		map.put("test.lex", lexicon);

		String[] commands = {
				"OPEN 'test.lex' as TEST",
				"WRITE TEST as 'write.lex'",
				"CLOSE TEST as 'close.lex'"
		};

		SoundChangeScript sca = new StandardScript(commands, new MockFileHandler(map));
		sca.process();

		assertFalse(sca.hasLexicon("TEST"));
		assertTrue(map.containsKey("close.lex"));
		assertTrue(map.containsKey("write.lex"));
		assertEquals(lexicon, map.get("write.lex"));
		assertEquals(lexicon, map.get("close.lex"));
	}
}
