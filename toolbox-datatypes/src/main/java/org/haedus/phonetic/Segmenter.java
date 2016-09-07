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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.haedus.phonetic;

import org.haedus.enums.FormatterMode;
import org.haedus.exceptions.ParseException;
import org.haedus.machines.Expression;
import org.haedus.phonetic.model.FeatureModel;
import org.haedus.phonetic.model.FeatureSpecification;
import org.haedus.phonetic.model.StandardFeatureModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Segmenter provides functionality to split strings into an an array where
 * each element represents a series of characters grouped according to their
 * functional value as diacritical marks or combining marks.
 *
 * @author Samantha Fiona Morrigan McCabe
 */
public final class Segmenter {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(Segmenter.class);

	private static final Pattern BACKREFERENCE_PATTERN = Pattern.compile("(\\$[^\\$]*\\d+)");

	private static final int BINDER_START      = 0x035C;
	private static final int BINDER_END        = 0x0362;
	private static final int SUPERSCRIPT_ZERO  = 0x2070;
	private static final int SUBSCRIPT_SMALL_T = 0x209C;
	private static final int SUPERSCRIPT_TWO   = 0x00B2;
	private static final int SUPERSCRIPT_THREE = 0x00B3;
	private static final int SUPERSCRIPT_ONE   = 0x00B9;

	// Prevent the class from being instantiated
	private Segmenter() {}

	public static List<Expression> getExpressions(String string, Collection<String> keys, FormatterMode formatterMode) {

		List<String> strings = getSegmentedString(string, keys, formatterMode);

		List<Expression> list = new ArrayList<Expression>();
		if (!strings.isEmpty()) {

			Expression buffer = new Expression();
			for (String symbol : strings) {
				if (symbol.equals("*") || symbol.equals("?") || symbol.equals("+")) {
					buffer.setMetacharacter(symbol);
					buffer = updateBuffer(list, buffer);
				} else if (symbol.equals("!")) {
					// first in an expression
					buffer = updateBuffer(list, buffer);
					buffer.setNegative(true);
				} else {
					buffer = updateBuffer(list, buffer);
					buffer.setExpression(symbol);
				}
			}
			if (!buffer.getExpression().isEmpty()) {
				list.add(buffer);
			}
		}
		return list;
	}

	private static Expression updateBuffer(Collection<Expression> list, Expression buffer) {
		// Add the contents of buffer if not empty
		if (!buffer.isEmpty()) {
			list.add(buffer);
			return new Expression();
		} else {
			return buffer;
		}
	}

	public static Segment getSegment(String string, FeatureModel model, FormatterMode formatterMode) {
		return getSegment(string, model, new HashSet<String>(), formatterMode);
	}

	public static Segment getSegment(String string, FeatureModel model, Collection<String> reservedStrings, FormatterMode formatterMode) {
		Collection<String> keys = getKeys(model, reservedStrings);
		String normalString = formatterMode.normalize(string);
		FeatureSpecification specification = model.getSpecification();
		List<Symbol> segmentedSymbol = getCompositeSymbols(normalString, keys, formatterMode);
		if (segmentedSymbol.size() >= 1) {
			Symbol symbol = segmentedSymbol.get(0);
			String head = symbol.getHead();
			List<String> tail = symbol.getTail();

			if (head.startsWith("[")) {
				if (tail.isEmpty()) {
					return specification.getSegmentFromFeatures(head);
				} else {
					throw new ParseException("Attempting to attach diacritics "
							+ tail + " to a feature definition: " + head);
				}
			} else {
				return model.getSegment(head, tail);
			}
		} else {
			return null;
		}
	}

	public static List<String> getSegmentedString(String word, Collection<String> keys, FormatterMode formatterMode) {
		String normalString = formatterMode.normalize(word);
		List<Symbol> segmentedSymbol = getCompositeSymbols(normalString, keys, formatterMode);
		List<String> list = new ArrayList<String>();
		for (Symbol symbol : segmentedSymbol) {
			StringBuilder head = new StringBuilder(symbol.getHead());
			for (String s : symbol.getTail()) {
				head.append(s);
			}
			list.add(head.toString());
		}
		return list;
	}

