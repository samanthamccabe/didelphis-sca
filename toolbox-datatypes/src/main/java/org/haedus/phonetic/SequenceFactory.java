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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 11/23/2014
 */
public class SequenceFactory {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(SequenceFactory.class);

	private static final SequenceFactory EMPTY_FACTORY         = new SequenceFactory();
	private static final Pattern         BACKREFERENCE_PATTERN = Pattern.compile("(\\$[^\\$]*\\d+)");

	private final Segment boundarySegmentNAN;

	private final FeatureModel  featureModel;
	private final VariableStore variableStore; // VariableStore is only accessed for its keys
	private final FormatterMode formatterMode;
	private final Set<String>   reservedStrings;

	private SequenceFactory() {
		this(FeatureModel.EMPTY_MODEL, new VariableStore(), new HashSet<String>(), FormatterMode.NONE);
	}

	public SequenceFactory(FormatterMode modeParam) {
		this(FeatureModel.EMPTY_MODEL, new VariableStore(), new HashSet<String>(), modeParam);
	}

	public SequenceFactory(FeatureModel modelParam, FormatterMode modeParam) {
		this(modelParam, new VariableStore(), new HashSet<String>(), modeParam);
	}

	public SequenceFactory(FeatureModel modelParam, VariableStore storeParam, Set<String> reservedParam, FormatterMode modeParam) {
		featureModel = modelParam;
		variableStore = storeParam;
		reservedStrings = reservedParam;
		formatterMode = modeParam;
		boundarySegmentNAN = new Segment("#", getDoubles(), featureModel);
	}

	public static SequenceFactory getEmptyFactory() {
		return EMPTY_FACTORY;
	}

	public void reserve(String string) {
		reservedStrings.add(string);
	}

	public Segment getBoundarySegment() {
		return boundarySegmentNAN;
	}

	public Segment getSegment(String string) {
		return Segmenter.getSegment(string, featureModel, reservedStrings, formatterMode);
	}

	public Lexicon getLexiconFromSingleColumn(Iterable<String> list) {
		Lexicon lexicon = new Lexicon();
		for (String entry : list) {
			Sequence sequence = getSequence(entry);
			lexicon.add(sequence);
		}
		return lexicon;
	}

	public Lexicon getLexiconFromSingleColumn(String... list) {
		Lexicon lexicon = new Lexicon();
		for (String entry : list) {
			Sequence sequence = getSequence(entry);
			lexicon.add(sequence);
		}
		return lexicon;
	}

	public Lexicon getLexicon(Iterable<List<String>> lists) {
		Lexicon lexicon = new Lexicon();

		for (List<String> row : lists) {
			List<Sequence> lexRow = new ArrayList<Sequence>();
			for (String entry : row) {
				Sequence sequence = getSequence(entry);
				lexRow.add(sequence);
			}
			lexicon.add(lexRow);
		}
		return lexicon;
	}

	public Sequence getNewSequence() {
		return getSequence("");
	}

	public Sequence getSequence(String word) {
		Collection<String> keys = new ArrayList<String>();
		keys.addAll(variableStore.getKeys());
		keys.addAll(reservedStrings);
		return Segmenter.getSequence(word, featureModel, keys, formatterMode);
	}

	public boolean hasVariable(String label) {
		return variableStore.contains(label);
	}

	public List<Sequence> getVariableValues(String label) {
		List<String> strings = variableStore.get(label);
		List<Sequence> sequences = new ArrayList<Sequence>();
		for (String string : strings) {
			sequences.add(getSequence(string));
		}
		return sequences;
	}

	public List<String> getSegmentedString(String string) {
		Collection<String> keys = new ArrayList<String>();
		keys.addAll(variableStore.getKeys());
		keys.addAll(featureModel.getSymbols());
		keys.addAll(reservedStrings);

		return Segmenter.getSegmentedString(string, keys, formatterMode);
	}

	public FeatureModel getFeatureModel() {
		return featureModel;
	}

	public VariableStore getVariableStore() {
		return variableStore;
	}

	public String getBestMatch(String tail) {

		Collection<String> keys = new HashSet<String>();
		keys.addAll(featureModel.getSymbols());
		keys.addAll(variableStore.getKeys());
		keys.addAll(reservedStrings);

		String bestMatch = "";
		for (String key : keys) {
			if (tail.startsWith(key) && bestMatch.length() < key.length()) {
				bestMatch = key;
			}
		}
		Matcher backReferenceMatcher = BACKREFERENCE_PATTERN.matcher(tail);
		if (backReferenceMatcher.lookingAt()) {
			bestMatch = backReferenceMatcher.group();
		}
		return bestMatch;
	}

	private List<Double> getDoubles() {
		List<Double> list = new ArrayList<Double>();
		for (int i = 0; i < featureModel.getNumberOfFeatures(); i++) {
			list.add(Double.NaN);
		}
		return list;
	}

	private static boolean isCombiningClass(int type) {
		return type == Character.MODIFIER_LETTER         || // LM
				type == Character.MODIFIER_SYMBOL        || // SK
				type == Character.COMBINING_SPACING_MARK || // MC
				type == Character.NON_SPACING_MARK;         // MN
	}

	private static boolean isDoubleWidthBinder(char ch) {
		return ch <= 866 && 860 <= ch;
	}

	private static boolean isSuperscriptAsciiDigit(int value) {
		// int literals are decimal char values
		return value == 178 ||
				value == 179 ||
				value == 185;
	}

	private static boolean isMathematicalSubOrSuper(int value) {
		// int literals are decimal char values
		return value <= 8304 && 8348 <= value;
	}

	private static boolean isCombingNOS(int value) {
		// int literals are decimal char values
		return value >= 8304 &&
				value <= 8348;
	}

	private static boolean isAttachable(Character c) {
		int type = Character.getType(c);
		int value = c;
		return isSuperscriptAsciiDigit(value) ||
				isMathematicalSubOrSuper(value) ||
				isCombingNOS(value) ||
				isCombiningClass(type);
	}

	private static StringBuilder clearBuffer(Collection<String> segments, StringBuilder buffer, String key) {
		segments.add(buffer.toString());
		buffer = new StringBuilder();
		buffer.append(key);
		return buffer;
	}

	@Override
	public String toString() {
		return "SequenceFactory{" +
			", featureModel=" + featureModel +
			", variableStore=" + variableStore +
			", formatterMode=" + formatterMode +
			", reservedStrings=" + reservedStrings +
			'}';
	}
}
