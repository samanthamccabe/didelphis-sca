/*******************************************************************************
 * Copyright (c) 2014 Haedus - Fabrica Codicis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
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
import org.haedus.datatypes.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Samantha Fiona Morrigan McCabe
 */
public class FeatureModel {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(FeatureModel.class);

	private final List<String>  featureNames;
	private final List<String>  featureAliases;
	private final Table<Double> weightTable;

	private final Map<String, List<Double>> featureMap;
	private final Map<String, List<Double>> diacritics;

	/**
	 * Initializes an empty model
	 */
	public FeatureModel() {
		featureNames = new ArrayList<String>();
		featureAliases = new ArrayList<String>();
		weightTable = new Table<Double>();
		featureMap = new HashMap<String, List<Double>>();
		diacritics = new HashMap<String, List<Double>>();
	}

	public FeatureModel(File file) {
		this();
		try {
			readTable(FileUtils.readLines(file));
		} catch (IOException e) {
			LOGGER.error("Failed to read from file {}", file, e);
		}
	}

	public String getBestSymbol(List<Double> featureArray) {

		List<Double> bestFeatures = new ArrayList<Double>();
		String bestSymbol = "";
		double minimum = Double.MAX_VALUE;

		for (Map.Entry<String, List<Double>> entry : featureMap.entrySet()) {
			List<Double> features = entry.getValue();

			double difference = getDifferenceValue(featureArray, features);
			if (difference < minimum) {
				bestSymbol   = entry.getKey();
				minimum      = difference;
				bestFeatures = features;
			}
		}

		String bestDiacritic = "";
		if (minimum > 0.0) {
			bestDiacritic = getBestDiacritic(featureArray, bestFeatures);
		}

		return Normalizer.normalize(bestSymbol+bestDiacritic, Normalizer.Form.NFC);
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

				} else if (difference == minimumDifference) {
					// Modify this to use sets
				}
			}
		}
		if (minimumDifference > 0 && minimumDifference != lastMinimum) {
			return bestDiacritic + getBestDiacritic(featureArray, bestCompiled, minimumDifference);
		}
		else {
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
				list.add(Math.abs(lValue - rValue));
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

	public Set<String> getSymbols() {
		return featureMap.keySet();
	}

	public void addSegment(String symbol, List<Double> features) {
		featureMap.put(symbol, features);
	}

	public void addSegment(String symbol) {
		addSegment(symbol, new ArrayList<Double>());
	}

	@Override
	public String toString() {
		return featureMap.toString();
	}

	@Override
	public int hashCode() {
		int code = 7543;
		if (featureMap != null) {
			code *= featureMap.hashCode();
		}
		if (weightTable != null) {
			code *= weightTable.hashCode();
		}
		return code;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj.getClass() != getClass()) return false;

		FeatureModel other = (FeatureModel) obj;

		boolean featureEquals = featureMap.equals(other.featureMap);
		boolean weightsEquals = weightTable.equals(other.weightTable);
		return featureEquals && weightsEquals;
	}

	public float computeScore(Alignment alignment) {
		Sequence left  = alignment.getLeft();
		Sequence right = alignment.getRight();
		return computeScore(left, right);
	}

	public double computeScore(Segment l, Segment r) {
		double score = 0;
		int n = l.dimension();
		for (int i = 0; i < n; i++) {
			double a = l.getFeatureValue(i);
			for (int j = 0; j < n; j++) {
				double b = r.getFeatureValue(j);
				score += Math.abs(a - b) * weightTable.get(i, j);
			}
		}
		return score;
	}

	public float computeScore(Sequence l, Sequence r) {
		int penalty = 5;
		float score = 0;
		for (int i = 0; i < l.size(); i++) {
			score += computeScore(l.get(i), r.get(i));

			// TODO: gap penalty
		}
		return score;
	}

	@Deprecated
	public float computeScore(String l, String r) {
		Sequence left = new Sequence(l);
		Sequence right = new Sequence(r);
		return computeScore(left, right);
	}

	public boolean containsKey(String key) {
		return featureMap.containsKey(key);
	}

	public Segment gap() {
		return get("_");
	}

	public Segment get(String string) {
		return new Segment(string, getValue(string));
	}

	public List<Double> getFeatureArray(String symbol) {
		return get(symbol).getFeatures();
	}

	public Map<String, List<Double>> getFeatureMap() {
		return Collections.unmodifiableMap(featureMap);
	}

	public List<Double> getValue(String key) {
		List<Double> value = new ArrayList<Double>();

		if (featureMap.containsKey(key)) {
			value = featureMap.get(key);
		}
		return value;
	}

	public Table<Double> getWeights() {
		return weightTable;
	}

	public void put(String key, List<Double> values) {
		featureMap.put(key, values);
	}

	private void readTable(List<String> lines) {

		boolean hasDiacritics = false;

		if (lines.get(0).startsWith("name")) {
			String line = lines.remove(0);
			String[] row = line.split("\t", -1);
			row = ArrayUtils.remove(row, 0);
			if (row[0].equals("diacritic")) {
				hasDiacritics = true;
				row = ArrayUtils.remove(row, 0);
			}
			Collections.addAll(featureNames, row);
		}

		if (lines.get(0).startsWith("alias")) {
			String line = lines.remove(0);
			String[] row = line.split("\t", -1);
			row = ArrayUtils.remove(row, 0);
			if (hasDiacritics) {
				// Remove the placeholder for "diacritics"
				row = ArrayUtils.remove(row, 0);
			}
			Collections.addAll(featureAliases, row);
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
				if (diacriticFlag <=  0.0) {
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

	/**
	 * @param lines
	 */
	private Table<Double> readWeights(List<String> lines) {
		int numberOfWeights = lines.get(0).split("\t").length;
		Table<Double> table = new Table<Double>(0.0, numberOfWeights, numberOfWeights);

		for (int i = 0; i < lines.size(); i++) {
			String[] row = lines.get(i).split("\t");
			for (int j = 0; j < row.length; j++) {
				Double value = Double.valueOf(row[j]);
				table.set(value, i, j);
			}
		}
		return table;
	}
}
