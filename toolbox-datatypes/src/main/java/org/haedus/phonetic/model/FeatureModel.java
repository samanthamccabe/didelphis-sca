/*******************************************************************************
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
 ******************************************************************************/

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.haedus.phonetic.model;

import org.haedus.enums.FormatterMode;
import org.haedus.exceptions.ParseException;
import org.haedus.phonetic.Segment;
import org.haedus.phonetic.features.FeatureArray;
import org.haedus.phonetic.features.SparseFeatureArray;
import org.haedus.phonetic.features.StandardFeatureArray;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Samantha Fiona Morrigan McCabe
 */
public class FeatureModel {
	
	private static final transient Logger LOGGER = LoggerFactory.getLogger(FeatureModel.class);

	public static final FeatureModel EMPTY_MODEL     = new FeatureModel();
	
	public static final Double UNDEFINED_VALUE = Double.NaN;
	@Deprecated
	public static final Double MASKING_VALUE   = Double.NEGATIVE_INFINITY;
	
	private static final Pattern VALUE_PATTERN   = Pattern.compile("(\\S+):(-?\\d)");
	private static final Pattern BINARY_PATTERN  = Pattern.compile("([\\+\\-])(\\S+)");
	private static final Pattern FEATURE_PATTERN = Pattern.compile("[,;]\\s?|\\s");

	private final Map<String, Integer>              featureNames;
	private final Map<String, Integer>              featureAliases;
	private final Map<String, FeatureArray<Double>> featureMap;
	private final Map<String, FeatureArray<Double>> diacritics;
	private final List<Constraint>                  constraints;

	private final List<Double> blankArray;

	public FormatterMode getFormatterMode() {
		return formatterMode;
	}

	private final FormatterMode formatterMode;

	// Initializes an empty model; access to this should only be through the EMPTY_MODEL field
	private FeatureModel() {
		featureNames   = new LinkedHashMap<String, Integer>();
		featureAliases = new LinkedHashMap<String, Integer>();
		featureMap     = new LinkedHashMap<String, FeatureArray<Double>>();
		diacritics     = new LinkedHashMap<String, FeatureArray<Double>>();
		constraints    = new ArrayList<Constraint>();
		blankArray     = new ArrayList<Double>();
		formatterMode  = FormatterMode.NONE;
	}

	public FeatureModel(InputStream stream, FormatterMode modeParam) {
		this(new FeatureModelLoader(stream, modeParam), modeParam);
	}

	public FeatureModel(File file, FormatterMode modeParam) {
		this(new FeatureModelLoader(file, modeParam), modeParam);
	}

	public FeatureModel(FeatureModelLoader loader, FormatterMode modeParam) {
		featureNames   = loader.getFeatureNames();
		featureAliases = loader.getFeatureAliases();
		featureMap     = loader.getFeatureMap();
		diacritics     = loader.getDiacritics();
		constraints    = loader.getConstraints();

		formatterMode = modeParam;

		blankArray = new ArrayList<Double>();
		for (int i = 0; i < featureNames.size(); i++) {
			blankArray.add(UNDEFINED_VALUE);
		}
	}

	@NotNull
	public static SparseFeatureArray<Double> getValueMap(
			String features,
			Map<String, Integer> aliases,
			Map<String, Integer> names) {

		String substring = features.substring(1, features.length() - 1);
		String[] array = FEATURE_PATTERN.split(substring);

		SparseFeatureArray<Double> arr =
				new SparseFeatureArray<Double>(names.size());

		for (String element : array) {
			Matcher valueMatcher  = VALUE_PATTERN.matcher(element);
			Matcher binaryMatcher = BINARY_PATTERN.matcher(element);

			if (valueMatcher.matches()) {
				String featureName  = valueMatcher.group(1);
				String featureValue = valueMatcher.group(2);
				Integer value = validate(featureName, features, aliases, names);
				arr.set(value, Double.valueOf(featureValue));
			} else if (binaryMatcher.matches()) {
				String featureName  = binaryMatcher.group(2);
				String featureValue = binaryMatcher.group(1);
				Integer value = validate(featureName, features, aliases, names);
				arr.set(value, featureValue.equals("+") ? 1.0 : -1.0);
			} else {
				throw new ParseException("Unrecognized feature \"" + element
						+ "\" in definition " + features);
			}
		}
		return arr;
	}

