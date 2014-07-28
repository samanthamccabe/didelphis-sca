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

	private static final String NORMALIZATION = "NORMALIZATION";
	private static final String SEGMENTATION  = "SEGMENTATION";

	private final FeatureModel model;
	private final Queue<Rule>  rules;

	private final Map<String, List<Sequence>> lexicons;

	// Adjustable Flags (non-final)
	private boolean         useSegmentation = true;
	private Normalizer.Form normalizerForm  = Normalizer.Form.NFD; // By Default
	private VariableStore   variables;


	public SoundChangeApplier() {
		rules     = new ArrayDeque<Rule>();
		model     = new FeatureModel();
		lexicons  = new HashMap<String, List<Sequence>>();
		variables = new VariableStore();
	}

	public SoundChangeApplier(String script) throws RuleFormatException {
		this(script.split("\\r?\\n"));
	}

	// Package-private
	SoundChangeApplier(Iterable<String> rules) throws RuleFormatException {
		this();
		parse(rules);
	}

	// Package-private
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

	public List<Sequence> processLexicon(Iterable<String> list) throws RuleFormatException {

		List<Sequence> lexicon = new ArrayList<Sequence>();
		lexicons.put("DEFAULT", lexicon);

		for (String item : list) {
			String word = Normalizer.normalize(item, normalizerForm);

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
				String cleanCommand = command.replaceAll(COMMENT_STRING + ".*", "");

				if (cleanCommand.contains("=")) {
					String[] commandParts = cleanCommand.trim().split("\\s+=\\s+");
					String   key          = commandParts[0];
					String[] elements     = commandParts[1].split("\\s+");

					// TODO: when parsing rules, normalization and segmentation have to be used
					variables = new VariableStore(variables);
					variables.add(key, elements);
				} else if (cleanCommand.contains(">")) {
					rules.add(new Rule(command, variables));
				} else if (cleanCommand.startsWith("USE ")) {
					String use = cleanCommand.replaceAll("^USE ", "").toUpperCase();
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

	private void setNormalization(String use) throws RuleFormatException {
		use = use.replaceAll(NORMALIZATION + ": *", "");
		try {
			normalizerForm = Normalizer.Form.valueOf(use);
		}
		catch (IllegalArgumentException e) {
			throw new RuleFormatException("Invalid Command: no such normalization mode \"" + use + "\"");
		}
	}

	private void setSegmentation(String use) throws RuleFormatException {
		use = use.replaceAll(SEGMENTATION + ": *", "");
		if (use.startsWith("FALSE")) {
			useSegmentation = false;
		} else if (use.startsWith("TRUE")) {
			useSegmentation = true;
		} else {
			throw new RuleFormatException("Unrecognized segmentation mode \"" + use + "\"");
		}
	}
}
