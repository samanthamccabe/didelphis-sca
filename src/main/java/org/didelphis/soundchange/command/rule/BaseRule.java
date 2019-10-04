/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.rule;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import org.didelphis.language.automata.Regex;
import org.didelphis.language.automata.matching.Match;
import org.didelphis.language.parsing.ParseException;
import org.didelphis.language.phonetic.SequenceFactory;
import org.didelphis.language.phonetic.features.FeatureArray;
import org.didelphis.language.phonetic.features.FeatureType;
import org.didelphis.language.phonetic.features.SparseFeatureArray;
import org.didelphis.language.phonetic.model.FeatureMapping;
import org.didelphis.language.phonetic.model.FeatureModel;
import org.didelphis.language.phonetic.segments.Segment;
import org.didelphis.language.phonetic.segments.StandardSegment;
import org.didelphis.language.phonetic.sequences.BasicSequence;
import org.didelphis.language.phonetic.sequences.Sequence;
import org.didelphis.soundchange.Condition;
import org.didelphis.soundchange.VariableStore;
import org.didelphis.soundchange.parser.ParserMemory;
import org.didelphis.utilities.Templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @since 0.0.0
 */
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BaseRule<T> implements Rule<T> {

	private static final Regex BACKREF   = new Regex("\\$([^$]*)(\\d+)");
	private static final Regex NOT       = new Regex("\\s*not\\s*", true);
	private static final Regex OR        = new Regex("\\s*or\\s*", true);
	private static final Regex SPACE     = new Regex("\\s+");
	private static final Regex TRANSFORM = new Regex("\\s*>\\s*");

	String ruleText;

	@Getter List<Condition<T>> conditions;
	@Getter List<Condition<T>> exceptions;

	SequenceFactory<T> factory;
	Map<Sequence<T>, Sequence<T>> transform;
	RuleMatcher<T> ruleMatcher;
	VariableStore variables;

	public BaseRule(String rule, ParserMemory<T> memory) {
		ruleText = rule;
		variables = memory.getVariables();
		factory = memory.factorySnapshot();
		ruleMatcher = new RuleMatcher<>();
		transform = new LinkedHashMap<>();
		exceptions = new ArrayList<>();
		conditions = new ArrayList<>();
		parseRule();
	}

	BaseRule(String rule, VariableStore variables, SequenceFactory<T> factory) {
		this.factory = factory;
		this.variables = variables;

		ruleText = rule;
		ruleMatcher = new RuleMatcher<>();
		transform = new LinkedHashMap<>();
		exceptions = new ArrayList<>();
		conditions = new ArrayList<>();
		parseRule();
	}

	BaseRule(String rule, SequenceFactory<T> factory) {
		this(rule, new VariableStore(), factory);
	}

	@Override
	public int applyAtIndex(Sequence<T> sequence, int index) {
		int startIndex = index;
		boolean unmatched = true;
		FeatureModel<T> model = factory.getFeatureMapping().getFeatureModel();

		// Check each source pattern
		for (Entry<Sequence<T>, Sequence<T>> entry : transform.entrySet()) {
			Sequence<T> source = entry.getKey();
			Sequence<T> target = entry.getValue();

			if (startIndex < sequence.size()) {
				ruleMatcher.reset();

				int testIndex = startIndex;

				// Step through the current source pattern
				testIndex = matchSource(sequence, source, testIndex);

				// This is checked second for a good reason: it may not be
				// possible to know the length of the matching initial until 
				// it's been evaluated, esp. in the case of a variable whose 
				// elements are allowed to have a length greater than 1. This is
				// because it is possible, or even likely, that a language might
				// have a set of multi-segment clusters which still pattern 
				// together, or which are part of conditioning environments.
				if (testIndex >= 0 && matchesCondition(sequence, startIndex, testIndex)) {
					// Now at this point, if everything worked, we can
					Sequence<T> removed = startIndex < testIndex
							? sequence.remove(startIndex, testIndex)
							: new BasicSequence<>(model);
					Sequence<T> replacement = getReplacement(removed, target);
					if (!replacement.isEmpty()) {
						sequence.insert(replacement, startIndex);
					}
					startIndex = testIndex + replacement.size() - removed.size();
					unmatched = false;
				}
			}
		}
		return unmatched ? startIndex + 1 : startIndex;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Sequence<T> sequence : transform.keySet()) {
			sb.append(sequence);
			sb.append(' ');
		}
		sb.append("> ");
		for (Sequence<T> sequence : transform.values()) {
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
		for (Condition<T> exception : exceptions) {
			sb.append(" NOT ");
			sb.append(exception);
		}
		return sb.toString();
	}

	private int matchSource(Sequence<T> sequence,
			Sequence<T> source,
			int testIndex) {
		int index = testIndex;
		for (int i = 0; i < source.size() && index >= 0; i++) {
			Sequence<T> subSequence = sequence.subsequence(index);
			Segment<T> segment = source.get(i);

			// Source symbol is a variable
			String symbol = segment.getSymbol();
			if (variables.contains(symbol)) {
				List<Sequence<T>> elements = getVariableSequences(symbol);
				boolean elementMatches = false;
				for (int k = 0; k < elements.size() && !elementMatches; k++) {
					Sequence<T> element = elements.get(k);
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
			} else if (isUnderspecified(segment)) {
				// This block excludes fully specified features, but we do not 
				// expect the use of bracket notation in this case

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
				index = subSequence.startsWith(segment) ? index + 1 : -1;
			}
		}
		return index;
	}

	private List<Sequence<T>> getVariableSequences(String key) {
		return variables.get(key)
				.stream()
				.map(factory::toSequence)
				.collect(Collectors.toList());
	}

	private void parseRule() {
		String transformString;
		// Check-and-parse for conditions
		if (ruleText.contains("/")) {
			String[] array = ruleText.split("/");
			if (array.length <= 1) {
				String message = Templates.create()
						.add("Condition was empty.")
						.data(ruleText)
						.build();
				throw new ParseException(message);
			} else {
				transformString = array[0].trim();
				String conditionString = array[1].trim();
				try {
					parseCondition(conditionString);
				} catch (ParseException e) {
					String message = Templates.create()
							.add("Error while parsing condition '{}'")
							.with(conditionString)
							.data(ruleText)
							.build();
					throw new ParseException(message, e);
				}
			}
		} else {
			transformString = ruleText;
			conditions.add(new Condition<>("_", factory));
		}
		parseTransform(transformString);
	}

	private void parseCondition(String conditionString) {
		Match<String> notMatcher = NOT.match(conditionString);
		if (notMatcher.matches()) {
			// if there is no regular condition
			// Takes the first one off, and splits on the rest
			for (String clause : NOT.split(conditionString)) {
				Match<String> orMatch = OR.find(clause);
				if (orMatch.matches()) {
					String message = Templates.create()
							.add("OR not allowed following a NOT")
							.data(conditionString)
							.build();
					throw new ParseException(message);
				}

				String trim = clause.trim();
				if (!trim.isEmpty()) {
					exceptions.add(new Condition<>(trim, variables, factory));
				}
			}

		} else if (NOT.find(conditionString).matches()) {
			List<String> split = NOT.split(conditionString, 2);
			String conditionClauses = split.get(0);
			String exceptionClauses = split.get(1);

			for (String con : OR.split(conditionClauses, -1)) {
				conditions.add(new Condition<>(con, variables, factory));
			}

			for (String exc : NOT.split(exceptionClauses, -1)) {
				exceptions.add(new Condition<>(exc, variables, factory));
			}
		} else {
			for (String s : OR.split(conditionString, -1)) {
				if (s.trim().isEmpty()){
					String message = Templates.create()
							.add("Found dangling OR: conditions to either side",
									"cannot be blank")
							.data(conditionString)
							.build();
					throw new ParseException(message);
				}
				conditions.add(new Condition<>(s, variables, factory));
			}
		}
	}

	/**
	 * Generates an appropriate sequence by filling in backreferences based on
	 * the provided maps.
	 *
	 * @param source
	 * @param target the "target" pattern; provides a template of indexed
	 *               variables and backreferences to be filled in
	 *
	 * @return a Sequence object with variables and references filled in
	 * according to the provided maps
	 */
	private Sequence<T> getReplacement(Sequence<T> source, Sequence<T> target) {
		int variableIndex = 1;
		FeatureModel<T> featureModel = source.getFeatureModel();
		Sequence<T> replacement = new BasicSequence<>(featureModel);
		// Step through the target pattern
		for (int i = 0; i < target.size(); i++) {
			Segment<T> segment = target.get(i);

			String symbol = segment.getSymbol();
			Match<String> matcher = BACKREF.match(symbol);

			if (matcher.matches()) {
				Sequence<T> sequence = getReference(featureModel, matcher);
				replacement.add(sequence);
			} else if (variables.contains(symbol)) {
				// Allows C > G transformations, where C and G have the same
				// number of elements
				List<Sequence<T>> elements = getVariableSequences(symbol);
				Integer anIndex = ruleMatcher.getIndex(variableIndex);
				Sequence<T> sequence = elements.get(anIndex);
				replacement.add(sequence);
				variableIndex++;
			} else if (isUnderspecified(segment)) {
				// Underspecified - overwrite the feature
				Segment<T> alter = new StandardSegment<>(source.get(i));
				alter.alter(segment);
				FeatureArray<T> features = alter.getFeatures();
				FeatureMapping<T> mapping = factory.getFeatureMapping();
				String bestSymbol = mapping.findBestSymbol(features);
				Segment<T> newSegment =
						new StandardSegment<>(bestSymbol, features);
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
	private Sequence<T> getReference(FeatureModel<T> model,
			Match<String> matcher) {
		String symbol = matcher.group(1);
		String digits = matcher.group(2);

		int reference = Integer.parseInt(digits);
		int integer = ruleMatcher.getIndex(reference);

		Sequence<T> sequence;
		if (integer == -1) {
			// -1 means it was an underspecified feature
			// but we need to know what was matched
			if (symbol == null || symbol.isEmpty()) {
				sequence = new BasicSequence<>(model);
				// add the captured segment
				Segment<T> captured = ruleMatcher.getSequence(reference).get(0);
				sequence.add(captured);
			} else {
				String message = Templates.create().add(
						"The use of feature substitution in this manner",
						"is not supported! "
				).build();
				throw new UnsupportedOperationException(message);
			}
		} else {
			String variable = (symbol == null || symbol.isEmpty())
					? ruleMatcher.getVariable(reference)
					: symbol;
			sequence = getVariableSequences(variable).get(integer);
		}
		return sequence;
	}

	private boolean matchesCondition(Sequence<T> word, int start, int end) {
		Iterator<Condition<T>> cI = conditions.iterator();
		Iterator<Condition<T>> eI = exceptions.iterator();

		boolean conditionMatch = false;

		if (cI.hasNext()) {
			while (cI.hasNext() && !conditionMatch) {
				Condition<T> condition = cI.next();
				conditionMatch = condition.isMatch(word, start, end);
			}
		} else {
			conditionMatch = true;
		}

		boolean exceptionMatch = false;
		if (eI.hasNext()) {
			while (eI.hasNext() && !exceptionMatch) {
				Condition<T> exception = eI.next();
				exceptionMatch = exception.isMatch(word, start, end);
			}
		}
		return conditionMatch && !exceptionMatch;
	}

	private void parseTransform(String transformation) {
		
		if (!transformation.contains(">")) {
			String message = Templates.create()
					.add("Missing \">\" sign!")
					.data(ruleText)
					.build();
			throw new ParseException(message);
		}

		if (transformation.contains("$[")) {
			String message = Templates.create().add(
					"Malformed transformation!",
					"Indexing with $[] is not permitted!"
			).data(transformation).build();
			throw new ParseException(message);
		}
		
		List<String> array = TRANSFORM.split(transformation);
		if (isMalformed(array)) {
			String message = Templates.create()
					.add("Malformed transformation.")
					.data(transformation)
					.build();
			throw new ParseException(message);
		}

		String sourceString = SPACE.replace(array.get(0), " ");
		String targetString = SPACE.replace(array.get(1), " ");

		// Split strings, but not within brackets []
		List<String> sourceList = parseToList(sourceString);
		List<String> targetList = parseToList(targetString);

		// fill in target for cases like "a b c > d"
		if (sourceList.contains("0") &&
				!(sourceList.size() == 1 && targetList.size() == 1)) {
			String message = Templates.create().add(
					"A rule may only use \"0\" in the source if ",
					"it is the only symbol in the source ",
					"pattern and the target size is exactly 1"
			).data(transformation).build();
			throw new ParseException(message);
		}

		balanceTransform(sourceList, targetList, transformation);

		for (int i = 0; i < sourceList.size(); i++) {
			// Also we need to correctly tokenize $1, $2 etc or $C1, $N2
			Sequence<T> source = factory.toSequence(sourceList.get(i));
			Sequence<T> target = factory.toSequence(targetList.get(i));
			validateTransform(source, target);
			transform.put(source, target);
		}
	}

	/**
	 * Once converted to features, ensure that the rule's transform is well-
	 * formed and has an appropriate structure
	 */
	private void validateTransform(Sequence<T> source, Sequence<T> target) {
		int j = 0;
		for (Segment<T> segment : target) {
			FeatureArray<T> features = segment.getFeatures();
			boolean underspecified = features.contains(null) ||
					features instanceof SparseFeatureArray;
			if (underspecified && source.size() <= j) {
				String message = Templates.create()
						.add("Unmatched underspecified segment in rule target.")
						.data(ruleText)
						.build();
				throw new ParseException(message);
			}
			j++;
		}
	}

	private static List<String> parseToList(String source) {
		List<String> list = new ArrayList<>();
		int start = 0;
		int end = 0;

		while (end < source.length()) {
			char c = source.charAt(end);
			if (c == ' ') {
				list.add(source.substring(start, end));
				end++;
				start = end;
			} else if (c == '[') {
				end = source.indexOf(']', end);
			} else {
				end++;
			}
		}
		list.add(source.substring(start, end));
		return list;
	}

	private static void balanceTransform(
			@NonNull List<String> source,
			@NonNull List<String> target,
			@NonNull String transformation
	) {
		if (target.size() > source.size()) {
			String message = Templates.create()
					.add("Target size cannot be greater than source size.")
					.data(transformation)
					.build();
			throw new ParseException(message);
		} else if (target.size() < source.size()) {
			if (target.size() == 1) {
				String first = target.get(0);
				while (target.size() < source.size()) {
					target.add(first);
				}
			} else {
				String message = Templates.create().add(
						"Target and source sizes may only be uneven if",
						"target size is exactly 1."
				).data(transformation).build();
				throw new ParseException(message);
			}
		}
	}

	private static <T> boolean isUnderspecified(Segment<T> segment) {
		FeatureType<T> type = segment.getFeatureModel().getFeatureType();
		FeatureArray<T> features = segment.getFeatures();
		return features instanceof SparseFeatureArray || 
				type.listUndefined().stream().anyMatch(features::contains);
	}

	private static boolean isMalformed(List<String> array) {
		return array.size() <= 1
				|| array.get(0).isEmpty()
				|| array.get(1).isEmpty();
	}

	private static final class RuleMatcher<T> {
		
		// Tracks which variable values are matched by the "source" pattern;
		//   an entry (2 -> 4) would indicate that the source matched the 4th
		//   value of the 2nd variable. This permits proper mapping between 
		//   source and target symbols when using back-references and indexed
		//   variables
		private final Map<Integer, Integer> indexMap;
		
		// Track which variable in the rule was matched, by symbol 
		private final Map<Integer, String> variableMap;
		
		// The actual sequence matched in the input  
		private final Map<Integer, Sequence<T>> sequenceMap;
		
		// Tracks the order of variables in the "source"
		//   pattern; i.e. the 2nd variable in the source pattern is referenced
		//   via {@code $2}. Unlike standard regular expressions, all variables
		//   are tracked, rather than tracking explicit groups 
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
			indexMap.put(referenceIndex, index);
		}

		private void addVariable(String variable) {
			variableMap.put(referenceIndex, variable);
		}

		private void addSequence(Sequence<T> sequence) {
			sequenceMap.put(referenceIndex, sequence);
		}

		private Integer getIndex(Integer i) {
			return indexMap.get(i);
		}

		private String getVariable(Integer i) {
			return variableMap.get(i);
		}

		private Sequence<T> getSequence(Integer i) {
			return sequenceMap.get(i);
		}
	}
}
