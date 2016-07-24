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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by samantha on 4/27/15.
 */
public class FeatureModelLoader {

	private static final transient Logger LOG =
			LoggerFactory.getLogger(FeatureModelLoader.class);

	private static final String SYMBOLS     = "SYMBOLS";
	private static final String MODIFIERS   = "MODIFIERS";
	private static final String ZONE_STRING = SYMBOLS + '|' + MODIFIERS;

	private static final Pattern COMMENT_PATTERN  = Pattern.compile("\\s*%.*");
	private static final Pattern ZONE_PATTERN     = Pattern.compile(ZONE_STRING);

	private static final Pattern SYMBOL_PATTERN = Pattern.compile("(\\S+)\\t(.*)");
	private static final Pattern TAB_PATTERN    = Pattern.compile("\\t");
	private static final Pattern SPEC_PATTERN   = Pattern.compile("SPECIFICATION:\\s\"([^\"])\"");
	
	private final String        sourcePath;
	private final FormatterMode formatterMode;
	
	private FeatureSpecification specification;
	
	private Map<String, FeatureArray<Double>> featureMap;
	private Map<String, FeatureArray<Double>> diacritics;

	public FeatureModelLoader(File file, FormatterMode modeParam) {
		sourcePath = file.getAbsolutePath();
		formatterMode = modeParam;
		featureMap = new LinkedHashMap<String, FeatureArray<Double>>();
		diacritics = new LinkedHashMap<String, FeatureArray<Double>>();
		try {
			readModelFromFileNewFormat(FileUtils.readLines(file, "UTF-8"), false);
		} catch (IOException e) {
			LOG.error("Failed to read from file {}", file, e);
		}
	}

	public FeatureModelLoader(String path, Iterable<String> file, FormatterMode modeParam) {
		sourcePath = path;
		formatterMode = modeParam;
		featureMap = new LinkedHashMap<String, FeatureArray<Double>>();
		diacritics = new LinkedHashMap<String, FeatureArray<Double>>();
		try {
			readModelFromFileNewFormat(file, false);
		} catch (IOException e) {
			LOG.error("Problem while reading from path {}", path, e);
		}
	}

	public FeatureModelLoader(InputStream stream, FormatterMode modeParam) {
		sourcePath = stream.toString();
		formatterMode = modeParam;
		featureMap = new LinkedHashMap<String, FeatureArray<Double>>();
		diacritics = new LinkedHashMap<String, FeatureArray<Double>>();
		try {
			readModelFromFileNewFormat(IOUtils.readLines(stream, "UTF-8"), true);
		} catch (IOException e) {
			LOG.error("Problem while reading from stream {}", stream, e);
		}
	}

	@Override
	public String toString() {
		return "FeatureModelLoader{" + sourcePath + '}';
	}

	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	public Map<String, FeatureArray<Double>> getFeatureMap() {
		return featureMap;
	}

	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	public Map<String, FeatureArray<Double>> getDiacritics() {
		return diacritics;
	}

	public FeatureSpecification getSpecification() {
		return specification;
	}

	private void readModelFromFileNewFormat(Iterable<String> file, boolean fromClassPath) throws IOException {
		String currentZone = "";

		specification = null;
		
		List<String> specZone = new ArrayList<String>();
		List<String> symbolZone = new ArrayList<String>();
		List<String> modifierZone = new ArrayList<String>();

		/* Probably what we need to do here is use the zones to capture every
		 * line up to the next zone or EOF. Put these in lists, one for each
		 * zone. Then parse each zone separately. This will reduce cyclomatic
		 * complexity and should avoid redundant checks.
		 */
		for (String string : file) {
			// Remove comments
			String line = COMMENT_PATTERN.matcher(string).replaceAll("");
			Matcher specMatcher = SPEC_PATTERN.matcher(line);
			Matcher zoneMatcher = ZONE_PATTERN.matcher(line);
			if (zoneMatcher.find()) {
				currentZone = zoneMatcher.group(0);
			} else if (specMatcher.find()) {
				String path = specMatcher.group(1);
				if (fromClassPath) {
					specification = FeatureSpecification.loadFromClassPath(path);
				} else {
					String newPath;
					if (path.startsWith("(\\w:)?/")) {
						newPath = path;
					} else {
						String parent = new File(sourcePath).getCanonicalFile().getParent();
						newPath = parent + '/' + path;
					}
					specification = FeatureSpecification.loadFromFile(newPath);
				}
			} else if (!line.isEmpty() && !line.trim().isEmpty()) {
				if (currentZone.isEmpty()) {
					specZone.add(line);
				} else if (currentZone.equals(SYMBOLS)) {
					symbolZone.add(line);
				} else if (currentZone.equals(MODIFIERS)) {
					modifierZone.add(line);
				}
			} else {
				// ignore and log (at the end?)
			}
		}
		
		if (specification == null) {
			specification = FeatureSpecification.loadFromString(specZone);
		}
		
		// Now parse each of the lists
		populateSymbols(symbolZone);
		populateModifiers(modifierZone);
	}

	private void populateModifiers(Iterable<String> modifierZone) {
		for (String entry : modifierZone) {
			Matcher matcher = SYMBOL_PATTERN.matcher(entry);

			if (matcher.matches()) {
				String symbol = matcher.group(1);
				String[] values = TAB_PATTERN.split(matcher.group(2), -1);
				
				FeatureArray<Double> array = new SparseFeatureArray<Double>(specification);
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

				int size = specification.size();
				
				List<FeatureType> featureTypes = specification.getFeatureTypes();
				FeatureArray<Double> features = new StandardFeatureArray<Double>(specification);
				for (int i = 0; i < size; i++) {
					FeatureType type = featureTypes.get(i);
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

	private List<Double> getUnderspecifiedArray() {
		List<Double> list = new ArrayList<Double>();
		int size = specification.size();
		for (int i = 0; i < size; i++) {
			list.add(FeatureModel.MASKING_VALUE);
		}
		return list;
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
}
