/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.haedusfc.datatypes.phonetic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Samuel McCabe, Haedus FC
 */
public class Segment {

	private final String      symbol;
	private final List<Float> features;

	/**
	 * Initialize an empty Segment
	 */
	public Segment() {
		symbol   = "";
		features = new ArrayList<Float>();
	}

	public Segment(String s) {
		symbol   = s;
		features = new ArrayList<Float>();
    }

    public Segment(String s, List<Float> featureArray) {
		symbol = s;
		features = new ArrayList<Float>(featureArray);
	}

    public Segment appendDiacritic(String diacriticSymbol, List<Float> diacriticFeatures) {
		List<Float> featureList = features;
        String s = symbol.concat(diacriticSymbol);

        for (int i = 1; i < diacriticFeatures.size(); i++) {
            float feature = diacriticFeatures.get(i);
            if (feature != -9) {
				featureList.set(i,feature);
            }
        }
        return new Segment(s, features);
    }

	public boolean equals(Object obj) {

		if (obj == null)                        return false;
		if (!getClass().equals(obj.getClass())) return false;

		Segment object = (Segment) obj;

		return getSymbol().equals(object.getSymbol()) &&
			   getFeatures().equals(object.getFeatures());
	}

    public int hashCode() {
        int hashCode = 29217;

	    for (int i = 0; i < features.size(); i++) {
		    double number = (Math.pow(features.get(i), i + 1) + 1) * 7;
		    hashCode += number;
	    }
	    hashCode += symbol.hashCode() * 23 + 1;

        return hashCode ;
    }

	public String getSymbol() {
		return symbol;
	}

	public List<Float> getFeatures() {
		return Collections.unmodifiableList(features);
	}

	public float getFeatureValue(int i) {
		return features.get(i);
	}

	public boolean isDiacritic() {
		return (features.get(0) == -1.0);
	}

	public int dimension() {
		return features.size();
	}

    @Override
    public String toString() {
        String featureString = " ";
		for (float _feature : features) {
			featureString += _feature + " ";
		}
        return  symbol + featureString.trim();
    }

	public boolean isEmpty() {
		return (symbol != null && symbol.isEmpty() && features.isEmpty());
	}
}
