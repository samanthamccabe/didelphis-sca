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

import org.didelphis.io.ClassPathFileHandler;
import org.didelphis.io.FileHandler;
import org.didelphis.io.MockFileHandler;
import org.didelphis.io.NullFileHandler;
import org.didelphis.language.parsing.FormatterMode;
import org.didelphis.language.phonetic.Lexicon;
import org.didelphis.language.phonetic.SequenceFactory;
import org.didelphis.language.phonetic.features.IntegerFeature;
import org.didelphis.language.phonetic.model.FeatureMapping;
import org.didelphis.language.phonetic.model.FeatureModelLoader;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created with IntelliJ IDEA. @author Samantha Fiona McCabe
 *
 * @date 9/19/13 Templates.
 */
public class StandardScriptTest {

	private static final transient Logger LOGGER =
			LoggerFactory.getLogger(StandardScriptTest.class);

	private static final ClassPathFileHandler CLASSPATH =
			ClassPathFileHandler.INSTANCE;

	private static final FeatureModelLoader<Integer> EMPTY =
			IntegerFeature.emptyLoader();

	private static final FeatureMapping<Integer> MAPPING =
			EMPTY.getFeatureMapping();


	private static final SequenceFactory<Integer> FACTORY_NONE =
			new SequenceFactory<>(MAPPING, FormatterMode.NONE);
	private static final SequenceFactory<Integer> FACTORY_INTELLIGENT =
			new SequenceFactory<>(MAPPING, FormatterMode.INTELLIGENT);

	@Test
	void testImportVariables() {
		String script1 = "" + "C = p t k\n" + "V = a i u\n";

		String script2 =
				"" + "OPEN \"lexicon\" as LEXICON\n" + "IMPORT \"script1\"\n" +
						"a i u > 0 / VC_CV\n" + "CLOSE LEXICON as \"newlex\"";

		String lexicon = "" + "apaka\n" + "paku\n" + "atuku\n";

		Map<String, CharSequence> fileSystem = new HashMap<>();
		fileSystem.put("script1", script1);
		fileSystem.put("lexicon", lexicon);

		getScript(script2, new MockFileHandler(fileSystem)).process();

		String received = fileSystem.get("newlex").toString();
		String expected = "" + "apka\n" + "paku\n" + "atku";
		assertEquals(expected, received);
	}

	@Test
	void testImportReserve() {
		String script1 = "RESERVE ph th kh\n";

		String script2 =
				"" + "IMPORT \"script1\"\n" + "OPEN \"lexicon\" as LEXICON\n" +
						"p   t k  > b d g\n" + "ph th kh > f θ x\n" +
						"CLOSE LEXICON as \"newlex\"";

		String lexicon = "" + "apakha\n" + "phaku\n" + "athuku\n";

		Map<String, CharSequence> fileSystem = new HashMap<>();
		fileSystem.put("script1", script1);
		fileSystem.put("lexicon", lexicon);

		getScript(script2, new MockFileHandler(fileSystem)).process();

		String received = fileSystem.get("newlex").toString();
		String expected = "" + "abaxa\n" + "fagu\n" + "aθugu";
		assertEquals(expected, received);
	}

	@Test
	void testImportModelAndFormat() {
		String model = ClassPathFileHandler.INSTANCE.read("AT_hybrid.model");

		String script1 =
				"LOAD \"model\"\n" + "MODE " + FormatterMode.INTELLIGENT;

		String script2 =
				"" + "IMPORT \"script1\"\n" + "OPEN \"lexicon\" as LEXICON\n" +
						"[-voice, -son, -vot] > [+voice]\n" +
						"[-voice, -son, +vot] > [+cnt, -vot]\n" +
						"WRITE LEXICON as \"newlex\"";

		String lexicon = "" + "apakʰa\n" + "pʰaku\n" + "atʰuku\n";

		Map<String, CharSequence> fileSystem = new HashMap<>();
		fileSystem.put("model", model);
		fileSystem.put("script1", script1);
		fileSystem.put("lexicon", lexicon);

		getScript(script2, new MockFileHandler(fileSystem)).process();

		String received = fileSystem.get("newlex").toString();
		String expected = "" + "abaxa\n" + "ɸagu\n" + "asugu";

		assertEquals(expected, received);
	}