	public Segment getSegmentFromFeatures(String features) {

		FeatureArray<Double> map =
				getValueMap(features, featureAliases, featureNames);

		return new Segment(features, map, this);
	}

	public List<Constraint> getConstraints() {
		return Collections.unmodifiableList(constraints);
	}

	public String getBestSymbol(FeatureArray<Double> featureArray) {

		FeatureArray<Double> bestFeatures = null;
		String bestSymbol = "";
		double minimum = Double.MAX_VALUE;

		for (Map.Entry<String, FeatureArray<Double>> entry : featureMap.entrySet()) {
			FeatureArray<Double> features = entry.getValue();
			double difference = getDifferenceValue(featureArray, features);
			if (difference < minimum) {
				bestSymbol = entry.getKey();
				minimum = difference;
				bestFeatures = features;
			}
		}

		StringBuilder sb = new StringBuilder();
		if (minimum > 0.0) {
			Collection collection = getBestDiacritic(featureArray, bestFeatures);
			for (String diacritic : diacritics.keySet()) {
				if (collection.contains(diacritic)) {
					sb.append(diacritic);
				}
			}

		}
		return formatterMode.normalize(bestSymbol + sb);
	}

	public Set<String> getSymbols() {
		return Collections.unmodifiableSet(featureMap.keySet());
	}

	@Override
	public String toString() {
		String string;
		if (this == EMPTY_MODEL) {
			string = "EMPTY MODEL";
		} else {
			string = "FeatureModel(number.features=" + getNumberOfFeatures() + ", number.symbols=" + featureMap.size() + ')';
		}
		return string;
	}

	@Override
	public int hashCode() {
		int code = 91;
		code *= featureMap != null ? featureMap.hashCode() : 1;
		return code;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (getClass() != obj.getClass()) { return false; }

		FeatureModel other = (FeatureModel) obj;

		boolean diacriticsEquals = diacritics.equals(other.diacritics);
		boolean featureEquals    = featureMap.equals(other.getFeatureMap());
		boolean namesEquals      = featureNames.equals(other.featureNames);
		boolean aliasesEquals    = featureAliases.equals(other.featureAliases);
		return namesEquals && aliasesEquals && featureEquals && diacriticsEquals;
	}

	public boolean containsKey(String key) {
		return featureMap.containsKey(key);
	}

	public int getNumberOfFeatures() {
		return featureNames.size();
	}

	public Map<String, FeatureArray<Double>> getFeatureMap() {
		return Collections.unmodifiableMap(featureMap);
	}

	public FeatureArray<Double> getValue(String key) {
		if (featureMap.containsKey(key)) {
			return new StandardFeatureArray<Double>(featureMap.get(key));
		} else {
			return new StandardFeatureArray<Double>(blankArray);
		}
	}

	public Set<String> getFeatureNames() {
		return featureNames.keySet();
	}

	public List<Double> getBlankArray() {
		return blankArray;
	}

	public List<Double> getUnderspecifiedArray() {
		List<Double> list = new ArrayList<Double>();
		for (int i = 0; i < getNumberOfFeatures(); i++) {
			list.add(MASKING_VALUE);
		}
		return list;
	}

	// This should be here because how the segment is constructed is a function of what kind of model this is
	public Segment getSegment(String head, Iterable<String> modifiers) {
		FeatureArray<Double> featureArray = getValue(head); // May produce a null value if the head is not found for some reason
		StringBuilder sb = new StringBuilder(head);
		for (String modifier : modifiers) {
			sb.append(modifier);
			if (diacritics.containsKey(modifier)) {
				FeatureArray<Double> doubles = diacritics.get(modifier);
				for (int i = 0; i < doubles.size(); i++) {
					Double d = doubles.get(i);
					// this will need to change if we support value modification (up or down)
					if (d != null &&!d.equals(MASKING_VALUE)) {
						featureArray.set(i, d);
					}
				}
			}
		}
		return new Segment(sb.toString(), featureArray, this);
	}

