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
import org.haedus.datatypes.SymmetricTable;
import org.haedus.datatypes.Table;
import org.haedus.exceptions.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Samantha Fiona Morrigan McCabe
 */
public class FeatureModel {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(FeatureModel.class);

	private       int                        numberOfFeatures;
	private final Map<String, Integer>       labelIndices;
	private final Map<String, List<Double>> featureMap;
	private final SymmetricTable<Double>     weightTable;

	/**
	 * Initializes an empty model
	 */
	public FeatureModel() {
		numberOfFeatures = 0;

		labelIndices = new HashMap<String, Integer>();
		featureMap   = new HashMap<String, List<Double>>();
		weightTable  = new SymmetricTable<Double>();
	}

	public FeatureModel(Map<String, List<Double>> map, SymmetricTable<Double> weights) {
		numberOfFeatures = weights.getDimension();

		labelIndices = new HashMap<String, Integer>();
		featureMap   = map;
		weightTable  = weights;
	}

	public FeatureModel(File modelFile) throws ParseException {
		this();
		try {
			readTable(FileUtils.readLines(modelFile, "UTF-8"));
		}
		catch (IOException e) {
			LOGGER.error("Failed to read model rile at {}", modelFile.getAbsolutePath(), e);
		}
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
		if (obj == null)                  return false;
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

	public float computeScore(Segment l, Segment r) {
		float score = 0;
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
		Sequence left  = new Sequence(l);
		Sequence right = new Sequence(r);
		return computeScore(left, right);
	}

	public boolean containsKey(String key) {
		return featureMap.containsKey(key);
	}

	@Deprecated
	public Segment gap() {
		return get("_");
	}

	public Segment get(String string) {
		return new Segment(string, featureMap.get(string));
	}

	public List<Double> getFeatureArray(String symbol) {
		return featureMap.get(symbol);
	}

	public Map<String, List<Double>> getFeatureMap() {
		return Collections.unmodifiableMap(featureMap);
	}

	public String getBestSymbol(List<Double> features) {
		String bestSymbol = null;
		List<Double> bestFeatures = null;

		if (features.size() == numberOfFeatures) {
			// Find the base symbol with the smallest Euclidean distance
			double minDistance = Double.MAX_VALUE;
			for (Map.Entry<String, List<Double>> entry : featureMap.entrySet()) {

				String key = entry.getKey();
				List<Double> list = entry.getValue();
				// Only check base characters
				if (list.get(0) != -1) {
					double sumOfDeltas = 0.0;
					for (int i = 0; i < list.size(); i++) {
						double delta = features.get(i) - list.get(i);
						sumOfDeltas += Math.pow(delta, 2);
					}
					double distance = Math.sqrt(sumOfDeltas);
					if (distance <= minDistance) {
						bestSymbol = key;
						minDistance = distance;
						bestFeatures = list;
					}
				}
			}

			// Figure out which minimum set of diacritics
			Map<Integer, Double> deltas = new HashMap<Integer, Double>();
			for (int i = 0; i < features.size(); i++) {
				double delta = features.get(i) - bestFeatures.get(i);
				if (delta != 0) {
					deltas.put(i, delta);
				}
			}

			Map<String, List<Double>> candidates = new HashMap<String, List<Double>>();

			for (Map.Entry<Integer, Double> deltaEntry : deltas.entrySet()) {
				Integer index = deltaEntry.getKey();
				Double value = deltaEntry.getValue();
				for (Map.Entry<String, List<Double>> entry : featureMap.entrySet()) {
					String key = entry.getKey();
					List<Double> list = entry.getValue();
					// Only check diacritics
					if (list.get(0) == Double.MIN_VALUE &&
					    list.get(index).equals(value)) {
						candidates.put(key, list);
					}
				}
			}

			for (Map.Entry<String, List<Double>> entry : candidates.entrySet()) {
				String symbol = entry.getKey();
				List<Double> list = entry.getValue();

				List<Double> test = new ArrayList<Double>(features);

				for (int i = 1; i < list.size(); i++) {
					Double value = list.get(i);
					if (value != Double.MIN_VALUE) {
						test.set(i, value);
					}
				}

				if (test.equals(features)) {
					bestSymbol += symbol;
					break;
				}
			}
		}
		return bestSymbol;
	}

	public SymmetricTable<Double> getWeights() {
		return weightTable;
	}

	public void put(String key, List<Double> values) {
		featureMap.put(key, values);
	}

	private void readTable(List<String> lines) throws ParseException {
		// Identify the labels
		if (!lines.isEmpty() && lines.get(0).startsWith("\t")) {
			String[] labels = lines.remove(0).trim().split("\\t");
			numberOfFeatures = labels.length;
			for (int i = 0; i < numberOfFeatures; i++) {
				labelIndices.put(labels[i], i);
			}
		}

		for (String line : lines) {

			String[] row = line.split("\\t", -1);
			String keys = row[0];

			row = ArrayUtils.remove(row, 0);
			if (row.length != numberOfFeatures) {
				throw new ParseException(
						"Improper row size! Expected " + numberOfFeatures +
				        " features but found " + row.length + "\nSee line " + line
				);
			}

			List<Double> features = new ArrayList<Double>();
			for (String cell : row) {
				if (cell.isEmpty()) {
					features.add(Double.MIN_VALUE);
				} else {
				double featureValue = new Double(cell);
				features.add(featureValue);
				}
			}

			for (String key : keys.split(" ")) {
				featureMap.put(key, features);
			}
		}
	}

	/**
	 * @param lines
	 */
	private Table<Float> readWeights(List<String> lines) {
		int numberOfWeights = lines.get(0).split("\t").length;
		Table<Float> table = new Table<Float>(0f, numberOfWeights, numberOfWeights);

		for (int i = 0; i < lines.size(); i++) {
			String[] row = lines.get(i).split("\t");
			for (int j = 0; j < row.length; j++) {
				Float value = new Float(row[j]);
				table.set(value, i, j);
			}
		}
		return table;
	}

	private Segment compile(String w) {
		String head = "";
		int l = w.length();
		int i = l;
		while (i > 0) {                             // Loop back-to-front and find the head
			String slice = w.substring(0, i);
			if (containsKey(slice)) {
				head = slice;
				break;
			} else {
				i--;
			}
		}
		Segment segment = get(head);                // Initialize the segment
		for (int j = l; j > i; j--) {               // Loop over the rest to put the diacritics.
			String slice = w.substring(i, j);
			if (containsKey(slice)) {
				List<Double> featureArray = getFeatureArray(slice);
				segment = segment.appendDiacritic(slice, featureArray);
				i = j - 1;
			}
		}
		return segment;
	}
}
