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

package org.haedus.datatypes.phonetic;

import org.haedus.datatypes.NormalizerMode;
import org.haedus.datatypes.SegmentationMode;
import org.haedus.datatypes.Segmenter;
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

	private final Map<String, List<Sequence>> variables;

	// FeatureModel and SegmentationMode is ONLY called in order to use the Segmenter
	private final FeatureModel     featureModel;
	private final SegmentationMode segmentationMode;
	private final NormalizerMode   normalizerMode;

	public VariableStore() {
		this(FeatureModel.EMPTY_MODEL, SegmentationMode.DEFAULT, NormalizerMode.NFD);

	}

	public VariableStore(FeatureModel modelParam) {
		this(modelParam, SegmentationMode.DEFAULT, NormalizerMode.NFD);
	}

	public VariableStore(FeatureModel modelParam, SegmentationMode segParam, NormalizerMode normParam) {
		segmentationMode = segParam;
		normalizerMode   = normParam;
		featureModel     = modelParam;
		variables        = new LinkedHashMap<String, List<Sequence>>(INITIAL_CAPACITY);
	}

	public VariableStore(VariableStore otherStore) {
		variables = new HashMap<String, List<Sequence>>(otherStore.variables);

		featureModel     = otherStore.featureModel;
		segmentationMode = otherStore.segmentationMode;
		normalizerMode   = otherStore.normalizerMode;
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

		for (Map.Entry<String, List<Sequence>> entry : variables.entrySet()) {
			sb.append(entry.getKey());
			sb.append(" =");
			for (Sequence sequence : entry.getValue()) {
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

			List<Sequence> expanded = new ArrayList<Sequence>();
			for (String value : elements) {
				expanded.addAll(expandVariables(value));
			}
			variables.put(key, expanded);
		} else {
			throw new VariableDefinitionFormatException(command);
		}
	}

	private Collection<Sequence> expandVariables(String element) {
		List<Sequence> list = new ArrayList<Sequence>();
		List<Sequence> swap = new ArrayList<Sequence>();

		list.add(Segmenter.getSequence(element, featureModel, getKeys(), segmentationMode, normalizerMode));

		// Find a thing that might be a variable
		boolean wasModified = true;
		while (wasModified) {
			wasModified = false;
			for (Sequence sequence : list) {
				for (int i = 0; i < sequence.size(); i++) {
					String symbol = getBestMatch(sequence.getSubsequence(i));
					if (contains(symbol)) {
						Sequence best = Segmenter.getSequence(element, featureModel, getKeys(), segmentationMode, normalizerMode);
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

	@Deprecated
	public List<Sequence> get(Sequence sequence) {
		return get(sequence.toString());
	}

	public Set<String> getKeys() {
		if (variables.isEmpty()) {
			return new HashSet<String>();
		} else {
			return variables.keySet();
		}
	}

	public List<Sequence> get(String key) {
		return variables.get(key);
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
}
