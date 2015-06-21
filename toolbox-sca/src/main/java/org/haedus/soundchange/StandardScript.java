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
import org.haedus.io.DiskFileHandler;
import org.haedus.io.FileHandler;
import org.haedus.io.NullFileHandler;
import org.haedus.phonetic.FeatureModel;
import org.haedus.phonetic.FeatureModelLoader;
import org.haedus.phonetic.SequenceFactory;
import org.haedus.phonetic.VariableStore;
import org.haedus.soundchange.command.LexiconCloseCommand;
import org.haedus.soundchange.command.LexiconOpenCommand;
import org.haedus.soundchange.command.LexiconWriteCommand;
import org.haedus.soundchange.command.Rule;
import org.haedus.soundchange.command.ScriptExecuteCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Samantha Fiona Morrigan McCabe
 * Date: 4/18/13
 * Time: 11:46 PM
 */
public class StandardScript extends AbstractScript {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(StandardScript.class);

	private static final String FILEHANDLE = "([A-Z0-9_]+)";
	private static final String FILEPATH   = "[\"\']([^\"\']+)[\"\']";
	
	private static final Pattern NORMALIZER = Pattern.compile("(NORMALIZE|normalize)");
	private static final Pattern EXECUTE    = Pattern.compile("(EXECUTE|execute)");
	private static final Pattern IMPORT     = Pattern.compile("(IMPORT|import)");
	private static final Pattern OPEN       = Pattern.compile("(OPEN|open)");
	private static final Pattern WRITE      = Pattern.compile("(WRITE|write)");
	private static final Pattern CLOSE      = Pattern.compile("(CLOSE|close)");
	private static final Pattern BREAK      = Pattern.compile("(BREAK|break)");
	private static final Pattern LOAD       = Pattern.compile("(LOAD|load)");

	private static final Pattern CLOSE_PATTERN      = Pattern.compile(CLOSE.pattern() + "\\s+" + FILEHANDLE + "\\s+(as\\s)?" + FILEPATH);
	private static final Pattern WRITE_PATTERN      = Pattern.compile(WRITE.pattern() + "\\s+" + FILEHANDLE + "\\s+(as\\s)?" + FILEPATH);
	private static final Pattern OPEN_PATTERN       = Pattern.compile(OPEN.pattern()  + "\\s+" + FILEPATH + "\\s+(as\\s)?" + FILEHANDLE);
	private static final Pattern NORMALIZER_PATTERN = Pattern.compile(NORMALIZER.pattern() + ":? *");
	private static final Pattern EXECUTE_PATTERN    = Pattern.compile(EXECUTE.pattern() + "\\s+");
	private static final Pattern IMPORT_PATTERN     = Pattern.compile(IMPORT.pattern() + "\\s+");
	private static final Pattern LOAD_PATTERN       = Pattern.compile(LOAD.pattern() + "\\s+");
	private static final Pattern QUOTES_PATTERN     = Pattern.compile("\"|\'");

	private final FileHandler  fileHandler;
	//	private final VariableStore variables;
	private final Set<String>  reservedSymbols;

	public StandardScript(CharSequence script) {
		this(script, DiskFileHandler.getDefaultInstance());
	}

	// Visible for testing
	StandardScript(CharSequence script, FileHandler fileHandlerParam) {
		this(NEWLINE_PATTERN.split(script), fileHandlerParam); // Splits newlines and removes padding whitespace
	}

	// Visible for testing
	StandardScript(String[] array) {
		this(array, NullFileHandler.INSTANCE);
	}

	// Visible for testing
	StandardScript(String[] array, FileHandler fileHandlerParam) {
//		variables = new VariableStore(model);
		fileHandler = fileHandlerParam;
		reservedSymbols = new HashSet<String>();

		Collection<String> list = new ArrayList<String>();
		Collections.addAll(list, array);
		parse(list);
	}

	// Visible for testing
	Collection<String> getReservedSymbols() {
		return Collections.unmodifiableSet(reservedSymbols);
	}

