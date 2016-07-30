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

package org.haedus.phonetic.model;

import org.haedus.enums.FormatterMode;
import org.haedus.phonetic.SpecificationBearer;
import org.haedus.phonetic.Segment;
import org.haedus.phonetic.features.FeatureArray;
import org.haedus.phonetic.features.SparseFeatureArray;
import org.haedus.phonetic.features.StandardFeatureArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Samantha Fiona Morrigan McCabe
 */
public class FeatureModel implements SpecificationBearer {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(FeatureModel.class);

	public static final FeatureModel EMPTY_MODEL = new FeatureModel();
	
	private final FeatureSpecification specification;
	
	private final Map<String, FeatureArray<Double>> featureMap;
	private final Map<String, FeatureArray<Double>> modifiers;

	public FormatterMode getFormatterMode() {
		return formatterMode;
	}

	private final FormatterMode formatterMode;

	// Initializes an empty model; access to this should only be through the
	// EMPTY_MODEL field
	private FeatureModel() {
		specification = FeatureSpecification.EMPTY;
		featureMap = new LinkedHashMap<String, FeatureArray<Double>>();
		modifiers = new LinkedHashMap<String, FeatureArray<Double>>();
		formatterMode = FormatterMode.NONE;
	}

	public FeatureModel(InputStream stream, FormatterMode modeParam) {
		this(new FeatureModelLoader(stream, modeParam), modeParam);
	}

	public FeatureModel(File file, FormatterMode modeParam) {
		this(new FeatureModelLoader(file, modeParam), modeParam);
	}

	public FeatureModel(FeatureModelLoader loader, FormatterMode modeParam) {
		featureMap    = loader.getFeatureMap();
		modifiers = loader.getDiacritics();
		specification = loader.getSpecification();
		formatterMode = modeParam;
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
			Collection collection = getBestDiacritic(featureArray, bestFeatures, Double.MAX_VALUE);
			for (String diacritic : modifiers.keySet()) {
				if (collection.contains(diacritic)) {
					sb.append(diacritic);
				}
			}
		}
		return formatterMode.normalize(bestSymbol + sb);
	}

	// Return a list of all segments g such that matches.matches(input) is true
	public Collection<Segment> getMatchingSegments(Segment input) {
		Collection<Segment> collection = new ArrayList<Segment>();

		FeatureArray<Double> features = input.getFeatures();

		for (Map.Entry<String, FeatureArray<Double>> entry : featureMap.entrySet()) {
			// This implementation will work but wastes a lot of time on object
			// allocation
			FeatureArray<Double> value = entry.getValue();
			if (value.matches(features)) {
				Segment segment = new Segment(entry.getKey(), value, specification);
				collection.add(segment);
			}
		}

		return collection;
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
			string = "FeatureModel(number.symbols=" + featureMap.size() + ')';
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
		boolean diacriticsEquals = modifiers.equals(other.modifiers);
		boolean featureEquals    = featureMap.equals(other.getFeatureMap());
		boolean specEquals = specification.equals(other.specification);
		return specEquals && featureEquals && diacriticsEquals;
	}

	public boolean containsKey(String key) {
		return featureMap.containsKey(key);
	}

	public Map<String, FeatureArray<Double>> getFeatureMap() {
		return Collections.unmodifiableMap(featureMap);
	}
	
	public Map<String, FeatureArray<Double>> getModifiers() {
		return Collections.unmodifiableMap(modifiers);
	}

	public FeatureArray<Double> getValue(String key) {
		if (featureMap.containsKey(key)) {
			return new StandardFeatureArray<Double>(featureMap.get(key));
		} else {
			return new StandardFeatureArray<Double>(
					FeatureSpecification.UNDEFINED_VALUE,
					specification);
		}
	}
	
	// This should be here because how the segment is constructed is a function
	// of what kind of model this is
	public Segment getSegment(String head, Iterable<String> modifiers) {
		// May produce a null value if the head is not found for some reason
		FeatureArray<Double> featureArray = getValue(head);
		StringBuilder sb = new StringBuilder(head);
		for (String modifier : modifiers) {
			sb.append(modifier);
			if (this.modifiers.containsKey(modifier)) {
				FeatureArray<Double> doubles = this.modifiers.get(modifier);
				for (int i = 0; i < doubles.size(); i++) {
					Double d = doubles.get(i);
					// this will need to change if we support value modification (up or down)
					if (d != null) {
						featureArray.set(i, d);
					}
				}
			}
		}
		return new Segment(sb.toString(), featureArray, specification);
	}

	@Override
	public FeatureSpecification getSpecification() {
		return specification;
	}

	private static FeatureArray<Double> getDifferenceArray(FeatureArray<Double> left, FeatureArray<Double> right) {
		List<Double> list = new ArrayList<Double>();
		if (left.getSpecification().equals(right.getSpecification())) {
			for (int i = 0; i < left.size(); i++) {
				Double l = left.get(i);
				Double r = right.get(i);
				list.add(getDifference(l, r));
			}
		} else {
			LOGGER.warn("Attempt to compare arrays of differing length! {} vs {}", left, right);
		}
		return new StandardFeatureArray<Double>(list, left.getSpecification());
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
		if (a == null && b == null) {
			return 0.0;
		} else if (a == null || a.isNaN()) {
			return Math.abs(b);
		} else if (b == null || b.isNaN()) {
			return Math.abs(a);
		} else if (a.equals(b)) {
			return 0.0;
		} else  {
			return Math.abs(a - b);
		}
	}

	private Collection<String> getBestDiacritic(
			FeatureArray<Double> featureArray,
			FeatureArray<Double> bestFeatures,
			double lastMinimum) {
		
		String bestDiacritic = "";
		double minimumDifference = lastMinimum;
		FeatureArray<Double> best = new SparseFeatureArray<Double>(specification);
		Collection<String> diacriticList = new ArrayList<String>();

		for (Map.Entry<String, FeatureArray<Double>> entry : modifiers.entrySet()) {
			FeatureArray<Double> diacriticFeatures = entry.getValue();

			FeatureArray<Double> compiled = new StandardFeatureArray<Double>(bestFeatures);
			compiled.alter(diacriticFeatures);

			if (!compiled.equals(bestFeatures)) {
				double difference = getDifferenceValue(compiled, featureArray);
				if (difference < minimumDifference) {
					minimumDifference = difference;
					bestDiacritic = entry.getKey();
					best = compiled;
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
}
