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

package org.haedus.soundchange;

import org.haedus.datatypes.Segmenter;
import org.haedus.datatypes.phonetic.FeatureModel;
import org.haedus.datatypes.phonetic.Segment;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.VariableStore;
import org.haedus.soundchange.exceptions.RuleFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Samantha Fiona Morrigan McCabe
 * Date: 4/7/13
 * Time: 5:40 PM
 */
public class Rule {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(Rule.class);

	private static final Pattern BACKREFERENCE = Pattern.compile("\\$([^\\$]*)(\\d+)");

	private final String                  ruleText;
	private final Map<Sequence, Sequence> transform;
	private final List<Condition>         conditions;
	private final List<Condition>         exceptions;
	private final VariableStore           variableStore;
	private final FeatureModel            featureModel;

	public Rule(String rule) throws RuleFormatException {
		this(rule, new VariableStore(), true);
	}

	public Rule(String rule, VariableStore variables, boolean useSegmentation) throws RuleFormatException {
		this(rule, new FeatureModel(), variables, useSegmentation);
	}

	public Rule(String rule, FeatureModel model, VariableStore variables, boolean useSegmentation) throws RuleFormatException {
		ruleText      = rule;
		variableStore = variables;
		featureModel  = model;
		transform     = new LinkedHashMap<Sequence, Sequence>();
		exceptions    = new ArrayList<Condition>();
		conditions    = new ArrayList<Condition>();

		String transform;
		// Check-and-parse for conditions
		if (ruleText.contains("/")) {
			String[] array = ruleText.split("/");
			if (array.length <= 1) {
				throw new RuleFormatException("Condition was empty!");
			} else {
				transform = array[0].trim();

				String conditionString = array[1].trim();
				if (conditionString.contains("NOT")) {
					String[] split = conditionString.split("\\s+NOT\\s+");
					if (split.length == 2) {
						for (String con : split[0].split("\\s+OR\\s+")) {
							conditions.add(new Condition(con, variableStore, model));
						}
						for (String exc : split[1].split("\\s+OR\\s+")) {
							exceptions.add(new Condition(exc, variableStore, model));
						}
					} else {
						throw new RuleFormatException("Illegal NOT expression in " + ruleText);
					}
				} else {
					for (String s : conditionString.split("\\s+OR\\s+")) {
						conditions.add(new Condition(s, variableStore, model));
					}
				}
			}
		} else {
			transform = ruleText;
			conditions.add(new Condition());
		}
		parseTransform(transform, useSegmentation);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Sequence sequence : transform.keySet()) {
			sb.append(sequence.toStringClean());
			sb.append(" ");
		}
		sb.append("> ");
		for (Sequence sequence : transform.values()) {
			sb.append(sequence.toStringClean());
			sb.append(" ");
		}
		sb.append("/ ");
		for (int i = 0; i < conditions.size(); i++) {
			sb.append(conditions.get(i).toString());
			if (i < conditions.size() - 1) {
				sb.append(" OR ");
			}
		}
		return sb.toString();
	}

	public void execute(SoundChangeApplier sca) {
		for (List<Sequence> lexicon : sca.getLexicons()) {
			for (int i = 0; i < lexicon.size(); i++) {
				Sequence word = lexicon.get(i);
				lexicon.set(i, apply(word));
			}
		}
	}

	public Sequence apply(Sequence input) {
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

					Map<Integer, Integer> indexMap = new HashMap<Integer, Integer>();
					Map<Integer, String> variableMap = new HashMap<Integer, String>();

					// Step through the source pattern
					int referenceIndex = 1;
					int testIndex = index;
					boolean match = true;
					for (int i = 0; i < source.size() && match; i++) {
						Sequence subSequence = output.getSubsequence(testIndex);
						Segment segment = source.get(i);
						if (variableStore.contains(segment.getSymbol())) {
							List<Sequence> elements = variableStore.get(segment.getSymbol());
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
						Sequence replacement = getReplacementSequence(target, indexMap, variableMap);
						noMatch = false;
						if (replacement.size() > 0) {
							output.insert(replacement, startIndex);
						}
						index = index + (replacement.size() - removed.size());
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

	private Sequence getReplacementSequence(Sequence target, Map<Integer, Integer> indexMap, Map<Integer, String> variableMap) {
		int variableIndex = 1;
		Sequence replacement = new Sequence(new ArrayList<String>(), featureModel);
		// Step through the target pattern
		for (int i = 0; i < target.size(); i++) {
			Segment segment = target.get(i);

			Matcher matcher = BACKREFERENCE.matcher(segment.getSymbol());
			if (matcher.matches()) {

				String symbol = matcher.group(1);
				String digits = matcher.group(2);

				int reference = Integer.valueOf(digits);
				int integer   = indexMap.get(reference);

				String variable;
				if (symbol.isEmpty()) {
					variable = variableMap.get(reference);
				} else {
					variable = symbol;
				}

				Sequence sequence = variableStore.get(variable).get(integer);
				replacement.add(sequence);
			} else if (variableStore.contains(segment.getSymbol())) {
				List<Sequence> elements = variableStore.get(segment.getSymbol());
				Integer  anIndex  = indexMap.get(variableIndex);
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

	private void parseTransform(String transformation, boolean useSegmentation) throws RuleFormatException {
		if (transformation.contains(">")) {
			String[] array = transformation.split("\\s*>\\s*");

			if (array.length <= 1) {
				throw new RuleFormatException("Malformed transformation! " + transformation);
			} else {
				List<String> sourceString = new ArrayList<String>();
				List<String> targetString = new ArrayList<String>();

				Collections.addAll(sourceString, array[0].split("\\s+"));
				Collections.addAll(targetString, array[1].split("\\s+"));

				balanceTransform(sourceString, targetString);

				for (int i = 0; i < sourceString.size(); i++) {
					// Also, we need to correctly tokenize $1, $2 etc or $C1,$N2
					Sequence source = Segmenter.getSequence(sourceString.get(i), featureModel, variableStore, useSegmentation);
					Sequence target = Segmenter.getSequence(targetString.get(i), featureModel, variableStore, useSegmentation);

					transform.put(source, target);
				}
			}
		} else {
			throw new RuleFormatException("Rule missing \">\" sign! " + ruleText);
		}
	}

	private void balanceTransform(List<String> source, List<String> target) throws RuleFormatException {
		if (target.size() > source.size()) {
			throw new RuleFormatException("Source/Target size error! " + source + " < " + target);
		}
		if (target.size() < source.size()) {
			if (target.size() == 1) {
				String first = target.get(0);
				while (target.size() < source.size()) {
					target.add(first);
				}
			} else {
				throw new RuleFormatException("Source/Target size error! " + source + " > " + target + " and target size is greater than 1!");
			}
		}
	}
}
