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

package org.haedus.phonetic;

import org.haedus.phonetic.features.FeatureArray;
import org.haedus.phonetic.features.StandardFeatureArray;
import org.haedus.phonetic.model.Constraint;
import org.haedus.phonetic.model.FeatureModel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Samantha Fiona Morrigan McCabe
 */
public class Segment implements ModelBearer, Comparable<Segment> {

	public static final Segment EMPTY_SEGMENT = new Segment("∅");

	private final FeatureModel model;
	private final String       symbol;
	private final FeatureArray<Double> features;

	// Copy-constructor
	public Segment(Segment segment) {
		symbol = segment.symbol;
		model = segment.model;
		features = segment.features;
	}

	@Deprecated
	public Segment(String s, FeatureModel modelParam) {
		symbol = s;
		model = modelParam;
		features = model.getValue(s);
	}

	public Segment(String s, FeatureArray<Double> featureArray, FeatureModel modelParam) {
		symbol = s;
		model = modelParam;
		features = new StandardFeatureArray<Double>(featureArray);
	}

	// Used to create the empty segment
	private Segment(String string) {
		symbol = string;
		model = FeatureModel.EMPTY_MODEL;
		features = new StandardFeatureArray<Double>(0);
	}

	/**
	 * Combines the two segments, applying all fully specified features from the other segement onto this one
	 * @param other an underspecified segment from which to take changes
	 * @return a new segment based on this one, with modifications from the other
	 */
	public Segment alter(Segment other) {
		validateModelOrFail(other);

		Collection<Integer> alteredIndices = new ArrayList<Integer>();

		FeatureArray<Double> otherFeatures = new StandardFeatureArray<Double>(features);
		for (int j = 0; j < otherFeatures.size(); j++) {
			double value = other.getFeatureValue(j);
			if (!FeatureModel.MASKING_VALUE.equals(value)) {
				otherFeatures.set(j, value);
				alteredIndices.add(j);
			}
		}
		
		// For each altered index, check if the constraints apply 
		for (int index : alteredIndices) {
			for (Constraint constraint : model.getConstraints()) {
				applyConstraint(index, otherFeatures, constraint);
			}
		}

		String newSymbol = model.getBestSymbol(otherFeatures);
		return new Segment(newSymbol, otherFeatures, model);
	}

	/**
	 * Determines if a segment is consistent with this segment.
	 * Two segments are consistent with each other if all corresponding features are equal OR if one is NaN
	 *
	 * @param other another segment to compare to this one
	 * @return true if all specified (non NaN) features in either segment are equal
	 */
	public boolean matches(Segment other) {
		validateModelOrFail(other);

		int size = features.size();
		if (isUndefined() && other.isUndefined()) {
			return symbol.equals(other.symbol);
		} else if (size > 0) {
			return features.matches(other.getFeatures());
		} else {
			return equals(other);
		}
	}

	public static boolean matchesFeatures(FeatureArray<Double> features1, FeatureArray<Double> features2) {
		if (features1.size()!= features2.size()) {return false;}
		if (features1 == features2) {return true;}
		if (features1.equals(features2)) {return true;}

		int size = features1.size();
		for (int i = 0; i < size; i++) {
			Double a = features1.get(i);
			Double b = features2.get(i);
			// Two-way comparison? I'm not certain this is the most desirable semantics.
			if (!a.equals(b) &&
			    !b.equals(FeatureModel.MASKING_VALUE) &&
			    !a.equals(FeatureModel.MASKING_VALUE)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public FeatureModel getModel() {
		return model;
	}

	@Override
	public int hashCode() {
		int hash = 19;
		hash *= 31 + symbol.hashCode();
		hash *= 31 + features.hashCode();
		hash *= 31 + model.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (null == obj) { return false; }
		if (getClass() != obj.getClass()) { return false; }

		Segment other = (Segment) obj;
		 return model.equals(other.model) &&
			 features.equals(other.features) &&
			 symbol.equals(other.symbol);
	}

	@Override
	public String toString() {
		return symbol;
	}

	public String getSymbol() {
		return symbol;
	}

	public FeatureArray<Double> getFeatures() {
		return features;
	}

	public double getFeatureValue(int i) {
		Double value = features.get(i);
		return value != null ? value : FeatureModel.MASKING_VALUE;
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

	public void setFeatureValue(int index, double value) {
		features.set(index, value);
		for (Constraint constraint : model.getConstraints()) {
			applyConstraint(index, features, constraint);
		}
	}

	private static void applyConstraint(
			int index,
			FeatureArray<Double> features,
			Constraint constraint) {


		FeatureArray<Double> source = constraint.getSource();
		if (source.get(index) != null) {
//			boolean match = true;
//			for (Map.Entry<Integer, Double> entry : source.entrySet()) {
//				match &= features.get(entry.getKey()).equals(entry.getValue());
//			}

			if (source.matches(features)) {
//				for (Map.Entry<Integer, Double> entry : constraint.getTarget().entrySet()) {
//					features.set(entry.getKey(), entry.getValue());
//				}
				features.alter(constraint.getTarget());
			}
		}
	}

	@Deprecated
	public boolean isUndefined() {
		return model.getBlankArray().equals(features);
	}

	@Deprecated
	public boolean isUnderspecified() {
		return features.contains(null)||
		       features.contains(FeatureModel.MASKING_VALUE);
	}

	@Override
	public int compareTo(@NotNull Segment o) {
		if (equals(o)) {
			return 0;
		} else {
//			validateModelOrFail(o);
			int value = features.compareTo(o.getFeatures());
			if (value == 0) {
				// If we get here, there is either no features, or feature arrays are equal
				return symbol.compareTo(o.getSymbol()); // so just compare the symbols
			}
			return value;
		}
	}

	private void validateModelOrFail(ModelBearer that) {
		FeatureModel otherModel = that.getModel();
		if (!model.equals(otherModel)) {
			throw new RuntimeException(
				"Attempting to interoperate " + that.getClass() + " with an incompatible featureModel!\n" +
					'\t' + this + '\t' + model.getFeatureNames() + '\n' +
					'\t' + that + '\t' + otherModel.getFeatureNames()
			);
		}
	}
}
