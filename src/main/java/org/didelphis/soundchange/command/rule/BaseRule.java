/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.rule;

import org.didelphis.common.language.exceptions.ParseException;
import org.didelphis.common.language.phonetic.Segment;
import org.didelphis.common.language.phonetic.SequenceFactory;
import org.didelphis.common.language.phonetic.features.FeatureArray;
import org.didelphis.common.language.phonetic.features.SparseFeatureArray;
import org.didelphis.common.language.phonetic.model.FeatureModel;
import org.didelphis.common.language.phonetic.model.FeatureSpecification;
import org.didelphis.common.language.phonetic.sequences.Sequence;
import org.didelphis.soundchange.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Samantha Fiona Morrigan McCabe
 * Date: 4/7/13
 * Time: 5:40 PM
 */
public class BaseRule implements Rule {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(BaseRule.class);
	
	private static final Pattern BACKREFERENCE = Pattern.compile("\\$([^$]*)(\\d+)");
	private static final Pattern NOT_PATTERN   = Pattern.compile("\\s*not\\s*", Pattern.CASE_INSENSITIVE);
	private static final Pattern OR_PATTERN    = Pattern.compile("\\s*or\\s*", Pattern.CASE_INSENSITIVE);
	
	private static final Pattern WHITESPACE = Pattern.compile("\\s+");
	private static final Pattern TRANSFORM = Pattern.compile("\\s*>\\s*");

	private final String          ruleText;
	private final List<Condition> conditions;
	private final List<Condition> exceptions;

	private final SequenceFactory factory;
	private final Map<Sequence, Sequence> transform;
	private final RuleMatcher ruleMatcher;

	public BaseRule(String rule, SequenceFactory factoryParam) {
		ruleText = rule;
		factory = factoryParam;
		ruleMatcher = new RuleMatcher();
		transform = new LinkedHashMap<>();
		exceptions = new ArrayList<>();
		conditions = new ArrayList<>();
		parseRule();
	}

	@Override
	public void run() {
		// Does nothing
	}

	@Override
	public Sequence apply(Sequence sequence) {
		// Step through the word to see if the rule might apply, i.e. if the
		// source pattern can be found
		for (int index = 0; index < sequence.size(); ) {
			index = applyAtIndex(sequence, index);
		}
		return sequence;
	}

	@Override
	public int applyAtIndex(Sequence sequence, int index) {
		int startIndex = index;
		boolean unmatched = true;
		// Check each source pattern
		for (Map.Entry<Sequence, Sequence> entry : transform.entrySet()) {
			Sequence source = entry.getKey();
			Sequence target = entry.getValue();
			
			if (startIndex < sequence.size()) {
				ruleMatcher.reset();
				
				int testIndex = startIndex;
				// Step through the current source pattern
				testIndex = matchSource(sequence, source, testIndex);

				// This is checked second for a good reason: it may not be
				// possible to know in advance what is the length of the
				// matching initial until it's been evaluated, esp. in the
				// case of a variable whose elements are allowed to have a
				// length greater than 1. This is allowed because it is
				// possible, or even likely, that a language might have a
				// set of multi-segment clusters which still pattern
				// together, or which are part of conditioning environments.
				if (testIndex >= 0 && conditionsMatch(sequence, startIndex, testIndex)) {
					// Now at this point, if everything worked, we can
					Sequence removed = (startIndex < testIndex) 
						  ? sequence.remove(startIndex, testIndex) 
						  : factory.getNewSequence();
					Sequence replacement = getReplacement(removed, target);
					if (!replacement.isEmpty()) {
						sequence.insert(replacement, startIndex);
					}
					startIndex = testIndex + replacement.size() - removed.size();
					unmatched = false;
				}
			}
		}
		return unmatched ? startIndex+1 : startIndex;
	}

