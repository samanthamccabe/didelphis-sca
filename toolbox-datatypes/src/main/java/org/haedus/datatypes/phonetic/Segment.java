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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Samantha Fiona Morrigan McCabe
 */
public class Segment {

	private final String       symbol;
	private final List<Double> features;

	/**
	 * Initialize an empty Segment
	 */
	public Segment() {
		symbol   = "";
		features = new ArrayList<Double>();
	}

	public Segment(Segment segment) {
		symbol   = segment.getSymbol();
		features = segment.getFeatures();
	}

	public Segment(String s) {
		symbol   = s;
		features = new ArrayList<Double>();
    }

    public Segment(String s, List<Double> featureArray) {
		symbol = s;
		features = new ArrayList<Double>(featureArray);
	}

	public void setFeature(int feature, double value) {
		features.set(feature, value);
	}

    public Segment appendDiacritic(String diacriticSymbol, List<Double> diacriticFeatures) {
		List<Double> featureList = features;
        String s = symbol.concat(diacriticSymbol);

        for (int i = 1; i < diacriticFeatures.size(); i++) {
            double feature = diacriticFeatures.get(i);
            if (feature != -9) {
				featureList.set(i,feature);
            }
        }
        return new Segment(s, features);
    }

	public boolean equals(Object obj) {

		if (obj == null)                  return false;
		if (getClass() != obj.getClass()) return false;

		Segment object = (Segment) obj;
		return symbol.equals(object.symbol) && features.equals(object.features);
	}

    public int hashCode() {
        return 19 * symbol.hashCode() * features.hashCode();
    }

	public String getSymbol() {
		return symbol;
	}

	public List<Double> getFeatures() {
		return Collections.unmodifiableList(features);
	}

	public double getFeatureValue(int i) {
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
        StringBuilder sb = new StringBuilder(symbol);
		for (double feature : features) {
			sb.append(" ").append(feature);
		}
        return sb.toString();
    }

	public boolean isEmpty() {
		return (symbol != null && symbol.isEmpty() && features.isEmpty());
	}
}
