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

import org.didelphis.enums.FormatterMode;
import org.didelphis.exceptions.ParseException;
import org.didelphis.io.FileHandler;
import org.didelphis.phonetic.SequenceFactory;
import org.didelphis.phonetic.model.FeatureModel;
import org.didelphis.phonetic.model.FeatureModelLoader;
import org.didelphis.phonetic.model.StandardFeatureModel;
import org.didelphis.soundchange.ErrorLogger;
import org.didelphis.soundchange.command.LexiconCloseCommand;
import org.didelphis.soundchange.command.LexiconOpenCommand;
import org.didelphis.soundchange.command.LexiconWriteCommand;
import org.didelphis.soundchange.command.ScriptExecuteCommand;
import org.didelphis.soundchange.command.StandardRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by samantha on 11/8/16.
 */
public class ScriptParser {
	
	private static final String FILEHANDLE     = "([A-Z0-9_]+)";
	private static final String FILEPATH       = "[\"\']([^\"\']+)[\"\']";
	private static final String VAR_ELEMENT = "([^\\s/_>=<\\-:;,\\.\\$#!\\*\\+\\?\\{\\}\\(\\)\\|\\\\]|\\[[^\\]]+\\])+";

	private static final String AS = "\\s+(as\\s)?";
	private static final String S = "\\s+";

	private static final Pattern MODE     = compile("MODE");
	private static final Pattern EXECUTE  = compile("EXECUTE");
	private static final Pattern IMPORT   = compile("IMPORT");
	private static final Pattern OPEN     = compile("OPEN");
	private static final Pattern WRITE    = compile("WRITE");
	private static final Pattern CLOSE    = compile("CLOSE");
	private static final Pattern BREAK    = compile("BREAK");
	private static final Pattern LOAD     = compile("LOAD");
	private static final Pattern RESERVE  = compile("RESERVE");
	private static final Pattern COMPOUND = compile("COMPOUND");
	private static final Pattern END      = compile("END");

	private static final Pattern COMMENT_PATTERN    = compile("%.*");
	private static final Pattern NEWLINE_PATTERN    = compile("(\\r?\\n|\\r)");
	private static final Pattern RESERVE_PATTERN    = compile(RESERVE, S);
	private static final Pattern WHITESPACE_PATTERN = compile(S);

	private static final Pattern CLOSE_PATTERN   = compile(CLOSE, S, FILEHANDLE, AS, FILEPATH);
	private static final Pattern WRITE_PATTERN   = compile(WRITE, S, FILEHANDLE, AS, FILEPATH);
	private static final Pattern OPEN_PATTERN    = compile(OPEN, S, FILEPATH, AS, FILEHANDLE);
	private static final Pattern MODE_PATTERN    = compile(MODE, S);
	private static final Pattern EXECUTE_PATTERN = compile(EXECUTE, S);
	private static final Pattern IMPORT_PATTERN  = compile(IMPORT, S);
	private static final Pattern LOAD_PATTERN    = compile(LOAD, S);
	private static final Pattern QUOTES_PATTERN  = compile("\"|\'");

	private static final Pattern RULE_PATTERN      = compile("(\\[[^\\]]+\\]|[^>])+\\s+>");
	private static final Pattern VAR_NEXT_LINE     = compile("(",VAR_ELEMENT,"\\s+)*",VAR_ELEMENT);
	private static final Pattern RULE_CONTINUATION = compile("\\s*(/|or|not)");
	private static final Pattern PATH_PATTERN      = compile("[\\\\/][^/\\\\]*$");
	
	private final String scriptPath;
	private final CharSequence scriptData;
	private final FileHandler fileHandler;
	private final ErrorLogger logger;

	private final Queue<Runnable> commands;
	private final ParserMemory memory;

	private boolean isParsed;
	private int lineNumber;
	
	public ScriptParser(String scriptPath, CharSequence scriptData,
			FileHandler fileHandler, ErrorLogger logger) {
		this.scriptPath = scriptPath;
		this.scriptData = scriptData;
		this.fileHandler = fileHandler;
		this.logger = logger;
		
		commands = new ArrayDeque<Runnable>();
		memory = new ParserMemory();
	}