	@Test
	void testImportFormatter() {
		String script1 = "" + "C = p t k\n" + "V = a i u\n";
		// In this case the import is in the main script;
		// the test ensures it is not overwritten
		String script2 = "MODE " + FormatterMode.INTELLIGENT + '\n' +
				"OPEN \"lexicon\" as LEXICON\n" + "IMPORT \"script1\"\n" +
				"p  t  k  > b d g\n" + "pʰ tʰ kʰ > f θ x\n" +
				"CLOSE LEXICON as \"newlex\"";

		String lexicon = "" + "apakʰa\n" + "pʰaku\n" + "atʰuku\n";

		Map<String, CharSequence> fileSystem = new HashMap<>();
		fileSystem.put("script1", script1);
		fileSystem.put("lexicon", lexicon);

		getScript(script2, new MockFileHandler(fileSystem)).process();

		String received = fileSystem.get("newlex").toString();
		String expected = "" + "abaxa\n" + "fagu\n" + "aθugu";
		assertEquals(expected, received);
	}

	@Test
	void testExecute() {
		Map<String, CharSequence> fileSystem = new HashMap<>();
		FileHandler fileHandler = new MockFileHandler(fileSystem);

		String rules = CLASSPATH.read("testRuleLarge01.txt");

		// Append output clause
		rules = "OPEN 'testRuleLarge01.lex' as LEXICON\n" +
				rules + '\n' + "MODE COMPOSITION\n" +
				"CLOSE LEXICON AS \'output.lex\'";

		String words = "tussḱyos";
		String outpt = "tusciyos";

		fileSystem.put("testRuleLarge01.lex", words);
		fileSystem.put("testRuleLarge01.txt", rules);

		String executeRule = "EXECUTE 'testRuleLarge01.txt'";
		StandardScript<Integer> script = getScript(executeRule, fileHandler);

		script.process();

		String received = fileSystem.get("output.lex").toString();

		assertEquals(outpt, received);
	}

	@Test
	void testExecuteLexiconFile() {
		Map<String, CharSequence> fileSystem = new HashMap<>();
		FileHandler fileHandler = new MockFileHandler(fileSystem);

		String rules = CLASSPATH.read("testRuleLarge01.txt");
		String words = CLASSPATH.read("testRuleLarge01.lex");
		String outpt = CLASSPATH.read("testRuleLargeOut01.lex");

		// Append output clause
		rules = "MODE INTELLIGENT\n" +
				"OPEN \"testRuleLarge01.lex\" as LEXICON\n" +
				rules +
				"\nMODE COMPOSITION\n" +
				"CLOSE LEXICON AS \'output.lex\'";

		fileSystem.put("testRuleLarge01.lex", words);
		fileSystem.put("testRuleLarge01.txt", rules);

		String executeRule = "EXECUTE 'testRuleLarge01.txt'";
		StandardScript<Integer> script = getScript(executeRule, fileHandler);

		script.process();

		String received = fileSystem.get("output.lex").toString();

		assertEquals(outpt.replaceAll("\r\n|\n|\r", "\n"), received);
	}