	private void parse(Iterable<String> strings) {

		FormatterMode formatterMode = FormatterMode.NONE;
		VariableStore variables = new VariableStore();
		FeatureModel featureModel = FeatureModel.EMPTY_MODEL;
		
		for (String string : strings) {
			if (!string.startsWith(COMMENT_STRING) && !string.isEmpty()) {
				String command = COMMENT_PATTERN.matcher(string).replaceAll("").trim();
				if (LOAD.matcher(command).lookingAt()) {
					featureModel = loadModel(command, fileHandler,formatterMode);
				} else if (EXECUTE.matcher(command).lookingAt()) {
					executeScript(command);
				} else if (IMPORT.matcher(command).lookingAt()) {
					importScript(command);
				} else if (OPEN.matcher(command).lookingAt()) {
					SequenceFactory factory = new SequenceFactory(
							featureModel,
							new VariableStore(variables),         // Be sure to defensively copy
							new HashSet<String>(reservedSymbols), // Be sure to defensively copy
							formatterMode
					);
					openLexicon(command, factory);
				}else if (WRITE.matcher(command).lookingAt()) {
					writeLexicon(command);
				} else if (CLOSE.matcher(command).lookingAt()) {
					closeLexicon(command);
				} else if (command.contains("=")) {
					variables.add(command);
				} else if (command.contains(">")) {
					// This is probably the correct scope; if other commands change the variables or segmentation mode,
					// we could get unexpected behavior if this is initialized outside the loop
					SequenceFactory factory = new SequenceFactory(
							featureModel,
							new VariableStore(variables),
							new HashSet<String>(reservedSymbols),
							formatterMode
					);
					commands.add(new Rule(command, lexicons, factory));
				} else if (NORMALIZER.matcher(command).lookingAt()) {
					formatterMode = setNormalizer(command);
				} else if (RESERVE.matcher(command).lookingAt()) {
					String reserve = RESERVE_PATTERN.matcher(command).replaceAll("");
					Collections.addAll(reservedSymbols, WHITESPACE_PATTERN.split(reserve));
				} else if (BREAK.matcher(command).lookingAt()) {
					// Stop parsing commands
					break;
				} else {
					LOGGER.warn("Unrecognized Command: {}", string);
				}
			}
		}
	}

	private static FeatureModel loadModel(CharSequence command, FileHandler handler, FormatterMode mode) {
		String input = LOAD_PATTERN.matcher(command).replaceAll("");
		String path  = QUOTES_PATTERN.matcher(input).replaceAll("");

		FeatureModelLoader loader = new FeatureModelLoader(handler.readLines(path), mode);
		return new FeatureModel(loader, mode);
	}

	/**
	 * OPEN "some_lexicon.txt" (as) FILEHANDLE to load the contents of that file into a lexicon stored against the file-handle;
	 *
	 * @param command the whole command staring from OPEN, specifying the path and file-handle
	 */
	private void openLexicon(String command, SequenceFactory factory) {
		Matcher matcher = OPEN_PATTERN.matcher(command);
		if (matcher.lookingAt()) {
			String path   = matcher.group(2);
			String handle = matcher.group(4);
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
	private void closeLexicon(String command) {
		Matcher matcher = CLOSE_PATTERN.matcher(command);
		if (matcher.lookingAt()) {
			String handle = matcher.group(2);
			String path   = matcher.group(4);
			commands.add(new LexiconCloseCommand(lexicons, path, handle, fileHandler));
		} else {
			throw new ParseException("Command seems to be ill-formatted: " + command);
		}
	}

	/**
	 * WRITE FILEHANDLE (as) "some_output1.txt" to save the current state of the lexicon to the specified file, but leave the handle open
	 *
	 * @param command the whole command starting from WRITE, specifying the file-handle and path
	 * @throws ParseException
	 */
	private void writeLexicon(String command) {
		Matcher matcher = WRITE_PATTERN.matcher(command);
		if (matcher.lookingAt()) {
			String handle = matcher.group(2);
			String path   = matcher.group(4);
			commands.add(new LexiconWriteCommand(lexicons, path, handle, fileHandler));
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
		List<String> strings = fileHandler.readLines(path);
		parse(strings);
	}

	/**
	 * EXECUTE other rule files, which just does what that rule file does in a separate process;
	 *
	 * @param command the whole command starting with 'EXECUTE'
	 */
	private void executeScript(CharSequence command) {
		String path = EXECUTE_PATTERN.matcher(command).replaceAll("");
		commands.add(new ScriptExecuteCommand(path));
	}

	private static FormatterMode setNormalizer(CharSequence command) {
		String mode = NORMALIZER_PATTERN.matcher(command).replaceAll("");
		try {
			return FormatterMode.valueOf(mode.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new ParseException("Unsupported mode: "+mode, e);
		}
	}

	@Override
	public String toString() {
		return "StandardScript{" +
			"fileHandler=" + fileHandler +
			", reservedSymbols=" + reservedSymbols +
			'}';
	}
}