	public static Sequence getSequence(String word, FeatureModel model, Collection<String> reservedStrings, FormatterMode formatterMode) {
		Collection<String> keys = getKeys(model, reservedStrings);
		String normalString = formatterMode.normalize(word);
		List<Symbol> list = getCompositeSymbols(normalString, keys, formatterMode);
		FeatureSpecification specification = model.getSpecification();
		Sequence sequence = new Sequence(specification);
		for (Symbol item : list) {
			String head = item.getHead();
			List<String> tail = item.getTail();

			Segment segment;
			if (head.startsWith("[") && !keys.contains(head) && model != StandardFeatureModel.EMPTY_MODEL) {
				segment = specification.getSegmentFromFeatures(head);
			} else {
				segment = model.getSegment(head, tail);
			}
			sequence.add(segment);
		}
		return sequence;
	}

	private static Collection<String> getKeys(FeatureModel model, Collection<String> reserved) {
		Collection<String> keys = new ArrayList<String>();
		keys.addAll(model.getSymbols());
		if (reserved != null) {
			keys.addAll(reserved);
		}
		return keys;
	}

	private static List<String> separateBrackets(String word) {
		List<String> list = new ArrayList<String>();
		StringBuilder buffer = new StringBuilder();

		for (int i = 0; i < word.length(); ) {
			char c = word.charAt(i);
			if (c == '{') {
				if (buffer.length() != 0) {
					list.add(buffer.toString());
					buffer = new StringBuilder();
				}
				int index = getIndex(word, '{', '}', i) + 1;
				String substring = word.substring(i, index);
				list.add(substring);
				i = index;
			} else if (c == '(') {
				if (buffer.length() != 0) {
					list.add(buffer.toString());
					buffer = new StringBuilder();
				}
				int index = getIndex(word, '(', ')', i) + 1;
				String substring = word.substring(i, index);
				list.add(substring);
				i = index;
			} else if (c == '[') {
				if (buffer.length() != 0) {
					list.add(buffer.toString());
					buffer = new StringBuilder();
				}
				int index = getIndex(word, '[', ']', i) + 1;
				String substring = word.substring(i, index);
				list.add(substring);
				i = index;
			} else {
				buffer.append(c);
				i++;
			}
		}
		if (buffer.length() != 0) {
			list.add(buffer.toString());
		}
		return list;
	}

	private static Collection<Symbol> getThings(String word, Iterable<String> keys) {
		Collection<Symbol> segments = new ArrayList<Symbol>();

		Symbol symbol = new Symbol();
		int length = word.length();
		for (int i = 0; i < length; ) {

			// Get the word from current position on
			String substring = word.substring(i);
			// Find the longest string in keys which he substring starts with
			String key = getBestMatch(substring, keys);
			if (symbol.isEmpty()) {
				// Assume that the first symbol must be a base-character
				// This doesn't universally word (pre-nasalized, pre-aspirated),
				// but we don't support this in our model yet
				if (key.isEmpty()) {
					// No special error handling if word starts with diacritic,
					// but may be desirable
					symbol.appendHead(word.charAt(i));
				} else {
					symbol.appendHead(key);
					i = key.length() - 1;
				}
			} else {
				char ch = word.charAt(i); // Grab current character
				if (isAttachable(ch)) {   // is it a standard diacritic?
					if (isDoubleWidthBinder(ch) && i < length - 1) {
						i++;
						// Jump ahead and grab the next character
						symbol.appendHead(word.charAt(i));
					} else {
						symbol.appendTail(ch);
					}
				} else {
					// Not a diacritic
					if (!symbol.isEmpty()) { segments.add(symbol); }
					symbol = new Symbol();
					if (key.isEmpty()) {
						symbol.appendHead(ch);
					} else {
						symbol.appendHead(key);
						i += key.length() - 1;
					}
				}
			}
			i++;
		}
		if (!symbol.isEmpty()) { segments.add(symbol); }
		return segments;
	}

	private static Collection<Symbol> segmentNaively(String word, Iterable<String> keys) {
		Collection<Symbol> segments = new ArrayList<Symbol>();
		for (int i = 0; i < word.length(); i++) {

			String key = getBestMatch(word.substring(i), keys);
			Symbol symbol = new Symbol();
			if (key.isEmpty()) {
				symbol.appendHead(word.substring(i, i + 1));
			} else {
				symbol.appendHead(key);
				i = i + key.length() - 1;
			}

			if (!symbol.isEmpty()) {
				segments.add(symbol);
			}
		}
		return segments;
	}

