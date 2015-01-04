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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Samantha Fiona Morrigan McCabe
 */
public class Segment {

	private final FeatureModel model;
	private final String       symbol;
	private final List<Double> features;

	// Copy-constructor
	public Segment(Segment segment) {
		symbol   = segment.getSymbol();
		model    = segment.getFeatureModel();
		features = segment.getFeatures();
	}

	@Deprecated
	public Segment(String s, FeatureModel modelParam) {
		symbol   = s;
		model    = modelParam;
		features = model.getValue(s);
	}

	public Segment(String s, List<Double> featureArray, FeatureModel modelParam) {
		symbol   = s;
		model    = modelParam;
		features = new ArrayList<Double>(featureArray);
	}

	// Test only
	@Deprecated
	Segment(String string) {
		symbol   = string;
		model    = FeatureModel.EMPTY_MODEL;
		features = new ArrayList<Double>();
	}

	public FeatureModel getFeatureModel() {
		return model;
	}

	@Override
	public int hashCode() {
		return 19 * symbol.hashCode() * features.hashCode() * model.hashCode();
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null)                  { return false; }
		if (getClass() != obj.getClass()) { return false; }

		Segment other = (Segment) obj;
		return symbol.equals(other.getSymbol()) &&
		       features.equals(other.getFeatures())
		       && model.equals(other.getFeatureModel());
	}

	@Override
	public String toString() {
		return symbol;
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

	public int getNumberOfFeatures() {
		return features.size();
	}

	public String toStringLong() {
		StringBuilder sb = new StringBuilder(symbol + '\t');
		for (Double feature : features) {
			if (feature.equals(Double.NaN)) {
				sb.append(" ***");
			} else {
				if (feature < 0.0) {
					sb.append(feature);
				} else {
					sb.append(' ');
					sb.append(feature);
				}
			}
			sb.append(' ');
		}
		return sb.toString();
	}

	public boolean isEmpty() {
		return symbol != null && symbol.isEmpty() && features.isEmpty();
	}
}
