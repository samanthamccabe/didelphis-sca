/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 * (at your option) any later version.                                        *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 ******************************************************************************/

package org.didelphis.soundchange;

import org.didelphis.enums.FormatterMode;
import org.didelphis.io.ClassPathFileHandler;
import org.didelphis.io.FileHandler;
import org.didelphis.io.MockFileHandler;
import org.didelphis.io.NullFileHandler;
import org.didelphis.phonetic.Lexicon;
import org.didelphis.phonetic.SequenceFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
public class StandardScriptMultilineTest {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(StandardScriptMultilineTest.class);

	public static final ClassPathFileHandler CLASSPATH_HANDLER   = ClassPathFileHandler.getDefaultInstance();
	public static final SequenceFactory      FACTORY_NONE        = new SequenceFactory(FormatterMode.NONE);
	public static final SequenceFactory      FACTORY_INTELLIGENT = new SequenceFactory(FormatterMode.INTELLIGENT);

	@Ignore
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

		Map<String, String> fileSystem = new HashMap<String, String>();
		fileSystem.put("script1", script1);
		fileSystem.put("lexicon", lexicon);

		new StandardScript(script2, new MockFileHandler(fileSystem)).process();

		String received = fileSystem.get("newlex");
		String expected = "" +
			"apka\n" +
			"paku\n" +
			"atku";
		assertEquals(expected, received);
	}

	@Ignore
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

		Map<String, String> fileSystem = new HashMap<String, String>();
		fileSystem.put("script1", script1);
		fileSystem.put("lexicon", lexicon);

		new StandardScript(script2, new MockFileHandler(fileSystem)).process();

		String received = fileSystem.get("newlex");
		String expected = "" +
			"abaxa\n" +
			"fagu\n" +
			"aθugu";
		assertEquals(expected, received);
	}

	@Ignore
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

		Map<String, String> fileSystem = new HashMap<String, String>();
		fileSystem.put("model",   model);
		fileSystem.put("script1", script1);
		fileSystem.put("lexicon", lexicon);

		new StandardScript(script2, new MockFileHandler(fileSystem)).process();

		String received = fileSystem.get("newlex");
		String expected = "" +
			"abaxa\n" +
			"ɸagu\n" +
			"asugu";

		assertEquals(expected, received);
	}

	@Ignore
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

		Map<String, String> fileSystem = new HashMap<String, String>();
		fileSystem.put("script1", script1);
		fileSystem.put("lexicon", lexicon);

		new StandardScript(script2, new MockFileHandler(fileSystem)).process();

		String received = fileSystem.get("newlex");
		String expected = "" +
			"abaxa\n" +
			"fagu\n" +
			"aθugu";
		assertEquals(expected, received);
	}

	@Ignore
	@Test
	public void testExecute() throws Exception {
		Map<String, String> fileSystem = new HashMap<String, String>();
		FileHandler fileHandler = new MockFileHandler(fileSystem);

		String rules = getStringFromClassPath("testRuleLarge01.txt");
		String words = getStringFromClassPath("testRuleLarge01.lex");
		String outpt = getStringFromClassPath("testRuleLargeOut01.lex");

		// Append output clause
		rules = rules + "\n" +
			"MODE COMPOSITION\n" +
			"CLOSE LEXICON AS \'output.lex\'";

		fileSystem.put("testRuleLarge01.lex", words);
		fileSystem.put("testRuleLarge01.txt", rules);

		String executeRule = "EXECUTE 'testRuleLarge01.txt'";
		StandardScript script = new StandardScript("testExecute", executeRule, fileHandler);

		script.process();

		String received = fileSystem.get("output.lex");

		assertEquals(outpt.replaceAll("\\r\\n|\\n|\\r","\n"), received);
	}

	@Ignore
	@Test(timeout = 2000)
	public void testLoop() {
		String commands =
				"P = pw p t k" + '\n' +
				"B = bw b d g" + '\n' +
				"V = a o"      + '\n' +
				"P > B / V_V"  + '\n' +
				"P = p t k"    + '\n' +
				"B = b d g"    + '\n' +
				"B > 0 / #_c";

		new StandardScript(commands, NullFileHandler.INSTANCE);
	}

	@Ignore
	@Test
	public void reserveTest() {
		String commands = "RESERVE ph th kh";
		StandardScript sca = new StandardScript(commands, NullFileHandler.INSTANCE);
		sca.process();
		Collection<String> received = sca.getReservedSymbols();
		Collection<String> expected = new HashSet<String>();
		expected.add("ph");
		expected.add("th");
		expected.add("kh");
		assertEquals(expected, received);
	}

	@Ignore
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

	@Ignore
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

	@Ignore
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

		String commands =
				"OPEN 'test.lex' as TEST\n" +
				"WRITE TEST as 'write.lex'\n" +
				"CLOSE TEST as 'close.lex'";

		SoundChangeScript sca = new StandardScript(commands, new MockFileHandler(map));
		sca.process();

		assertFalse(sca.hasLexicon("TEST"));
		assertTrue(map.containsKey("close.lex"));
		assertTrue(map.containsKey("write.lex"));
		assertEquals(lexicon, map.get("write.lex"));
		assertEquals(lexicon, map.get("close.lex"));
	}

	private static String getStringFromClassPath(String name) throws IOException {
		InputStream rulesStream = StandardScriptMultilineTest.class
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
}
