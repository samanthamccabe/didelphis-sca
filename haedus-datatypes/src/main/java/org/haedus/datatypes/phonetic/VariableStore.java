package org.haedus.datatypes.phonetic;

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

	private final FeatureModel model;
	private final Map<String, List<Sequence>> assignmentMap;

	public VariableStore() {
		model         = new FeatureModel();
		assignmentMap = new HashMap<String, List<Sequence>>();
//		orderedKeys   = new ArrayList<String>();
	}

	public VariableStore(VariableStore otherStore) {
		assignmentMap = new HashMap<String, List<Sequence>>();
//		orderedKeys   = new ArrayList<String>();

		assignmentMap.putAll(otherStore.assignmentMap);
		model = otherStore.model;
	}

	public List<Sequence> get(String key) {
		return assignmentMap.get(key);
	}

	public void add(String key, Iterable<String> values) {

		List<Sequence> expanded = new ArrayList<Sequence>();

		for (String value : values) {
			expanded.addAll(expandVariables(value));
		}
		assignmentMap.put(key,expanded);
	}

	public void add(String key, String... values) {
		Collection<String> list = new ArrayList<String>();
		Collections.addAll(list,values);
		add(key,list);
	}

	public List<Sequence> expandVariables(String element) {
		List<Sequence> list = new ArrayList<Sequence>();
		List<Sequence> swap = new ArrayList<Sequence>();
		list.add(new Sequence(element,model,this));

		// Find a thing that might be a variable
		boolean wasModified = true;
		while (wasModified) {
			wasModified = false;

			for (Sequence sequence : list) {
				for (int i = 0; i < sequence.size(); i++) {

					String symbol = getBestMatch(sequence.getSubsequence(i));

					if (contains(symbol)) {
						Sequence best = new Sequence(symbol,model,this);
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
		if (assignmentMap.isEmpty()) {
			return new HashSet<String>();
		} else {
			return assignmentMap.keySet();
		}
	}

	public List<Sequence> get(Sequence sequence) {
		return get(sequence.toString());
	}

	public boolean contains(String symbol) {
		return assignmentMap.containsKey(symbol);
	}

	public boolean contains(Sequence symbol) {
		return contains(symbol.toString());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Map.Entry<String, List<Sequence>> entry : assignmentMap.entrySet()) {
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

	public void set(String key, List<String> elements)
	{
		List<Sequence> expanded = new ArrayList<Sequence>();

		for (String element : elements) {
			expanded.addAll(expandVariables(element));
		}
		assignmentMap.put(key, expanded);
	}
}
