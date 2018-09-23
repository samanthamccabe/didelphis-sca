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

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@FieldDefaults(makeFinal =  true, level = AccessLevel.PRIVATE)
class StandardScriptTest {

	static ClassPathFileHandler CLASSPATH = ClassPathFileHandler.INSTANCE;
	static FeatureModelLoader<Integer> EMPTY = IntegerFeature.emptyLoader();
	static FeatureMapping<Integer> MAPPING = EMPTY.getFeatureMapping();
	
	static SequenceFactory<Integer> FACTORY_NONE 
			= new SequenceFactory<>(MAPPING, FormatterMode.NONE);
	static SequenceFactory<Integer> FACTORY_INTELLIGENT 
			= new SequenceFactory<>(MAPPING, FormatterMode.INTELLIGENT);

	@Test
	void testImportVariables() {
		String script1 = String.join("\n",
				"C = p t k",
				"V = a i u"
		);

		String script2 = String.join("\n",
				"OPEN 'lexicon' as LEXICON",
				"IMPORT 'script1'",
				"a i u > 0 / VC_CV",
				"CLOSE LEXICON as 'newlex'"
		);

		String lexicon = joinLines("apaka", "paku", "atuku");

		Map<String, String> fileSystem = new HashMap<>();
		fileSystem.put("script1", script1);
		fileSystem.put("lexicon", lexicon);

		StandardScript<Integer> script = getScript(
				script2,
				new MockFileHandler(fileSystem)
		);
		script.process();

		String received = fileSystem.get("newlex");
		String expected = joinLines("apka", "paku", "atku");
		assertEquals(expected, received);
	}

	@Test
	void testImportReserve() {
		String script1 = "RESERVE ph th kh\n";

		String script2 = String.join("\n",
				"IMPORT 'script1'",
				"OPEN 'lexicon' as LEXICON",
				"p   t k  > b d g",
				"ph th kh > f θ x",
				"CLOSE LEXICON as 'newlex'"
		);

		String lexicon = joinLines("apakha", "phaku", "athuku");

		Map<String, String> fileSystem = new HashMap<>();
		fileSystem.put("script1", script1);
		fileSystem.put("lexicon", lexicon);

		getScript(script2, new MockFileHandler(fileSystem)).process();

		String received = fileSystem.get("newlex");
		String expected = joinLines("abaxa","fagu","aθugu");
		assertEquals(expected, received);
	}

	private static String joinLines(String... strings) {
		return String.join("\n", strings);
	}
	
	@Test
	void testImportModelAndFormat() {
		String model = ClassPathFileHandler.INSTANCE.read("AT_hybrid.model");

		String script1 =
				"LOAD \"model\"\n" + "MODE " + FormatterMode.INTELLIGENT;

		String script2 = joinLines(
				"IMPORT 'script1'",
				"OPEN 'lexicon' as LEXICON",
				"[-voice, -son, -vot] > [+voice]",
				"[-voice, -son, +vot] > [+cnt, -vot]",
				"WRITE LEXICON as 'newlex'"
		);

		String lexicon = joinLines("apakʰa", "pʰaku", "atʰuku");

		Map<String, String> fileSystem = new HashMap<>();
		fileSystem.put("model", model);
		fileSystem.put("script1", script1);
		fileSystem.put("lexicon", lexicon);

		getScript(script2, new MockFileHandler(fileSystem)).process();

		String received = fileSystem.get("newlex");
		String expected = joinLines("abaxa", "ɸagu", "asugu");

		assertEquals(expected, received);
	}

	@Test
	void testImportFormatter() {
		String script1 = joinLines("C = p t k", "V = a i u");
		// In this case the import is in the main script;
		// the test ensures it is not overwritten
		String script2 = joinLines(
				"MODE " + FormatterMode.INTELLIGENT,
				"OPEN 'lexicon' as LEXICON",
				"IMPORT 'script1'",
				"p  t  k  > b d g",
				"pʰ tʰ kʰ > f θ x",
				"CLOSE LEXICON as 'newlex'"
		);

		String lexicon = joinLines(
				"apakʰa",
				"pʰaku",
				"atʰuku"
		);

		Map<String, String> fileSystem = new HashMap<>();
		fileSystem.put("script1", script1);
		fileSystem.put("lexicon", lexicon);

		getScript(script2, new MockFileHandler(fileSystem)).process();

		String received = fileSystem.get("newlex");
		String expected = joinLines(
				"abaxa",
				"fagu",
				"aθugu"
		);
		assertEquals(expected, received);
	}