	@Override
	public int hashCode() {
		return Objects.hash(ruleText, conditions, exceptions, factory, transform);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {return true;}
		if (!(o instanceof BaseRule)) {return false;}
		BaseRule baseRule = (BaseRule) o;
		return Objects.equals(ruleText, baseRule.ruleText) 
				       && Objects.equals(conditions, baseRule.conditions) 
				       && Objects.equals(exceptions, baseRule.exceptions)
				       && Objects.equals(factory, baseRule.factory)
				       && Objects.equals(transform, baseRule.transform);
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
		for (Condition exception : exceptions) {
			sb.append(" NOT ");
			sb.append(exception);
		}
		return sb.toString();
	}

	private static List<String> parseToList(String source) {
		List<String> list = new ArrayList<>();
		int start = 0;
		int end   = 0;

		while (end < source.length()) {
			char c = source.charAt(end);
			if (c == ' ') {
				list.add(source.substring(start, end));
				end++;
				start=end;
			} else if (c == '[') {
				end = source.indexOf(']', end);
			} else {
				end++;
			}
		}
		list.add(source.substring(start, end));
		return list;
	}

	private static void balanceTransform(List<String> source, List<String> target, String transformation) {
		if (target.size() > source.size()) {
			throw new ParseException("Target size cannot be greater than " +
					"source size.", transformation);
		} else if (target.size() < source.size()) {
			if (target.size() == 1) {
				String first = target.get(0);
				while (target.size() < source.size()) {
					target.add(first);
				}
			} else {
				throw new ParseException("Target and source sizes may only be" +
						" uneven if target size is exactly 1.", transformation);
			}
		}
	}

	private int matchSource(Sequence sequence, Sequence source, int testIndex) {
		int index = testIndex;
		for (int i = 0; i < source.size() && index >= 0; i++) {
			Sequence subSequence = sequence.subsequence(index);
			Segment segment = source.get(i);

			// Source symbol is a variable
			String symbol = segment.getSymbol();
			if (factory.hasVariable(symbol)) {
				List<Sequence> elements = factory.getVariableValues(symbol);
				boolean elementMatches = false;
				for (int k = 0; k < elements.size() && !elementMatches; k++) {
					Sequence element = elements.get(k);
					if (subSequence.startsWith(element)) {
						ruleMatcher.addIndex(k);
						ruleMatcher.addVariable(symbol);
						ruleMatcher.addSequence(element);
						ruleMatcher.incrementIndex();
						index += element.size();
						elementMatches = true;
					}
				}
				// If none of the variable elements match, fail
				index = elementMatches ? index : -1;
			} else if (segment.isUnderspecified()) {
				// theoretically, this excludes fully specified features, but then
				// why would use use bracket notation for that? just use the symbol

				// Otherwise it's the same as a literal
				if (subSequence.startsWith(segment)) {
					ruleMatcher.addIndex(-1);
					ruleMatcher.addVariable(symbol);
					ruleMatcher.addSequence(subSequence.subsequence(0, 1));
					index++;
				} else {
					index = -1;
				}
			} else if (!symbol.equals("0")) {
				index =
					  subSequence.startsWith(segment) ? index + 1 : -1;
			}
		}
		return index;
	}

	private void parseRule() {
		String transformString;
		// Check-and-parse for conditions
		if (ruleText.contains("/")) {
			String[] array = ruleText.split("/");
			if (array.length <= 1) {
				throw new ParseException("Condition was empty.", ruleText);
			} else {
				transformString = array[0].trim();
				try {
					parseCondition(array[1].trim());
				} catch (ParseException e) {
					throw new ParseException(e.getMessage(), ruleText, e);
				}
			}
		} else {
			transformString = ruleText;
			conditions.add(new Condition("_", factory));
		}
		parseTransform(transformString);
	}

