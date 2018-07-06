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
public class ScriptParser<T> {

	private static final String FILEHANDLE = "([A-Z0-9_]+)";
	private static final String FILEPATH = "[\"\']([^\"\']+)[\"\']";
	private static final String VAR_ELEMENT =
			"([^\\s/_>=<\\-:;,\\.\\$#!\\*\\+\\?\\{\\}\\(\\)\\|\\\\]|\\[[^\\]]+\\])+";

	private static final String AS = "\\s+(as\\s)?";
	private static final String S = "\\s+";

	private static final Pattern MODE = compile("MODE");
	private static final Pattern EXECUTE = compile("EXECUTE");
	private static final Pattern IMPORT = compile("IMPORT");
	private static final Pattern OPEN = compile("OPEN");
	private static final Pattern WRITE = compile("WRITE");
	private static final Pattern CLOSE = compile("CLOSE");
	private static final Pattern BREAK = compile("BREAK");
	private static final Pattern LOAD = compile("LOAD");
	private static final Pattern RESERVE = compile("RESERVE");
	private static final Pattern COMPOUND = compile("COMPOUND");
	private static final Pattern END = compile("END");

	private static final Pattern COMMENT_PATTERN = compile("%.*");
	private static final Pattern NEWLINE_PATTERN = compile("(\\r?\\n|\\r)");
	private static final Pattern RESERVE_PATTERN = compile(RESERVE, S);
	private static final Pattern WHITESPACE_PATTERN = compile(S);

	private static final Pattern CLOSE_PATTERN =
			compile(CLOSE, S, FILEHANDLE, AS, FILEPATH);
	private static final Pattern WRITE_PATTERN =
			compile(WRITE, S, FILEHANDLE, AS, FILEPATH);
	private static final Pattern OPEN_PATTERN =
			compile(OPEN, S, FILEPATH, AS, FILEHANDLE);
	private static final Pattern MODE_PATTERN = compile(MODE, S);
	private static final Pattern EXECUTE_PATTERN = compile(EXECUTE, S);
	private static final Pattern IMPORT_PATTERN = compile(IMPORT, S);
	private static final Pattern LOAD_PATTERN = compile(LOAD, S);
	private static final Pattern QUOTES_PATTERN = compile("\"|\'");

	private static final Pattern RULE_PATTERN =
			compile("(\\[[^\\]]+\\]|[^>])+\\s+>");
	private static final Pattern VAR_NEXT_LINE =
			compile("(", VAR_ELEMENT, "\\s+)*", VAR_ELEMENT);
	private static final Pattern RULE_CONTINUATION = compile("\\s*(/|or|not)");
	private static final Pattern PATH_PATTERN = compile("[\\\\/][^/\\\\]*$");

	private final String scriptPath;
	private final FeatureType<T> type;
	private final CharSequence scriptData;
	private final FileHandler fileHandler;
	private final ErrorLogger logger;

	private final Queue<Runnable> commands;
	private final ParserMemory<T> memory;

	private boolean isParsed;
	private int lineNumber;

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
		if (isParsed) {
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
		isParsed = true;
	}

	private void parseCommand(List<String> lines, String command) {
		FormatterMode formatterMode = memory.getFormatterMode();
		if (LOAD.matcher(command).lookingAt()) {
			FeatureMapping<T> featureModel =
					loadModel(scriptPath, command, type, fileHandler,
							formatterMode);
			memory.setFeatureMapping(featureModel);
		} else if (EXECUTE.matcher(command).lookingAt()) {
			executeScript(scriptPath, command);
		} else if (IMPORT.matcher(command).lookingAt()) {
			importScript(scriptPath, command);
		} else if (OPEN.matcher(command).lookingAt()) {
			openLexicon(scriptPath, command, memory.factorySnapshot());
		} else if (WRITE.matcher(command).lookingAt()) {
			writeLexicon(scriptPath, command, formatterMode);
		} else if (CLOSE.matcher(command).lookingAt()) {
			closeLexicon(scriptPath, command, formatterMode);
		} else if (command.contains("=")) {
			StringBuilder sb = new StringBuilder(command);
			String next = nextLine(lines);
			while ((next != null) && VAR_NEXT_LINE.matcher(next).matches() &&
					!matchesOr(next, BREAK, COMPOUND, MODE)) {
				sb.append('\n').append(next);
				lineNumber++;
				next = nextLine(lines);
			}
			memory.getVariables().add(sb.toString());
		} else if (RULE_PATTERN.matcher(command).lookingAt()) {
			StringBuilder sb = new StringBuilder(command);
			String next = nextLine(lines);
			while ((next != null) &&
					RULE_CONTINUATION.matcher(next).lookingAt()) {
				sb.append('\n').append(next);
				lineNumber++;
				next = nextLine(lines);
			}
			ParserMemory<T> parserMemory = new ParserMemory<>(memory);
			Rule<T> rule = new StandardRule<>(sb.toString(), parserMemory);
			commands.add(rule);
		} else if (MODE.matcher(command).lookingAt()) {
			memory.setFormatterMode(setNormalizer(command));
		} else if (RESERVE.matcher(command).lookingAt()) {
			String reserve = RESERVE_PATTERN.matcher(command).replaceAll("");
			Collections.addAll(memory.getReserved(),
					WHITESPACE_PATTERN.split(reserve));
		} else if (BREAK.matcher(command).lookingAt()) {
			lineNumber = Integer.MAX_VALUE;
		} else {
			logger.add(scriptPath, lineNumber, command, "Unrecognized Command");
		}
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
			commands.add(new LexiconOpenCommand(memory.getLexicons(), fullPath,
					handle, fileHandler, factory));
		} else {
			throw new ParseException("Incorrectly formatted OPEN statement.",
					command);
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
			commands.add(new LexiconCloseCommand(memory.getLexicons(), fullPath,
					handle, fileHandler, mode));
		} else {
			throw new ParseException("Incorrectly formatted CLOSE statement.",
					command);
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
			commands.add(new LexiconWriteCommand(memory.getLexicons(), fullPath,
					handle, fileHandler, mode));
		} else {
			throw new ParseException("Incorrectly formatted WRITE statement.",
					command);
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
		Collections.addAll(list,
				String.valueOf(handler.read(fullPath)).split("\r?\n|\r"));
		FeatureModelLoader<T> loader =
				new FeatureModelLoader<>(fullPath, type, handler, list);
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
		StringBuilder sb = new StringBuilder(start);
		for (String p : regex) {
			sb.append(p);
		}
		return Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
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
