/******************************************************************************
 * Copyright (c) 2016. Samantha Fiona McCabe                                  *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *     http://www.apache.org/licenses/LICENSE-2.0                             *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 ******************************************************************************/

package org.haedus.phonetic.model;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.haedus.enums.FormatterMode;
import org.haedus.exceptions.ParseException;
import org.haedus.phonetic.features.FeatureArray;
import org.haedus.phonetic.features.SparseFeatureArray;
import org.haedus.phonetic.features.StandardFeatureArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by samantha on 4/27/15.
 */
public class FeatureModelLoader {

	private static final transient Logger LOG = LoggerFactory.getLogger(FeatureModelLoader.class);

	private static final Pattern ZONE_PATTERN     = Pattern.compile("FEATURES|SYMBOLS|MODIFIERS|WEIGHTS|CONSTRAINTS");
	private static final Pattern COMMENT_PATTERN  = Pattern.compile("\\s*%.*");
	private static final Pattern FEATURES_PATTERN = Pattern.compile("(\\w+)\\s+(\\w*)\\s+(ternary|binary|unary|numeric(\\(-?\\d,\\d\\))?)");

	private static final Pattern SYMBOL_PATTERN = Pattern.compile("(\\S+)\\t(.*)");
	private static final Pattern TAB_PATTERN    = Pattern.compile("\\t");

	private final List<Type> featureTypes  = new ArrayList<Type>();
	private final List<Constraint> constraints = new ArrayList<Constraint>();
	
	private final Map<String, Integer> featureNames   = new LinkedHashMap<String, Integer>();
	private final Map<String, Integer> featureAliases = new LinkedHashMap<String, Integer>();

	private final Map<String, FeatureArray<Double>> featureMap = new LinkedHashMap<String, FeatureArray<Double>>();
	private final Map<String, FeatureArray<Double>> diacritics = new LinkedHashMap<String, FeatureArray<Double>>();

	private final String modelSource;

	private final FormatterMode formatterMode;

	public FeatureModelLoader(File file, FormatterMode modeParam) {
		modelSource = "file:" + file.getAbsolutePath();
		formatterMode = modeParam;
		try {
			readModelFromFileNewFormat(FileUtils.readLines(file, "UTF-8"));
		} catch (IOException e) {
			LOG.error("Failed to read from file {}", file, e);
		}
	}

	public FeatureModelLoader(Iterable<String> file, FormatterMode modeParam) {
		modelSource = file.toString();
		formatterMode = modeParam;
		readModelFromFileNewFormat(file);
	}

	public FeatureModelLoader(InputStream stream, FormatterMode modeParam) {
		modelSource = stream.toString();
		formatterMode = modeParam;

		try {
			readModelFromFileNewFormat(IOUtils.readLines(stream, "UTF-8"));
		} catch (IOException e) {
			LOG.error("Problem while reading from stream {}", stream, e);
		}
	}

	@Override
	public String toString() {
		return "FeatureModelLoader{" + modelSource + '}';
	}

	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	public Map<String, Integer> getFeatureNames() {
		return featureNames;
	}

	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	public Map<String, Integer> getFeatureAliases() {
		return featureAliases;
	}

	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	public Map<String, FeatureArray<Double>> getFeatureMap() {
		return featureMap;
	}

	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	public Map<String, FeatureArray<Double>> getDiacritics() {
		return diacritics;
	}

	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	public List<Constraint> getConstraints() {
		return constraints;
	}

	private void readModelFromFileNewFormat(Iterable<String> file) {
		Zone currentZone = Zone.NONE;
		
		Collection<String> featureZone    = new ArrayList<String>();
		Collection<String> constraintZone = new ArrayList<String>();
		Collection<String> symbolZone     = new ArrayList<String>();
		Collection<String> modifierZone   = new ArrayList<String>();

		/* Probably what we need to do here is use the zones to capture every line up to the next zone
		 * or EOF. Put these in lists, one for each zone. Then parse each zone separately. This will
		 * reduce cyclomatic complexity and should avoid redundant checks.
		 */
		for (String string : file) {
			// Remove comments
			String line = COMMENT_PATTERN.matcher(string).replaceAll("");
			Matcher matcher = ZONE_PATTERN.matcher(line);
			if (matcher.find()) {
				String zoneName = matcher.group(0);
				currentZone = Zone.valueOf(zoneName);
			} else if (!line.isEmpty() && !line.trim().isEmpty()) {
				
				switch(currentZone) {
					case FEATURES:
						featureZone.add(line.toLowerCase());
						break;
					case CONSTRAINTS:
						constraintZone.add(line);
						break;
					case SYMBOLS:
						symbolZone.add(line);
						break;
					case MODIFIERS:
						modifierZone.add(line);
						break;
				}
			}
		}
		// Now parse each of the lists
		populateFeatures(featureZone);
		populateConstraints(constraintZone);
		populateSymbols(symbolZone);
		populateModifiers(modifierZone);
	}