	private ScriptParser(String scriptPath, CharSequence scriptData,
			ScriptParser scriptParser) {
		this.scriptPath = scriptPath;
		this.scriptData = scriptData;
		
		fileHandler = scriptParser.fileHandler;
		logger = scriptParser.logger;
		commands = scriptParser.commands;
		memory = scriptParser.memory;
	}
	
	@Override
	public String toString() {
		return "ScriptParser{" + "scriptPath='" + scriptPath + '\'' + '}';
	}

	public void parse() {
		if (isParsed) { return; } // Cutoff

		List<String> lines = getStrings(scriptData);
		for (; lineNumber < lines.size(); lineNumber++) {
			String string = lines.get(lineNumber);
			if (!COMMENT_PATTERN.matcher(string).matches() && !string.isEmpty()) {
				int errorLine = lineNumber + 1;
				try {
					parseCommand(lines, string.trim());
				} catch (ParseException e) {
					logger.add(scriptPath, errorLine, string, e);
				}
			}
		}
		isParsed = true;
	}

	private void parseCommand(List<String> lines, String string) {
		String command = COMMENT_PATTERN.matcher(string).replaceAll("").trim();
		FormatterMode formatterMode = memory.getFormatterMode();
		if (LOAD.matcher(command).lookingAt()) {
			FeatureModel featureModel = loadModel(scriptPath, command, fileHandler, formatterMode);
			memory.setFeatureModel(featureModel);
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
			while (next != null && VAR_NEXT_LINE.matcher(next).matches()
					&& !matchesOr(next, BREAK, COMPOUND, MODE)) {
				sb.append('\n').append(next);
				lineNumber++;
				next = nextLine(lines);
			}
			memory.getVariables().add(sb.toString());
		} else if (RULE_PATTERN.matcher(command).lookingAt()) {
			StringBuilder sb = new StringBuilder(command);
			String next = nextLine(lines);
			while (next != null && RULE_CONTINUATION.matcher(next).lookingAt()) {
				sb.append('\n').append(next);
				lineNumber++;
				next = nextLine(lines);
			}
			commands.add(new StandardRule(sb.toString(), memory.getLexicons(),
					memory.factorySnapshot()));
		} else if (MODE.matcher(command).lookingAt()) {
			memory.setFormatterMode(setNormalizer(command));
		} else if (RESERVE.matcher(command).lookingAt()) {
			String reserve = RESERVE_PATTERN.matcher(command).replaceAll("");
			Collections.addAll(memory.getReserved(),WHITESPACE_PATTERN.split(reserve));
		} else if (BREAK.matcher(command).lookingAt()) {
			lineNumber = Integer.MAX_VALUE; 
		} else {
			logger.add(scriptPath, lineNumber, string, null);
		}
	}

	public Queue<Runnable> getCommands() {
		return commands;
	}

	public ParserMemory getMemory() {
		return memory;
	}
	
	@Nullable
	private String nextLine(List<String> lines) {
		String next;
		next = lineNumber + 1 < lines.size() ? lines.get(lineNumber + 1).trim() : null;
		return next;
	}
	
	/**
	 * OPEN "some_lexicon.txt" (as) FILEHANDLE to load the contents of that
	 * scriptPath into a lexicon stored against the scriptPath-handle;
	 *
	 * @param filePath path of the parent script
	 * @param command the whole command staring from OPEN, specifying the path and scriptPath-handle
	 */
	private void openLexicon(String filePath, String command, SequenceFactory factory) {
		Matcher matcher = OPEN_PATTERN.matcher(command);
		if (matcher.lookingAt()) {
			String path   = matcher.group(1);
			String handle = matcher.group(3);
			String fullPath = getPath(filePath, path);
			commands.add(new LexiconOpenCommand(memory.getLexicons(), fullPath, handle, fileHandler, factory));
		} else {
			throw new ParseException("Command seems to be ill-formatted: " + command);
		}
	}

