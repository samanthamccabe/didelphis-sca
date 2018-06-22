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

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.didelphis.io.FileHandler;
import org.didelphis.language.parsing.FormatterMode;
import org.didelphis.language.parsing.ParseException;
import org.didelphis.language.phonetic.SequenceFactory;
import org.didelphis.language.phonetic.features.FeatureType;
import org.didelphis.language.phonetic.model.FeatureMapping;
import org.didelphis.language.phonetic.model.FeatureModelLoader;
import org.didelphis.soundchange.ErrorLogger;
import org.didelphis.soundchange.command.io.LexiconCloseCommand;
import org.didelphis.soundchange.command.io.LexiconOpenCommand;
import org.didelphis.soundchange.command.io.LexiconWriteCommand;
import org.didelphis.soundchange.command.io.ScriptExecuteCommand;
import org.didelphis.soundchange.command.io.ScriptImportCommand;
import org.didelphis.soundchange.command.rule.Rule;
import org.didelphis.soundchange.command.rule.StandardRule;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Samantha Fiona McCabe
 * @date 2016-11-08
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScriptParser<T> {

	static final String FILEHANDLE = "([A-Z0-9_]+)";
	static final String FILEPATH = "[\"\']([^\"\']+)[\"\']";
	static final String VAR_ELEMENT = "([^\\s/_>=<\\-:;,\\.\\$#!\\*\\+\\?\\{\\}\\(\\)\\|\\\\]|\\[[^\\]]+\\])+";

	static final String AS = "\\s+(as\\s)?";
	static final String S = "\\s+";

	static final Pattern MODE = compile("MODE");
	static final Pattern EXECUTE = compile("EXECUTE");
	static final Pattern IMPORT = compile("IMPORT");
	static final Pattern OPEN = compile("OPEN");
	static final Pattern WRITE = compile("WRITE");
	static final Pattern CLOSE = compile("CLOSE");
	static final Pattern BREAK = compile("BREAK");
	static final Pattern LOAD = compile("LOAD");
	static final Pattern RESERVE = compile("RESERVE");
	static final Pattern COMPOUND = compile("COMPOUND");
	static final Pattern END = compile("END");

	static final Pattern COMMENT_PATTERN = compile("%.*");
	static final Pattern NEWLINE_PATTERN = compile("(\\r?\\n|\\r)");
	static final Pattern RESERVE_PATTERN = compile(RESERVE, S);
	static final Pattern WHITESPACE_PATTERN = compile(S);

	static final Pattern CLOSE_PATTERN = compile(
			CLOSE,
			S,
			FILEHANDLE,
			AS,
			FILEPATH
	);
	static final Pattern WRITE_PATTERN = compile(
			WRITE,
			S,
			FILEHANDLE,
			AS,
			FILEPATH
	);
	static final Pattern OPEN_PATTERN = compile(
			OPEN,
			S,
			FILEPATH,
			AS,
			FILEHANDLE
	);
	static final Pattern MODE_PATTERN = compile(MODE, S);
	static final Pattern EXECUTE_PATTERN = compile(EXECUTE, S);
	static final Pattern IMPORT_PATTERN = compile(IMPORT, S);
	static final Pattern LOAD_PATTERN = compile(LOAD, S);
	static final Pattern QUOTES_PATTERN = compile("\"|\'");

	static final Pattern RULE_PATTERN = compile("(\\[[^\\]]+\\]|[^>])+\\s+>");
	static final Pattern VAR_NEXT_LINE = compile(
			"(",
			VAR_ELEMENT,
			"\\s+)*",
			VAR_ELEMENT
	);
	static final Pattern RULE_CONTINUATION = compile("\\s*(/|or|not)");
	static final Pattern PATH_PATTERN = compile("[\\\\/][^/\\\\]*$");

	final String scriptPath;
	final FeatureType<T> type;
	final CharSequence scriptData;
	final FileHandler fileHandler;
	final ErrorLogger logger;

	final Queue<Runnable> commands;
	final ParserMemory<T> memory;

	int lineNumber;

	public ScriptParser(String scriptPath, FeatureType<T> type,
			CharSequence scriptData, FileHandler fileHandler,
			ErrorLogger logger) {
		this(scriptPath, type, scriptData, fileHandler, logger,
				new ParserMemory<>(type));
	}

	private ScriptParser(String scriptPath, FeatureType<T> type,
			CharSequence scriptData, FileHandler fileHandler,
			ErrorLogger logger, ParserMemory<T> memory) {

		this.scriptPath = scriptPath;
		this.type = type;
		this.scriptData = scriptData;
		this.fileHandler = fileHandler;
		this.logger = logger;
		this.memory = memory;

		commands = new ArrayDeque<>();
	}

	@Override
	public String toString() {
		return "ScriptParser{" + "scriptPath='" + scriptPath + '\'' + '}';
	}

	public void parse() {
		if (!commands.isEmpty()) {
			return;
		} // Cutoff

		List<String> lines = getStrings(scriptData);
		for (; lineNumber < lines.size(); lineNumber++) {
			String string = lines.get(lineNumber);
			String command =
					COMMENT_PATTERN.matcher(string).replaceAll("").trim();
			if (!command.isEmpty()) {
				int errorLine = lineNumber + 1;
				try {
					parseCommand(lines, command);
				} catch (ParseException e) {
					logger.add(scriptPath, errorLine, "", e.getMessage());
				}
			}
		}
	}

	private void parseCommand(List<String> lines, String command) {
		FormatterMode formatterMode = memory.getFormatterMode();
		if (startsWith(LOAD, command)) {
			FeatureMapping<T> featureModel = loadModel(scriptPath, command, type, fileHandler, formatterMode);
			memory.setFeatureMapping(featureModel);
		} else if (startsWith(EXECUTE, command)) {
			executeScript(scriptPath, command);
		} else if (startsWith(IMPORT, command)) {
			importScript(scriptPath, command);
		} else if (startsWith(OPEN, command)) {
			openLexicon(scriptPath, command, memory.factorySnapshot());
		} else if (startsWith(WRITE, command)) {
			writeLexicon(scriptPath, command, formatterMode);
		} else if (startsWith(CLOSE, command)) {
			closeLexicon(scriptPath, command, formatterMode);
		} else if (command.contains("=")) {
			String sb = command;
			String next = nextLine(lines);
			while ((next != null) && VAR_NEXT_LINE.matcher(next).matches() &&
					!matchesOr(next, BREAK, COMPOUND, MODE)) {
				sb += '\n' + next;
				lineNumber++;
				next = nextLine(lines);
			}
			memory.getVariables().add(sb);
		} else if (startsWith(RULE_PATTERN, command)) {
			String sb = command;
			String next = nextLine(lines);
			while ((next != null) && startsWith(RULE_CONTINUATION, next)) {
				sb += '\n' + next;
				lineNumber++;
				next = nextLine(lines);
			}
			ParserMemory<T> parserMemory = new ParserMemory<>(memory);
			Rule<T> rule = new StandardRule<>(sb, parserMemory);
			commands.add(rule);
		} else if (startsWith(MODE, command)) {
			memory.setFormatterMode(setNormalizer(command));
		} else if (startsWith(RESERVE, command)) {
			String reserve = RESERVE_PATTERN.matcher(command).replaceAll("");
			Collections.addAll(memory.getReserved(), WHITESPACE_PATTERN.split(reserve));
		} else if (startsWith(BREAK, command)) {
			lineNumber = Integer.MAX_VALUE;
		} else {
			logger.add(scriptPath, lineNumber, command, "Unrecognized Command");
		}
	}

	private static boolean startsWith(Pattern pattern, String string) {
		return pattern.matcher(string).lookingAt();
	}

	public Queue<Runnable> getCommands() {
		return commands;
	}

	public ParserMemory<T> getMemory() {
		return memory;
	}

	private String nextLine(List<String> lines) {
		return (lineNumber + 1) < lines.size() ? lines.get(lineNumber + 1)
				.trim() : null;
	}

	/**
	 * OPEN "some_lexicon.txt" (as) FILEHANDLE to load the contents of that
	 * scriptPath into a lexicon stored against the scriptPath-handle;
	 *
	 * @param filePath path of the parent script
	 * @param command  the whole command staring from OPEN, specifying the path
	 *                 and scriptPath-handle
	 */
	private void openLexicon(String filePath, String command,
			SequenceFactory<T> factory) {
		Matcher matcher = OPEN_PATTERN.matcher(command);
		if (matcher.lookingAt()) {
			String path = matcher.group(1);
			String handle = matcher.group(3);
			String fullPath = getPath(filePath, path);
			commands.add(new LexiconOpenCommand<>(memory.getLexicons(), fullPath,
					handle, fileHandler, factory));
		} else {
			throw ParseException.builder()
					.add("Incorrectly formatted OPEN statement.")
					.data(command)
					.build();
		}
	}

	/**
	 * CLOSE FILEHANDLE (as) "some_output2.txt" to close the scriptPath-handle
	 * and save the lexicon to the specified scriptPath.
	 *
	 * @param filePath path of the parent script
	 * @param command  the whole command starting from CLOSE, specifying the
	 *                 scriptPath-handle and path
	 *
	 * @throws ParseException
	 */
	private void closeLexicon(String filePath, String command,
			FormatterMode mode) {
		Matcher matcher = CLOSE_PATTERN.matcher(command);
		if (matcher.lookingAt()) {
			String handle = matcher.group(1);
			String path = matcher.group(3);
			String fullPath = getPath(filePath, path);
			commands.add(new LexiconCloseCommand<>(memory.getLexicons(), fullPath,
					handle, fileHandler, mode));
		} else {
			throw ParseException.builder()
					.add("Incorrectly formatted CLOSE statement.")
					.data(command)
					.build();
		}
	}

	/**
	 * WRITE FILEHANDLE (as) "some_output1.txt" to save the current state of the
	 * lexicon to the specified scriptPath, but leave the handle open
	 *
	 * @param filePath path of the parent script
	 * @param command  the whole command starting from WRITE, specifying the
	 *                 scriptPath-handle and path
	 *
	 * @throws ParseException
	 */
	private void writeLexicon(String filePath, String command,
			FormatterMode mode) {
		Matcher matcher = WRITE_PATTERN.matcher(command);
		if (matcher.lookingAt()) {
			String handle = matcher.group(1);
			String path = matcher.group(3);
			String fullPath = getPath(filePath, path);
			commands.add(new LexiconWriteCommand<>(memory.getLexicons(), fullPath,
					handle, fileHandler, mode));
		} else {
			throw ParseException.builder()
					.add("Incorrectly formatted WRITE statement.")
					.data(command)
					.build();
		}
	}

	/**
	 * IMPORT other rule files, which basically inserts those commands into your
	 * current rule scriptPath; Unlike other commands, this runs immediately and
	 * inserts the new commands into the current sound change applier
	 *
	 * @param filePath path of the parent script
	 * @param command  the whole command starting with 'IMPORT'
	 */
	private void importScript(String filePath, CharSequence command) {
		String input = IMPORT_PATTERN.matcher(command).replaceAll("");
		String path = QUOTES_PATTERN.matcher(input).replaceAll("");
		CharSequence data = fileHandler.read(path);
		String fullPath = getPath(filePath, path);
		ScriptParser<T> scriptParser =
				new ScriptParser<>(fullPath, type, data, fileHandler, logger,
						memory);
		scriptParser.parse();
		commands.add(new ScriptImportCommand(filePath, fileHandler, logger,
				scriptParser.getCommands()));
	}

	/**
	 * EXECUTE other rule files, which just does what that rule scriptPath does
	 * in a separate process;
	 *
	 * @param filePath path of the parent script
	 * @param command  the whole command starting with 'EXECUTE'
	 */
	private void executeScript(String filePath, CharSequence command) {
		String input = EXECUTE_PATTERN.matcher(command).replaceAll("");
		String path = QUOTES_PATTERN.matcher(input).replaceAll("");
		String fullPath = getPath(filePath, path);
		commands.add(new ScriptExecuteCommand<>(fullPath, type, fileHandler,
				logger));
	}

	private static List<String> getStrings(CharSequence scriptData) {
		List<String> lines = new ArrayList<>();
		Collections.addAll(lines, NEWLINE_PATTERN.split(scriptData));
		return lines;
	}

	private static <T> FeatureMapping<T> loadModel(String filePath,
			CharSequence command, FeatureType<T> type, FileHandler handler,
			FormatterMode mode) {
		String input = LOAD_PATTERN.matcher(command).replaceAll("");
		String path = QUOTES_PATTERN.matcher(input).replaceAll("");
		String fullPath = getPath(filePath, path);

		List<String> list = new ArrayList<>();
		CharSequence charSequence = handler.read(fullPath);
		Collections.addAll(list, charSequence.toString().split("\r?\n|\r"));
		FeatureModelLoader<T> loader =
				new FeatureModelLoader<>(type, handler, list);
		return loader.getFeatureMapping();
	}

	private static String getPath(String filePath, String path) {
		return filePath.contains("/") || filePath.contains("\\") ?
				PATH_PATTERN.matcher(filePath).replaceAll("/") + path : path;
	}

	private static FormatterMode setNormalizer(CharSequence command) {
		String mode = MODE_PATTERN.matcher(command).replaceAll("");
		try {
			return FormatterMode.valueOf(mode.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw ParseException.builder(e)
					.add("Unsupported formatter mode {}")
					.with(mode)
					.data(command)
					.build();
		}
	}

	private static Pattern compile(String start, String... regex) {
		String sb = start;
		for (String p : regex) {
			sb += p;
		}
		return Pattern.compile(sb, Pattern.CASE_INSENSITIVE);
	}

	private static Pattern compile(Pattern pattern, String... regex) {
		return compile(pattern.pattern(), regex);
	}

	private static boolean matchesOr(CharSequence string, Pattern... patterns) {
		for (Pattern pattern : patterns) {
			boolean matches = pattern.matcher(string).lookingAt();
			if (matches) {
				return true;
			}
		}
		return false;
	}
}
