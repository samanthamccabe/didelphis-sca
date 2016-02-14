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

package org.haedus.phonetic;

import org.haedus.enums.FormatterMode;
import org.haedus.exceptions.VariableDefinitionFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Samantha Fiona Morrigan McCabe
 * Date: 9/23/13
 * Time: 11:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class VariableStore {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(VariableStore.class);

	private static final int     INITIAL_CAPACITY  = 20;
	private static final Pattern EQUALS_PATTERN    = Pattern.compile("\\s*=\\s*");
	private static final Pattern DELIMITER_PATTERN = Pattern.compile("\\s+");

	private final Map<String, List<String>> variables;

	public VariableStore() {
		variables = new LinkedHashMap<String, List<String>>(INITIAL_CAPACITY);
	}

	public VariableStore(VariableStore otherStore) {
		variables = new HashMap<String, List<String>>(otherStore.variables);
	}

	public boolean isEmpty() {
		return variables.isEmpty();
	}

	@Deprecated // User should prefer contains(String)
	public boolean contains(Sequence symbol) {
		return contains(symbol.toString());
	}

	public boolean contains(String symbol) {
		return variables.containsKey(symbol);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Map.Entry<String, List<String>> entry : variables.entrySet()) {
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

	public void add(String command) throws VariableDefinitionFormatException {
		String[] parts = EQUALS_PATTERN.split(command.trim());

		if (parts.length == 2) {
			String key = parts[0];
			String[] elements = DELIMITER_PATTERN.split(parts[1]);

			List<String> expanded = new ArrayList<String>();
			for (String value : elements) {
				expanded.addAll(expandVariables(value));
			}
			variables.put(key, expanded);
		} else {
			throw new VariableDefinitionFormatException(command);
		}
	}

	public void addAll(VariableStore variableStore) {
		variables.putAll(variableStore.variables);
	}

	private Collection<String> expandVariables(String element) {
		List<List<String>> list = new ArrayList<List<String>>();
		List<List<String>> swap = new ArrayList<List<String>>();

		List<String> segmentedString = Segmenter.getSegmentedString(element, getKeys(), FormatterMode.NONE);
		list.add(segmentedString);

		boolean modified = true;
		while (modified) {
			modified = false;
			for (List<String> strings : list) {
				for (int i = 0; i < strings.size(); i++) {
					String string = strings.get(i);

					if (contains(string)) {
						modified = true;
						for (String s : get(string)) {
							List<String> newList = new ArrayList<String>(strings);
							newList.set(i, s);
							swap.add(newList);
						}
						break;
					}
				}
			}
			if (!swap.isEmpty()) {
				list = swap;
				swap = new ArrayList<List<String>>();
			}
		}

		Collection<String> expansions = new ArrayList<String>();
		for (List<String> strings : list) {
			StringBuilder sb = new StringBuilder(strings.size());
			for (String string : strings) {
				sb.append(string);
			}
			expansions.add(sb.toString());
		}

		return expansions;
	}

	public Set<String> getKeys() {
		if (variables.isEmpty()) {
			return new HashSet<String>();
		} else {
			return variables.keySet();
		}
	}

	public List<String> get(String key) {
		return variables.get(key);
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
