package org.haedus.phonetic;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.haedus.enums.FormatterMode;
import org.haedus.exceptions.ParseException;
import org.haedus.tables.SymmetricTable;
import org.haedus.tables.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
 * Created by samantha on 4/27/15.
 */
@SuppressWarnings("ReturnOfCollectionOrArrayField")
public class FeatureModelLoader {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(FeatureModelLoader.class);

	private static final Pattern ZONE_PATTERN       = Pattern.compile("FEATURES|SYMBOLS|MODIFIERS|WEIGHTS");
	private static final Pattern NEWLINE_PATTERN    = Pattern.compile("(\\r?\\n|\\n)");
	private static final Pattern COMMENT_PATTERN    = Pattern.compile("\\s*%.*");
	private static final Pattern FEATURES_PATTERN   = Pattern.compile("(\\w+)\\s+(\\w*)\\s*(binary|unary|numeric\\(-?\\d,\\d\\))");
	private static final Pattern SYMBOL_PATTERN     = Pattern.compile("(\\S+)\\t(.*)", Pattern.UNICODE_CHARACTER_CLASS);
	private static final Pattern TAP_PATTERN        = Pattern.compile("\\t");
	private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\r?\\n|\\r");

	private final Map<String, Integer>      featureNames   = new HashMap<String, Integer>();
	private final Map<String, Integer>      featureAliases = new HashMap<String, Integer>();
	private final Map<String, List<Double>> featureMap     = new LinkedHashMap<String, List<Double>>();
	private final Map<String, List<Double>> diacritics     = new LinkedHashMap<String, List<Double>>();

	private final FormatterMode formatterMode;

	public FeatureModelLoader(File file, FormatterMode modeParam) {
		formatterMode = modeParam;
		try {
			readModelFromFileNewFormat(FileUtils.readLines(file, "UTF-8"));
		} catch (IOException e) {
			LOGGER.error("Failed to read from file {}", file, e);
		}
	}

	public FeatureModelLoader(Iterable<String> file, FormatterMode modeParam) {
		formatterMode = modeParam;
		readModelFromFileNewFormat(file);
	}

	public FeatureModelLoader(InputStream stream, FormatterMode modeParam) {
		formatterMode = modeParam;
		
		try {
			readModelFromFileNewFormat(IOUtils.readLines(stream, "UTF-8"));
		} catch (IOException e) {
			LOGGER.error("Problem while reading from stream {}", stream, e);
		}
	}

	public Map<String, Integer> getFeatureNames() {
		return featureNames;
	}

	public Map<String, Integer> getFeatureAliases() {
		return featureAliases;
	}

	public Map<String, List<Double>> getFeatureMap() {
		return featureMap;
	}

	public Map<String, List<Double>> getDiacritics() {
		return diacritics;
	}

	private void readModelFromFileNewFormat(Iterable<String> file) {
		Zone currentZone = Zone.NONE;
		
		Collection<String> featureZone  = new ArrayList<String>();
		Collection<String> symbolZone   = new ArrayList<String>();
		Collection<String> modifierZone = new ArrayList<String>();

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
			} else if (!line.isEmpty()) {
				if (currentZone == Zone.FEATURES) {
					featureZone.add(line.toLowerCase());
				} else if (currentZone == Zone.SYMBOLS) {
					symbolZone.add(line);
				} else if (currentZone == Zone.MODIFIERS) {
					modifierZone.add(line);
				}
			}
		}
		// Now parse each of the lists
		populateFeatures(featureZone);
		populateSymbols(symbolZone);
		populateModifiers(modifierZone);
	}

	private void populateModifiers(Iterable<String> modifierZone) {
		for (String entry : modifierZone) {
			Matcher matcher = SYMBOL_PATTERN.matcher(entry);

			if (matcher.matches()) {
				String symbol = matcher.group(1);
				String[] values = matcher.group(2).split("\\t", -1);

				List<Double> features = new ArrayList<Double>();
				for (String value : values) {
					features.add(getDouble(value, FeatureModel.MASKING_VALUE));
				}
				diacritics.put(symbol, features);
			} else {
				LOGGER.error("Unrecognized diacritic definition {}", entry);
			}
		}
	}

	private void populateSymbols(Iterable<String> symbolZone) {
		for (String entry : symbolZone) {
			Matcher matcher = SYMBOL_PATTERN.matcher(entry);

			if (matcher.matches()) {
				String symbol = formatterMode.normalize(matcher.group(1));
				String[] values = TAP_PATTERN.split(matcher.group(2), -1);

				List<Double> features = new ArrayList<Double>();
				for (String value : values) {
					features.add(getDouble(value, FeatureModel.UNDEFINED_VALUE));
				}
				featureMap.put(symbol, features);
			} else {
				LOGGER.error("Unrecognized symbol definition {}", entry);
			}
		}
	}

	private void populateFeatures(Iterable<String> featureZone) {
		int i = 0;
		for (String entry : featureZone) {
			Matcher matcher = FEATURES_PATTERN.matcher(entry);

			if (matcher.matches()) {

				String name = matcher.group(1);
				String alias = matcher.group(2);
				String type = matcher.group(3);

				featureNames.put(name, i);
				featureAliases.put(alias, i);

			} else {
				LOGGER.error("Unrecognized command in FEATURE block: {}", entry);
				throw new ParseException("Unrecognized command in FEATURE block: " + entry);
			}
			i++;
		}
	}

	private static double getDouble(String cell, double defaultValue) {
		double featureValue;
		if (cell.isEmpty()) {
			featureValue = defaultValue;
		} else if (cell.equals("+")) {
			featureValue = 1.0;
		} else if (cell.equals("-")) {
			featureValue = -1.0;
		} else {
			featureValue = Double.valueOf(cell);
		}
		return featureValue;
	}

	private enum Zone {
		FEATURES("FEATURES"),
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
}
