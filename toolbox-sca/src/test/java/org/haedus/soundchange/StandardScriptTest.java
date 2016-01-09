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

import org.haedus.enums.FormatterMode;
import org.haedus.exceptions.ParseException;
import org.haedus.io.ClassPathFileHandler;
import org.haedus.io.FileHandler;
import org.haedus.io.MockFileHandler;
import org.haedus.phonetic.Lexicon;
import org.haedus.phonetic.LexiconMap;
import org.haedus.phonetic.SequenceFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
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
public class StandardScriptTest {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(StandardScriptTest.class);

	public static final ClassPathFileHandler CLASSPATH_HANDLER   = ClassPathFileHandler.getDefaultInstance();
	public static final SequenceFactory      FACTORY_NONE        = new SequenceFactory(FormatterMode.NONE);
	public static final SequenceFactory      FACTORY_INTELLIGENT = new SequenceFactory(FormatterMode.INTELLIGENT);


	@Test(expected = ParseException.class)
	public void testBadMode() {
		new StandardScript("testBadMode","MODE:XXX");
	}

	@Test
	public void testExecute() throws Exception {
		Map<String, String> fileSystem = new HashMap<String, String>();
		FileHandler fileHandler = new MockFileHandler(fileSystem);

		String rules = getStringFromClassPath("testRuleLarge01.txt");
		String words = getStringFromClassPath("testRuleLarge01.lex");
		String outpt = getStringFromClassPath("testRuleLargeOut01.lex");

		// Append output clause
		rules = rules.concat("\n" +
				"MODE COMPOSITION\n" +
				"CLOSE LEXICON AS \'output.lex\'");

		fileSystem.put("testRuleLarge01.lex", words);
		fileSystem.put("testRuleLarge01.txt", rules);

		String executeRule = "EXECUTE 'testRuleLarge01.txt'";
		StandardScript script = new StandardScript("testExecute", executeRule, new LexiconMap(), fileHandler);

		script.process();

		String received = fileSystem.get("output.lex");

		assertEquals(outpt, received);
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

	@Test
	public void testRuleLargeWithModel() throws Exception {

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

		SoundChangeScript sca = new StandardScript(
			"LOAD 'features.model'\nIMPORT 'testRuleLarge01.txt'", CLASSPATH_HANDLER
		);
		sca.process();
		Lexicon received = sca.getLexicon("LEXICON");
		Lexicon expected = FACTORY_INTELLIGENT.getLexiconFromSingleColumn(exWords);
		assertEquals(expected, received);
	}

	@Test
	public void testNakh() throws Exception {
		SoundChangeScript sca = new StandardScript(
			"LOAD 'reduced.model'\nIMPORT 'nakh.rules'", CLASSPATH_HANDLER
		);
		sca.process();
		Lexicon received = sca.getLexicon("PROTO");
		assertTrue(true);
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

//	@Test
//	public void reserveTest() {
//		String[] commands = {
//				"SEGMENTATION: FALSE",
//				"RESERVE ph th kh"
//		};
//		StandardScript sca = new StandardScript(commands);
//		sca.process();
//		Collection<String> received = sca.getReservedSymbols();
//		Collection<String> expected = new HashSet<String>();
//		expected.add("ph");
//		expected.add("th");
//		expected.add("kh");
//		assertEquals(expected, received);
//	}

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

	private static String getStringFromClassPath(String name) throws IOException {
		InputStream rulesStream = StandardScriptTest.class.getClassLoader().getResourceAsStream(name);
		Reader reader = new BufferedReader(new InputStreamReader(rulesStream));

		StringBuilder sb = new StringBuilder();

		int c = reader.read();
		while (c >= 0) {
			sb.append((char) c);
			c = reader.read();
		}
		return sb.toString();
	}
}