	/**
	 * CLOSE FILEHANDLE (as) "some_output2.txt" to close the scriptPath-handle
	 * and save the lexicon to the specified scriptPath.
	 *
	 * @param filePath path of the parent script
	 * @param command the whole command starting from CLOSE, specifying the
	 * scriptPath-handle and path
	 * @throws  ParseException
	 */
	private void closeLexicon(String filePath, String command, FormatterMode mode) {
		Matcher matcher = CLOSE_PATTERN.matcher(command);
		if (matcher.lookingAt()) {
			String handle = matcher.group(1);
			String path   = matcher.group(3);
			String fullPath = getPath(filePath, path);
			commands.add(new LexiconCloseCommand(
					memory.getLexicons(), fullPath, handle, fileHandler, mode));
		} else {
			throw new ParseException("Command seems to be ill-formatted: " + command);
		}
	}

	/**
	 * WRITE FILEHANDLE (as) "some_output1.txt" to save the current state of the
	 * lexicon to the specified scriptPath, but leave the handle open
	 *
	 * @param filePath path of the parent script
	 * @param command the whole command starting from WRITE, specifying the
	 * scriptPath-handle and path
	 * @throws ParseException
	 */
	private void writeLexicon(String filePath, String command, FormatterMode mode) {
		Matcher matcher = WRITE_PATTERN.matcher(command);
		if (matcher.lookingAt()) {
			String handle = matcher.group(1);
			String path   = matcher.group(3);
			String fullPath = getPath(filePath, path);
			commands.add(new LexiconWriteCommand(
					memory.getLexicons(), fullPath, handle, fileHandler, mode));
		} else {
			throw new ParseException("Command seems to be ill-formatted: " + command);
		}
	}

	/**
	 * IMPORT other rule files, which basically inserts those commands into your
	 * current rule scriptPath; Unlike other commands, this runs immediately and
	 * inserts the new commands into the current sound change applier
	 *
	 * @param filePath path of the parent script
	 * @param command the whole command starting with 'IMPORT'
	 */
	private void importScript(String filePath, CharSequence command) {
		String input = IMPORT_PATTERN.matcher(command).replaceAll("");
		String path  = QUOTES_PATTERN.matcher(input).replaceAll("");
		String data = fileHandler.read(path);
		String fullPath = getPath(filePath, path);
		new ScriptParser(fullPath, data, this).parse();
	}

	/**
	 * EXECUTE other rule files, which just does what that rule scriptPath does
	 * in a separate process;
	 *
	 * @param filePath path of the parent script
	 * @param command the whole command starting with 'EXECUTE'
	 */
	private void executeScript(String filePath, CharSequence command) {
		String input = EXECUTE_PATTERN.matcher(command).replaceAll("");
		String path  = QUOTES_PATTERN.matcher(input).replaceAll("");
		String fullPath = getPath(filePath, path);
		commands.add(new ScriptExecuteCommand(fullPath, fileHandler, logger));
	}
	
	@NotNull
	private static List<String> getStrings(CharSequence scriptData) {
		List<String> lines = new ArrayList<String>();
		Collections.addAll(lines, NEWLINE_PATTERN.split(scriptData));
		return lines;
	}

	private static FeatureModel loadModel(String filePath,CharSequence command,
	                                      FileHandler handler, FormatterMode mode) {
		String input = LOAD_PATTERN.matcher(command).replaceAll("");
		String path  = QUOTES_PATTERN.matcher(input).replaceAll("");
		String fullPath = getPath(filePath, path);
		FeatureModelLoader loader = new FeatureModelLoader(fullPath, 
				handler.readLines(fullPath), mode);
		return new StandardFeatureModel(loader);
	}

	@NotNull
	private static String getPath(String filePath, String path) {
		String parentPath;
		if (filePath.contains("/") || filePath.contains("\\")) {
			parentPath = PATH_PATTERN.matcher(filePath).replaceAll("/");
		} else {
			parentPath = "";
		}
		return parentPath + path;
	}

	private static FormatterMode setNormalizer(CharSequence command) {
		String mode = MODE_PATTERN.matcher(command).replaceAll("");
		try {
			return FormatterMode.valueOf(mode.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new ParseException("Unsupported mode: " + mode, e);
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
