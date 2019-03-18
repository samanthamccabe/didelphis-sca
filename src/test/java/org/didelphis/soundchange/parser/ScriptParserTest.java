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
import org.didelphis.io.MockFileHandler;
import org.didelphis.io.NullFileHandler;
import org.didelphis.language.parsing.ParseException;
import org.didelphis.soundchange.VariableStore;
import org.didelphis.language.phonetic.features.IntegerFeature;
import org.didelphis.soundchange.ErrorLogger;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScriptParserTest {

	@Test
	void testProjectFileStructure() {
		Map<String, String> map = new HashMap<>();

		String variablesScript1 = "X = 1 2 3 4";
		String variablesScript2 = "V = a e i o u";
		String variablesScript3 = "C = p t k q\nIMPORT 'variables2'";

		map.put("variables1", variablesScript1);
		map.put("variables2", variablesScript2);
		map.put("variables3", variablesScript3);

		FileHandler handler = new MockFileHandler(map);

		String commands = "" +
				"IMPORT 'variables1'\n" +
				"IMPORT 'variables3'\n";

		ScriptParser<Integer> parser = getParser(commands, handler);
		parser.parse();

		ProjectFile mainProjectFile = parser.getMainProjectFile();
		List<ProjectFile> children = mainProjectFile.getChildren();
		assertEquals(2, children.size());
		assertEquals(1, children.get(1).getChildren().size());
	}

	@Test
	void testIllegalCharacterinVariable() {
		assertFails("C = >");
		assertFails("C = |");
		assertFails("C = .");
		assertFails("C = ,");
		assertFails("C = :");
		assertFails("C = ;");
		assertFails("C = <");
		assertFails("C = /");
		assertFails("C = \\");
		assertFails("C = +");
		assertFails("C = *");
		assertFails("C = ?");
		assertFails("C = #");
		assertFails("C = $");
		assertFails("C = !");
	}

	private static void assertFails(String data) {
		assertThrows(ParseException.class, () -> {
			NullFileHandler instance = NullFileHandler.INSTANCE;
			ScriptParser<Integer> parser = getParser(data, instance);
			parser.parse();
		});
	}

	@Test
	void testImportAfterMultilineVariable() {
		String commands = "" +
			"C = p  t  k  \n" +
			"    ph th kh \n" +
			"    f  s  x  \n" +
			"IMPORT 'unknown'";

		NullFileHandler handler = NullFileHandler.INSTANCE;
		ScriptParser<Integer> parser = getParser(commands, handler);
		parser.parse();

		ParserMemory<Integer> memory = parser.getMemory();
		VariableStore variableStore = memory.getVariables();
		List<String> list = variableStore.get("C");

		assertEquals(9, list.size());
	}

	@Test
	void testMultilineVariable() {
		String commands =
				"C = p  t  k  \n" +
				"    ph th kh \n" +
				"    f  s  x  \n";

		NullFileHandler handler = NullFileHandler.INSTANCE;
		ScriptParser<Integer> parser = getParser(commands, handler);
		parser.parse();
		ParserMemory<Integer> memory = parser.getMemory();
		VariableStore variableStore = memory.getVariables();
		List<String> list = variableStore.get("C");

		assertEquals(9, list.size());
	}

	@Test
	void testMultilineVariableBracket() {
		String commands = "" +
				"C = p   t   k   \n" +
				"    ph  th  kh  \n" +
				"    [P] [T] [K] \n";

		NullFileHandler handler = NullFileHandler.INSTANCE;
		ScriptParser<Integer> parser = getParser(commands, handler);
		parser.parse();
		ParserMemory<Integer> memory = parser.getMemory();
		VariableStore variableStore = memory.getVariables();
		List<String> list = variableStore.get("C");

		assertEquals(9, list.size());
	}

	@Test
	void testMultilineVariableOverparse() {
		String commands = "" +
				"C   =  p   t   k  \n" +
				"[W] = [X] [Y] [Z] \n";

		NullFileHandler handler = NullFileHandler.INSTANCE;
		ScriptParser<Integer> parser = getParser(commands, handler);
		parser.parse();
		ParserMemory<Integer> memory = parser.getMemory();
		VariableStore variableStore = memory.getVariables();
		List<String> cList = variableStore.get("C");
		List<String> xList = variableStore.get("[W]");
		assertEquals(3, cList.size());
		assertEquals(3, xList.size());
	}

	@Test
	void reserveTest() {
		String commands = "RESERVE ph th kh";
		ScriptParser<Integer> parser =
				getParser(commands, NullFileHandler.INSTANCE);
		parser.parse();
		ParserMemory<Integer> memory = parser.getMemory();
		Collection<String> received = memory.getReserved();
		Collection<String> expected = new HashSet<>();
		expected.add("ph");
		expected.add("th");
		expected.add("kh");
		assertEquals(expected, received);
	}

	private static ScriptParser<Integer> getParser(String commands,
			FileHandler handler) {
		return new ScriptParser<>("", IntegerFeature.INSTANCE, commands,
				handler, new ErrorLogger());
	}
}
