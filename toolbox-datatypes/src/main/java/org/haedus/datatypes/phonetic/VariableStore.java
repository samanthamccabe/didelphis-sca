package org.haedus.datatypes.phonetic;

import org.haedus.datatypes.Segmenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: goats
 * Date: 9/23/13
 * Time: 11:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class VariableStore {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(VariableStore.class);

	private final FeatureModel                model;
	private final Map<String, List<Sequence>> variables;

	public VariableStore() {
		model     = new FeatureModel();
		variables = new HashMap<String, List<Sequence>>();
	}

	public VariableStore(FeatureModel featureModel) {
		model     = featureModel;
		variables = new HashMap<String, List<Sequence>>();
	}

	public VariableStore(VariableStore otherStore) {
		variables = new HashMap<String, List<Sequence>>();
		variables.putAll(otherStore.variables);
		model = otherStore.model;
	}

	public List<Sequence> get(String key) {
		return variables.get(key);
	}

	public void put(String key, Iterable<String> values, boolean useSegmentation) {
		List<Sequence> expanded = new ArrayList<Sequence>();
		for (String value : values) {
			expanded.addAll(expandVariables(value, useSegmentation));
		}
		variables.put(key, expanded);
	}

	// testing only
	public void put(String key, String[] values) {
		put(key, values, true);
	}

	public void put(String key, String[] values, boolean useSegmentation) {
		Collection<String> list = new ArrayList<String>();
		Collections.addAll(list, values);
		put(key, list, useSegmentation);
	}

	public List<Sequence> expandVariables(String element, boolean useSegmentation) {
		List<Sequence> list = new ArrayList<Sequence>();
		List<Sequence> swap = new ArrayList<Sequence>();

		if (useSegmentation) {
			Sequence sequence = Segmenter.getSequence(element, model, this);
			list.add(sequence);
		} else {
			// TODO: except, this contains model too...
			Sequence sequence = Segmenter.getSequenceNaively(element, model, this);
			list.add(sequence);
		}

		// Find a thing that might be a variable
		boolean wasModified = true;
		while (wasModified) {
			wasModified = false;
			for (Sequence sequence : list) {
				for (int i = 0; i < sequence.size(); i++) {
					String symbol = getBestMatch(sequence.getSubsequence(i));
					if (contains(symbol)) {
						Sequence best;
						if (useSegmentation) {
							best = Segmenter.getSequence(symbol, model, this);
						} else {
							best = Segmenter.getSequenceNaively(symbol, model, this);
						}
						for (Sequence terminal : get(best)) {
							swap.add(sequence.replaceFirst(best, terminal));
						}
					}
				}
			}
			if (!swap.isEmpty()) {
				list = swap;
				swap = new ArrayList<Sequence>();
				wasModified = true;
			}
		}
		return list;
	}

	private String getBestMatch(Sequence tail) {
		String bestMatch = "";
		for (String key : getKeys()) {
			if (tail.toString().startsWith(key) && bestMatch.length() < key.length()) {
				bestMatch = key;
			}
		}
		return bestMatch;
	}

	public Set<String> getKeys() {
		if (variables.isEmpty()) {
			return new HashSet<String>();
		} else {
			return variables.keySet();
		}
	}

	public List<Sequence> get(Sequence sequence) {
		return get(sequence.toString());
	}

	public boolean contains(String symbol) {
		return variables.containsKey(symbol);
	}

	public boolean contains(Sequence symbol) {
		return contains(symbol.toString());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Map.Entry<String, List<Sequence>> entry : variables.entrySet()) {
			sb.append(entry.getKey());
			sb.append(" =");
			for (Sequence sequence : entry.getValue()) {
				sb.append(" ");
				sb.append(sequence);
			}
			sb.append("\n");
		}
		return sb.toString().trim();
	}
}