	private void populateConstraints(Iterable<String> constraintZone) {
		for (String entry : constraintZone) {
			String[] split = entry.split("\\s*>\\s*");

			String source = split[0];
			String target = split[1];

			SparseFeatureArray<Double> sMap = FeatureModel.getValueMap(
					source,
					featureAliases,
					featureNames
			);
			SparseFeatureArray<Double> tMap = FeatureModel.getValueMap(
					target,
					featureAliases,
					featureNames
			);
			constraints.add(new Constraint(entry, sMap, tMap));
		}
	}

	private void populateModifiers(Iterable<String> modifierZone) {
		for (String entry : modifierZone) {
			Matcher matcher = SYMBOL_PATTERN.matcher(entry);

			if (matcher.matches()) {
				String symbol = matcher.group(1);
				String[] values = TAB_PATTERN.split(matcher.group(2), -1);

				int size = getNumberOfFeatures();

				FeatureArray<Double> array = new SparseFeatureArray<Double>(size);
				int i = 0;
				for (String value : values) {
					if (!value.isEmpty()) {
						array.set(i, getDouble(value, FeatureModel.MASKING_VALUE));
					}
					i++;
				}
				diacritics.put(symbol, array);
			} else {
				LOG.error("Unrecognized diacritic definition {}", entry);
			}
		}
	}

	private void populateSymbols(Iterable<String> symbolZone) {
		for (String entry : symbolZone) {
			Matcher matcher = SYMBOL_PATTERN.matcher(entry);

			if (matcher.matches()) {
				String symbol = formatterMode.normalize(matcher.group(1));
				String[] values = TAB_PATTERN.split(matcher.group(2), -1);

				int size = getNumberOfFeatures();

				FeatureArray<Double> features = new StandardFeatureArray<Double>(size);
				for (int i = 0; i < featureTypes.size();i++) {
					Type type = featureTypes.get(i);
					String value = values[i];
					if (!type.matches(value)) {
						LOG.warn("Value '{}' at position {} is not valid for {} in array: {}",
							value, i, type, Arrays.toString(values));
					} 
					features.set(i, getDouble(value, FeatureModel.UNDEFINED_VALUE));
				}
				checkFeatureCollisions(symbol, features);
				featureMap.put(symbol, features);
			} else {
				LOG.error("Unrecognized symbol definition {}", entry);
			}
		}
	}

	private int getNumberOfFeatures() {
		return featureNames.size();
	}

	private void checkFeatureCollisions(String symbol, FeatureArray<Double> features) {
		if (featureMap.containsValue(features)) {
			for (Map.Entry<String, FeatureArray<Double>> e : featureMap.entrySet()) {
				if (features.equals(e.getValue())) {
					LOG.warn("Collision between features {} and {} --- " +
							"both have value {}", symbol, e.getKey(), features);
				}
			}
		}
	}

	private void populateFeatures(Iterable<String> featureZone) {
		int i = 0;
		for (String entry : featureZone) {
			Matcher matcher = FEATURES_PATTERN.matcher(entry);

			if (matcher.matches()) {

				String name  = matcher.group(1);
				String alias = matcher.group(2);
				String type = matcher.group(3).replaceAll("\\(.*\\)","");
				
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
	}

	private static double getDouble(String cell, Double defaultValue) {
		double featureValue;
		if (cell.isEmpty()) {
			featureValue = defaultValue;
		} else if (cell.equals("+")) {
			featureValue = 1.0;
		} else if (cell.equals("-") || cell.equals("−")) {
			featureValue = -1.0;
		} else {
			featureValue = Double.valueOf(cell);
		}
		return featureValue;
	}

	private enum Zone {
		FEATURES("FEATURES"),
		CONSTRAINTS("CONSTRAINTS"),
		SYMBOLS("SYMBOLS"),
		MODIFIERS("MODIFIERS"),
		NONE("NONE");

		private final String value;

		Zone(String v) {
			value = v;
		}

		String value() {
			return value;
		}
	}
	
	private enum Type {
		UNARY("(\\+)?"),
		BINARY("(\\+|-|−)?"),
		TERNARY("(\\+|-|−|0)?"),
		NUMERIC("(-?\\d+)?");

		private Pattern pattern;

		Type(String value) {
			pattern = Pattern.compile(value);
		}
		
		boolean matches(CharSequence value) {
			return pattern.matcher(value).matches();
		}
	}
}
