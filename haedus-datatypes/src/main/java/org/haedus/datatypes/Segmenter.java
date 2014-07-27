/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.haedus.datatypes;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Segmenter provides functionality to split strings into an an array where each element
 * represents a series of characters grouped according to their functional value as diacritic
 * marks or combining marks.
 * @author Goats
 */
public class Segmenter {

	public static List<String> segment(CharSequence word) {
	    return segment(word, new ArrayList<String>(), Normalizer.Form.NFC);
	}

	public static List<String> segment(CharSequence word, Iterable<String> keys) {
		return segment(word, keys, Normalizer.Form.NFC);
	}

	/**
	 *
	 * @param word
	 * @param keys
	 * @param form should be either NFC or NFD
	 * @return
	 */
	public static List<String> segment(CharSequence word , Iterable<String> keys, Normalizer.Form form) {
		List<String>  segments = new ArrayList<String>();

		String string = Normalizer.normalize(word, form);

		StringBuilder buffer = new StringBuilder();
		int length = string.length();
		for (int i = 0; i < length; i++) {
			String key = getBestMatch(string.substring(i), keys);
			if (i == 0) {
				if (key.isEmpty()) {
					buffer.append(string.charAt(0));
				} else {
					buffer.append(key);
					i += key.length() - 1;
				}
			} else {
				char c = string.charAt(i);
				if (isAttachable(c)) {
					buffer.append(c);
					if (isDoubleWidthBinder(c) && i < length - 1) {
						i++;
						buffer.append(string.charAt(i));
					}
				} else {
					if (key.isEmpty()) {
						buffer = clearBuffer(segments, buffer, String.valueOf(c));
					} else {
						buffer = clearBuffer(segments, buffer, key);
						i += key.length() - 1;
					}
				}
			}
		}
		segments.add(buffer.toString());
		return segments;
	}

	private static StringBuilder clearBuffer(Collection<String> segments, StringBuilder buffer, String key) {
		segments.add(buffer.toString());
		buffer = new StringBuilder();
		buffer.append(key);
		return buffer;
	}

	private static String getBestMatch(String tail, Iterable<String> keys) {
		String bestMatch = "";
		for (String key : keys) {
			if (tail.startsWith(key) && bestMatch.length() < key.length()) {
				bestMatch = key;
			}
		}
		return bestMatch;
	}

	private static boolean isDoubleWidthBinder(char ch) {
		return ch <= 866 && 860 <= ch;
	}

	private static boolean isAttachable(Character c) {
		int type = Character.getType(c);
		int value = c;
		return	isSuperscriptAsciiDigit(value)   ||
				isMathematicalSubOrSuper(value)  ||
				isCombingNOS(value)              ||
				isCombiningClass(type);
	}

	private static boolean isMathematicalSubOrSuper(int value) {
		// int literals are decimal char values
		return (value <= 8304) && (8348 <= value);
	}

	private static boolean isSuperscriptAsciiDigit(int value) {
		// int literals are decimal char values
		return	(value == 178) ||
				(value == 179) ||
				(value == 185);
	}

	private static boolean isCombingNOS(int value) {
		// int literals are decimal char values
		return	(value >= 8304) &&
				(value <= 8348);
	}

	private static boolean isCombiningClass(int type) {
		return	(type == Character.MODIFIER_LETTER)			|| // LM
				(type == Character.MODIFIER_SYMBOL)			|| // SK
				(type == Character.COMBINING_SPACING_MARK)	|| // MC
				(type == Character.NON_SPACING_MARK);          // MN
	}
}
