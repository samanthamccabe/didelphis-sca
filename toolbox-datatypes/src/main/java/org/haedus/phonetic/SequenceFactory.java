/**
 * ****************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ****************************************************************************
 */

package org.haedus.phonetic;

import org.haedus.enums.FormatterMode;
import org.haedus.machines.Expression;
import org.haedus.phonetic.features.FeatureArray;
import org.haedus.phonetic.features.StandardFeatureArray;
import org.haedus.phonetic.model.FeatureModel;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

	private static final SequenceFactory EMPTY_FACTORY  = new SequenceFactory();

	private static final Pattern BACKREFERENCE_PATTERN = Pattern.compile("(\\$[^\\$]*\\d+)");

	private final FeatureModel  featureModel;
	private final VariableStore variableStore;
	private final FormatterMode formatterMode;
	private final Set<String>   reservedStrings;

	private final Segment  dotSegment;
	private final Segment  borderSegment;

	private final Sequence dotSequence;
	private final Sequence borderSequence;

	private SequenceFactory() {
		this(FeatureModel.EMPTY_MODEL, new VariableStore(), new HashSet<String>(), FormatterMode.NONE);
	}

	public SequenceFactory(FormatterMode modeParam) {
		this(FeatureModel.EMPTY_MODEL, new VariableStore(), new HashSet<String>(), modeParam);
	}

	public SequenceFactory(FeatureModel modelParam, FormatterMode modeParam) {
		this(modelParam, new VariableStore(), new HashSet<String>(), modeParam);
	}

	public SequenceFactory(FeatureModel model, VariableStore store, Set<String> reserved, FormatterMode mode) {
		featureModel    = model;
		variableStore   = store;
		reservedStrings = reserved;
		formatterMode   = mode;

//		List<Double> featureArray = Collections.unmodifiableList(featureModel.getBlankArray());
		FeatureArray<Double> featureArray = new StandardFeatureArray<Double>(featureModel.getBlankArray());

		dotSegment    = new Segment(".", featureArray, featureModel);
		borderSegment = new Segment("#", featureArray, featureModel);

		dotSequence    = new Sequence(dotSegment);
		borderSequence = new Sequence(borderSegment);
	}

	public static SequenceFactory getEmptyFactory() {
		return EMPTY_FACTORY;
	}

	public void reserve(String string) {
		reservedStrings.add(string);
	}

	public Segment getDotSegment() {
		return dotSegment;
	}

	public Segment getBorderSegment() {
		return borderSegment;
	}

	public Sequence getDotSequence() {
		return dotSequence;
	}

	public Sequence getBorderSequence() {
		return borderSequence;
	}

	public Segment getSegment(String string) {
		if (string.equals("#")) {
			return borderSegment;
		} else if (string.equals(".")) {
			return dotSegment;
		} else {
			return Segmenter.getSegment(string, featureModel, reservedStrings, formatterMode);
		}
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
		if (word.equals("#")) {
			Sequence sequence = new Sequence(featureModel);
			sequence.add(borderSegment);
			return sequence;
		} else if (word.equals(".")) {
			Sequence sequence = new Sequence(featureModel);
			sequence.add(dotSegment);
			return sequence;
		} else {
			Collection<String> keys = new ArrayList<String>();
			keys.addAll(variableStore.getKeys());
			keys.addAll(reservedStrings);
			return Segmenter.getSequence(word, featureModel, keys, formatterMode);
		}
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

	public List<Expression> getExpressions(String string) {
		return Segmenter.getExpressions(string, getKeys(), formatterMode);
	}

	public List<String> getSegmentedString(String string) {
		return Segmenter.getSegmentedString(string, getKeys(), formatterMode);
	}

	public FeatureModel getFeatureModel() {
		return featureModel;
	}

	public VariableStore getVariableStore() {
		return variableStore;
	}

	public String getBestMatch(String tail) {

		Collection<String> keys = getKeys();

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


	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (null == obj) return false;
		if (getClass() != obj.getClass()) return false;

		SequenceFactory that = (SequenceFactory) obj;
		return featureModel.equals(that.featureModel) &&
						formatterMode == that.formatterMode &&
						reservedStrings.equals(that.reservedStrings) &&
						variableStore.equals(that.variableStore);
	}

	@Override
	public int hashCode() {
		int result = 1175;
		result = 31 * result + featureModel.hashCode();
		result = 31 * result + variableStore.hashCode();
		result = 31 * result + formatterMode.hashCode();
		result = 31 * result + reservedStrings.hashCode();
		return result;
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

	@NotNull
	private Collection<String> getKeys() {
		Collection<String> keys = new ArrayList<String>();
		keys.addAll(variableStore.getKeys());
		keys.addAll(featureModel.getSymbols());
		keys.addAll(reservedStrings);
		return keys;
	}
}