	@Test
	void testExecute() {
		Map<String, String> fileSystem = new HashMap<>();
		FileHandler fileHandler = new MockFileHandler(fileSystem);

		String rules = CLASSPATH.read("testRuleLarge01.txt");

		// Append output clause
		rules = joinLines("OPEN 'testRuleLarge01.lex' as LEXICON",
				rules,
				"MODE COMPOSITION",
				"CLOSE LEXICON AS 'output.lex'");

		//              tusscyos
		String words = "tussḱyos";
		String outpt = "tusciyos";

		fileSystem.put("testRuleLarge01.lex", words);
		fileSystem.put("testRuleLarge01.txt", rules);

		String executeRule = "EXECUTE 'testRuleLarge01.txt'";
		StandardScript<Integer> script = getScript(executeRule, fileHandler);

		script.process();

		String received = fileSystem.get("output.lex");

		assertEquals(outpt, received);
	}

	@Test
	void testExecuteLexiconFile() {
		Map<String, String> fileSystem = new HashMap<>();
		FileHandler fileHandler = new MockFileHandler(fileSystem);

		String rules = CLASSPATH.read("testRuleLarge01.txt");
		String words = CLASSPATH.read("testRuleLarge01.lex");
		String outpt = CLASSPATH.read("testRuleLargeOut01.lex");

		// Append output clause
		rules = joinLines("MODE INTELLIGENT",
				"OPEN 'testRuleLarge01.lex' as LEXICON",
				rules,
				"MODE COMPOSITION",
				"CLOSE LEXICON AS 'output.lex'"
		);

		fileSystem.put("testRuleLarge01.lex", words);
		fileSystem.put("testRuleLarge01.txt", rules);

		String executeRule = "EXECUTE 'testRuleLarge01.txt'";
		StandardScript<Integer> script = getScript(executeRule, fileHandler);

		script.process();

		String received = fileSystem.get("output.lex");

		assertEquals(outpt.replaceAll("\r\n|\n|\r", "\n"), received);
	}

	@Test
	void testDebugLexiconFile() {
		Map<String, String> fileSystem = new HashMap<>();
		FileHandler fileHandler = new MockFileHandler(fileSystem);

		String rules = CLASSPATH.read("testRuleLarge01.txt");
		String words = "h₂rǵ-i-ḱuon-";
		String outpt = "ərɟicwon";

		// Append output clause
		rules = joinLines(
				"MODE INTELLIGENT",
				"OPEN 'testRuleLarge01.lex' as LEXICON",
				rules,
				"MODE COMPOSITION",
				"CLOSE LEXICON AS 'output.lex'"
		);

		fileSystem.put("testRuleLarge01.lex", words);
		fileSystem.put("testRuleLarge01.txt", rules);

		String executeRule = "EXECUTE 'testRuleLarge01.txt'";
		StandardScript<Integer> script = getScript(executeRule, fileHandler);

		script.process();

		String received = fileSystem.get("output.lex");

		assertEquals(outpt.replaceAll("\r\n|\n|\r", "\n"), received);
	}
	
	@Test
	void testRuleLarge01() {
		String[] output = CLASSPATH.read("testRuleLargeOut01.lex").split("\n");

		String script = joinLines(
				"MODE INTELLIGENT",
				"OPEN 'testRuleLarge01.lex' as LEXICON",
				"IMPORT 'testRuleLarge01.txt'"
		);

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
		String commands = joinLines(
				"P = pw p t k",
				"B = bw b d g",
				"V = a o",
				"P > B / V_V",
				"P = p t k",
				"B = b d g",
				"B > 0 / #_c"
		);

		StandardScript<Integer> ignored = getScript(commands, NullFileHandler.INSTANCE);
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
		String commands = joinLines(
				"OPEN 'default.lex' AS DEFAULT",
				"e > a / h_",
				"   or    _h",
				"   not  y_",
				"WRITE DEFAULT AS 'received.lex'"
		);

		Map<String, String> map = new HashMap<>();
		map.put("default.lex", joinLines("teha", "hen", "yeh"));

		FileHandler handler = new MockFileHandler(map);
		SoundChangeScript<Integer> script = getScript(commands, handler);
		script.process();

		String received = map.get("received.lex");
		String expected = joinLines("taha", "han", "yeh");
		assertEquals(expected, received);
	}

	@Test
	void testWrite01() {
		String lexicon = joinLines(
				"apat",
				"takan",
				"kepak",
				"pik",
				"ket"
		);

		Map<String, String> map = new HashMap<>();
		map.put("test.lex", lexicon);

		String commands = joinLines(
				"OPEN 'test.lex' as TEST",
				"WRITE TEST as 'write.lex'",
				"CLOSE TEST as 'close.lex'"
		);

		SoundChangeScript<Integer> sca =
				getScript(commands, new MockFileHandler(map));
		sca.process();

		assertTrue(map.containsKey("close.lex"));
		assertTrue(map.containsKey("write.lex"));
		assertEquals(lexicon, map.get("write.lex"));
		assertEquals(lexicon, map.get("close.lex"));
	}

	private static StandardScript<Integer> getScript(String commands, FileHandler handler) {
		return new StandardScript<>("", IntegerFeature.INSTANCE, commands, handler, new ErrorLogger());
	}

}
