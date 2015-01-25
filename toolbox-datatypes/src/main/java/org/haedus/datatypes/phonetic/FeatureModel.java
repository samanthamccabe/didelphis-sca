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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.haedus.datatypes.phonetic;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.haedus.datatypes.tables.RectangularTable;
import org.haedus.datatypes.tables.Table;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
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

	public static final  FeatureModel EMPTY_MODEL = new FeatureModel();

	private static final Pattern NEWLINE_PATTERN = Pattern.compile("\\n\\r?|\\r");
	private static final Pattern COMMENT_PATTERN = Pattern.compile("\\s*%.*");

	private final Map<String, Integer>      featureNames;
	private final Map<String, Integer>      featureAliases;
	private final Map<String, List<Double>> featureMap;
	private final Map<String, List<Double>> diacritics;
	private final RectangularTable<Double>  weightTable;

	/**
	 * Initializes an empty model; access to this should only be through the EMPTY_MODEL field
	 */
	private FeatureModel() {
		featureNames = new HashMap<String, Integer>();
		featureAliases = new HashMap<String, Integer>();
		featureMap = new LinkedHashMap<String, List<Double>>();
		diacritics = new LinkedHashMap<String, List<Double>>();
		weightTable = new RectangularTable<Double>(0.0, 0, 0);
	}

	public FeatureModel(File file) {
		this();
		try {
			readModelFromFile(FileUtils.readLines(file, "UTF-8"));
		} catch (IOException e) {
			LOGGER.error("Failed to read from file {}", file, e);
		}
	}

	// This should be here because how the segment is constructed is a function of what kind of model this is
	public Segment getSegment(String head, Iterable<String> modifiers) {
		List<Double> featureArray = getValue(head); // May produce a null value if the head is not found for some reason

		StringBuilder sb = new StringBuilder();
		for (String modifier : modifiers) {
			sb.append(modifier);
			if (diacritics.containsKey(modifier)) {
				List<Double> doubles = diacritics.get(modifier);
				for (int i = 0; i < doubles.size(); i++) {
					Double d = doubles.get(i);
					// TODO: this will need to change if we support value modification (up or down)
					if (!d.isNaN()) {
						featureArray.set(i, d);
					}
				}
			}
		}
		return new Segment(sb.toString(), featureArray, this);
	}

	public String getBestSymbol(List<Double> featureArray) {

		List<Double> bestFeatures = new ArrayList<Double>();
		String bestSymbol = "";
		double minimum = Double.MAX_VALUE;

		for (Map.Entry<String, List<Double>> entry : featureMap.entrySet()) {
			List<Double> features = entry.getValue();

			double difference = getDifferenceValue(featureArray, features);
			if (difference < minimum) {
				bestSymbol = entry.getKey();
				minimum = difference;
				bestFeatures = features;
			}
		}

		String bestDiacritic = "";
		if (minimum > 0.0) {
			bestDiacritic = getBestDiacritic(featureArray, bestFeatures);
		}
		return Normalizer.normalize(bestSymbol + bestDiacritic, Normalizer.Form.NFC);
	}

	public Set<String> getSymbols() {
		return Collections.unmodifiableSet(featureMap.keySet());
	}

	public void add(Segment segment) {
		featureMap.put(segment.getSymbol(), segment.getFeatures());
	}

	@Override
	public int hashCode() {
		int code = 91;
		code *= featureMap  != null ? featureMap.hashCode()  : 1;
		code *= weightTable != null ? weightTable.hashCode() : 1;
		return code;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)                  return false;
		if (obj.getClass() != getClass()) return false;

		FeatureModel other = (FeatureModel) obj;

		boolean featureEquals = featureMap.equals(other.getFeatureMap());
		boolean weightsEquals = weightTable.equals(other.getWeights());
		return featureEquals && weightsEquals;
	}

	public double computeScoreUsingWeights(Segment l, Segment r) {

		modelConsistencyCheck(l, r);

		double score = 0.0;
		int n = getNumberOfFeatures();
		for (int i = 0; i < n; i++) {
			double a = l.getFeatureValue(i);
			for (int j = 0; j < n; j++) {
				double b = r.getFeatureValue(j);
				if (weightTable.getNumberColumns() == getNumberOfFeatures()) {
					score += getDifference(a, b) * weightTable.get(i, j);
				} else {
					score += getDifference(a, b);
				}
				score += getDifference(a, b);
			}
		}
		return score;
	}

	private void modelConsistencyCheck(ModelBearer l, ModelBearer r) {
		FeatureModel mL = l.getFeatureModel();
		FeatureModel mR = r.getFeatureModel();

		if (!mL.equals(this) && !mR.equals(this)) {
			throw new RuntimeException(
				"Attempting to compare segments using an incompatible model!\n" +
					'\t' + l + '\t' + mL.getFeatureNames() + '\n' +
					'\t' + r + '\t' + mR.getFeatureNames() + '\n' +
					"\tUsing model: " + getFeatureNames()
			);
		}
	}

	public double computeScore(Segment l, Segment r) {
		double score = 0;
		for (int i = 0; i < getNumberOfFeatures(); i++) {
			double a = l.getFeatureValue(i);
			double b = r.getFeatureValue(i);
			score += getDifference(a, b);
		}
		return score;
	}

	public boolean containsKey(String key) {
		return featureMap.containsKey(key);
	}

	public int getNumberOfFeatures() {
		return featureNames.size();
	}

	public Map<String, List<Double>> getFeatureMap() {
		return Collections.unmodifiableMap(featureMap);
	}

	public List<Double> getValue(String key) {
		if (featureMap.containsKey(key)) {
			return featureMap.get(key);
		} else if (getNumberOfFeatures() == 0) {
			return new ArrayList<Double>();
		} else {
			LOGGER.error("Unable to find " + key +"  in model.");
			return null;
		}
	}

	public Table<Double> getWeights() {
		return weightTable;
	}

	public Set<String> getFeatureNames() {
		return featureNames.keySet();
	}

	private String getBestDiacritic(List<Double> featureArray, List<Double> bestFeatures, double lastMinimum) {
		String bestDiacritic = "";
		double minimumDifference = lastMinimum;
		List<Double> bestCompiled = new ArrayList<Double>();

		for (Map.Entry<String, List<Double>> entry : diacritics.entrySet()) {
			List<Double> diacriticFeatures = entry.getValue();
			List<Double> compiledFeatures = new ArrayList<Double>();
			if (diacriticFeatures.size() == bestFeatures.size()) {
				for (int i = 0; i < diacriticFeatures.size(); i++) {
					Double left = diacriticFeatures.get(i);
					Double right = bestFeatures.get(i);

					if (left.isNaN()) {
						compiledFeatures.add(right);
					} else {
						compiledFeatures.add(left);
					}
				}
			} else {
				LOGGER.error("Difference in array sizes: {} vs  {}", diacriticFeatures, bestFeatures);
			}

			if (!compiledFeatures.equals(bestFeatures)) {
				double difference = getDifferenceValue(compiledFeatures, featureArray);
				if (difference < minimumDifference) {
					minimumDifference = difference;
					bestDiacritic = entry.getKey();
					bestCompiled = compiledFeatures;
				}
			}
		}
		// TODO: how does changing this condition affect behavior?
		if (minimumDifference > 0.0 && minimumDifference != lastMinimum) {
			return bestDiacritic + getBestDiacritic(featureArray, bestCompiled, minimumDifference);
		} else {
			return bestDiacritic;
		}
	}

	private String getBestDiacritic(List<Double> featureArray, List<Double> bestFeatures) {
		return getBestDiacritic(featureArray, bestFeatures, Double.MAX_VALUE);
	}

	private List<Double> getDifferenceArray(List<Double> left, List<Double> right) {
		List<Double> list = new ArrayList<Double>();
		if (left.size() == right.size()) {
			for (int i = 0; i < left.size(); i++) {
				Double l = left.get(i);
				Double r = right.get(i);
				double lValue = l.isNaN() ? 0 : l;
				double rValue = r.isNaN() ? 0 : r;
				list.add(getDifference(lValue, rValue));
			}
		} else {

			LOGGER.warn("Attempt to compare arrays of differing length! {} vs {}", left, right);
		}
		return list;
	}

	private double getDifferenceValue(List<Double> left, List<Double> right) {
		double sum = 0.0;
		List<Double> differenceArray = getDifferenceArray(left, right);
		if (differenceArray.isEmpty()) {
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

	private void readModelFromFileNewFormat(CharSequence file) {
		Zone currentZone = Zone.NONE;

		Pattern pattern = Pattern.compile("([\\w\\(\\)]+)\\s+(.*)");
		for (String string : NEWLINE_PATTERN.split(file)) {
			// Remove comments
			String line = COMMENT_PATTERN.matcher(string).replaceAll("");
			if (line.startsWith(Zone.FEATURES.value())) {
				currentZone = Zone.FEATURES;
			} else if (line.startsWith(Zone.SYMBOLS.value())) {
				currentZone = Zone.SYMBOLS;
			} else if (line.startsWith(Zone.MODIFIERS.value())) {
				currentZone = Zone.MODIFIERS;
			} else if (line.startsWith(Zone.WEIGHTS.value())){
				currentZone = Zone.WEIGHTS;
			} else {
				// Process in current zone
				Matcher matcher = pattern.matcher(line);
				if (matcher.matches()) {
					String head = matcher.group(1);
					String tail = matcher.group(2);
					switch (currentZone) {
						case FEATURES:
							// Head is feature
							// Tail is alias, value type
							break;
						case SYMBOLS:
							// Head is symbol
							// Tail is feature array
							break;
						case MODIFIERS:
							// Head is modifier
							// tail is
							break;
						case WEIGHTS:
							// This has a completely different structure
							// ????
							break;
						case NONE:
							// Fallthrough
						default:
							// command outside of zone
							LOGGER.debug("Well-formed command outside of legal zone: {} {}", head, tail);
					}
				} else if (!line.isEmpty()) {
					LOGGER.warn("Unrecognized statement {}", line);
				}
			}
		}
	}

	private void readModelFromFile(List<String> lines) {

		boolean hasDiacritics = false;
		if (lines.get(0).startsWith("name")) {
			String line = lines.remove(0);
			String[] row = line.split("\t", -1);

			row = ArrayUtils.remove(row, 0);
			if (row[0].equals("diacritic")) {
				hasDiacritics = true;
				row = ArrayUtils.remove(row, 0);
			}
			for (int i = 0; i < row.length; i++) {
				featureNames.put(row[i], i);
			}
		}

		if (lines.get(0).startsWith("alias")) {
			String   line = lines.remove(0);
			String[] row  = line.split("\t", -1);

			row = ArrayUtils.remove(row, 0);
			if (hasDiacritics) {
				// Remove the placeholder for "diacritics"
				row = ArrayUtils.remove(row, 0);
			}
			for (int i = 0; i < row.length; i++) {
				featureAliases.put(row[i], i);
			}
		}

		for (String line : lines) {

			String[] row = line.split("\t", -1);
			String keys = row[0].trim();
			row = ArrayUtils.remove(row, 0);

			// Read feature specification
			List<Double> features = new ArrayList<Double>();
			for (String cell : row) {

				double featureValue;
				if (cell.isEmpty()) {
					featureValue = Double.NaN;
				} else if (cell.equals("+")) {
					featureValue = 1.0;
				} else if (cell.equals("-")) {
					featureValue = -1.0;
				} else {
					featureValue = Double.valueOf(cell);
				}
				features.add(featureValue);
			}
			// Create mapping
			if (hasDiacritics) {
				Double diacriticFlag = features.remove(0);
				if (diacriticFlag <= 0.0) {
					for (String key : keys.split(" ")) {
						featureMap.put(key, features);
					}
				} else {
					for (String key : keys.split(" ")) {
						diacritics.put(key, features);
					}
				}
			} else {
				for (String key : keys.split(" ")) {
					featureMap.put(key, features);
				}
			}
		}
	}

	private Table<Double> readWeights(List<String> lines) {
		int numberOfWeights = lines.get(0).split("\t").length;
		Table<Double> table = new RectangularTable<Double>(0.0, numberOfWeights, numberOfWeights);

		for (int i = 0; i < lines.size(); i++) {
			String[] row = lines.get(i).split("\t");
			for (int j = 0; j < row.length; j++) {
				Double value = Double.valueOf(row[j]);
				table.set(value, i, j);
			}
		}
		return table;
	}

	private enum Zone {
		FEATURES("FEATURES"),
		SYMBOLS("SYMBOLS"),
		MODIFIERS("MODIFIERS"),
		WEIGHTS("WEIGHTs"),
		NONE("NONE");

		private final String value;

		Zone(String v) {
			value = v;
		}

		String value() {
			return value;
		}
	}
}
