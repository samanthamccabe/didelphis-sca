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
import java.util.*;

/**
 * @author Samantha Fiona Morrigan McCabe
 */
public class FeatureModel {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(FeatureModel.class);

	private final Map<String, List<Float>> featureMap;
	private final Table<Float>             weightTable;

	/**
	 * Initializes an empty model
	 */
	public FeatureModel() {
		featureMap  = new HashMap<String, List<Float>>();
		weightTable = new Table<Float>();
	}

	public FeatureModel(Map<String, List<Float>> map, Table<Float> weights) {
		featureMap  = map;
		weightTable = weights;
	}

	public FeatureModel(File modelFile) {
		this();
		try {
			List<String> lines = FileUtils.readLines(modelFile, "UTF-8");
			featureMap.putAll(readTable(lines));
		}
		catch (IOException e) {
			LOGGER.error("Failed to read model rile at {}", modelFile.getAbsolutePath(), e);
		}
	}

	public Set<String> getSymbols() {
		return featureMap.keySet();
	}

	public void addSegment(String symbol, List<Float> features) {
		featureMap.put(symbol, features);
	}

	public void addSegment(String symbol) {
		addSegment(symbol, new ArrayList<Float>());
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
			float a = l.getFeatureValue(i);
			for (int j = 0; j < n; j++) {
				float b = r.getFeatureValue(j);
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

	public List<Float> getFeatureArray(String symbol) {
		return get(symbol).getFeatures();
	}

	public Map<String, List<Float>> getFeatureMap() {
		return Collections.unmodifiableMap(featureMap);
	}

	public List<Float> getValue(String key) {
		List<Float> value = new ArrayList<Float>();

		if (featureMap.containsKey(key)) {
			value = featureMap.get(key);
		}
		return value;
	}

	public Table<Float> getWeights() {
		return weightTable;
	}

	public void put(String key, List<Float> values) {
		featureMap.put(key, values);
	}

	private Map<String, List<Float>> readTable(List<String> lines) {
		Map<String, List<Float>> listMap = new HashMap<String, List<Float>>();

		// TODO: we'll need to parse out the header
		String header = lines.remove(0); // This will require that there is a header
		Map<String, String> featureLabels = new LinkedHashMap<String, String>();

		for (String line : lines) {

			String[] row = line.split("\t");
			String keys = row[0];

			row = ArrayUtils.remove(row, 0);
			List<Float> features = new ArrayList<Float>();

			for (String cell : row) {
				float featureValue = new Float(cell);
				features.add(featureValue);
			}

			for (String key : keys.split(" ")) {
				listMap.put(key, features);
			}
		}
		return listMap;
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
				List<Float> featureArray = getFeatureArray(slice);
				segment = segment.appendDiacritic(slice, featureArray);
				i = j - 1;
			}
		}
		return segment;
	}
}
