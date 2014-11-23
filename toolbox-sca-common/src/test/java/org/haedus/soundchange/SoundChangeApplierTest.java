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

import org.haedus.datatypes.Segmenter;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.VariableStore;
import org.haedus.exceptions.ParseException;
import org.haedus.io.ClassPathFileHandler;
import org.haedus.io.MockFileHandler;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
	private static final transient Logger LOGGER = LoggerFactory.getLogger(SoundChangeApplierTest.class);

	private static void assertNotEquals(Object expected, Object received) {
		assertFalse(expected.equals(received));
	}

	@Test(expected = ParseException.class)
	public void testNormalizerBadMode() throws ParseException {
		new SoundChangeApplier(new String[]{ "NORMALIZATION:XXX" });
	}

	@Test(expected = ParseException.class)
	public void testSegmentationBadMode() throws ParseException {
		new SoundChangeApplier(new String[]{ "SEGMENTATION:XXX" });
	}

	@Test
	public void testBreak() throws ParseException {
		String[] commands = { "x > y",
				"BREAK",
				"a > b"
		};
		SoundChangeApplier sca = new SoundChangeApplier(commands);

		List<String> words    = toList("x", "xxa","a");
		List<String> expected = toList("y", "yya","a");

		List<Sequence> received  = sca.processLexicon(words);
		List<Sequence> sequences = toSequences(expected, sca);
		testLists(received, sequences);
	}

	@Test
	public void testNormalizerNFD() throws ParseException {
		String[] commands = { "NORMALIZATION:NFD" };

		List<String> lexicon = new ArrayList<String>();
		Collections.addAll(lexicon, "á", "ā", "ï", "à", "ȍ", "ő");
		SoundChangeApplier soundChangeApplier = new SoundChangeApplier(commands);

		List<Sequence> expected = new ArrayList<Sequence>();
		List<Sequence> received = soundChangeApplier.processLexicon(lexicon);

		for (String s : lexicon) {
			String word = Normalizer.normalize(s, Normalizer.Form.NFD);
			Sequence sequence = Segmenter.getSequence(
					word,
					soundChangeApplier.getFeatureModel(),
					soundChangeApplier.getVariables());
			expected.add(sequence);
		}
		assertEquals(expected, received);
	}

	@Test
	public void testNormalizerNFC() throws ParseException {
		String[] commands = { "NORMALIZATION:NFC" };

		List<String> lexicon = new ArrayList<String>();
		Collections.addAll(lexicon, "á", "ā", "ï", "à", "ȍ", "ő");
		SoundChangeApplier soundChangeApplier = new SoundChangeApplier(commands);

		List<Sequence> expected = new ArrayList<Sequence>();
		List<Sequence> received = soundChangeApplier.processLexicon(lexicon);

		for (String s : lexicon) {
			String word = Normalizer.normalize(s, Normalizer.Form.NFC);
			Sequence sequence = new Sequence(word);
			expected.add(sequence);
		}
		assertEquals(expected, received);
	}

	@Test
	public void TestNormalizerNFCvsNFD() throws ParseException {
		String[] commands = { "NORMALIZATION:NFC" };

		List<String> lexicon = new ArrayList<String>();
		Collections.addAll(lexicon, "á", "ā", "ï", "à", "ȍ", "ő");
		SoundChangeApplier soundChangeApplier = new SoundChangeApplier(commands);

		List<Sequence> expected = new ArrayList<Sequence>();
		List<Sequence> received = soundChangeApplier.processLexicon(lexicon);

		for (String s : lexicon) {
			String word = Normalizer.normalize(s, Normalizer.Form.NFD);
			Sequence sequence = new Sequence(word);
			expected.add(sequence);
		}
		assertNotEquals(expected, received);
	}

	@Test
	public void simpleRuleTest01() throws ParseException {
		String[] commands = {
				"a > e",
				"d > t / _#"
		};

		SoundChangeApplier sca = new SoundChangeApplier(commands);

		List<String> words    = toList("abad", "abada", "ad", "ado");
		List<String> expected = toList("ebet", "ebede", "et", "edo");

		List<Sequence> received = sca.processLexicon(words);
		List<Sequence> sequences = toSequences(expected, sca);
		testLists(received, sequences);
	}

	@Test
	public void simpleRuleTest02() throws ParseException {
		String[] commands = {
				"NORMALIZATION: NFD",
				"ḱʰ ḱ ǵ > cʰ c ɟ",
				"cʰs cs ɟs > ks ks ks",
				"s > 0 / {cʰ  c  ɟ}_",
				"tk tʰkʰ ct ɟt ck  > ks ks ɕt ɕt ɕk",
				"tc dc tcʰ tʰcʰ > cc"
		};

		SoundChangeApplier sca = new SoundChangeApplier(commands);

		List<String> words    = toList("ruḱso", "tkeh", "oḱto", "artḱos");
		List<String> expected = toList("rukso", "kseh", "oɕto", "arccos");

		List<Sequence> received = sca.processLexicon(words);
		List<Sequence> sequences = toSequences(expected, sca);
		testLists(received, sequences);
	}

	@Test
	public void simpleRuleTest03() throws ParseException {
		String[] commands = {
				"- > 0",
				"h₁ h₂ h₃ h₄ > ʔ x ɣ ʕ",
				"hₓ hₐ > ʔ ʕ",
				"b  d  ǵ  g  gʷ  > bʰ dʰ ǵʰ gʰ gʷʰ / _{ x ɣ }",
				"p  t  ḱ  k  kʷ  > pʰ tʰ ḱʰ kʰ kʷʰ / _{ x ɣ }",
				"bʰ dʰ ǵʰ gʰ gʷʰ > pʰ tʰ ḱʰ kʰ kʷʰ",
				"ḱ ḱʰ ǵ > c cʰ ɟ"
				};

		List<String> words = toList(
				"h₂oḱ-ri-", "bʰaḱehₐ-",
				"dʰh₁ilehₐ-", "ǵenh₁trihₐ-",
				"h₂rǵ-i-ḱuon-", "h₂wedh₂-",
				"dʰǵʰuhₓ", "dʰh₁ilehₐ-");

		List<String> expected = toList(
				"xocri", "pʰaceʕ",
				"tʰʔileʕ", "ɟenʔtriʕ",
				"xrɟicuon", "xwetʰx",
				"tʰcʰuʔ", "tʰʔileʕ");

		SoundChangeApplier sca = new SoundChangeApplier(commands);
		List<Sequence> received = sca.processLexicon(words);
		List<Sequence> sequences = toSequences(expected, sca);
		testLists(received, sequences);
	}

	private void testLists(List<Sequence> received, List<Sequence> sequences) {
		assertEquals(sequences.size(), received.size());
		for (int i = 0; i < sequences.size(); i++) {
			assertEquals(sequences.get(i), received.get(i));
		}
	}

	@Test
	public void simpleRuleTest04() throws ParseException {
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

		List<String> words = toList(
				"xocri",    "pʰaceʕ",
				"tʰʔileʕ",  "ɟenʔtriʕ",
				"xrɟicuon", "xwetʰx",
				"tʰcʰuʔ",   "tʰʔileʕ");

		List<String> expected = toList(
				"xocri",    "pʰacaʔ",
				"tʰilaʔ",   "ɟenʔtrī",
				"xrɟicuon", "xwetʰx",
				"tʰcʰū",    "tʰilaʔ");

		SoundChangeApplier sca = new SoundChangeApplier(commands);
		List<Sequence> received = sca.processLexicon(words);
		List<Sequence> sequences = toSequences(expected, sca);
		testLists(received, sequences);
	}

	@Test
	public void reassignmentTest01() throws ParseException {
		String[] commands = {
				"% Comment",
				"C = p t k pʰ tʰ kʰ n m r l",
				"e o > i u / _C",

				"C = p t k",
				"i u > a a / _C",
		};

		SoundChangeApplier sca = new SoundChangeApplier(commands);

		List<String> words = toList("otetʰ");
		List<String> expected = toList("atitʰ");

		List<Sequence> received = sca.processLexicon(words);

		assertEquals(toSequences(expected, sca), received);
	}

	@Test
	public void ruleTest01() throws ParseException {
		String[] commands = {
				"% Comment",
				"C = p t k pʰ tʰ kʰ n m r l",
				"e o > i u / #_C",
		};

		SoundChangeApplier sca = new SoundChangeApplier(commands);

		List<String> words = toList(
				"epet",
				"epete",
				"et",
				"eto",
				"om",
				"elon",
				"tet");

		List<String> expected = toList(
				"ipet",
				"ipete",
				"it",
				"ito",
				"um",
				"ilon",
				"tet");

		List<Sequence> received = sca.processLexicon(words);

		assertEquals(toSequences(expected, sca), received);
	}

	@Test
	public void ruleTest02() throws ParseException {

		String[] commands = {
				"K  = p  t  k",
				"CH = pʰ tʰ kʰ",
				"R = r l",
				"N = n m",
				"C  = K CH N R",
				"G  = b d g",
				"V = a e i o u ā ē ī ō ū",
				"CH > G / _R?VV?C*CH"
		};

		SoundChangeApplier sca = new SoundChangeApplier(commands);

		List<String> words    = toList("pʰapʰa");
		List<String> expected = toList("bapʰa");

		List<Sequence> received = sca.processLexicon(words);

		assertEquals(toSequences(expected, sca), received);
	}

	@Test
	public void testExpansion01() throws Exception {

		SoundChangeApplier sca = new SoundChangeApplier(
				new String[]{"IMPORT 'testExpansion01.txt'"},
				new ClassPathFileHandler()
		);
		sca.processLexicon(new ArrayList<String>());

		VariableStore vs = sca.getVariables();
		testExpansion(vs, "V", "a e i o u ə ā ē ī ō ū ə̄");
		testExpansion(vs, "C", "pʰ p b tʰ t d cʰ c ɟ kʰ k g kʷʰ kʷ gʷ s m n r l j w");
	}

	private void testExpansion(VariableStore vs, String key, String terminals) {
		assertEquals(toSequences(terminals, new SoundChangeApplier()), vs.get(key));
	}

	@Test
	public void testRuleLarge01() throws Exception {

		List<String> words = toList(
				"h₂oḱ-ri-",        "bʰaḱehₐ-",
				"dʰh₁ilehₐ-",      "ǵenh₁trihₐ-",
				"h₂rǵ-i-ḱuon-",    "h₂wedh₂-",
				"dʰǵʰuhₓ",         "dʰǵʰem-en",
				"ǵʰes-l-dḱomth₂",  "gʷyéh₃uyom",
				"tussḱyos",        "trh₂-we",
				"teuhₐ-",          "telh₂-",
				"somo-ǵn̩h₁-yo-s", "sem-s",
				"h₁óh₁es-",        "mlan");

		List<String> expected = toList(
				"xocri",         "pʰacā",
				"tʰilā",         "ɟentrī",
				"ərɟicwon",      "əwetʰə",
				"ccū",           "ccemen",
				"cʰesəlccomtʰə", "byôuyom",
				"tusciyos",      "tə̄rwe",
				"tou",           "telə",
				"somoɟəyos",     "sēm",
				"ôwes",          "blan");

		SoundChangeApplier sca = new SoundChangeApplier(
				new String[]{"IMPORT 'testRuleLarge01.txt'"},
				new ClassPathFileHandler()
		);

		List<Sequence> received  = sca.processLexicon(words);
		List<Sequence> sequences = toSequences(expected, sca);
		testLists(received, sequences);
	}

	@Test
	public void testLoop01() throws ParseException {
		String[] commands = {
				"P = pw p t k",
				"B = bw b d g",
				"V = a o",
				"P > B / V_V",
				"P = p t k",
				"B = b d g",
				"B > 0 / #_c"
		};

		new SoundChangeApplier(commands);
	}

	@Test
	public void testDebug002() throws ParseException {
		String commands =
				"AT = î\n" +
				"C  = þ s n\n" +
				"IN = ĕ\n" +
				"IN > 0 / ATC_#";

		SoundChangeApplier sca = new SoundChangeApplier(commands);

		List<String> list = new ArrayList<String>();

		list.add("þîsĕ");
		list.add("þîsnĕ");

		List<String> expected = toList(
				"þîs",
				"þîsnĕ");

		List<Sequence> received = sca.processLexicon(list);

		assertEquals(toSequences(expected, sca), received);
	}

	@Test
	public void simpleNosegmentation() throws ParseException {
		String[] commands = {
				"NORMALIZATION: NONE",
				"SEGMENTATION: FALSE",
				"ḱʰ ḱ ǵ > cʰ c ɟ",
				"cʰs cs ɟs > ks ks ks",
				"s > 0 / {cʰ  c  ɟ}_",
				"tk tʰkʰ ct ɟt ck  > ks ks ɕt ɕt ɕk",
				"tc dc tcʰ tʰcʰ > cc"
		};

		SoundChangeApplier sca = new SoundChangeApplier(commands);

		List<String> words    = toList("ruḱso", "tkeh", "oḱto", "artḱos");
		List<String> expected = toList("rukso", "kseh", "oɕto", "arccos");

		List<Sequence> received  = sca.processLexicon(words);
		List<Sequence> sequences = toSequences(expected, sca);
		testLists(received, sequences);
	}

	@Test
	public void simpleNoSegmentation01() throws ParseException {
		String[] commands = {
				"NORMALIZATION: NONE",
				"SEGMENTATION: FALSE",
				"ḱ  > ɟ",
				"ḱʰ > cʰ",
				"ǵ  > j"
		};

		SoundChangeApplier sca = new SoundChangeApplier(commands);

		List<String> words    = toList("ruḱo", "ḱʰeh", "oḱto", "arǵos");
		List<String> expected = toList("ruɟo", "ɟʰeh", "oɟto", "arjos");

		List<Sequence> received  = sca.processLexicon(words);
		List<Sequence> sequences = toSequences(expected, sca);
		testLists(received, sequences);
	}

	@Test
	public void reserveTest() throws ParseException {
		String[] commands = {
				"SEGMENTATION: FALSE",
				"RESERVE ph th kh"
		};
		SoundChangeApplier sca = new SoundChangeApplier(commands);

		Collection<String> received = sca.getFeatureModel().getSymbols();
		Collection<String> expected = new HashSet<String>();
		expected.add("ph");
		expected.add("th");
		expected.add("kh");
		assertEquals(expected, received);
	}

	@Test
	public void reserveNaiveSegmentationTest() throws ParseException {
		String[] commands = {
				"SEGMENTATION: FALSE",
				"RESERVE ph th kh",
				"ph th kh > f h x"
		};

		SoundChangeApplier sca = new SoundChangeApplier(commands);

		List<String> words    = toList("kho");
		List<String> expected = toList("xo");

		List<Sequence> received  = sca.processLexicon(words);
		List<Sequence> sequences = toSequences(expected, sca);
		testLists(received, sequences);
	}

	@Test
	public void reserveDefaultSegmentationTest() throws ParseException {
		String[] commands = {
				"SEGMENTATION: TRUE",
				"RESERVE ph th kh",
				"ph th kh > f h x"
		};

		SoundChangeApplier sca = new SoundChangeApplier(commands);

		List<String> words    = toList("rukho", "khek", "ophto", "arthos", "taphos");
		List<String> expected = toList("ruxo",  "xek",  "ofto",  "arhos",  "tafos");

		List<Sequence> received  = sca.processLexicon(words);
		List<Sequence> sequences = toSequences(expected, sca);
		testLists(received, sequences);
	}

	@Test
	public void testMetathesis01() throws ParseException {
		String[] commands = {
				"C = p t k",
				"N = m n",
				"Cn Cm > nC nC / _#"
		};
		SoundChangeApplier sca = new SoundChangeApplier(commands);

		List<String> words    = toList("atn");
		List<String> expected = toList("ant");

		List<Sequence> received  = sca.processLexicon(words);
		List<Sequence> sequences = toSequences(expected, sca);
		testLists(received, sequences);
	}

	@Test
	public void testOpen01() throws ParseException {

		String[] lexicon = {
				"apat",
				"takan",
				"kepak",
				"pik",
				"ket"
		};

		String[] commands = { "OPEN \'testLexicon.lex\' as TEST" };
		SoundChangeApplier sca = new SoundChangeApplier(commands, new ClassPathFileHandler());
		sca.process();

		assertTrue("Lexicon 'TEST' not found.", sca.hasLexicon("TEST"));

		List<String> strings = toList(lexicon);

		List<List<Sequence>> expected = new ArrayList<List<Sequence>>();
		List<List<Sequence>> received = sca.getLexicon("TEST");

		for (String string : strings) {
			expected.add(toSequences(string, sca));
		}

		assertEquals(expected, received);
	}

	@Test
	public void testOpen02() throws ParseException {
		String lexicon = "" +
		                 "apat\n" +
		                 "takan\n" +
		                 "kepak\n" +
		                 "pik\n" +
		                 "ket";

		Map<String, String> map = new HashMap<String, String>();
		map.put("test.lex", lexicon);

		String[] commands = { "OPEN 'test.lex' as TEST" };

		SoundChangeApplier sca = new SoundChangeApplier(commands, new MockFileHandler(map));
		sca.process();

		List<List<Sequence>> received = sca.getLexicon("TEST");
		List<List<Sequence>> expected = new ArrayList<List<Sequence>>();

		for (String string : lexicon.split("\n")) {
			expected.add(toSequences(string, sca));
		}

		assertEquals(expected, received);
	}

	@Test
	public void testWrite01() throws ParseException {
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

		SoundChangeApplier sca = new SoundChangeApplier(commands, new MockFileHandler(map));
		sca.process();

		assertFalse(sca.hasLexicon("TEST"));
		assertTrue(map.containsKey("close.lex"));
		assertTrue(map.containsKey("write.lex"));
		assertEquals(lexicon, map.get("write.lex"));
		assertEquals(lexicon, map.get("close.lex"));
	}

	/* UTILITY METHODS */
	private List<Sequence> toSequences(Iterable<String> strings, SoundChangeApplier sca) {
		List<Sequence> list = new ArrayList<Sequence>();

		NormalizerMode mode = sca.getNormalizerMode();
		for (String s : strings) {
			String s2;
			if (mode == NormalizerMode.NONE) {
				s2 = s;
			} else {
				Normalizer.Form form = Normalizer.Form.valueOf(mode.toString());
				s2 = Normalizer.normalize(s, form);
			}

			list.add(Segmenter.getSequence(s2,
					sca.getFeatureModel(),
					sca.getVariables(),
					sca.getSegmentationMode()));
		}
		return list;
	}

	private List<Sequence> toSequences(String string, SoundChangeApplier sca) {
		return toSequences(toList(string.split("\\s+")), sca);
	}

	private List<String> toList(String... strings) {
		List<String> list = new ArrayList<String>();
		Collections.addAll(list, strings);
		return list;
	}
}
