package org.haedusfc.soundchange;

import org.haedusfc.datatypes.phonetic.FeatureModel;
import org.haedusfc.datatypes.phonetic.Sequence;
import org.haedusfc.soundchange.exceptions.RuleFormatException;
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

	private final FeatureModel  model;
	private final Queue<Rule>   rules;

	private final Map<String, List<Sequence>> lexicons;

	private VariableStore variables;
	private Normalizer.Form normalizerMode;

	public SoundChangeApplier() {
		rules     = new ArrayDeque<Rule>();
		model     = new FeatureModel();
		lexicons  = new HashMap<String, List<Sequence>>();
		variables = new VariableStore();
	}

	public SoundChangeApplier(String script) throws RuleFormatException {
		this(script.split("\\r?\\n"));
	}

	public SoundChangeApplier(Iterable<String> rules) throws RuleFormatException {
		this();
		parse(rules);
	}

	public SoundChangeApplier(String[] array) throws RuleFormatException {
		this();
		Collection<String> list = new ArrayList<String>();
		Collections.addAll(list,array);
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
			lexicon.add(new Sequence(item, model, variables));
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

					variables = new VariableStore(variables);
					variables.add(key, elements);
				} else if (cleanCommand.contains(">")) {
					rules.add(new Rule(command, variables));
				} else {
					// TODO:
					LOGGER.warn("Unreckognized Command: {}", command);
				}
			}
		}
	}
}
