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

package org.didelphis.soundchange.parser;

import org.didelphis.io.FileHandler;
import org.didelphis.language.parsing.ParseException;
import org.didelphis.soundchange.LexiconMap;
import org.didelphis.soundchange.VariableStore;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.didelphis.utilities.Strings.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith (MockitoExtension.class)
class ScriptParserTest {

	@Mock
	private FileHandler fileHandler;

	@Test
	void testProjectFileStructure() throws IOException {

		String vars1 = "X = 1 2 3 4";
		String vars2 = "V = a e i o u";
		String vars3 = "C = p t k q\nIMPORT 'var2'";

		when(fileHandler.read("var1")).thenReturn(vars1);
		when(fileHandler.read("var2")).thenReturn(vars2);
		when(fileHandler.read("var3")).thenReturn(vars3);

		String commands = joinNL(
				"IMPORT 'var1'\n",
				"IMPORT 'var3'\n");

		ScriptParser parser = getParser(commands);
		parser.parse();
	}

	private void assertFails(String data) {
		assertThrows(ParseException.class, () -> testParse(data));
	}

	private void testParse(String data) {
		ScriptParser parser = getParser(data);
		parser.parse();
	}

	@Test
	void testBlank() {
		ScriptParser parser = getParser("\n   ");
		assertTrue(parser.parse());
	}

	@Test
	void testOpenDebug() {
		String commands = joinNL(
				"@debug",
				"open 'path' as HANDLE ",
				"a > b");

		ScriptParser parser = getParser(commands);
		parser.parse();

		LexiconMap lexicons = parser.getMemory().getLexicons();

		Set<String> debugKeys = lexicons.getDebugKeys();
		assertTrue(debugKeys.contains("HANDLE"));

		for (Runnable command : parser.getCommands()) {
			command.run();
		}
	}

	@Test
	void testImportAfterMultilineVariable() throws IOException {

		when(fileHandler.read(any())).thenReturn("");

		String commands = joinNL(
			"C = p  t  k  ",
			"    ph th kh ",
			"    f  s  x  ",
			"IMPORT 'unknown'");

		ScriptParser parser = getParser(commands);
		parser.parse();

		ParserMemory memory = parser.getMemory();
		VariableStore variableStore = memory.getVariables();
		List<String> list = variableStore.get("C");

		assertEquals(9, list.size());
	}

	@Test
	void testMultilineVariable() {

		String commands = joinNL(
				"C = p  t  k  ",
				"    ph th kh ",
				"    f  s  x  "
		);

		ScriptParser parser = getParser(commands);
		parser.parse();
		ParserMemory memory = parser.getMemory();
		VariableStore variableStore = memory.getVariables();
		List<String> list = variableStore.get("C");

		assertEquals(9, list.size());
	}

	@Test
	void testMultilineVariableBracket() {

		String commands = joinNL(
				"C = p   t   k   ",
				"    ph  th  kh  ",
				"    [P] [T] [K] ");

		ScriptParser parser = getParser(commands);
		parser.parse();
		ParserMemory memory = parser.getMemory();
		VariableStore variableStore = memory.getVariables();
		List<String> list = variableStore.get("C");

		assertEquals(9, list.size());
	}

	@Test
	void testMultilineVariableOverparse() {

		String commands = joinNL(
				"C   =  p   t   k  ",
				"[W] = [X] [Y] [Z] ");

		ScriptParser parser = getParser(commands);
		parser.parse();
		ParserMemory memory = parser.getMemory();
		VariableStore variableStore = memory.getVariables();
		List<String> cList = variableStore.get("C");
		List<String> xList = variableStore.get("[W]");
		assertEquals(3, cList.size());
		assertEquals(3, xList.size());
	}

	@Test
	void reserveTest() {
		String commands = "RESERVE ph th kh";
		ScriptParser parser = getParser(commands);
		parser.parse();
		ParserMemory memory = parser.getMemory();
		Collection<String> received = memory.getReserved();
		Collection<String> expected = new HashSet<>();
		expected.add("ph");
		expected.add("th");
		expected.add("kh");
		assertEquals(expected, received);
	}

	@Test
	void multilineRule() {
		String commands = joinNL(
				"a > 0 / _#",
				"    OR #_ ");
		ScriptParser parser = getParser(commands);
		parser.parse();

		assertFalse(parser.getCommands().isEmpty());
	}

	@Test
	void newFormatRule() {
		String command = joinNL(
				"[+con +velar +round] > [-round] / _[+round]      % Kw > K / {o u}",
				"                     | [-velar -round +bilabial] % Kw > P");

		ScriptParser parser = getParser(command);
		boolean success = parser.parse();

		assertTrue(success);
	}

	private ScriptParser getParser(String commands) {
		return new ScriptParser("test_script", commands, fileHandler);
	}
}
