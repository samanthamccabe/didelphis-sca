package org.haedus.soundchange;

import org.haedus.datatypes.phonetic.FeatureModel;
import org.haedus.datatypes.phonetic.Segment;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.VariableStore;
import org.haedus.soundchange.exceptions.RuleFormatException;
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
 * User: goats
 * Date: 4/18/13
 * Time: 11:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class SoundChangeApplier {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(SoundChangeApplier.class);

	private static final String COMMENT_STRING = "%";
	private static final String NORMALIZATION  = "NORMALIZATION";
	private static final String SEGMENTATION   = "SEGMENTATION";

	private final FeatureModel model;
	private final Queue<Rule>  rules;

	private final Map<String, List<Sequence>> lexicons;

	// Adjustable Flags (non-final), with defaults
	private boolean        useSegmentation = true;
	private NormalizerMode normalizerMode  = NormalizerMode.NFD;
	private VariableStore  variables;

	public SoundChangeApplier() {
		rules     = new ArrayDeque<Rule>();
		model     = new FeatureModel();
		lexicons  = new HashMap<String, List<Sequence>>();
		variables = new VariableStore();
	}

	public SoundChangeApplier(String script) throws RuleFormatException {
		this(script.split("\\r?\\n"));
	}

    // Package-private: for tests only
	SoundChangeApplier(Iterable<String> rules) throws RuleFormatException {
		this();
		parse(rules);
	}

	// Package-private: for tests only
	SoundChangeApplier(String[] array) throws RuleFormatException {
		this();
		Collection<String> list = new ArrayList<String>();
		Collections.addAll(list, array);
		parse(list);
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

	public List<Sequence> processLexicon(Iterable<String> list) throws RuleFormatException {

		List<Sequence> lexicon = new ArrayList<Sequence>();
		lexicons.put("DEFAULT", lexicon);

		for (String item : list) {
			String word = normalize(item);

			Sequence sequence;
			if (useSegmentation) {
				sequence = new Sequence(word, model, variables);
			} else { // No Segmentation
				sequence = new Sequence();
				for (int i = 0; i < word.length(); i++) {
					sequence.add(new Segment(word.substring(i, i+1)));
				}
			}
			lexicon.add(sequence);
		}
		// Should test later if this is better than for-each
		while (!rules.isEmpty()) {
			rules.remove().execute(this);
		}
		return lexicon;
	}

	private void parse(Iterable<String> commands) throws RuleFormatException {
		for (String command : commands) {
			if (!command.startsWith(COMMENT_STRING)) {
                String trimmedCommand = command.replaceAll(COMMENT_STRING + ".*", "");
                String cleanedCommand = normalize(trimmedCommand);

				if (cleanedCommand.contains("=")) {
					String[] parts    = cleanedCommand.trim().split("\\s+=\\s+");
					String   key      = parts[0];
					String[] elements = parts[1].split("\\s+");

					variables = new VariableStore(variables);
					variables.put(key, elements, useSegmentation);
				} else if (cleanedCommand.contains(">")) {
					rules.add(new Rule(cleanedCommand, variables, useSegmentation));
				} else if (cleanedCommand.startsWith("USE ")) {

					String use = cleanedCommand.replaceAll("^USE ", "").toUpperCase();
					if (use.startsWith(NORMALIZATION)) {
						setNormalization(use);
					} else if (use.startsWith(SEGMENTATION)) {
						setSegmentation(use);
					}
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

	private void setNormalization(String command) throws RuleFormatException {
		String mode = command.replaceAll(NORMALIZATION + ": *", "");

         try {
             normalizerMode = NormalizerMode.valueOf(mode);
         } catch (IllegalArgumentException e) {
             throw new RuleFormatException("Invalid Command: no such normalization mode \"" + mode + "\"");
         }
	}

	private void setSegmentation(String command) throws RuleFormatException {
		String mode = command.replaceAll(SEGMENTATION + ": *", "");

		if (mode.startsWith("FALSE")) {
			useSegmentation = false;
		} else if (mode.startsWith("TRUE")) {
			useSegmentation = true;
		} else {
			throw new RuleFormatException("Unrecognized segmentation mode \"" + mode + "\"");
		}
	}
}
