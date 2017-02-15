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

import org.didelphis.common.io.FileHandler;
import org.didelphis.common.io.NullFileHandler;
import org.didelphis.common.language.phonetic.VariableStore;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by samantha on 11/8/16.
 */
public class ScriptParserTest {
	@Test
	public void testMultilineVariable() {
		String commands = "" +
				"C = p  t  k \n" +
				"    ph th kh\n" +
				"    f  s  x \n";

		NullFileHandler handler = NullFileHandler.INSTANCE;
		ScriptParser parser = getParser(commands, handler);
		parser.parse();
		ParserMemory memory = parser.getMemory();
		VariableStore variableStore = memory.getVariables();
		List<String> list = variableStore.get("C");

		assertEquals(9, list.size());
	}

	@Test
	public void testMultilineVariableBracket() {
		String commands = "" +
				"C = p   t   k  \n" +
				"    ph  th  kh \n" +
				"    [P] [T] [K]\n";

		NullFileHandler handler = NullFileHandler.INSTANCE;
		ScriptParser parser = getParser(commands, handler);
		parser.parse();
		ParserMemory memory = parser.getMemory();
		VariableStore variableStore = memory.getVariables();
		List<String> list = variableStore.get("C");

		assertEquals(9, list.size());
	}

	@Test
	public void testMultilineVariableOverparse() {
		String commands = "" +
				"C   =  p   t   k \n" +
				"[W] = [X] [Y] [Z]";

		NullFileHandler handler = NullFileHandler.INSTANCE;
		ScriptParser parser = getParser(commands, handler);
		parser.parse();
		ParserMemory memory = parser.getMemory();
		VariableStore variableStore = memory.getVariables();
		List<String> cList = variableStore.get("C");
		List<String> xList = variableStore.get("[W]");
		assertEquals(3, cList.size());
		assertEquals(3, xList.size());
	}

	@Test
	public void reserveTest() {
		String commands = "RESERVE ph th kh";
		ScriptParser parser = getParser(commands, NullFileHandler.INSTANCE);
		parser.parse();
		ParserMemory memory = parser.getMemory();
		Collection<String> received = memory.getReserved();
		Collection<String> expected = new HashSet<>();
		expected.add("ph");
		expected.add("th");
		expected.add("kh");
		assertEquals(expected, received);
	}

	private static ScriptParser getParser(String commands, FileHandler handler) {
		return new ScriptParser("", commands, handler, null);
	}
}
