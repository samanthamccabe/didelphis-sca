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

package org.didelphis.phonetic.model;

import org.didelphis.exceptions.ParseException;
import org.didelphis.phonetic.Segment;
import org.didelphis.phonetic.features.FeatureArray;
import org.didelphis.phonetic.features.SparseFeatureArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Samantha Fiona Morrigan McCabe Created: 7/2/2016
 */
public final class FeatureSpecification {

	private static final transient Logger LOG =
			LoggerFactory.getLogger(FeatureSpecification.class);
	
	public static final FeatureSpecification EMPTY = new FeatureSpecification(0);

	public static final Double UNDEFINED_VALUE = Double.NaN;

	private static final String VALUE  = "(-?\\d|[A-Zα-ω]+)";
	private static final String NAME   = "(\\w+)";
	private static final String ASSIGN = "([=:><])";
	
	private static final Pattern FEATURES_PATTERN = Pattern.compile(
			"(\\w+)\\s+(\\w*)\\s+(ternary|binary|numeric(\\(-?\\d,\\d\\))?)",
			Pattern.CASE_INSENSITIVE
	);

	private static final Pattern VALUE_PATTERN   = Pattern.compile(VALUE + ASSIGN + NAME);
	private static final Pattern BINARY_PATTERN  = Pattern.compile("(\\+|\\-)" + NAME);
	private static final Pattern FEATURE_PATTERN = Pattern.compile("[,;]\\s*|\\s+");
	private static final Pattern FANCY_PATTERN   = Pattern.compile("−");
	
	private final int size;
	
	private final List<String> featureNames;
	private final List<FeatureType> featureTypes;
	private final List<Constraint> constraints;
	
	private final Map<String, FeatureArray<Double>> aliases;
	private final Map<String, Integer> featureIndices;

	public static FeatureSpecification loadFromClassPath(String path) throws IOException {

		InputStream stream = FeatureSpecification.class
				.getClassLoader()
				.getResourceAsStream(path);
		BufferedReader reader =
				new BufferedReader(new InputStreamReader(stream, "UTF-8"));

		List<String> lines = new ArrayList<String>();
		String line = reader.readLine();
		while (line != null) {
			lines.add(line);
			line = reader.readLine();
		}
		return loadFromString(lines);
	}

