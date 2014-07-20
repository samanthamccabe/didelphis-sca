/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.haedusfc.datatypes.phonetic;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.haedusfc.datatypes.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Haedus FC 2012
 *
 * @author Goats
 */
public class FeatureModel {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(FeatureModel.class);

	private Map<String, List<Float>> featureMap;
	private Table<Float>             weightTable;

	/**
	 * Initializes an empty model
	 */
	public FeatureModel() {
		super();
		featureMap  = new HashMap<String, List<Float>>();
		weightTable = new Table<Float>();
	}

	public FeatureModel(Map<String, List<Float>> map, Table<Float> weights) {
		super();
		featureMap  = map;
		weightTable = weights;
	}

	/**
	 * Initializes a model from a valid file
	 * @param modelPath
     */
    public FeatureModel(String modelPath, String weightsPath) {

		try {
			List<String> modelFile = FileUtils.readLines(new File(modelPath));
			featureMap = readTable(modelFile);
		} catch (IOException e) {
			LOGGER.error("Unable to read from path '{}'", modelPath);
		}

		try {
			List<String> modelFile = FileUtils.readLines(new File(weightsPath));
			weightTable = readWeights(modelFile);
		} catch (IOException e) {
			LOGGER.error("Unable to read from path '{}'", weightsPath);
		}
    }

	@Override
	public int hashCode() {
		return 7543 * featureMap.hashCode() * weightTable.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj.getClass() != getClass()) return false;

		FeatureModel other = (FeatureModel) obj;

		return featureMap.equals(other.getFeatureMap()) && weightTable.equals(other.getWeights());
	}

    public float computeScore(Alignment alignment) {
        Sequence left  = alignment.getLeft();
        Sequence right = alignment.getRight();
        return computeScore(left,right);
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

	public float computeScore(String l, String r) {
		Sequence left  = new Sequence(l);
		Sequence right = new Sequence(r);
		return computeScore(left,right);
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

        for (String line : lines) {
            String[] row  = line.split("\t");
            String   keys = row[0];

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
        Table<Float> table  = new Table<Float>(0f,numberOfWeights, numberOfWeights);

        for (int i = 0; i < lines.size(); i++) {
            String[] row = lines.get(i).split("\t");
            for(int j = 0; j < row.length; j++) {
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
		for (int j = l; j > i; j--) {               // Loop over the rest to add the diacritics.
			String slice = w.substring(i, j);
			if (containsKey(slice)) {
				List<Float> featureArray = getFeatureArray(slice);
				segment = segment.appendDiacritic(slice,featureArray);
				i = j - 1;
			}
		}
		return segment;
	}
}
