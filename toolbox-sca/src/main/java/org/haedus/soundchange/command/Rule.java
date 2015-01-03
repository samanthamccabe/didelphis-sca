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

package org.haedus.soundchange.command;

import com.google.common.annotations.VisibleForTesting;
import org.haedus.datatypes.SegmentationMode;
import org.haedus.datatypes.phonetic.FeatureModel;
import org.haedus.datatypes.phonetic.Segment;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.SequenceFactory;
import org.haedus.datatypes.phonetic.VariableStore;
import org.haedus.soundchange.Condition;
import org.haedus.soundchange.exceptions.RuleFormatException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Samantha Fiona Morrigan McCabe
 * Date: 4/7/13
 * Time: 5:40 PM
 */
public class Rule implements Command {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(Rule.class);

	private static final Pattern BACKREFERENCE      = Pattern.compile("\\$([^\\$]*)(\\d+)");
	private static final Pattern NOT_PATTERN        = Pattern.compile("\\s+NOT\\s+");
	private static final Pattern OR_PATTERN         = Pattern.compile("\\s+OR\\s+");
	private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
	private static final Pattern TRANSFORM_PATTERN  = Pattern.compile("\\s*>\\s*");

	private final String          ruleText;
	private final List<Condition> conditions;
	private final List<Condition> exceptions;

	private final SequenceFactory factory;

	private final Map<Sequence, Sequence> transform;

	private final Map<String, List<List<Sequence>>> lexicons;

	public Rule(String rule) {
		this(rule, new VariableStore(), SegmentationMode.DEFAULT);
	}

	public Rule(String rule, VariableStore variables, SegmentationMode mode) {
		this(rule, new HashMap<String, List<List<Sequence>>>(), new FeatureModel(), variables, mode);
	}

	public Rule(String rule, FeatureModel model, VariableStore variables, SegmentationMode mode) {
		this(rule, new HashMap<String, List<List<Sequence>>>(), model, variables, mode);
	}

	public Rule(String rule, Map<String, List<List<Sequence>>> lexiconsParam, SequenceFactory factoryParam) {
		ruleText   = rule;
		lexicons   = lexiconsParam;
		factory    = factoryParam;
		transform  = new LinkedHashMap<Sequence, Sequence>();
		exceptions = new ArrayList<Condition>();
		conditions = new ArrayList<Condition>();

		populateConditions();
	}

