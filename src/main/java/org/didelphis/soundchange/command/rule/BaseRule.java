/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.rule;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

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
import org.didelphis.language.phonetic.sequences.PhoneticSequence;
import org.didelphis.language.phonetic.sequences.Sequence;
import org.didelphis.soundchange.VariableStore;
import org.didelphis.soundchange.parser.ParserMemory;
import org.didelphis.utilities.Strings;
import org.didelphis.utilities.Templates;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
public class BaseRule implements Rule {

	private static final Logger LOG = LogManager.getLogger(BaseRule.class);

	private static final Regex BACKREF   = new Regex("\\$([^$]*)(\\d+)");

	private static final Regex SPACE     = new Regex("\\s+");
	private static final Regex TRANSFORM = new Regex("\\s*>\\s*");
	private static final Regex PIPE      = new Regex("\\s*\\|\\s*");
	private static final Regex CONDITION = new Regex("\\s*/\\s*");

	String ruleText;

	SequenceFactory factory;
	RuleMatcher     ruleMatcher;
	VariableStore   variables;

	// Symbols which can be transformed by the rule
	List<Sequence> symbols;

	// Transform targets and conditions
	Map<Condition, List<Sequence>> conditionMap;

	@NonFinal
	@Setter
	boolean useDebug;

	public BaseRule(String rule, ParserMemory memory) {
		this(rule, memory.getVariables(), memory.factorySnapshot());
	}

	@Deprecated
	BaseRule(String rule, VariableStore variables, SequenceFactory factory) {
		this.factory = factory;
		this.variables = variables;

		ruleText = rule;
		ruleMatcher = new RuleMatcher();

		symbols = new ArrayList<>();
		conditionMap = new LinkedHashMap<>();

		parseRule();
	}


	BaseRule(String rule, SequenceFactory factory) {
		this(rule, new VariableStore(), factory);
	}

	@Override
	public String toString() {
		return ruleText;
	}

	@Override
	public Sequence apply(Sequence sequence) {
		LOG.trace("Applying to: {}",sequence.toString());
		// Step through the word to see if the rule might apply, i.e. if the
		// source pattern can be found
		int index = 0;
		while (index < sequence.size()) {
			index = applyAtIndex(sequence, index);
		}
		return sequence;
	}

	@Override
	public int applyAtIndex(Sequence sequence, int index) {

		if (index >= sequence.size()) {
			return index;
		}

		int startIndex = index;
		boolean unmatched = true;
		FeatureMapping mapping = factory.getFeatureMapping();
		FeatureModel model = mapping.getFeatureModel();

		String original = useDebug ? mapping.findBestSymbols(sequence) : "";

		// Check each source pattern
		for (int i = 0, symbolsSize = symbols.size(); i < symbolsSize; i++) {
			Sequence source = symbols.get(i);

			ruleMatcher.reset();

			int testIndex = startIndex;

			// Step through the current source pattern
			testIndex = matchSource(sequence, source, testIndex);

			if (testIndex < 0) {
				continue;
			}

			// find if the conditions match
			for (Entry<Condition, List<Sequence>> entry : conditionMap.entrySet()) {
				if (entry.getKey().isMatch(sequence, startIndex, testIndex)) {
					Sequence target = entry.getValue().get(i);

					// Now at this point, if everything worked, we can
					Sequence removed = startIndex < testIndex
							? sequence.remove(startIndex, testIndex)
							: new PhoneticSequence(model);

					Sequence replacement = getReplacement(removed, target);
					if (!replacement.isEmpty()) {
						sequence.insert(replacement, startIndex);
					}

					startIndex = testIndex + replacement.size() - removed.size();
					unmatched = false;
					break;
				}
			}
		}

		if (!unmatched && useDebug) {
			LOG.info("{} --> {}",
					Strings.padRight(original, 10),
					mapping.findBestSymbols(sequence));
		}

		return unmatched ? startIndex + 1 : startIndex;
	}

	@Override
	public void run() {
		// nothing?
	}