	public static FeatureSpecification loadFromFile(String path) throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(path));
		
		List<String> lines = new ArrayList<String>();
		String line = reader.readLine();
		while (line != null) {
			lines.add(line);
			line = reader.readLine();
		}
		return loadFromString(lines);
	}
	
	public static FeatureSpecification loadFromString(List<String> data) {
		Loader loader = new Loader(data);
		return loader.getSpecification();
	}
	
	public static FeatureSpecification loadFromString(String data) {
		List<String> lines = new ArrayList<String>();
		Collections.addAll(lines, data.split("\\r\\n?|\\n"));
		return loadFromString(lines);
	}
	
	private FeatureSpecification(int size) {
		this.size = size;

		featureNames = new ArrayList<String>();
		featureTypes = new ArrayList<FeatureType>();
		constraints = new ArrayList<Constraint>();
		
		aliases = new HashMap<String, FeatureArray<Double>>();
		featureIndices = new HashMap<String, Integer>();
	}
	
	private FeatureSpecification(int size,
		List<String> featureNames,
		List<FeatureType> featureTypes,
		List<Constraint> constraints,
		Map<String, FeatureArray<Double>> aliases,
		Map<String, Integer> featureIndices
	) {
		this.size           = size;
		this.featureNames   = featureNames;
		this.featureTypes   = featureTypes;
		this.constraints    = constraints;
		this.aliases        = aliases;
		this.featureIndices = featureIndices;
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
	
	public int size() {
		return size;
	}
	
	public int getIndex(String featureName) {
		Integer index = featureIndices.get(featureName);
		return index == null ? -1 : index;
	}

	public List<String> getFeatureNames() {
		return Collections.unmodifiableList(featureNames);
	}

	public List<Constraint> getConstraints() {
		return Collections.unmodifiableList(constraints);
	}

	public List<FeatureType> getFeatureTypes() {
		return Collections.unmodifiableList(featureTypes);
	}

	public Segment getSegmentFromFeatures(String string) {
		FeatureArray<Double> array = getFeatureArray(string);
		return new Segment(string, array, this);
	}

	private FeatureArray<Double> getFeatureArray(String features) {
		String string = FANCY_PATTERN
				.matcher(features.substring(1, features.length() - 1))
				.replaceAll(Matcher.quoteReplacement("-"));
		
		FeatureArray<Double> arr = new SparseFeatureArray<Double>(this);
		for (String element : FEATURE_PATTERN.split(string)) {
			Matcher valueMatcher  = VALUE_PATTERN.matcher(element);
			Matcher binaryMatcher = BINARY_PATTERN.matcher(element);

			if (aliases.containsKey(element)) {
				arr.alter(aliases.get(element));
			} else if (valueMatcher.matches()) {
				String featureName  = valueMatcher.group(3);
				String assignment   = valueMatcher.group(2);
				String featureValue = valueMatcher.group(1);
				Integer value = retrieveIndex(featureName, features, featureIndices);
				arr.set(value, Double.valueOf(featureValue));
			} else if (binaryMatcher.matches()) {
				String featureName = binaryMatcher.group(2);
				String featureValue = binaryMatcher.group(1);
				Integer value = retrieveIndex(featureName, features, featureIndices);
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

		private final List<String> featureNames;
		private final List<FeatureType> featureTypes;
		private final List<Constraint> constraints;
		
		private final Map<String, Integer> featureIndices;

		private final Map<String, FeatureArray<Double>> aliases;

		private Loader(Iterable<String> data) {
			// parse the fields, create raw representations
			// instantiate the specification object w size
			// create correct internal objects with instance
			// add objects back to instance

			featureNames = new ArrayList<String>();
			featureTypes = new ArrayList<FeatureType>();
			constraints = new ArrayList<Constraint>();
			
			featureIndices = new HashMap<String, Integer>();
			
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
			// Once the main feature definitions are parsed, it's possible
			// to create the specification instance
			instance = new FeatureSpecification(
					featureNames.size(),
					featureNames,
					featureTypes,
					constraints,
					aliases,
					featureIndices);
			
			populateAliases(aliasZone);
			populateConstraints(constraintZone);
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
						featureTypes.add(FeatureType.valueOf(type.toUpperCase()));
					} catch (IllegalArgumentException e) {
						throw new ParseException("Illegal feature type " + type
								+" in definition: " + entry, e);
					}
					featureNames.add(name);
					featureIndices.put(name, i);
					featureIndices.put(alias, i);

				} else {
					LOG.error("Unrecognized command in FEATURE block: {}", entry);
					throw new ParseException("Unrecognized command in FEATURE block"
							+ ' ' + entry);
				}
				i++;
			}
		}

		private void populateAliases(Collection<String> strings) {
			for (String string : strings) {
				String[] split = string.split("\\s*=\\s*", 2);

				String alias = split[0].replaceAll("\\[|\\]", "");
				String value = split[1];

				aliases.put(alias, instance.getFeatureArray(value));
			}
		}

		private void populateConstraints(Iterable<String> constraintZone) {
			for (String entry : constraintZone) {
				String[] split = entry.split("\\s*>\\s*", 2);

				String source = split[0];
				String target = split[1];

				FeatureArray<Double> sMap = instance.getFeatureArray(source);
				FeatureArray<Double> tMap = instance.getFeatureArray(target);
				constraints.add(new Constraint(entry, sMap, tMap, instance));
			}
		}
	}

	@Override
	public String toString() {
		return "FeatureSpecification{" + featureNames + '}';
	}
}
