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

package org.haedus.soundchange;

import org.haedus.enums.FormatterMode;
import org.haedus.exceptions.ParseException;
import org.haedus.io.FileHandler;
import org.haedus.phonetic.model.FeatureModel;
import org.haedus.phonetic.model.FeatureModelLoader;
import org.haedus.phonetic.Lexicon;
import org.haedus.phonetic.LexiconMap;
import org.haedus.phonetic.SequenceFactory;
import org.haedus.phonetic.VariableStore;
import org.haedus.soundchange.command.Command;
import org.haedus.soundchange.command.LexiconCloseCommand;
import org.haedus.soundchange.command.LexiconOpenCommand;
import org.haedus.soundchange.command.LexiconWriteCommand;
import org.haedus.soundchange.command.Rule;
import org.haedus.soundchange.command.ScriptExecuteCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Samantha Fiona Morrigan McCabe
 * Date: 4/18/13
 * Time: 11:46 PM
 */
public class StandardScript implements SoundChangeScript {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(StandardScript.class);

	private static final String COMMENT_STRING = "%";
	private static final String FILEHANDLE     = "([A-Z0-9_]+)";
	private static final String FILEPATH       = "[\"\']([^\"\']+)[\"\']";

	private static final Pattern MODE    = Pattern.compile("(MODE|mode)");
	private static final Pattern EXECUTE = Pattern.compile("(EXECUTE|execute)");
	private static final Pattern IMPORT  = Pattern.compile("(IMPORT|import)");
	private static final Pattern OPEN    = Pattern.compile("(OPEN|open)");
	private static final Pattern WRITE   = Pattern.compile("(WRITE|write)");
	private static final Pattern CLOSE   = Pattern.compile("(CLOSE|close)");
	private static final Pattern BREAK   = Pattern.compile("(BREAK|break)");
	private static final Pattern LOAD    = Pattern.compile("(LOAD|load)");
	private static final Pattern RESERVE = Pattern.compile("(RESERVE|reserve)");

	private static final Pattern COMMENT_PATTERN    = Pattern.compile(COMMENT_STRING + ".*");
	private static final Pattern NEWLINE_PATTERN    = Pattern.compile("\\s*(\\r?\\n|\\r)\\s*");
	private static final Pattern RESERVE_PATTERN    = Pattern.compile(RESERVE.pattern() + ":? *");
	private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

	private static final Pattern CLOSE_PATTERN   = Pattern.compile(CLOSE.pattern() + "\\s+" + FILEHANDLE + "\\s+((as|AS)\\s)?" + FILEPATH);
	private static final Pattern WRITE_PATTERN   = Pattern.compile(WRITE.pattern() + "\\s+" + FILEHANDLE + "\\s+((as|AS)\\s)?" + FILEPATH);
	private static final Pattern OPEN_PATTERN    = Pattern.compile(OPEN.pattern() + "\\s+" + FILEPATH + "\\s+((as|AS)\\s)?" + FILEHANDLE);
	private static final Pattern MODE_PATTERN    = Pattern.compile(MODE.pattern() + ":? *");
	private static final Pattern EXECUTE_PATTERN = Pattern.compile(EXECUTE.pattern() + "\\s+");
	private static final Pattern IMPORT_PATTERN  = Pattern.compile(IMPORT.pattern() + "\\s+");
	private static final Pattern LOAD_PATTERN    = Pattern.compile(LOAD.pattern() + "\\s+");
	private static final Pattern QUOTES_PATTERN  = Pattern.compile("\"|\'");

	private final String         scriptId;
	private final FileHandler    fileHandler;
	private final Queue<Command> commands;
	private final LexiconMap     lexicons;
	private final VariableStore  variables;
	private final Set<String>    reserved;

	// Need these as fields or IMPORT doesn't work correctly
	private FormatterMode formatterMode;
	private FeatureModel  featureModel;