	@Deprecated
	public Rule(String rule, Map<String, List<List<Sequence>>> lexiconsParam, FeatureModel model, VariableStore variables, SegmentationMode mode) throws RuleFormatException {
		this(rule, lexiconsParam, new SequenceFactory(model, variables, mode));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Sequence sequence : transform.keySet()) {
			sb.append(sequence);
			sb.append(' ');
		}
		sb.append("> ");
		for (Sequence sequence : transform.values()) {
			sb.append(sequence);
			sb.append(' ');
		}
		sb.append("/ ");
		for (int i = 0; i < conditions.size(); i++) {
			sb.append(conditions.get(i));
			if (i < conditions.size() - 1) {
				sb.append(" OR ");
			}
		}
		return sb.toString();
	}

	@Override
	public void execute() {
		for (List<List<Sequence>> lexicon : lexicons.values()) {
			for (List<Sequence> row : lexicon) {
				for (int i = 0; i < row.size(); i++) {
					Sequence word = row.get(i);
					row.set(i, apply(word));
				}
			}
		}
	}

	@VisibleForTesting
	Sequence apply(Sequence input) {
		Sequence output = new Sequence(input);
		// Step through the word to see if the rule might apply, i.e. if the source pattern can be found
		for (int index = 0; index < output.size(); ) {
			int startIndex = index;
			boolean noMatch = true;
			// Check each source pattern
			for (Map.Entry<Sequence, Sequence> entry : transform.entrySet()) {
				Sequence source = entry.getKey();
				Sequence target = entry.getValue();

				if (index < output.size()) {

					Map<Integer, Integer> indexMap    = new HashMap<Integer, Integer>();
					Map<Integer, String>  variableMap = new HashMap<Integer, String>();

					// Step through the source pattern
					int referenceIndex = 1;
					int testIndex = index;
					boolean match = true;
					for (int i = 0; i < source.size() && match; i++) {
						Sequence subSequence = output.getSubsequence(testIndex);
						Segment segment = source.get(i);
						if (factory.hasVariable(segment.getSymbol())) {
							List<Sequence> elements = factory.getVariableValues(segment.getSymbol());
							boolean elementMatches = false;
							for (int k = 0; k < elements.size() && !elementMatches; k++) {
								Sequence element = elements.get(k);
								if (subSequence.startsWith(element)) {
									indexMap.put(referenceIndex, k);
									variableMap.put(referenceIndex, segment.getSymbol());

									referenceIndex++;
									testIndex += element.size();
									elementMatches = true;
								}
							}
							match = elementMatches;
						} else {
							// It' a literal
							match = subSequence.startsWith(segment);
							if (match) {
								testIndex++;
							}
						}
					}

					if (match && conditionsMatch(output, startIndex, testIndex)) {
						index = testIndex;
						// Now at this point, if everything worked, we can
						Sequence removed = output.remove(startIndex, index);
						// Generate replacement
						Sequence replacement = getReplacementSequence(target, variableMap, indexMap);
						noMatch = false;
						if (!replacement.isEmpty()) {
							output.insert(replacement, startIndex);
						}
						index += replacement.size() - removed.size();
						startIndex = index;
					}
				}
			}
			if (noMatch) {
				index++;
			}
		}
		return output;
	}

	private void populateConditions() {
		String transformString;
		// Check-and-parse for conditions
		if (ruleText.contains("/")) {
			String[] array = ruleText.split("/");
			if (array.length <= 1) {
				throw new RuleFormatException("Condition was empty!");
			} else {
				transformString = array[0].trim();

				String conditionString = array[1].trim();
				if (conditionString.contains("NOT")) {
					String[] split = NOT_PATTERN.split(conditionString);
					if (split.length == 2) {
						for (String con : OR_PATTERN.split(split[0])) {
							conditions.add(new Condition(con, factory));
						}
						for (String exc : OR_PATTERN.split(split[1])) {
							exceptions.add(new Condition(exc, factory));
						}
					} else {
						throw new RuleFormatException("Illegal NOT expression in " + ruleText);
					}
				} else {
					for (String s : OR_PATTERN.split(conditionString)) {
						conditions.add(new Condition(s, factory));
					}
				}
			}
		} else {
			transformString = ruleText;
			conditions.add(new Condition("_", factory));
		}
		parseTransform(transformString);
	}

	/**
	 * Generates an appropriate sequence by filling in backreferences based on the provided maps.
	 * @param target the "target" pattern; provides a template of indexed variables and backreferences to be filled in
	 * @param variableMap Tracks the order of variables used in the "source" pattern; i.e. the 2nd variable in the source
	 *                    pattern is referenced via {@code $2}. Unlike standard regular expressions, all variables are
	 *                    tracked, rather than tracking explicit groups
	 * @param indexMap tracks which variable values are matched by the "source" pattern; a value of (2 -> 4) would
	 *                 indicate that the source matched the 4th value of the 2nd variable. This permits proper mapping
	 *                 between source and target symbols when using backreferences and indexed variables
	 * @return a Sequence object with variables and references filled in according to the provided maps
	 */
	private Sequence getReplacementSequence(Sequence target, Map<Integer, String> variableMap, Map<Integer, Integer> indexMap) {
		int variableIndex = 1;
		Sequence replacement = factory.getNewSequence();
		// Step through the target pattern
		for (int i = 0; i < target.size(); i++) {
			Segment segment = target.get(i);

			Matcher matcher = BACKREFERENCE.matcher(segment.getSymbol());
			if (matcher.matches()) {

				String symbol = matcher.group(1);
				String digits = matcher.group(2);

				int reference = Integer.valueOf(digits);
				int integer = indexMap.get(reference);

				String variable;
				if (symbol.isEmpty()) {
					variable = variableMap.get(reference);
				} else {
					variable = symbol;
				}

				Sequence sequence = factory.getVariableValues(variable).get(integer);
				replacement.add(sequence);
			} else if (factory.hasVariable(segment.getSymbol())) {
				List<Sequence> elements = factory.getVariableValues(segment.getSymbol());
				Integer anIndex = indexMap.get(variableIndex);
				Sequence sequence = elements.get(anIndex);
				replacement.add(sequence);
				variableIndex++;
			} else if (!segment.getSymbol().equals("0")) {
				replacement.add(segment);
			}
		}
		return replacement;
	}

	private boolean conditionsMatch(Sequence word, int startIndex, int endIndex) {
		boolean conditionMatch = false;
		boolean exceptionMatch = false;
		Iterator<Condition> cI = conditions.iterator();
		while (cI.hasNext() && !conditionMatch) {
			Condition condition = cI.next();
			conditionMatch = condition.isMatch(word, startIndex, endIndex);
		}
		Iterator<Condition> eI = exceptions.iterator();
		while (eI.hasNext() && !exceptionMatch) {
			Condition exception = eI.next();
			exceptionMatch = exception.isMatch(word, startIndex, endIndex);
		}
		return conditionMatch && !exceptionMatch;
	}

	private void parseTransform(String transformation) {
		if (transformation.contains(">")) {
			String[] array = TRANSFORM_PATTERN.split(transformation);

			if (array.length <= 1) {
				throw new RuleFormatException("Malformed transformation! " + transformation);
			} else {
				List<String> sourceString = new ArrayList<String>();
				List<String> targetString = new ArrayList<String>();

				Collections.addAll(sourceString, WHITESPACE_PATTERN.split(array[0]));
				Collections.addAll(targetString, WHITESPACE_PATTERN.split(array[1]));

				balanceTransform(sourceString, targetString);

				for (int i = 0; i < sourceString.size(); i++) {
					// Also, we need to correctly tokenize $1, $2 etc or $C1,$N2
					Sequence source = factory.getSequence(sourceString.get(i));
					Sequence target = factory.getSequence(targetString.get(i));

					transform.put(source, target);
				}
			}
		} else {
			throw new RuleFormatException("Rule missing \">\" sign! " + ruleText);
		}
	}

	private static void balanceTransform(List<String> source, List<String> target) {
		if (target.size() > source.size()) {
			throw new RuleFormatException("Source/Target totalSize error! " + source + " < " + target);
		}
		if (target.size() < source.size()) {
			if (target.size() == 1) {
				String first = target.get(0);
				while (target.size() < source.size()) {
					target.add(first);
				}
			} else {
				throw new RuleFormatException("Source/Target totalSize error! " + source + " > " + target + " and target totalSize is greater than 1!");
			}
		}
	}
}
