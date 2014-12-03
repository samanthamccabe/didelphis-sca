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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.haedus.datatypes;

import org.slf4j.LoggerFactory;

import org.haedus.datatypes.phonetic.FeatureModel;
import org.haedus.datatypes.phonetic.Segment;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.VariableStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Segmenter provides functionality to split strings into an an array where each element
 * represents a series of characters grouped according to their functional value as diacritical
 * marks or combining marks.
 *
 * @author Samantha Fiona Morrigan McCabe
 */
public final class Segmenter {

	public static final Pattern BACKREFERENCE_PATTERN = Pattern.compile("(\\$[^\\$]*\\d+)");

	private static final transient org.slf4j.Logger LOGGER       = LoggerFactory.getLogger(Segmenter.class);
	public static final            int              BINDER_START = 860;
	public static final int BINDER_END = 866;

	// Prevent the class from being instantiated
	private Segmenter() {
	}

	// Will throw null if segmentation mode is not supported
	public static List<String> getSegmentedString(String word, Iterable<String> keys, SegmentationMode modeParam) {
		List<String> list;
		if (modeParam == SegmentationMode.DEFAULT) {
			list = segment(word, keys);
		} else if (modeParam == SegmentationMode.NAIVE) {
			list = segmentNaively(word, keys);
		} else {
			throw new UnsupportedOperationException("Unsupported segmentation mode " + modeParam);
		}
		return list;
	}

	public static Sequence getSequence(String word, FeatureModel model, VariableStore variables, SegmentationMode mode) {

		Collection<String> keys = new ArrayList<String>();
		keys.addAll(model.getSymbols());
		keys.addAll(variables.getKeys());

		List<String> list = getSegmentedString(word, keys, mode);

		List<Segment> segments = new ArrayList<Segment>();

		for (String item : list) {
			// Get the base
			String baseCharacter = getBestMatch(item, model.getSymbols());
			// treat following characters as diacritics, look them up individually
			// and create their feature values
		}

		//		return model.getSequence(list);
		return null;
	}

	private static List<String> segment(String word, Iterable<String> keys) {
		List<String> segments = new ArrayList<String>();

		StringBuilder buffer = new StringBuilder();
		int length = word.length();
		for (int i = 0; i < length; i++) {
			String substring = word.substring(i);       // Get the word from current position on
			String key = getBestMatch(substring, keys); // Find the longest string in keys which he substring starts with
			if (i == 0) {
				// Assume that the first symbol must be a diacritic
				// This doesn't universally word (prenasalized, preaspirated), but we don't support this in our model yet
				if (key.isEmpty()) {
					buffer.append(word.charAt(0));
				} else {
					buffer.append(key);
					i = key.length() - 1;
				}
			} else {
				char ch = word.charAt(i); // Grab current character
				if (isAttachable(ch)) {   // is it a standard diacritic?
					buffer.append(ch);
					if (isDoubleWidthBinder(ch) && i < length - 1) {
						i++;
						// Jump ahead and grab the next character
						buffer.append(word.charAt(i));
					}
				} else {
					// Not a diacritic
					segments.add(buffer.toString()); // take buffer contents, and transfer to the list
					buffer = new StringBuilder();    // wipe the buffer
					if (key.isEmpty()) {
						buffer.append(ch);
					} else {
						buffer.append(key);
						i += key.length() - 1;
					}
				}
			}
		}
		segments.add(buffer.toString());
		return segments;
	}

	// Finds longest item in keys which the provided string starts with
	// Also can be used to grab index symbols
	private static String getBestMatch(String tail, Iterable<String> keys) {
		String bestMatch = "";

		String string = removeDoubleWidthBinders(tail);

		for (String key : keys) {
			if (string.startsWith(key) && bestMatch.length() < key.length()) {
				bestMatch = key;
			}
		}

		Matcher backReferenceMatcher = BACKREFERENCE_PATTERN.matcher(string);
		if (backReferenceMatcher.lookingAt()) {
			bestMatch = backReferenceMatcher.group();
		}

		return bestMatch;
	}

	private static boolean isAttachable(char c) {
		return isSuperscriptAsciiDigit(c)  ||
		       isMathematicalSubOrSuper(c) ||
		       isCombingNOS(c)             ||
		       isCombiningClass(c);
	}

	private static boolean isCombingNOS(int value) {
		// int literals are decimal char values
		return (value >= 8304) &&
		       (value <= 8348);
	}

	private static boolean isCombiningClass(int type) {
		return (type == Character.MODIFIER_LETTER)        || // LM
		       (type == Character.MODIFIER_SYMBOL)        || // SK
		       (type == Character.COMBINING_SPACING_MARK) || // MC
		       (type == Character.NON_SPACING_MARK);         // MN
	}

	private static String removeDoubleWidthBinders(String string) {
		for (char c = BINDER_START; c <= BINDER_END; c++) {
			string = string.replace(c+"", "");
			// TODO: this is awful
		}
		return string;
	}

	private static boolean isDoubleWidthBinder(char ch) {
		return ch <= BINDER_END && BINDER_START <= ch;
	}

	private static boolean isSuperscriptAsciiDigit(int value) {
		// int literals are decimal char values
		return (value == 178) ||
		       (value == 179) ||
		       (value == 185);
	}

	private static boolean isMathematicalSubOrSuper(int value) {
		// int literals are decimal char values
		return (value <= 8304) && (8348 <= value);
	}

	private static StringBuilder clearBuffer(Collection<String> segments, StringBuilder buffer, String key) {
		segments.add(buffer.toString());
		buffer = new StringBuilder();
		buffer.append(key);
		return buffer;
	}

	private static List<String> segmentNaively(String word, Iterable<String> keys) {
		List<String> segments = new ArrayList<String>();
		for (int i = 0; i < word.length(); i++) {

			String key = getBestMatch(word.substring(i), keys);
			if (key.isEmpty()) {
				segments.add(word.substring(i, i + 1));
			} else {
				segments.add(key);
				i = i + key.length() - 1;
			}
		}
		return segments;
	}
}
