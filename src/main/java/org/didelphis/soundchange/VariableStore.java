/*=============================================================================
 = Copyright (c) 2017. Samantha Fiona McCabe (Didelphis)
 =
 = Licensed under the Apache License, Version 2.0 (the "License");
 = you may not use this file except in compliance with the License.
 = You may obtain a copy of the License at
 =     http://www.apache.org/licenses/LICENSE-2.0
 = Unless required by applicable law or agreed to in writing, software
 = distributed under the License is distributed on an "AS IS" BASIS,
 = WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 = See the License for the specific language governing permissions and
 = limitations under the License.
 =============================================================================*/

package org.didelphis.soundchange;

import org.didelphis.language.exceptions.ParseException;
import org.didelphis.language.phonetic.Segmenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Samantha Fiona McCabe
 * Date: 9/23/13
 * Time: 11:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class VariableStore {

	private static final Logger LOG = LoggerFactory.getLogger(
			VariableStore.class);

	private static final int INITIAL_CAPACITY = 20;

	private static final Pattern EQUALS_PATTERN = Pattern.compile("\\s*=\\s*");
	private static final Pattern DELIMITER_PATTERN = Pattern.compile("\\s+");

	private final Map<String, List<String>> variables;
	private Segmenter segmenter;

	public VariableStore(Segmenter segmenter) {
		this.segmenter = segmenter;
		variables = new LinkedHashMap<>(INITIAL_CAPACITY);
	}

	public VariableStore(VariableStore otherStore) {
		segmenter = otherStore.segmenter;
		variables = new HashMap<>(otherStore.variables);
	}

	public Segmenter getSegmenter() {
		return segmenter;
	}

	public void setSegmenter(Segmenter segmenter) {
		this.segmenter = segmenter;
	}

	public boolean isEmpty() {
		return variables.isEmpty();
	}

	public boolean contains(String symbol) {
		return variables.containsKey(symbol);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Entry<String, List<String>> entry : variables.entrySet()) {
			sb.append(entry.getKey());
			sb.append(" =");
			for (String sequence : entry.getValue()) {
				sb.append(' ');
				sb.append(sequence);
			}
			sb.append('\n');
		}
		return sb.toString().trim();
	}

	public void add(String command) {
		String[] parts = EQUALS_PATTERN.split(command.trim());

		if (parts.length == 2) {
			String key = parts[0];
			String[] elements = DELIMITER_PATTERN.split(parts[1]);

			List<String> expanded = new ArrayList<>();
			for (String value : elements) {
				expanded.addAll(expandVariables(value));
			}
			variables.put(key, expanded);
		} else {
			throw new ParseException(
					"Variable definition can only contain one = sign.",
					command);
		}
	}

	public void addAll(VariableStore variableStore) {
		variables.putAll(variableStore.variables);
	}

	public Set<String> getKeys() {
		return variables.isEmpty() ? new HashSet<>() : variables.keySet();
	}

	public List<String> get(String key) {
		return variables.get(key);
	}

	private Collection<String> expandVariables(String element) {
		List<List<String>> list = new ArrayList<>();
		List<List<String>> swap = new ArrayList<>();

		list.add(segmenter.split(element, getKeys()));

		boolean modified = true;
		while (modified) {
			modified = false;
			for (List<String> strings : list) {
				for (int i = 0; i < strings.size(); i++) {
					String string = strings.get(i);

					if (contains(string)) {
						modified = true;
						for (String s : get(string)) {
							List<String> newList = new ArrayList<>(strings);
							newList.set(i, s);
							swap.add(newList);
						}
						break;
					}
				}
			}
			if (!swap.isEmpty()) {
				list = swap;
				swap = new ArrayList<>();
			}
		}

		Collection<String> expansions = new ArrayList<>();
		for (List<String> strings : list) {
			StringBuilder sb = new StringBuilder(strings.size());
			for (String string : strings) {
				sb.append(string);
			}
			expansions.add(sb.toString());
		}

		return expansions;
	}

	private String getBestMatch(String tail) {
		String bestMatch = "";
		for (String key : getKeys()) {
			if (tail.startsWith(key) && bestMatch.length() < key.length()) {
				bestMatch = key;
			}
		}
		return bestMatch;
	}
}