	// locates the
	private int matchSource(Sequence sequence,
			Sequence source,
			int testIndex) {
		int index = testIndex;
		for (int i = 0; i < source.size() && index >= 0; i++) {
			Sequence subSequence = sequence.subsequence(index);
			Segment segment = source.get(i);

			// Source symbol is a variable
			String symbol = segment.getSymbol();
			if (variables.contains(symbol)) {
				List<Sequence> elements = getVariableSequences(symbol);
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
			} else if (factory.getReservedStrings().contains(symbol)) {
				index = subSequence.startsWith(segment) ? index + 1 : -1;
			} else if (isUnderspecified(segment)) {
				// This block excludes fully specified features, but we do not
				// expect the use of bracket notation in this case,
				// otherwise it's the same as a literal
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

	private List<Sequence> getVariableSequences(String key) {
		return variables.get(key)
				.stream()
				.map(factory::toSequence)
				.collect(Collectors.toList());
	}

	private void parseRule() {

		List<String> split = TRANSFORM.split(ruleText.trim());

		if (split.size() < 2) {
			throw new ParseException(""); // TODO:
		}

		String source = split.get(0).trim();
		String transform = split.get(1).trim();

		if (source.isEmpty()) {
			throw new ParseException(""); // TODO:
		}

		if (transform.isEmpty()) {
			throw new ParseException(""); // TODO:
		}

		// Populate the source symbols
		parseToList(source).stream()
				.map(factory::toSequence)
				.forEach(symbols::add);

		// Duplicates
		if (new HashSet<>(symbols).size() != symbols.size()) {
			throw new ParseException(""); // TODO:
		}

		List<String> transforms = PIPE.split(transform);
		for (String item : transforms) {
			List<String> splitTransform = CONDITION.split(item, 2);

			String transformation = splitTransform.get(0);
			if (transformation.contains("$[")) {
				throw new ParseException("Indexing with $[] is not permitted");
			}

			// Handle target symbol list
			List<Sequence> target = parseToList(transformation).stream()
					.map(factory::toSequence)
					.collect(Collectors.toList());
			balanceTransform(target);
			validateTransform(target);

			// handle condition here
			Condition condition;
			if (splitTransform.size() > 1) {
				String rawCondition = splitTransform.get(1);
				condition = new ConditionGroup(variables, factory, rawCondition);
			} else {
				condition = new EmptyCondition();
			}
			conditionMap.put(condition, target);
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
	private Sequence getReplacement(Sequence source, Sequence target) {
		int variableIndex = 1;
		FeatureModel featureModel = source.getFeatureModel();
		Sequence replacement = new PhoneticSequence(featureModel);
		// Step through the target pattern
		for (int i = 0; i < target.size(); i++) {
			Segment segment = target.get(i);

			String symbol = segment.getSymbol();
			Match<String> matcher = BACKREF.match(symbol);

			if (matcher.matches()) {
				Sequence sequence = getReference(featureModel, matcher);
				replacement.add(sequence);
			} else if (variables.contains(symbol)) {
				// Allows C > G transformations, where C and G have the same
				// number of elements
				List<Sequence> elements = getVariableSequences(symbol);
				Integer anIndex = ruleMatcher.getIndex(variableIndex);
				Sequence sequence = elements.get(anIndex);
				replacement.add(sequence);
				variableIndex++;
			} else if (isUnderspecified(segment)) {
				// Underspecified - overwrite the feature
				Segment alter = new StandardSegment(source.get(i));
				alter.alter(segment);
				FeatureArray features = alter.getFeatures();
				FeatureMapping mapping = factory.getFeatureMapping();
				String bestSymbol = mapping.findBestSymbol(features);
				Segment newSegment = new StandardSegment(bestSymbol, features);
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
	private Sequence getReference(FeatureModel model,
			Match<String> matcher) {
		String symbol = matcher.group(1);
		String digits = matcher.group(2);

		int reference = Integer.parseInt(digits);
		int integer = ruleMatcher.getIndex(reference);

		Sequence sequence;
		if (integer == -1) {
			// -1 means it was an underspecified feature
			// but we need to know what was matched
			if (symbol == null || symbol.isEmpty()) {
				sequence = new PhoneticSequence(model);
				// add the captured segment
				Segment captured = ruleMatcher.getSequence(reference).get(0);
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

	/**
	 * Once converted to features, ensure that the rule's transform is well-
	 * formed and has an appropriate structure
	 */
	private void validateTransform(List<Sequence> target) {
		for (int i = 0; i < symbols.size(); i++) {
			Sequence sourceSegments = symbols.get(i);
			Sequence targetSegments = target.get(i);
			int j = 0;
			for (Segment segment : targetSegments) {
				FeatureArray features = segment.getFeatures();
				boolean underspecified = features instanceof SparseFeatureArray;
				if (underspecified && sourceSegments.size() <= j) {
					throw new ParseException(
							"Unmatched underspecified segment in rule target.");
				}
				j++;
			}
		}
	}

	private static List<String> parseToList(String source) {
		List<String> list = new ArrayList<>();
		int start = 0;
		int end = 0;

		while (end < source.length()) {
			char c = source.charAt(end);
			if (c == ' ') {
				if (start != end) {
					list.add(source.substring(start, end));
				}
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

	private void balanceTransform(@NonNull List<Sequence> target) {
		int size = symbols.size();
		if (target.size() > size) {
			String message = Templates.create()
					.add("Target size cannot be greater than source size.")
					.build();
			throw new ParseException(message);
		} else if (target.size() < size) {
			if (target.size() == 1) {
				Sequence first = target.get(0);
				while (target.size() < size) {
					target.add(first);
				}
			} else {
				String message = Templates.create().add(
						"Target and source sizes may only be uneven if",
						"target size is exactly 1."
				).build();
				throw new ParseException(message);
			}
		}
	}

	private static  boolean isUnderspecified(Segment segment) {
		FeatureType type = segment.getFeatureModel().getFeatureType();
		FeatureArray features = segment.getFeatures();
		return features instanceof SparseFeatureArray ||
				type.listUndefined().stream().anyMatch(features::contains);
	}

	private static final class RuleMatcher {

		// Tracks which variable values are matched by the "source" pattern;
		//   an entry (2 -> 4) would indicate that the source matched the 4th
		//   value of the 2nd variable. This permits proper mapping between
		//   source and target symbols when using back-references and indexed
		//   variables
		private final Map<Integer, Integer> indexMap;

		// Track which variable in the rule was matched, by symbol
		private final Map<Integer, String> variableMap;

		// The actual sequence matched in the input
		private final Map<Integer, Sequence> sequenceMap;

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

	private static final class EmptyCondition implements Condition {
		@Override
		public boolean isMatch(Sequence word, int index) {
			return true;
		}

		@Override
		public boolean isMatch(Sequence word, int startIndex, int endIndex) {
			return true;
		}
	}
}
