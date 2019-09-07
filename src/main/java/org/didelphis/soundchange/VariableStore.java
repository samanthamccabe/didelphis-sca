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

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.didelphis.language.automata.Regex;
import org.didelphis.language.parsing.FormatterMode;
import org.didelphis.language.parsing.ParseException;
import org.didelphis.language.parsing.Segmenter;
import org.didelphis.soundchange.parser.ParserTerms;
import org.didelphis.utilities.Templates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class {@code VariableStore}
 *
 * @since 0.0.0
 */
@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VariableStore {

	private static final Regex EQUALS_PATTERN    = new Regex("\\s*=\\s*");
	private static final Regex DELIMITER_PATTERN = new Regex("\\s+");

	final Map<String, List<String>> variables;
	Segmenter segmenter;

	public VariableStore(Segmenter segmenter) {
		this.segmenter = segmenter;
		variables = new LinkedHashMap<>();
	}

	public VariableStore(VariableStore otherStore) {
		segmenter = otherStore.segmenter;
		variables = new HashMap<>(otherStore.variables);
	}

	public VariableStore() {
		this(FormatterMode.NONE);
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

	public void add(String command) {
		List<String> parts = EQUALS_PATTERN.split(command.trim());

		if (parts.size() == 2) {
			String key = parts.get(0);
			List<String> elements = DELIMITER_PATTERN.split(parts.get(1));
			List<String> expanded = new ArrayList<>();
			for (String value : elements) {
				if (ParserTerms.SPECIAL.matches(value)) {
					throw new ParseException("Illegal value " + value);
				}
				expanded.addAll(expandVariables(value));
			}
			variables.put(key, expanded);
		} else {
			String message = Templates.create()
					.add("Variable definition can only contain one = sign.")
					.data(command)
					.build();
			throw new ParseException(message);
		}
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

		// Pass empty delimiters
		Map<String, String> delimiters = Collections.emptyMap();
		list.add(segmenter.split(element, getKeys(), delimiters));

		boolean modified;
		do {
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
		} while (modified);

		return list.stream()
				.map(strings -> {
					StringBuilder sb = new StringBuilder();
					for (String string: strings) {
						sb.append(string);
					}
					return sb.toString();
				})
				.collect(Collectors.toList());
	}
}