	@Test
	void testDebugLexiconFile() {
		Map<String, CharSequence> fileSystem = new HashMap<>();
		FileHandler fileHandler = new MockFileHandler(fileSystem);

		String rules = CLASSPATH.read("testRuleLarge01.txt");
		String words = "h₂rǵ-i-ḱuon-";
		String outpt = "ərɟicwon";

		// Append output clause
		rules = "MODE INTELLIGENT\n" +
				"OPEN \"testRuleLarge01.lex\" as LEXICON\n" +
				rules +
				"\nMODE COMPOSITION\n" +
				"CLOSE LEXICON AS \'output.lex\'";

		fileSystem.put("testRuleLarge01.lex", words);
		fileSystem.put("testRuleLarge01.txt", rules);

		String executeRule = "EXECUTE 'testRuleLarge01.txt'";
		StandardScript<Integer> script = getScript(executeRule, fileHandler);

		script.process();

		String received = fileSystem.get("output.lex").toString();

		assertEquals(outpt.replaceAll("\r\n|\n|\r", "\n"), received);
	}
	
	@Test
	void testRuleLarge01() {
		String[] output = CLASSPATH.read("testRuleLargeOut01.lex").split("\n");

		String script = "MODE INTELLIGENT\n" +
				"OPEN \"testRuleLarge01.lex\" as LEXICON\n" +
				"IMPORT 'testRuleLarge01.txt'";

		SoundChangeScript<Integer> sca = getScript(script, CLASSPATH);
		sca.process();

		Lexicon<Integer> received = sca.getLexicons().getLexicon("LEXICON");
		Lexicon<Integer> expected =
				Lexicon.fromSingleColumn(FACTORY_INTELLIGENT,
						Arrays.asList(output));
		assertEquals(expected, received);
	}

	@Test
	void testLoop() {
		String commands = "P = pw p t k\nB = bw b d g\nV = a o\n" +
				"P > B / V_V \nP = p t k   \nB = b d g   \nB > 0 / #_c";

		StandardScript<Integer> ignored =
				getScript(commands, NullFileHandler.INSTANCE);
	}

	@Test
	void testOpen01() {

		String[] lexicon = {"apat", "takan", "kepak", "pik", "ket"};

		SoundChangeScript<Integer> sca =
				getScript("OPEN \'testLexicon.lex\' as TEST", CLASSPATH);
		sca.process();
		Lexicon<Integer> expected =
				Lexicon.fromSingleColumn(FACTORY_NONE, Arrays.asList(lexicon));
		Lexicon<Integer> received = sca.getLexicons().getLexicon("TEST");
		assertEquals(expected, received);
	}

	@Test
	void testMultilineRuleOrNot() {
		String commands =
				"" + "OPEN 'default.lex' AS DEFAULT\n" + "e > a / h_ \n" +
						"   or    _h\n" + "   not  y_\n" +
						"WRITE DEFAULT AS 'received.lex'";

		Map<String, CharSequence> map = new HashMap<>();
		map.put("default.lex", "teha\n" + "hen\n" + "yeh");

		FileHandler handler = new MockFileHandler(map);
		SoundChangeScript<Integer> script = getScript(commands, handler);
		script.process();

		String received = map.get("received.lex").toString();
		String expected = "" + "taha\n" + "han\n" + "yeh";
		assertEquals(expected, received);
	}

	@Test
	void testWrite01() {
		String lexicon =
				"" + "apat\n" + "takan\n" + "kepak\n" + "pik\n" + "ket";

		Map<String, CharSequence> map = new HashMap<>();
		map.put("test.lex", lexicon);

		String commands =
				"OPEN 'test.lex' as TEST\n" + "WRITE TEST as 'write.lex'\n" +
						"CLOSE TEST as 'close.lex'";

		SoundChangeScript<Integer> sca =
				getScript(commands, new MockFileHandler(map));
		sca.process();

		assertTrue(map.containsKey("close.lex"));
		assertTrue(map.containsKey("write.lex"));
		assertEquals(lexicon, map.get("write.lex"));
		assertEquals(lexicon, map.get("close.lex"));
	}

	private static StandardScript<Integer> getScript(String commands,
			FileHandler handler) {
		return new StandardScript<>("", IntegerFeature.INSTANCE, commands,
				handler, new ErrorLogger());
	}

}
