package org.didelphis.soundchange;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScriptErrorTest {

	@Test
	void buildWithContext() {

		List<String> scriptData = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			scriptData.add("Line " + i);
		}

		String expected = "(E000) Descriptive Error Message\n"
				+ "====================\n"
				+ "script_name.rsca\n"
				+ "====================\n"
				+ "| 10 |     Line 10\n"
				+ "| 11 |     Line 11\n"
				+ "| 12 | ==> Line 12\n"
				+ "|    |          ^^\n"
				+ "| 13 |     Line 13\n"
				+ "| 14 |     Line 14\n"
				+ "====================\n";

		String received = new ScriptError()
				.withMessage("(E000) Descriptive Error Message")
				.withScriptName("script_name.rsca")
				.withLineNumber(12)
				.withDividerSize(20)
				.withScripData(scriptData)
				.withErrorLength(2)
				.withErrorPosition(5)
				.build();

		assertEquals(expected, received);
	}

	@Test
	void buildWithMoreContext() {

		List<String> scriptData = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			scriptData.add("Line " + i);
		}

		String expected = "(E000) Descriptive Error Message\n"
				+ "====================\n"
				+ "script_name.rsca\n"
				+ "====================\n"
				+ "| 09 |     Line 9\n"
				+ "| 10 |     Line 10\n"
				+ "| 11 |     Line 11\n"
				+ "| 12 | ==> Line 12\n"
				+ "|    |          ^^\n"
				+ "| 13 |     Line 13\n"
				+ "| 14 |     Line 14\n"
				+ "| 15 |     Line 15\n"
				+ "====================\n";

		String received = new ScriptError()
				.withMessage("(E000) Descriptive Error Message")
				.withScriptName("script_name.rsca")
				.withContextSize(3)
				.withDividerSize(20)
				.withLineNumber(12)
				.withScripData(scriptData)
				.withErrorLength(2)
				.withErrorPosition(5)
				.build();

		assertEquals(expected, received);
	}
}