	private static int getIndex(CharSequence string, char left, char right, int startIndex) {
		int count = 1;
		int endIndex = startIndex;

		boolean matched = false;
		for (int i = startIndex + 1; i < string.length() && !matched; i++) {
			char ch = string.charAt(i);
			if (ch == right && count == 1) {
				matched = true;
				endIndex = i;
			} else if (ch == right) {
				count++;
			} else if (ch == left) {
				count--;
			}
		}
		if (!matched) {
			LOGGER.warn("Unmatched " + left + " in " + string);
		}
		return endIndex;
	}

	// Finds longest item in keys which the provided string starts with
	// Also can be used to grab index symbols
	private static String getBestMatch(String word, Iterable<String> keys) {
		String bestMatch = "";
		for (String key : keys) {
			if (word.startsWith(key) && bestMatch.length() < key.length()) {
				bestMatch = key;
			}
		}

		Matcher backReferenceMatcher = BACKREFERENCE_PATTERN.matcher(word);
		if (backReferenceMatcher.lookingAt()) {
			bestMatch = backReferenceMatcher.group();
		}
		return bestMatch;
	}

	private static boolean isAttachable(char ch) {
		return isSuperscriptAsciiDigit(ch) ||
				isMathematicalSubOrSuper(ch) ||
				isCombiningClass(ch);
	}

	private static boolean isDoubleWidthBinder(char ch) {
		return BINDER_START <= ch && ch <= BINDER_END;
	}

	private static boolean isSuperscriptAsciiDigit(char value) {
		// int literals are decimal char values
		return value == SUPERSCRIPT_TWO   ||
			   value == SUPERSCRIPT_THREE ||
			   value == SUPERSCRIPT_ONE;
	}

	private static boolean isMathematicalSubOrSuper(char value) {
		// int literals are decimal char values
		return SUPERSCRIPT_ZERO <= value && value <= SUBSCRIPT_SMALL_T;
	}

	private static boolean isCombiningClass(char ch) {
		int type = Character.getType(ch);
		
		return type == Character.MODIFIER_LETTER     || // LM
			type == Character.MODIFIER_SYMBOL        || // SK
			type == Character.COMBINING_SPACING_MARK || // MC -- this is only used in Brahmic scripts
			type == Character.NON_SPACING_MARK;         // MN
	}

	private static List<Symbol> getCompositeSymbols(String word, Iterable<String> keys, FormatterMode segParam) {

		List<Symbol> symbols = new ArrayList<Symbol>();
		List<String> separatedString = separateBrackets(word);
		if (segParam == FormatterMode.INTELLIGENT) {
			for (String s : separatedString) {
				if (s.startsWith("{") || s.startsWith("(") || s.startsWith("[")) {
					symbols.add(new Symbol(s));
				} else {
					symbols.addAll(getThings(s, keys));
				}
			}
		} else if (segParam == FormatterMode.DECOMPOSITION ||
				segParam == FormatterMode.COMPOSITION ||
				segParam == FormatterMode.NONE) {
			for (String s : separatedString) {
				if (s.startsWith("{") || s.startsWith("(") || s.startsWith("[")) {
					symbols.add(new Symbol(s));
				} else {
					symbols.addAll(segmentNaively(s, keys));
				}
			}
		} else {
			throw new UnsupportedOperationException("Unsupported segmentation mode " + segParam);
		}
		return symbols;
	}

	private static final class Symbol {
		@SuppressWarnings("StringBufferField")
		private final StringBuilder head;
		private final List<String>  tail;

		private Symbol(String headParam) {
			this();
			head.append(headParam);
		}

		private Symbol() {
			head = new StringBuilder();
			tail = new ArrayList<String>();
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(8);
			sb.append(head);
			for (String s : tail) {
				sb.append(s);
			}

			return sb.toString();
		}

		private boolean isEmpty() {
			return head.length() == 0;
		}

		private String getHead() {
			return head.toString();
		}

		private List<String> getTail() {
			return tail;
		}

		private void appendHead(String string) {
			head.append(string);
		}

		private void appendHead(char ch) {
			head.append(ch);
		}

		private void appendTail(char ch) {
			tail.add(String.valueOf(ch));
		}
	}
}