	private void parseCondition(String conditionString) {
		Matcher notMatcher = NOT_PATTERN.matcher(conditionString);
		if (notMatcher.lookingAt()) {
			// if there is no regular condition
			// Takes the first one off, and splits on the rest
			String first = notMatcher.replaceFirst("");
			String[] split = NOT_PATTERN.split(first);

			for (String clause : split) {
				if (OR_PATTERN.matcher(clause).find()) {
					throw new ParseException("OR not allowed following a NOT",
							conditionString);
				}
				exceptions.add(new Condition(clause.trim(), factory));
			}

		} else if (notMatcher.find()) {
			String[] split = NOT_PATTERN.split(conditionString, 2);
			String conditionClauses = split[0];
			String exceptionClauses = split[1];

			for (String con : OR_PATTERN.split(conditionClauses, -1)) {
				conditions.add(new Condition(con, factory));
			}

			for (String exc : NOT_PATTERN.split(exceptionClauses, -1)) {
				exceptions.add(new Condition(exc, factory));
			}
		} else {
			for (String s : OR_PATTERN.split(conditionString, -1)) {
				conditions.add(new Condition(s, factory));
			}
		}
	}

	/**
	 * Generates an appropriate sequence by filling in backreferences based on
	 * the provided maps.
	 *
	 * @param source
	 * @param target the "target" pattern; provides a template of indexed
	 *      variables and backreferences to be filled in
	 * @return a Sequence object with variables and references filled in
	 * according to the provided maps
	 */
	private Sequence getReplacement(Sequence source, Sequence target) {
		int variableIndex = 1;
		Sequence replacement = factory.getNewSequence();
		// Step through the target pattern
		for (int i = 0; i < target.size(); i++) {
			Segment segment = target.get(i);

			String symbol = segment.getSymbol();
			Matcher matcher = BACKREFERENCE.matcher(symbol);
			
			if (matcher.matches()) {
				Sequence sequence = getReference(target, matcher);
				replacement.add(sequence);
			} else if (factory.hasVariable(symbol)) {
				// Allows C > G transformations, where C and G have the same
				// number of elements
				List<Sequence> elements = factory.getVariableValues(symbol);
				Integer anIndex = ruleMatcher.getIndex(variableIndex);
				Sequence sequence = elements.get(anIndex);
				replacement.add(sequence);
				variableIndex++;
			} else if (segment.isUnderspecified()) {
				// Underspecified - overwrite the feature
				Segment alter = source.get(i).alter(segment);
				FeatureArray<Double> features = alter.getFeatures();
				FeatureModel model = factory.getFeatureModel();
				String bestSymbol = model.getBestSymbol(features);
				FeatureSpecification spec = model.getSpecification();
				Segment newSegment = new Segment(bestSymbol, features, spec);
				replacement.add(newSegment);
			} else if (!symbol.equals("0")) {
				// Normal segment and not 0
				replacement.add(segment);
			}
			// Else: it's zero, do nothing
		}
		return replacement;
	}

	// Referent?
	private Sequence getReference(Sequence target, MatchResult matcher) {
		String symbol = matcher.group(1);
		String digits = matcher.group(2);

		int reference = Integer.valueOf(digits);
		int integer = ruleMatcher.getIndex(reference);

		Sequence sequence;
		if (integer == -1) {
			// -1 means it was an underspecified feature
			// but we need to know what was matched
			if (symbol.isEmpty()) {
				sequence = factory.getNewSequence();
				// add the captured segment
				Segment captured = ruleMatcher.getSequence(reference).get(0);
				sequence.add(captured);
			} else {
				throw new RuntimeException("The use of feature substitution " +
						"in this manner is not supported! " + target);
			}
		} else {
			String variable = symbol.isEmpty()
					? ruleMatcher.getVariable(reference)
					: symbol;
			sequence = factory.getVariableValues(variable).get(integer);
		}
		return sequence;
	}

	private boolean conditionsMatch(Sequence word, int start, int end) {
		Iterator<Condition> cI = conditions.iterator();
		Iterator<Condition> eI = exceptions.iterator();

		boolean conditionMatch = false;

		if (cI.hasNext()) {
			while (cI.hasNext() && !conditionMatch) {
				Condition condition = cI.next();
				conditionMatch = condition.isMatch(word, start, end);
			}
		} else {
			conditionMatch = true;
		}

		boolean exceptionMatch = false;
		if (eI.hasNext()) {
			while (eI.hasNext() && !exceptionMatch) {
				Condition exception = eI.next();
				exceptionMatch = exception.isMatch(word, start, end);
			}
		}
		return conditionMatch && !exceptionMatch;
	}

