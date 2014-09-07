/*******************************************************************************
 * Copyright (c) 2014 Haedus - Fabrica Codicis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.haedus.soundchange;

import org.haedus.datatypes.Segmenter;
import org.haedus.datatypes.phonetic.FeatureModel;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.VariableStore;
import org.haedus.exceptions.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.Normalizer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * User: Samantha Fiona Morrigan McCabe
 * Date: 4/18/13
 * Time: 11:46 PM
 */
public class SoundChangeApplier {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(SoundChangeApplier.class);

	private static final String COMMENT_STRING = "%";
	private static final String NORMALIZATION  = "NORMALIZATION";
	private static final String SEGMENTATION   = "SEGMENTATION";

	private final FeatureModel model;
	private final Queue<Rule>  rules;

	private final Map<String, List<Sequence>> lexicons;    // Key: File-Handle; Value: List of Words
//	private final Map<String, String>         outputPaths; // Key: File-Handle; Value: File Path

	// Adjustable Flags (non-final), with defaults
	private boolean        useSegmentation = true;
	private NormalizerMode normalizerMode  = NormalizerMode.NFD;
	private VariableStore variables;

	public SoundChangeApplier() {
		rules     = new ArrayDeque<Rule>();
		model     = new FeatureModel();
		lexicons  = new HashMap<String, List<Sequence>>();
		variables = new VariableStore(model); // TODO: indicative of excess coupling
	}

	public SoundChangeApplier(String script) throws ParseException {
		this(script.split("\\s*(\\r?\\n|\\r)\\s*")); // Splits newlines and removes padding whitespace
	}

	// Package-private: for tests only
	SoundChangeApplier(Iterable<String> rules) throws ParseException {
		this();
		parse(rules);
	}

	// Package-private: for tests only
	SoundChangeApplier(String[] array) throws ParseException {
		this();
		Collection<String> list = new ArrayList<String>();
		Collections.addAll(list, array);
		parse(list);
	}

	// Package-private, testing only
	FeatureModel getFeatureModel() {
		return model;
	}

	public VariableStore getVariables() {
		return variables;
	}

	public Collection<List<Sequence>> getLexicons() {
		return lexicons.values();
	}

	public NormalizerMode getNormalizerMode() {
		return normalizerMode;
	}

	public boolean usesSegmentation() {
		return useSegmentation;
	}

	public List<Sequence> processLexicon(Iterable<String> list) throws ParseException {

		List<Sequence> lexicon = new ArrayList<Sequence>();
		lexicons.put("DEFAULT", lexicon);
		for (String item : list) {
			String word = normalize(item);
			Sequence sequence;
			if (useSegmentation) {
				sequence = Segmenter.getSequence(word, model, variables);
			} else {
				sequence = Segmenter.getSequenceNaively(word, model, variables);
			}
			lexicon.add(sequence);
		}
		// Should test later if this is better than for-each
		while (!rules.isEmpty()) {
			rules.remove().execute(this);
		}
		return lexicon;
	}

	private void parse(Iterable<String> commands) throws ParseException {
		for (String command : commands) {
			if (!command.startsWith(COMMENT_STRING) && !command.isEmpty()) {
                String trimmedCommand = command.replaceAll(COMMENT_STRING + ".*", "");
                String cleanedCommand = normalize(trimmedCommand);

				if (cleanedCommand.contains("=")) {
					variables = new VariableStore(variables);
					variables.add(cleanedCommand, useSegmentation);
				} else if (cleanedCommand.contains(">")) {
					rules.add(new Rule(cleanedCommand, variables, useSegmentation));
				} else if (cleanedCommand.startsWith("NORMALIZATION")) {
					setNormalization(cleanedCommand);
				} else if (cleanedCommand.startsWith("SEGMENTATION")) {
					setSegmentation(cleanedCommand);
				} else if (cleanedCommand.startsWith("RESERVE")) {
					String reserve = cleanedCommand.replaceAll("RESERVE:? *", "");
					String[] symbols = reserve.split(" +");

					for (String symbol : symbols) {
						model.addSegment(symbol);
					}
				} else if (cleanedCommand.startsWith("BREAK")) {
					// Stop parsing rules
					break;
				} else {
					LOGGER.warn("Unrecognized Command: {}", command);
				}
			}
		}
	}

    private String normalize(String string) {
        if (normalizerMode == NormalizerMode.NONE) {
            return string;
        } else {
            return Normalizer.normalize(string, Normalizer.Form.valueOf(normalizerMode.toString()));
        }
    }

	private void setNormalization(String command) throws ParseException {
		String mode = command.replaceAll(NORMALIZATION + ": *", "");
         try {
             normalizerMode = NormalizerMode.valueOf(mode);
         } catch (IllegalArgumentException e) {
             throw new ParseException("Invalid Command: no such normalization mode \"" + mode + "\"");
         }
	}

	private void setSegmentation(String command) throws ParseException {
		String mode = command.replaceAll(SEGMENTATION + ": *", "");
		if (mode.startsWith("FALSE")) {
			useSegmentation = false;
		} else if (mode.startsWith("TRUE")) {
			useSegmentation = true;
		} else {
			throw new ParseException("Unrecognized segmentation mode \"" + mode + "\"");
		}
	}
}
