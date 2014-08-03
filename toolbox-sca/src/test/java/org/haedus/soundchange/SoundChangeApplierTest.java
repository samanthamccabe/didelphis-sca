package org.haedus.soundchange;

import org.apache.commons.io.FileUtils;
import org.haedus.datatypes.phonetic.Segment;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.VariableStore;
import org.haedus.soundchange.exceptions.RuleFormatException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import sun.misc.IOUtils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created with IntelliJ IDEA.
 * User: goats
 * Date: 9/19/13
 * Time: 10:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class SoundChangeApplierTest {
	private static final transient Logger LOGGER = LoggerFactory.getLogger(SoundChangeApplierTest.class);

	private static void assertNotEquals(Object expected, Object received) {
		assertFalse(expected.equals(received));
	}

	@Test(expected = RuleFormatException.class)
	public void testNormalizerBadMode() throws RuleFormatException {
		new SoundChangeApplier(new String[]{ "USE NORMALIZATION:XXX" });
	}

	@Test(expected = RuleFormatException.class)
	public void testSegmentationBadMode() throws RuleFormatException {
		new SoundChangeApplier(new String[]{ "USE SEGMENTATION:XXX" });
	}

	@Test
	public void testNormalizerNFD() throws RuleFormatException {
		String[] commands = { "USE NORMALIZATION:NFD" };

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
		assertEquals(expected, received);
	}

	@Test
	public void testNormalizerNFC() throws RuleFormatException {
		String[] commands = { "USE NORMALIZATION:NFC" };

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
	public void TestNormalizerNFCvsNFD() throws RuleFormatException {
		String[] commands = { "USE NORMALIZATION:NFC" };

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
	public void simpleRuleTest01() throws RuleFormatException {
		String[] commands = {
				"a > e",
				"d > t / _#"
		};

		SoundChangeApplier sca = new SoundChangeApplier(commands);

		List<String> words    = toList("abad", "abada", "ad", "ado");
		List<String> expected = toList("ebet", "ebede", "et", "edo");

		List<Sequence> received = sca.processLexicon(words);

		assertEquals(toSequences(expected, sca), received);
	}

	@Test
	public void simpleRuleTest02() throws RuleFormatException {
		String[] commands = {
				"USE NORMALIZATION: NFD",
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

		assertEquals(toSequences(expected, sca), received);
	}

	@Test
	public void simpleRuleTest03() throws RuleFormatException {
		String[] commands = {
				"- > 0",
				"h₁ h₂ h₃ h₄ > ʔ x ɣ ʕ",
				"hₓ hₐ > ʔ ʕ",
				"b  d  ǵ  g  gʷ  > bʰ dʰ ǵʰ gʰ gʷʰ / _{ x ɣ }",
				"p  t  ḱ  k  kʷ  > pʰ tʰ ḱʰ kʰ kʷʰ / _{x ɣ}",
				"bʰ dʰ ǵʰ gʰ gʷʰ > pʰ tʰ ḱʰ kʰ kʷʰ",
				"ḱ ḱʰ ǵ ǵʰ > c cʰ ɟ ɟʰ" };

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

		assertEquals(toSequences(expected, sca), received);
	}

	@Test
	public void simpleRuleTest04() throws RuleFormatException {
		String[] commands = {
				"e é ē ê > a á ā â / {x ʕ}_",
				"e é ē ê > a á ā â / _{x ʕ}",
				"e é ē ê > o ó ō ô / ɣ_",
				"e é ē ê > o ó ō ô / _ɣ",
				"ɣ ʕ > x ʔ",
				"x ʔ > 0 / _{x ʔ}",
				"x ʔ > 0 / _{i y í}",
				"ix iʔ ux uʔ > ī ī ū ū" };

		List<String> words = toList(
				"xocri", "pʰaceʕ",
				"tʰʔileʕ", "ɟenʔtriʕ",
				"xrɟicuon", "xwetʰx",
				"tʰcʰuʔ", "tʰʔileʕ");

		List<String> expected = toList(
				"xocri", "pʰacaʔ",
				"tʰilaʔ", "ɟenʔtrī",
				"xrɟicuon", "xwetʰx",
				"tʰcʰū", "tʰilaʔ");

		SoundChangeApplier sca = new SoundChangeApplier(commands);
		List<Sequence> received = sca.processLexicon(words);

		assertEquals(toSequences(expected, sca), received);
	}

	@Test
	public void reassignmentTest01() throws RuleFormatException {
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
	public void ruleTest01() throws RuleFormatException {
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
	public void ruleTest02() throws RuleFormatException {
		String commands =
				"% Comment\n" +
				"C  = p t k pʰ tʰ kʰ n m r l\n" +
				"CH = pʰ tʰ kʰ\n" +
				"G  = b d g\n" +
				"R = r l\n" +
				"V = a e i o u ā ē ī ō ū\n" +

				"CH > G / _R?VV?C*CH\n";

		SoundChangeApplier sca = new SoundChangeApplier(commands);

		List<String> words    = toList("pʰapʰa");
		List<String> expected = toList("bapʰa");

		List<Sequence> received = sca.processLexicon(words);

		assertEquals(toSequences(expected, sca), received);
	}

	@Test
	public void testExpansion01() throws Exception {

		Resource resource = new ClassPathResource("testExpansion01.txt");
		String commands = FileUtils.readFileToString(resource.getFile());

		SoundChangeApplier sca = new SoundChangeApplier(commands);
		sca.processLexicon(new ArrayList<String>());

		VariableStore vs = sca.getVariables();

		LOGGER.info(vs.get("[PLOSIVE]").toString());

		testExpansion(vs, "V", "a e i o u ə ā ē ī ō ū ə̄");
		testExpansion(vs, "C", "pʰ p b tʰ t d cʰ c ɟ kʰ k g kʷʰ kʷ gʷ s m n r l j w");
	}

	private void testExpansion(VariableStore vs, String key, String terminals) {
		assertEquals(toSequences(terminals, new SoundChangeApplier()), vs.get(key));
	}

	@Test
	public void testRuleLarge01() throws Exception {

		Resource resource = new ClassPathResource("testRuleLarge01.txt");
		String commands = FileUtils.readFileToString(resource.getFile());

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
				"ôwes",          "blan"
		                              );

		SoundChangeApplier sca = new SoundChangeApplier(commands);
		List<Sequence> received = sca.processLexicon(words);

		assertEquals(toSequences(expected, sca), received);
	}

	@Test
	public void testLoop01() throws RuleFormatException {
		String commands =
				"P = pw p t k\n" +
				"B = bw b d g\n" +
				"V = a o\n" +
				"P > B / V_V\n" +
				"P = p t k\n" +
				"B = b d g\n" +
				"B > 0 / #_c";

		SoundChangeApplier sca = new SoundChangeApplier(commands);

		sca.processLexicon(new ArrayList<String>());
	}

	@Test
	public void testDebug002() throws RuleFormatException {
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
				"þîs", "þîsnĕ"
		                              );

		List<Sequence> received = sca.processLexicon(list);

		assertEquals(toSequences(expected, sca), received);
	}

    @Test
    public void simpleNoSegmentation() throws RuleFormatException {
        String[] commands = {
                "USE NORMALIZATION: NONE",
                "USE SEGMENTATION: FALSE",
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

        assertEquals(toSequences(expected, sca), received);
    }

    @Test
    public void simpleNoSegmentation01() throws RuleFormatException {
        String[] commands = {
                "USE NORMALIZATION: NONE",
                "USE SEGMENTATION: FALSE",
                "ḱ  > ɟ",
                "ḱʰ > cʰ",
                "ǵ  > j"
        };

        SoundChangeApplier sca = new SoundChangeApplier(commands);

        List<String> words    = toList("ruḱo", "ḱʰeh", "oḱto", "arǵos");
        List<String> expected = toList("ruɟo", "ɟʰeh", "oɟto", "arjos");

        List<Sequence> received = sca.processLexicon(words);

        assertEquals(toSequences(expected, sca), received);
    }

	private List<Sequence> toSequences(List<String> strings, SoundChangeApplier sca) {
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

            if (sca.usesSegmentation()) {
                list.add(new Sequence(s2));
            } else {
                Sequence sequence = new Sequence();
                for (int i = 0; i < s2.length(); i++) {
                    sequence.add(new Segment(s2.substring(i, i+1)));
                }
                list.add(sequence);
            }
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
