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

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.haedus.datatypes.NormalizerMode;
import org.haedus.datatypes.SegmentationMode;
import org.haedus.datatypes.phonetic.FeatureModel;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.SequenceFactory;
import org.haedus.datatypes.phonetic.VariableStore;
import org.haedus.exceptions.ParseException;
import org.haedus.io.DiskFileHandler;
import org.haedus.io.FileHandler;
import org.haedus.io.NullFileHandler;
import org.haedus.soundchange.command.Command;
import org.haedus.soundchange.command.LexiconCloseCommand;
import org.haedus.soundchange.command.LexiconOpenCommand;
import org.haedus.soundchange.command.LexiconWriteCommand;
import org.haedus.soundchange.command.Rule;
import org.haedus.soundchange.command.ScriptExecuteCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.Normalizer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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

	private static final String NORMALIZATION = "NORMALIZATION";
	private static final String SEGMENTATION  = "SEGMENTATION";
	private static final String EXECUTE       = "EXECUTE";
	private static final String IMPORT        = "IMPORT";
	private static final String OPEN          = "OPEN";
	private static final String WRITE         = "WRITE";
	private static final String CLOSE         = "CLOSE";
	private static final String FILEHANDLE    = "([A-Z0-9_]+)";
	private static final String FILEPATH      = "[\"\']([^\"\']+)[\"\']";

	private static final Pattern CLOSE_PATTERN         = Pattern.compile(CLOSE + "\\s+" + FILEHANDLE + "\\s+(as\\s)?" + FILEPATH);
	private static final Pattern WRITE_PATTERN         = Pattern.compile(WRITE + "\\s+" + FILEHANDLE + "\\s+(as\\s)?" + FILEPATH);
	private static final Pattern SEGMENTATION_PATTERN  = Pattern.compile(SEGMENTATION + ": *");
	private static final Pattern NORMALIZATION_PATTERN = Pattern.compile(NORMALIZATION + ": *");
	private static final Pattern EXECUTE_PATTERN       = Pattern.compile(EXECUTE + "\\s+");
	private static final Pattern IMPORT_PATTERN        = Pattern.compile(IMPORT + "\\s+");
	private static final Pattern QUOTES_PATTERN        = Pattern.compile("\"|\'");

	private final FileHandler   fileHandler;
	private final FeatureModel  model;
	private final VariableStore variables;
	private final Set<String>   reservedSymbols;


	private SegmentationMode segmentationMode = SegmentationMode.DEFAULT;
	private NormalizerMode   normalizerMode   = NormalizerMode.NFD;

	public StandardScript() {
		model = FeatureModel.EMPTY_MODEL;
		variables = new VariableStore(model);
		fileHandler = new DiskFileHandler();
		reservedSymbols = new HashSet<String>();
	}

	public StandardScript(CharSequence script) {
		this(script, new DiskFileHandler());
	}

	@VisibleForTesting
	StandardScript(CharSequence script, FileHandler fileHandlerParam) {
		this(NEWLINE_PATTERN.split(script), fileHandlerParam); // Splits newlines and removes padding whitespace
	}

	@VisibleForTesting
	StandardScript(String[] array) {
		this(array, new NullFileHandler());
	}

	@VisibleForTesting
	StandardScript(String[] array, FileHandler fileHandlerParam) {
		model = FeatureModel.EMPTY_MODEL;
		variables = new VariableStore(model);
		fileHandler = fileHandlerParam;
		reservedSymbols = new HashSet<String>();

		Collection<String> list = new ArrayList<String>();
		Collections.addAll(list, array);
		parse(list);
	}

	public void addLexicon(String handle, Iterable<List<String>> lexicon) {
		List<List<Sequence>> sequences = getSequences(lexicon);
		lexicons.put(handle, sequences);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		StandardScript rhs = (StandardScript) obj;
		return new EqualsBuilder()
				.append(fileHandler, rhs.fileHandler)
				.append(model, rhs.model)
				.append(commands, rhs.commands)
				.append(variables, rhs.variables)
				.append(lexicons, rhs.lexicons)
				.append(segmentationMode, rhs.segmentationMode)
				.append(normalizerMode, rhs.normalizerMode)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(fileHandler)
				.append(model)
				.append(commands)
				.append(variables)
				.append(lexicons)
				.toHashCode();
	}

	@VisibleForTesting
	FeatureModel getFeatureModel() {
		return model;
	}

	@VisibleForTesting
	VariableStore getVariables() {
		return variables;
	}

	@VisibleForTesting
	boolean hasLexicon(String handle) {
		return lexicons.containsKey(handle);
	}

	@VisibleForTesting
	NormalizerMode getNormalizerMode() {
		return normalizerMode;
	}

	@VisibleForTesting
	SegmentationMode getSegmentationMode() {
		return segmentationMode;
	}

	@VisibleForTesting
	Collection<String> getReservedSymbols() {
		return reservedSymbols;
	}

	@VisibleForTesting
	List<Sequence> processLexicon(List<String> list) {
		Collection<List<String>> lex = new ArrayList<List<String>>();
		lex.add(list);

		List<List<Sequence>> lexicon = getSequences(lex);
		lexicons.put("DEFAULT", lexicon);
		// Should test later if this is better than for-each
		for (Command command : commands) {
			command.execute();
		}
		return lexicon.get(0);
	}

	private List<List<Sequence>> getSequences(Iterable<List<String>> list) {

		SequenceFactory factory = new SequenceFactory(model, variables, reservedSymbols, segmentationMode, normalizerMode);

		List<List<Sequence>> lexicon = new ArrayList<List<Sequence>>();
		for (List<String> line : list) {
			List<Sequence> sequences = new ArrayList<Sequence>();
			for (String item : line) {
				String word = normalize(item);
				Sequence sequence = factory.getSequence(word);
				sequences.add(sequence);
			}
			lexicon.add(sequences);
		}
		return lexicon;
	}

	private void parse(Iterable<String> strings) {

		for (String string : strings) {
			if (!string.startsWith(COMMENT_STRING) && !string.isEmpty()) {
				String trimmedCommand = COMMENT_PATTERN.matcher(string).replaceAll("");
				String command = normalize(trimmedCommand);
				if (command.startsWith(EXECUTE)) {
					executeScript(command);
				} else if (command.startsWith(IMPORT)) {
					importScript(command);
				} else if (command.startsWith(OPEN)) {
					openLexicon(command);
				} else if (command.startsWith(WRITE)) {
					writeLexicon(command);
				} else if (command.startsWith(CLOSE)) {
					closeLexicon(command);
				} else if (command.contains("=")) {
					variables.add(command);
				} else if (command.contains(">")) {
					// This is probably the correct scope; if other commands change the variables or segmentation mode,
					// we could get unexpected behavior if this is initialized outside the loop
					SequenceFactory factory = new SequenceFactory(model, new VariableStore(variables), reservedSymbols, segmentationMode, normalizerMode);
					commands.add(new Rule(command, lexicons, factory));
				} else if (command.startsWith(NORMALIZATION)) {
					setNormalization(command);
				} else if (command.startsWith(SEGMENTATION)) {
					setSegmentation(command);
				} else if (command.startsWith(RESERVE)) {
					String reserve = RESERVE_PATTERN.matcher(command).replaceAll("");
					Collections.addAll(reservedSymbols, WHITESPACE_PATTERN.split(reserve));
				} else if (command.startsWith("BREAK")) {
					// Stop parsing commands
					break;
				} else {
					LOGGER.warn("Unrecognized Command: {}", string);
				}
			}
		}
	}

	/**
	 * OPEN "some_lexicon.txt" (as) FILEHANDLE to load the contents of that file into a lexicon stored against the file-handle;
	 *
	 * @param command the whole command staring from OPEN, specifying the path and file-handle
	 */
	private void openLexicon(String command) {
		Pattern pattern = Pattern.compile(OPEN + "\\s+" + FILEPATH + "\\s+(as\\s)?" + FILEHANDLE);
		Matcher matcher = pattern.matcher(command);

		if (matcher.lookingAt()) {
			String path = matcher.group(1);
			String handle = matcher.group(3);
			commands.add(new LexiconOpenCommand(path, handle, fileHandler, this));
		} else {
			throw new ParseException("Command seems to be ill-formatted: " + command);
		}
	}

	/**
	 * CLOSE FILEHANDLE (as) "some_output2.txt" to close the file-handle and save the lexicon to the specified file.
	 *
	 * @param command the whole command starting from CLOSE, specifying the file-handle and path
	 * @throws org.haedus.exceptions.ParseException
	 */
	private void closeLexicon(String command) {
		Matcher matcher = CLOSE_PATTERN.matcher(command);
		if (matcher.lookingAt()) {
			String handle = matcher.group(1);
			String path = matcher.group(3);
			commands.add(new LexiconCloseCommand(lexicons, path, handle, fileHandler));
		} else {
			throw new ParseException("Command seems to be ill-formatted: " + command);
		}
	}

	/**
	 * WRITE FILEHANDLE (as) "some_output1.txt" to save the current state of the lexicon to the specified file, but leave the handle open
	 *
	 * @param command the whole command starting from WRITE, specifying the file-handle and path
	 * @throws org.haedus.exceptions.ParseException
	 */
	private void writeLexicon(String command) {
		Matcher matcher = WRITE_PATTERN.matcher(command);
		if (matcher.lookingAt()) {
			String handle = matcher.group(1);
			String path = matcher.group(3);
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
		String path = QUOTES_PATTERN.matcher(input).replaceAll("");
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

	/**
	 * Sets the normalization mode of the sound change applier
	 *
	 * @param command the whole command, beginning with NORMALIZATION
	 */
	private void setNormalization(CharSequence command) {
		String mode = NORMALIZATION_PATTERN.matcher(command).replaceAll("");
		try {
			normalizerMode = NormalizerMode.valueOf(mode);
		} catch (IllegalArgumentException e) {
			throw new ParseException(e);
		}
	}

	/**
	 * Sets the segmentation mode of the sound change applier
	 *
	 * @param command the whole command, beginning with SEGMENTATION
	 */
	private void setSegmentation(CharSequence command) {
		String mode = SEGMENTATION_PATTERN.matcher(command).replaceAll("");
		if (mode.startsWith("FALSE")) {
			segmentationMode = SegmentationMode.NAIVE;
		} else if (mode.startsWith("TRUE")) {
			segmentationMode = SegmentationMode.DEFAULT;
		} else {
			throw new ParseException("Unrecognized segmentation mode \"" + mode + '"');
		}
	}

	private String normalize(String string) {
		if (normalizerMode == NormalizerMode.NONE) {
			return string;
		} else {
			return Normalizer.normalize(string, Normalizer.Form.valueOf(normalizerMode.toString()));
		}
	}
}
