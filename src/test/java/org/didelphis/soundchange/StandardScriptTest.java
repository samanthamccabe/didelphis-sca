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

import org.didelphis.common.io.ClassPathFileHandler;
import org.didelphis.common.io.FileHandler;
import org.didelphis.common.io.MockFileHandler;
import org.didelphis.common.io.NullFileHandler;
import org.didelphis.common.language.enums.FormatterMode;
import org.didelphis.common.language.phonetic.Lexicon;
import org.didelphis.common.language.phonetic.SequenceFactory;
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

	private static final ClassPathFileHandler CLASSPATH_HANDLER   = ClassPathFileHandler.getDefaultInstance();
	private static final SequenceFactory      FACTORY_NONE        = new SequenceFactory(FormatterMode.NONE);
	private static final SequenceFactory      FACTORY_INTELLIGENT = new SequenceFactory(FormatterMode.INTELLIGENT);

	@Test
	public void testImportVariables() throws Exception {
		String script1 = "" +
			"C = p t k\n" +
			"V = a i u\n";

		String script2 = "" +
			"OPEN \"lexicon\" as LEXICON\n" +
			"IMPORT \"script1\"\n" +
			"a i u > 0 / VC_CV\n" +
			"CLOSE LEXICON as \"newlex\"";

		String lexicon = "" +
			"apaka\n" +
			"paku\n" +
			"atuku\n";

		Map<String, CharSequence> fileSystem = new HashMap<>();
		fileSystem.put("script1", script1);
		fileSystem.put("lexicon", lexicon);

		getScript(script2, new MockFileHandler(fileSystem)).process();

		String received = fileSystem.get("newlex").toString();
		String expected = "" +
			"apka\n" +
			"paku\n" +
			"atku";
		assertEquals(expected, received);
	}

	@Test
	public void testImportReserve() throws Exception {
		String script1 = "RESERVE ph th kh\n";

		String script2 = "" +
			"IMPORT \"script1\"\n" +
			"OPEN \"lexicon\" as LEXICON\n" +
			"p   t k  > b d g\n" +
			"ph th kh > f θ x\n" +
			"CLOSE LEXICON as \"newlex\"";

		String lexicon = "" +
			"apakha\n" +
			"phaku\n" +
			"athuku\n";

		Map<String, CharSequence> fileSystem = new HashMap<>();
		fileSystem.put("script1", script1);
		fileSystem.put("lexicon", lexicon);

		getScript(script2, new MockFileHandler(fileSystem)).process();

		String received = fileSystem.get("newlex").toString();
		String expected = "" +
			"abaxa\n" +
			"fagu\n" +
			"aθugu";
		assertEquals(expected, received);
	}

	@Test
	public void testImportModelAndFormat() throws Exception {
		String model = new ClassPathFileHandler("UTF-8").read("AT_hybrid.model");

		String script1 = "LOAD \"model\"\n" +
			"MODE " + FormatterMode.INTELLIGENT;

		String script2 = "" +
			"IMPORT \"script1\"\n" +
			"OPEN \"lexicon\" as LEXICON\n" +
			"[-voice, -son, -vot] > [+voice]\n"+
			"[-voice, -son, +vot] > [+cnt, -vot]\n"+
			"WRITE LEXICON as \"newlex\"";

		String lexicon = "" +
			"apakʰa\n" +
			"pʰaku\n" +
			"atʰuku\n";

		Map<String, CharSequence> fileSystem = new HashMap<>();
		fileSystem.put("model",   model);
		fileSystem.put("script1", script1);
		fileSystem.put("lexicon", lexicon);

		getScript(script2, new MockFileHandler(fileSystem)).process();

		String received = fileSystem.get("newlex").toString();
		String expected = "" +
			"abaxa\n" +
			"ɸagu\n" +
			"asugu";

		assertEquals(expected, received);
	}

	@Test
	public void testImportFormatter() throws Exception {
		String script1 = "" +
			"C = p t k\n" +
			"V = a i u\n";
		// In this case the import is in the main script;
		// the test ensures it is not overwritten
		String script2 = "MODE " + FormatterMode.INTELLIGENT + '\n' +
			"OPEN \"lexicon\" as LEXICON\n" +
			"IMPORT \"script1\"\n" +
			"p  t  k  > b d g\n" +
			"pʰ tʰ kʰ > f θ x\n" +
			"CLOSE LEXICON as \"newlex\"";

		String lexicon = "" +
			"apakʰa\n" +
			"pʰaku\n" +
			"atʰuku\n";

		Map<String, CharSequence> fileSystem = new HashMap<>();
		fileSystem.put("script1", script1);
		fileSystem.put("lexicon", lexicon);

		getScript(script2, new MockFileHandler(fileSystem)).process();

		String received = fileSystem.get("newlex").toString();
		String expected = "" +
			"abaxa\n" +
			"fagu\n" +
			"aθugu";
		assertEquals(expected, received);
	}

	@Test
	public void testExecute() throws Exception {
		Map<String, CharSequence> fileSystem = new HashMap<>();
		FileHandler fileHandler = new MockFileHandler(fileSystem);

		String rules = getStringFromClassPath("testRuleLarge01.txt");

		// Append output clause
		rules = rules + '\n' +
				"MODE COMPOSITION\n" +
				"CLOSE LEXICON AS \'output.lex\'";

		String words = "tussḱyos";
		String outpt = "tusciyos";
		
		fileSystem.put("testRuleLarge01.lex", words);
		fileSystem.put("testRuleLarge01.txt", rules);

		String executeRule = "EXECUTE 'testRuleLarge01.txt'";
		StandardScript script = getScript(executeRule, fileHandler);

		script.process();

		String received = fileSystem.get("output.lex").toString();

		assertEquals(outpt, received);
	}
	
	@Test
	public void testExecuteLexiconFile() throws Exception {
		Map<String, CharSequence> fileSystem = new HashMap<>();
		FileHandler fileHandler = new MockFileHandler(fileSystem);

		String rules = getStringFromClassPath("testRuleLarge01.txt");
		String words = getStringFromClassPath("testRuleLarge01.lex");
		String outpt = getStringFromClassPath("testRuleLargeOut01.lex");

		// Append output clause
		rules = rules + '\n' +
			"MODE COMPOSITION\n" +
			"CLOSE LEXICON AS \'output.lex\'";

		fileSystem.put("testRuleLarge01.lex", words);
		fileSystem.put("testRuleLarge01.txt", rules);

		String executeRule = "EXECUTE 'testRuleLarge01.txt'";
		StandardScript script = getScript(executeRule, fileHandler);

		script.process();

		String received = fileSystem.get("output.lex").toString();

		assertEquals(outpt.replaceAll("\r\n|\n|\r","\n"), received);
	}

	@Test
	public void testRuleLarge01() throws Exception {
		String[] output = getStringFromClassPath("testRuleLargeOut01.lex").split("\n");

		String script = "IMPORT 'testRuleLarge01.txt'";

		SoundChangeScript sca = getScript(script, CLASSPATH_HANDLER);
		sca.process();

		Lexicon received = sca.getLexicons().getLexicon("LEXICON");
		Lexicon expected = FACTORY_INTELLIGENT.getLexiconFromSingleColumn(output);
		assertEquals(expected, received);
	}

	@Test
	public void testLoop() {
		String commands =
				"P = pw p t k" + '\n' +
				"B = bw b d g" + '\n' +
				"V = a o     " + '\n' +
				"P > B / V_V " + '\n' +
				"P = p t k   " + '\n' +
				"B = b d g   " + '\n' +
				"B > 0 / #_c";

		StandardScript ignored = getScript(commands, NullFileHandler.INSTANCE);
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

		SoundChangeScript sca = getScript("OPEN \'testLexicon.lex\' as TEST", CLASSPATH_HANDLER);
		sca.process();
		Lexicon expected = FACTORY_NONE.getLexiconFromSingleColumn(lexicon);
		Lexicon received = sca.getLexicons().getLexicon("TEST");
		assertEquals(expected, received);
	}

	@Test
	public void testMultilineRuleOrNot() {
		String commands = "" +
				"OPEN 'default.lex' AS DEFAULT\n" +
				"e > a / h_ \n" +
				"   or    _h\n" +
				"   not  y_\n" +
				"WRITE DEFAULT AS 'received.lex'";

		Map<String, CharSequence> map = new HashMap<>();
		map.put("default.lex", 
				"teha\n" + 
				"hen\n" + 
				"yeh");
		
		FileHandler handler = new MockFileHandler(map);
		SoundChangeScript script = getScript(commands, handler);
		script.process();

		String received = map.get("received.lex").toString();
		String expected = "" +
				"taha\n" +
				"han\n" +
				"yeh";
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

		Map<String, CharSequence> map = new HashMap<>();
		map.put("test.lex", lexicon);

		String commands =
				"OPEN 'test.lex' as TEST\n" +
				"WRITE TEST as 'write.lex'\n" +
				"CLOSE TEST as 'close.lex'";

		SoundChangeScript sca = getScript(commands, new MockFileHandler(map));
		sca.process();

		assertTrue(map.containsKey("close.lex"));
		assertTrue(map.containsKey("write.lex"));
		assertEquals(lexicon, map.get("write.lex"));
		assertEquals(lexicon, map.get("close.lex"));
	}

	private static String getStringFromClassPath(String name) throws IOException {
		InputStream rulesStream = StandardScriptTest.class
				.getClassLoader()
				.getResourceAsStream(name);

		Reader streamReader = new InputStreamReader(rulesStream, "UTF-8");
		Reader bufferedReader = new BufferedReader(streamReader);

		StringBuilder sb = new StringBuilder();

		int c = bufferedReader.read();
		while (c >= 0) {
			sb.append((char) c);
			c = bufferedReader.read();
		}
		return sb.toString();
	}

	private static StandardScript getScript(String commands, FileHandler handler) {
		return new StandardScript("", commands, handler, null);
	}

}