	private void parseTransform(String transformation) {
		if (transformation.contains(">")) {
			String[] array = TRANSFORM.split(transformation);

			if (array.length <= 1) {
				throw new ParseException("Malformed transformation.", 
						transformation);
			} else if (transformation.contains("$[")) {
				throw new ParseException("Malformed transformation! Indexing " +
						"with $[] is not permitted! ", transformation);
			} else {
				String sourceString = WHITESPACE.matcher(array[0]).replaceAll(" ");
				String targetString = WHITESPACE.matcher(array[1]).replaceAll(" ");

				// Split strings, but not within brackets []
				List<String> sourceList = parseToList(sourceString);
				List<String> targetList = parseToList(targetString);

				// fill in target for cases like "a b c > d"
				if (sourceList.contains("0") && (sourceList.size() != 1 || targetList.size() != 1)) {
					throw new ParseException("A rule may only use \"0\" in " +
							"the \"source\" if it is the only symbol in the " +
							"source pattern and the target size is exactly 1",
							transformation
					);
				}
				
				balanceTransform(sourceList, targetList, transformation);

				for (int i = 0; i < sourceList.size(); i++) {
					// Also, we need to correctly tokenize $1, $2 etc or $C1,$N2
					Sequence source = factory.getSequence(sourceList.get(i));
					Sequence target = factory.getSequence(targetList.get(i));
					validateTransform(source, target);
					transform.put(source, target);
				}
			}
		} else {
			throw new ParseException("Missing \">\" sign!", ruleText);
		}
	}

	/**
	 * Once converted to features, ensure that the rule's transform is well-
	 * formed and has an appropriate structure
	 */
	private void validateTransform(Sequence source, Sequence target) {
		int j = 0;
		for (Segment segment : target) {
			FeatureArray<Double> features = segment.getFeatures();
			boolean underspecified = features.contains(null) || features instanceof SparseFeatureArray;
			if (underspecified && source.size() <= j) {
				throw new ParseException("Unmatched underspecified segment in" +
						" target of rule.", ruleText);
			}
			j++;
		}
	}
	
	private static final class RuleMatcher {
		/* Tracks which variable values are matched by the "source" pattern; 
		 * an entry (2 -> 4) would indicate that the source matched the 4th 
		 * value of the 2nd variable. This permits proper mapping between source
		 * and target symbols when using backreferences and indexed variables */
		private final Map<Integer, Integer>  indexMap;
		/* Track which variable in the rule was matched, by symbol */
		private final Map<Integer, String>   variableMap;
		/* The actual sequence matched in the input  */
		private final Map<Integer, Sequence> sequenceMap;
		/* Tracks the order of variables in the "source"
		 * pattern; i.e. the 2nd variable in the source pattern is referenced
		 * via {@code $2}. Unlike standard regular expressions, all variables
		 * are tracked, rather than tracking explicit groups */
		private int referenceIndex;

		private RuleMatcher() {
			referenceIndex = 1;
			indexMap = new HashMap<>();
			variableMap = new HashMap<>();
			sequenceMap = new HashMap<>();
		}

		public void incrementIndex() {
			referenceIndex++;
		}

		private void reset() {
			referenceIndex = 1;
			indexMap.clear();
			variableMap.clear();
			sequenceMap.clear();
		}
		
		private void addIndex(Integer index) {
			indexMap.put(referenceIndex ,index);
		}
		
		private void addVariable(String variable) {
			variableMap.put(referenceIndex, variable);
		}
		
		private void addSequence(Sequence sequence) {
			sequenceMap.put(referenceIndex, sequence);
		}
		
		private Integer getIndex(Integer i) {
			return indexMap.get(i);
		}
		
		private String getVariable(Integer i) {
			return variableMap.get(i);
		}
		
		private Sequence getSequence(Integer i) {
			return sequenceMap.get(i);
		}
	}
}