	private static Integer validate(String label, String features, Map<String, Integer> aliases, Map<String, Integer> names) {
		if (aliases.containsKey(label)) {
			return aliases.get(label);
		}
		if (names.containsKey(label)) {
			return names.get(label);
		}
		throw new ParseException("Invalid feature label \"" + label + "\" provided in \"" + features + '"');
	}

	private static FeatureArray<Double> getDifferenceArray(FeatureArray<Double> left, FeatureArray<Double> right) {
		List<Double> list = new ArrayList<Double>();
		if (left.size() == right.size()) {
			for (int i = 0; i < left.size(); i++) {
				Double l = left.get(i);
				Double r = right.get(i);
				list.add(Math.abs(getDifference(l, r)));
			}
		} else {
			LOGGER.warn("Attempt to compare arrays of differing length! {} vs {}", left, right);
		}
		return new StandardFeatureArray<Double>(list);
	}

	private static double getDifferenceValue(FeatureArray<Double> left, FeatureArray<Double> right) {
		double sum = 0.0;
		FeatureArray<Double> differenceArray = getDifferenceArray(left, right);
		if (differenceArray.size() == 0) {
			sum = Double.NaN;
		} else {
			for (Double value : differenceArray) {
				sum += value;
			}
		}
		return sum;
	}

	private static Double getDifference(Double a, Double b) {
		if (a.equals(b)) {
			return 0.0;
		} else if (a.isNaN()) {
			return b;
		} else if (b.isNaN()) {
			return a;
		} else {
			return Math.abs(a - b);
		}
	}

	private Collection<String> getBestDiacritic(
			FeatureArray<Double> featureArray,
			FeatureArray<Double> bestFeatures,
			double lastMinimum) {

		int size = getNumberOfFeatures();

		String bestDiacritic = "";
		double minimumDifference = lastMinimum;
		FeatureArray<Double> best = new StandardFeatureArray<Double>(size);

		Collection<String> diacriticList = new ArrayList<String>();

		for (Map.Entry<String, FeatureArray<Double>> entry : diacritics.entrySet()) {
			FeatureArray<Double> diacriticFeatures = entry.getValue();
			FeatureArray<Double> compiledFeatures = new StandardFeatureArray<Double>(size);
//			if (diacriticFeatures.size() == bestFeatures.size()) {
				for (int i = 0; i < size; i++) {
					Double left  = diacriticFeatures.get(i);
					Double right = bestFeatures.get(i);

					if (left == null || left.equals(MASKING_VALUE)) {
						compiledFeatures.set(i, right);
					} else {
						compiledFeatures.set(i, left);
					}
				}
//			} else {
//				LOGGER.error("Difference in array sizes: {} vs  {}", diacriticFeatures, bestFeatures);
//			}

			if (!compiledFeatures.equals(bestFeatures)) {
				double difference = getDifferenceValue(compiledFeatures, featureArray);
				if (difference < minimumDifference) {
					minimumDifference = difference;
					bestDiacritic = entry.getKey();
					best = compiledFeatures;
				}
			}
		}

		if (minimumDifference > 0.0 && minimumDifference < lastMinimum) {
			diacriticList.add(bestDiacritic);
			diacriticList.addAll(getBestDiacritic(featureArray, best, minimumDifference));
		} else {
			diacriticList.add(bestDiacritic);
		}
		return diacriticList;
	}

	private Collection getBestDiacritic(
			FeatureArray<Double> featureArray,
			FeatureArray<Double> bestFeatures) {
		return getBestDiacritic(featureArray, bestFeatures, Double.MAX_VALUE);
	}
}
