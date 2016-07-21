/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 * * This program is free software: you can redistribute it and/or modify * it
 * under the terms of the GNU General Public License as published by * the Free
 * Software Foundation, either version 3 of the License, or * (at your option)
 * any later version. * * This program is distributed in the hope that it will
 * be useful, * but WITHOUT ANY WARRANTY; without even the implied warranty of *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the * GNU General
 * Public License for more details. * * You should have received a copy of the
 * GNU General Public License * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>. *
 ******************************************************************************/

package org.haedus.phonetic.model;

import org.haedus.exceptions.ParseException;
import org.haedus.phonetic.Segment;
import org.haedus.phonetic.features.FeatureArray;
import org.haedus.phonetic.features.SparseFeatureArray;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Samantha Fiona Morrigan McCabe Created: 7/2/2016
 */
public final class FeatureSpecification {

	public static final FeatureSpecification EMPTY = new FeatureSpecification(0);

	private static final String VALUE  = "(-?\\d|[A-Zα-ω]+)";
	private static final String NAME   = "(\\w+)";
	private static final String ASSIGN = "([=:><])";
	
	private static final Pattern VALUE_PATTERN   = Pattern.compile(VALUE + ASSIGN + NAME);
	private static final Pattern BINARY_PATTERN  = Pattern.compile("(\\+|\\-)" + NAME);
	private static final Pattern FEATURE_PATTERN = Pattern.compile("[,;]\\s*|\\s+");
	private static final Pattern FANCY_PATTERN   = Pattern.compile("−");
	
	private final int size;
	private final Map<String, FeatureArray<Double>> aliases;
	private final Map<String, Integer> featureIndices;
	private final List<String> featureNames; 
	private final List<Constraint> constraints;

	public FeatureSpecification(int size) {
		this.size = size;

		aliases = new HashMap<String, FeatureArray<Double>>();
		featureIndices = new HashMap<String, Integer>();
		featureNames = new ArrayList<String>();
		constraints = new ArrayList<Constraint>();
	}

	public static FeatureSpecification loadFromClassPath(String path)
			throws IOException {
		//get string contents

		String data;

		InputStream stream = FeatureSpecification.class
				.getClassLoader()
				.getResourceAsStream(path);
		BufferedReader reader =
				new BufferedReader(new InputStreamReader(stream, "UTF-8"));

		StringBuilder sb = new StringBuilder();

		reader.readLine();

		int r = reader.read();
		while (r >= 0) {
			sb.append((char) r);
			r = reader.read();
		}
		Loader loader = new Loader(sb.toString());

		return loader.getSpecification();
	}
	
	public FeatureSpecification(FeatureModelLoader loader) {
		size = loader.getNumberOfFeatures();
		aliases = loader.getAliases();

		featureIndices = new LinkedHashMap<String, Integer>();
		featureIndices.putAll(loader.getFeatureNames());
		featureIndices.putAll(loader.getFeatureAliases());

		constraints = loader.getConstraints();
		featureNames = new ArrayList<String>(loader.getFeatureNames().keySet());
	}