	public StandardScript(String id, CharSequence script, FileHandler handler) {
		this(id, handler, FormatterMode.NONE, FeatureModel.EMPTY_MODEL);

		Collection<String> lines = new ArrayList<String>();
		Collections.addAll(lines, NEWLINE_PATTERN.split(script));

		boolean fail = parse(id, lines);
		if (fail) {
			throw new ParseException("There were problems compiling the script " + id + "; please see logs for details");
		}
	}

	private StandardScript(String id, FileHandler handler, FormatterMode mode, FeatureModel model) {
		scriptId    = id;
		fileHandler = handler;

		lexicons  = new LexiconMap();
		commands  = new ArrayDeque<Command>();
		variables = new VariableStore();
		reserved  = new LinkedHashSet<String>();

		formatterMode = mode;
		featureModel  = model;
	}

	// Visible for testing
	StandardScript(CharSequence script, FileHandler fileHandlerParam) {
		this("DEFAULT", script, fileHandlerParam);
	}

	@Override
	public boolean hasLexicon(String handle) {
		return lexicons.hasHandle(handle);
	}

	@Override
	public Lexicon getLexicon(String handle) {
		return lexicons.get(handle);
	}

	@Override
	public Queue<Command> getCommands() {
		return commands;
	}

	@Override
	public void process() {
		for (Command command : commands) {
			command.execute();
		}
	}

	public Collection<String> getReservedSymbols() {
		return reserved;
	}

	private boolean parse(String id, Iterable<String> strings) {

		// For error reporting
		int lineNumber = 1;
		boolean fail = false;

		for (String string : strings) {
			if (!string.startsWith(COMMENT_STRING) && !string.isEmpty()) {
				String command = COMMENT_PATTERN.matcher(string).replaceAll("").trim();
				try {
					if (LOAD.matcher(command).lookingAt()) {
						featureModel = loadModel(command, fileHandler, formatterMode);
					} else if (EXECUTE.matcher(command).lookingAt()) {
						executeScript(command);
					} else if (IMPORT.matcher(command).lookingAt()) {
						importScript(command);
					} else if (OPEN.matcher(command).lookingAt()) {
						SequenceFactory factory = new SequenceFactory(
							featureModel,
							new VariableStore(variables),  // Be sure to defensively copy
							new HashSet<String>(reserved), // Be sure to defensively copy
							formatterMode
						);
						openLexicon(command, factory);
					} else if (WRITE.matcher(command).lookingAt()) {
						writeLexicon(command, formatterMode);
					} else if (CLOSE.matcher(command).lookingAt()) {
						closeLexicon(command, formatterMode);
					} else if (command.contains("=")) {
						variables.add(command);
					} else if (command.contains(">")) {
						// This is probably the correct scope; if other commands change the variables or segmentation mode,
						// we could get unexpected behavior if this is initialized outside the loop
						SequenceFactory factory = new SequenceFactory(
							featureModel,
							new VariableStore(variables),
							new HashSet<String>(reserved),
							formatterMode
						);
						commands.add(new Rule(command, lexicons, factory));
					} else if (MODE.matcher(command).lookingAt()) {
						formatterMode = setNormalizer(command);
					} else if (RESERVE.matcher(command).lookingAt()) {
						String reserve = RESERVE_PATTERN.matcher(command).replaceAll("");
						Collections.addAll(reserved, WHITESPACE_PATTERN.split(reserve));
					} else if (BREAK.matcher(command).lookingAt()) {
						break; // Stop parsing commands
					} else {
						LOGGER.warn("Unrecognized Command: {}", string);
					}
				} catch (Exception e) {
					fail = true;
					LOGGER.error("Script: {} Line: {} --- Compilation Error", id, lineNumber, e);
				}
			}
		}
		return fail;
	}

	private static FeatureModel loadModel(CharSequence command, FileHandler handler, FormatterMode mode) {
		String input = LOAD_PATTERN.matcher(command).replaceAll("");
		String path  = QUOTES_PATTERN.matcher(input).replaceAll("");

		FeatureModelLoader loader = new FeatureModelLoader(path, handler.readLines(path), mode);
		return new FeatureModel(loader, mode);
	}

