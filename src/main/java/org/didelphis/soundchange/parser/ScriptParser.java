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
import org.didelphis.language.automata.Automaton;
import org.didelphis.language.automata.matching.Match;
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
import org.didelphis.soundchange.command.rule.StandardRule;
import org.didelphis.utilities.Splitter;
import org.didelphis.utilities.Templates;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static org.didelphis.soundchange.parser.ParserTerms.*;

/**
 * @author Samantha Fiona McCabe
 * @date 2016-11-08
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScriptParser<T> {
	
	final String scriptPath;
	final FeatureType<T> type;
	final String scriptData;
	final FileHandler fileHandler;
	final ErrorLogger logger;
	final Collection<String> paths;
	final Queue<Runnable> commands;
	final ParserMemory<T> memory;
	int lineNumber;

	public ScriptParser(
			String scriptPath,
			FeatureType<T> type,
			String scriptData,
			FileHandler fileHandler,
			ErrorLogger logger
	) {
		this(scriptPath,
				type,
				scriptData,
				fileHandler,
				logger,
				new ParserMemory<>(type)
		);
	}

	private ScriptParser(
			String scriptPath,
			FeatureType<T> type,
			String scriptData,
			FileHandler fileHandler,
			ErrorLogger logger,
			ParserMemory<T> memory
	) {

		this.scriptPath = scriptPath;
		this.type = type;
		this.scriptData = scriptData;
		this.fileHandler = fileHandler;
		this.logger = logger;
		this.memory = memory;

		commands = new ArrayDeque<>();
		paths = new HashSet<>();
	}

	public Collection<String> getPaths() {
		return paths;
	}

	@Override
	public String toString() {
		return "ScriptParser{scriptPath='" + scriptPath + "'}";
	}

	/**
	 * @throws ParseException if any errors are encountered during processing
	 */
	public void parse() {
		if (!commands.isEmpty()) {
			return;
		} // Cutoff

		List<String> lines = Splitter.lines(scriptData);
		for (; lineNumber < lines.size(); lineNumber++) {
			String string = lines.get(lineNumber);
			String command = COMMENT.replace(string, "").trim();
			if (!command.isEmpty()) {
				int errorLine = lineNumber + 1;
				try {
					parseCommand(lines, command);
				} catch (ParseException e) {
					logger.add(scriptPath, errorLine, "", e.getMessage());
				}
			}
		}

		Collection<ErrorLogger.Error> errors = logger.getErrors();
		if (!errors.isEmpty()) {
			Templates.Builder builder = Templates.create();
			builder.add("Script compiled with errors:");
			for (ErrorLogger.Error error : errors) {
				builder.add("\n[{}] Line: {} --- {}\n{}");
				builder.with(
						error.getScript(),
						error.getLine(),
						error.getMessage(),
						error.getData()
				);
			}
			throw new ParseException(builder.build());
		}
	}

	public Queue<Runnable> getCommands() {
		return commands;
	}

	public ParserMemory<T> getMemory() {
		return memory;
	}

	private void parseCommand(List<String> lines, String command) {
		FormatterMode formatterMode = memory.getFormatterMode();
		if (LOAD.matches(command)) {
			FeatureMapping<T> featureModel = loadModel(
					scriptPath,
					command,
					fileHandler
			);
			memory.setFeatureMapping(featureModel);
		} else if (EXECUTE.matches(command)) {
			executeScript(scriptPath, command);
		} else if (IMPORT.matches(command)) {
			importScript(scriptPath, command);
		} else if (OPEN.matches(command)) {
			openLexicon(scriptPath, command, memory.factorySnapshot());
		} else if (WRITE.matches(command)) {
			writeLexicon(scriptPath, command, formatterMode);
		} else if (CLOSE.matches(command)) {
			closeLexicon(scriptPath, command, formatterMode);
		} else if (command.contains("=")) {
			StringBuilder sb = new StringBuilder(command);
			String next = nextLine(lines);
			while ((next != null) && VAR_NEXT_LINE.matches(next) &&
					!matchesOr(next, BREAK, COMPOUND, MODE)) {
				sb.append('\n');
				sb.append(next);
				lineNumber++;
				next = nextLine(lines);
			}
			memory.getVariables().add(sb.toString());
		} else if (RULE.matches(command)) {
			StringBuilder sb = new StringBuilder(command);
			String next = nextLine(lines);
			while ((next != null) && CONTINUATION.matches(next)) {
				sb.append('\n');
				sb.append(next);
				lineNumber++;
				next = nextLine(lines);
			}
			ParserMemory<T> parserMemory = new ParserMemory<>(memory);
			commands.add(new StandardRule<>(sb.toString(), parserMemory));
		} else if (MODE.matches(command)) {
			memory.setFormatterMode(setNormalizer(command));
		} else if (RESERVE.matches(command)) {
			String reserve = RESERVE.replace(command, "");
			Map<String, String> emptyMap = Collections.emptyMap();
			List<String> list = Splitter.whitespace(reserve, emptyMap);
			memory.getReserved().addAll(list);
		} else if (BREAK.matches(command)) {
			lineNumber = Integer.MAX_VALUE;
		} else {
			logger.add(scriptPath, lineNumber, command, "Unrecognized Command");
		}
	}

	private String nextLine(List<String> lines) {
		return (lineNumber + 1) < lines.size() 
				? lines.get(lineNumber + 1).trim() 
				: null;
	}

	/**
	 * OPEN "some_lexicon.txt" (as) FILEHANDLE to load the contents of that
	 * scriptPath into a lexicon stored against the scriptPath-handle;
	 *
	 * @param filePath path of the parent script
	 * @param command the whole command staring from OPEN, specifying the path
	 * 		and scriptPath-handle
	 */
	private void openLexicon(
			String filePath, String command, SequenceFactory<T> factory
	) {
		Match<String> matcher = OPEN.match(command);
		if (matcher.matches()) {
			String path = matcher.group(1);
			String handle = matcher.group(3);
			String fullPath = getPath(filePath, path);
			commands.add(new LexiconOpenCommand<>(
					memory.getLexicons(),
					fullPath,
					handle,
					fileHandler,
					factory
			));
			paths.add(fullPath);
		} else {
			String message = Templates.create()
					.add("Incorrectly formatted OPEN statement.")
					.data(command)
					.build();
			throw new ParseException(message);
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
		Match<String> matcher = CLOSE.match(command);
		if (matcher.matches()) {
			String handle = matcher.group(1);
			String path = matcher.group(3);
			String fullPath = getPath(filePath, path);
			commands.add(new LexiconCloseCommand<>(
					memory.getLexicons(),
					fullPath,
					handle,
					fileHandler,
					mode));
			paths.add(fullPath);
		} else {
			String message = Templates.create()
					.add("Incorrectly formatted CLOSE statement.")
					.data(command)
					.build();
			throw new ParseException(message);
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
		Match<String> matcher = WRITE.match(command);
		if (matcher.matches()) {
			String handle = matcher.group(1);
			String path = matcher.group(3);
			String fullPath = getPath(filePath, path);
			commands.add(new LexiconWriteCommand<>(
					memory.getLexicons(),
					fullPath,
					handle,
					fileHandler,
					mode));
			paths.add(fullPath);
		} else {
			String message = Templates.create()
					.add("Incorrectly formatted WRITE statement.")
					.data(command)
					.build();
			throw new ParseException(message);
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
	private void importScript(String filePath, String command) {
		String input = IMPORT.replace(command,"");
		String path = QUOTES.replace(input,"");
		try {
			String fullPath = getPath(filePath, path);
			String data = fileHandler.read(path);
			ScriptParser<T> scriptParser = new ScriptParser<>(
					fullPath,
					type,
					data,
					fileHandler,
					logger,
					memory
			);
			scriptParser.parse();
			commands.add(new ScriptImportCommand(
					filePath,
					fileHandler,
					logger,
					scriptParser.getCommands()
			));
			paths.add(fullPath);
			paths.addAll(scriptParser.getPaths());
		} catch (IOException e) {
			throw new ParseException("Unable to read from import " + path, e);
		}
	}

	/**
	 * Executes the script in a separate process and memory structure from the
	 * current one.
	 *
	 * @param filePath path of the parent script
	 * @param command the whole command starting with 'EXECUTE'
	 */
	private void executeScript(String filePath, String command) {
		String input = EXECUTE.replace(command, "");
		String path = QUOTES.replace(input, "");
		try {
		String fullPath = getPath(filePath, path);
		String data = fileHandler.read(path);
		ScriptParser<T> scriptParser = new ScriptParser<>(
				fullPath,
				type,
				data,
				fileHandler,
				logger,
				memory
		);
		scriptParser.parse();
		commands.add(new ScriptExecuteCommand<>(
				filePath,
				fileHandler,
				logger,
				scriptParser.getCommands()
		));
		paths.add(fullPath);
			paths.addAll(scriptParser.getPaths());
		} catch (IOException e) {
			throw new ParseException("Unable to read from import " + path, e);
		}
	}

	private FeatureMapping<T> loadModel(
			String filePath,
			String command,
			FileHandler handler
	) {
		String input = LOAD.replace(command,"");
		String path = QUOTES.replace(input,"");
		String fullPath = getPath(filePath, path);

		FeatureModelLoader<T> loader = new FeatureModelLoader<>(
				type,
				handler,
				fullPath
		);
		paths.add(fullPath);
		return loader.getFeatureMapping();
	}

	private static String getPath(String filePath, String path) {
		return filePath.contains("/") || filePath.contains("\\")
				? PATH.replace(filePath,"/") + path
				: path;
	}

	private static FormatterMode setNormalizer(String command) {
		String mode = MODE.replace(command,"");
		try {
			return FormatterMode.valueOf(mode.toUpperCase());
		} catch (IllegalArgumentException e) {
			String message = Templates.create()
					.add("Unsupported formatter mode {}")
					.with(mode)
					.data(command)
					.build();
			throw new ParseException(message, e);
		}
	}

	@SafeVarargs
	private static boolean matchesOr(
			String string,
			Automaton<String>... patterns
	) {
		for (Automaton<String> pattern : patterns) {
			boolean matches = pattern.matches(string);
			if (matches) {
				return true;
			}
		}
		return false;
	}
}