	@Override
	public int hashCode() {
		int code = size;
		code *= 31 + aliases.hashCode();
		code *= 31 + featureIndices.hashCode();
		code *= 31 + featureNames.hashCode();
		code *= 31 + constraints.hashCode();
		return code;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj instanceof FeatureSpecification) {
			FeatureSpecification that = (FeatureSpecification) obj;
			return size == that.size
					&& aliases.equals(that.aliases)
					&& featureIndices.equals(that.featureIndices)
					&& featureNames.equals(that.featureIndices)
					&& constraints.equals(that.constraints);
		}
		return false;
	}
	
	public int getSize() {
		return size;
	}
	
	public int getIndex(String featureName) {
		return featureIndices.get(featureName);
	}

	public List<String> getFeatureNames() {
		return Collections.unmodifiableList(featureNames);
	}

	public List<Constraint> getConstraints() {
		return Collections.unmodifiableList(constraints);
	}

	public Segment getSegmentFromFeatures(String string) {
		FeatureArray<Double> map =
				getValueMap(string, size, featureIndices, aliases);
		return new Segment(string, map, this);
	}

	@NotNull
	public SparseFeatureArray<Double> getValueMap(
			String features,
			int size,
			Map<String, Integer> names,
			Map<String, FeatureArray<Double>> aliases) {

		String string = FANCY_PATTERN
				.matcher(features.substring(1, features.length() - 1))
				.replaceAll(Matcher.quoteReplacement("-"));

		SparseFeatureArray<Double> arr = new SparseFeatureArray<Double>(size);

		for (String element : FEATURE_PATTERN.split(string)) {
			Matcher valueMatcher  = VALUE_PATTERN.matcher(element);
			Matcher binaryMatcher = BINARY_PATTERN.matcher(element);

			if (aliases.containsKey(element)) {
				arr.alter(aliases.get(element));
			} else if (valueMatcher.matches()) {
				String featureName  = valueMatcher.group(3);
				String assignment   = valueMatcher.group(2);
				String featureValue = valueMatcher.group(1);
				Integer value = retrieveIndex(featureName, features, names);
				arr.set(value, Double.valueOf(featureValue));
			} else if (binaryMatcher.matches()) {
				String featureName = binaryMatcher.group(2);
				String featureValue = binaryMatcher.group(1);
				Integer value = retrieveIndex(featureName, features, names);
				arr.set(value, featureValue.equals("+") ? 1.0 : -1.0);
			} else {
				throw new ParseException("Unrecognized feature \"" + element
						+ "\" in definition " + features);
			}
		}
		return arr;
	}

	private static Integer retrieveIndex(String label, String features, Map<String, Integer> names) {
		if (names.containsKey(label)) {
			return names.get(label);
		}
		throw new ParseException("Invalid feature label \"" + label
		                         + "\" provided in \"" + features + '"');
	}

	private static class Loader {
		private static final String FEATURES    = "FEATURES";
		private static final String CONSTRAINTS = "CONSTRAINTS";
		private static final String ALIASES     = "ALIASES";

		private static final Pattern ZONE_PATTERN = Pattern.compile(
				FEATURES + '|' +
				CONSTRAINTS + '|' +
				ALIASES
		);

		private static final Pattern COMMENT_PATTERN = Pattern.compile("\\s*%.*");

		private final FeatureSpecification instance;

		private final List<Type>       featureTypes;
		private final List<Constraint> constraints;

		private final Map<String, Integer> featureNames;
		private final Map<String, Integer> featureAliases;

		private Loader(Iterable<String> data) {
			// parse the fields, create raw representations
			// instantiate the specification object w size
			// create correct internal objects with instance
			// add objects back to instance

			featureNames = new ArrayList<String>();
			featureAliases = new ArrayList<String>();
			aliases = new HashMap<String, FeatureArray<Double>>();

			// refactor to method
			Collection<String> featureZone    = new ArrayList<String>();
			Collection<String> constraintZone = new ArrayList<String>();
			Collection<String> aliasZone      = new ArrayList<String>();

			String currentZone = "";
			for (String string : data) {
				String line = COMMENT_PATTERN.matcher(string)
				                             .replaceAll("")
				                             .trim();

				Matcher zoneMatcher = ZONE_PATTERN.matcher(line);
				if (zoneMatcher.find()) {
					currentZone = zoneMatcher.group(0);
				} else if (!line.isEmpty()) {
					if (currentZone.equals(FEATURES)) {
						featureZone.add(line.toLowerCase());
					} else if (currentZone.equals(CONSTRAINTS)) {
						constraintZone.add(line);
					} else if (currentZone.equals(ALIASES)) {
						aliasZone.add(line);
					}
				}
			}

			populateFeatures(featureZone);
			populateAliases(aliasZone);
			populateConstraints(constraintZone);

			instance = new FeatureSpecification(featureNames.size());
			
		}

		public FeatureSpecification getSpecification() {
			return instance;
		}

		private void populateFeatures(Iterable<String> featureZone) {
			int i = 0;
			for (String entry : featureZone) {
				Matcher matcher = FEATURES_PATTERN.matcher(entry);

				if (matcher.matches()) {

					String name  = matcher.group(1);
					String alias = matcher.group(2);
					// Ignore value range checks for now
					String type  = matcher.group(3).replaceAll("\\(.*\\)","");
					
					try { // catch and rethrow if type is invalid\
						featureTypes.add(Type.valueOf(type.toUpperCase()));
					} catch (IllegalArgumentException e) {
						throw new ParseException("Illegal feature type " + type
								+" in definition: " + entry, e);
					}
					
					featureNames.put(name, i);
					featureAliases.put(alias, i);

				} else {
					LOG.error("Unrecognized command in FEATURE block: {}", entry);
					throw new ParseException("Unrecognized command in FEATURE block"
							+ ' ' + entry);
				}
				i++;
			}
			numberOfFeatures = i;
		}

		private void populateAliases(Collection<String> strings) {
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.putAll(featureNames);
			map.putAll(featureAliases);

			for (String string : strings) {
				String[] split = string.split("\\s*=\\s*", 2);

				String alias = split[0].replaceAll("\\[|\\]", "");
				String value = split[1];

				aliases.put(alias, FeatureModel.getValueMap(value, numberOfFeatures, map, aliases));
			}
		}

		private void populateConstraints(Iterable<String> constraintZone) {
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.putAll(featureNames);
			map.putAll(featureAliases);

			for (String entry : constraintZone) {
				String[] split = entry.split("\\s*>\\s*", 2);

				String source = split[0];
				String target = split[1];

				FeatureArray<Double> sMap = FeatureModel.getValueMap(source, size, map, aliases);
				FeatureArray<Double> tMap = FeatureModel.getValueMap(target, size, map, aliases);
				constraints.add(new Constraint(entry, sMap, tMap));
			}
		}
	
	}
}