	/**
	 * OPEN "some_lexicon.txt" (as) FILEHANDLE to load the contents of that file into a lexicon
	 * stored against the file-handle;
	 *
	 * @param command the whole command staring from OPEN, specifying the path and file-handle
	 */
	private void openLexicon(String command, SequenceFactory factory) {
		Matcher matcher = OPEN_PATTERN.matcher(command);
		if (matcher.lookingAt()) {
			String path   = matcher.group(2);
			String handle = matcher.group(5);
			commands.add(new LexiconOpenCommand(lexicons, path, handle, fileHandler, factory));
		} else {
			throw new ParseException("Command seems to be ill-formatted: " + command);
		}
	}

	/**
	 * CLOSE FILEHANDLE (as) "some_output2.txt" to close the file-handle and save the lexicon to the specified file.
	 *
	 * @param command the whole command starting from CLOSE, specifying the file-handle and path
	 * @throws  ParseException
	 */
	private void closeLexicon(String command, FormatterMode mode) {
		Matcher matcher = CLOSE_PATTERN.matcher(command);
		if (matcher.lookingAt()) {
			String handle = matcher.group(2);
			String path   = matcher.group(5);
			commands.add(new LexiconCloseCommand(lexicons, path, handle, fileHandler, mode));
		} else {
			throw new ParseException("Command seems to be ill-formatted: " + command);
		}
	}

	/**
	 * WRITE FILEHANDLE (as) "some_output1.txt" to save the current state of the lexicon to the specified file,
	 * but leave the handle open
	 *
	 * @param command the whole command starting from WRITE, specifying the file-handle and path
	 * @throws ParseException
	 */
	private void writeLexicon(String command, FormatterMode mode) {
		Matcher matcher = WRITE_PATTERN.matcher(command);
		if (matcher.lookingAt()) {
			String handle = matcher.group(2);
			String path   = matcher.group(5);
			commands.add(new LexiconWriteCommand(lexicons, path, handle, fileHandler, mode));
		} else {
			throw new ParseException("Command seems to be ill-formatted: " + command);
		}
	}

	/**
	 * IMPORT other rule files, which basically inserts those commands into your current rule file;
	 * Unlike other commands, this runs immediately and inserts the new commands into the current sound change applier
	 *
	 * @param command the whole command starting with 'IMPORT'
	 */
	private void importScript(CharSequence command) {
		String input = IMPORT_PATTERN.matcher(command).replaceAll("");
		String path  = QUOTES_PATTERN.matcher(input).replaceAll("");
		String data = fileHandler.read(path);
		Collection<String> lines = new ArrayList<String>();
		Collections.addAll(lines, NEWLINE_PATTERN.split(data));
		parse(path, lines);
//		StandardScript script = new StandardScript(path, data, fileHandler);
//		commands.addAll(script.getCommands());
//		variables.addAll(script.variables);
//		reserved.addAll(script.reserved);
//		if (script.formatterMode != null) { formatterMode = script.formatterMode; }
//		if (script.featureModel  != null) { featureModel  = script.featureModel;  }
	}

	/**
	 * EXECUTE other rule files, which just does what that rule file does in a separate process;
	 *
	 * @param command the whole command starting with 'EXECUTE'
	 */
	private void executeScript(CharSequence command) {
		String input = EXECUTE_PATTERN.matcher(command).replaceAll("");
		String path  = QUOTES_PATTERN.matcher(input).replaceAll("");
		commands.add(new ScriptExecuteCommand(path, fileHandler));
	}

	private static FormatterMode setNormalizer(CharSequence command) {
		String mode = MODE_PATTERN.matcher(command).replaceAll("");
		try {
			return FormatterMode.valueOf(mode.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new ParseException("Unsupported mode: "+mode, e);
		}
	}

	@Override
	public String toString() {
		return "StandardScript{"+scriptId+'}';
	}
}
