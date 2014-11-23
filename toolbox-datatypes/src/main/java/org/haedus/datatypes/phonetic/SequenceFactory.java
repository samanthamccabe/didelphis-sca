package org.haedus.datatypes.phonetic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.haedus.datatypes.SegmentationMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: goats
 * Created: 11/23/2014
 */
public class SequenceFactory {

	public static final Pattern BACKREFERENCE_PATTERN = Pattern.compile("(\\$[^\\$]*\\d+)");

	private static final transient Logger LOGGER = LoggerFactory.getLogger(SequenceFactory.class);

	private FeatureModel     featureModel;
	private SegmentationMode segmentationMode;
	private VariableStore    variableStore;

	public SequenceFactory() {
		featureModel = new FeatureModel();
		variableStore = new VariableStore();
		segmentationMode = SegmentationMode.DEFAULT;
	}

	public SequenceFactory(FeatureModel modelParam, VariableStore storeParam) {
		featureModel = modelParam;
		variableStore = storeParam;
		segmentationMode = SegmentationMode.DEFAULT;
	}

	public Sequence getSequence(String word) {
		List<String> keys = new ArrayList<String>();
		keys.addAll(featureModel.getSymbols());
		keys.addAll(variableStore.getKeys());

		if (segmentationMode == SegmentationMode.DEFAULT) {
			List<String> list = segment(word, keys);
			return new Sequence(list, featureModel);
		} else if (segmentationMode == SegmentationMode.NAIVE) {
			List<String> list = segmentNaively(word, keys);
			return new Sequence(list, featureModel);
		} else {
			LOGGER.error("Mode {} not supported by this operation", segmentationMode);
			return null;
		}
	}

	public FeatureModel getFeatureModel() {
		return featureModel;
	}

	public void setFeatureModel(FeatureModel featureModel) {
		this.featureModel = featureModel;
	}

	public SegmentationMode getSegmentationMode() {
		return segmentationMode;
	}

	public void setSegmentationMode(SegmentationMode segmentationMode) {
		this.segmentationMode = segmentationMode;
	}

	public VariableStore getVariableStore() {
		return variableStore;
	}

	public void setVariableStore(VariableStore variableStore) {
		this.variableStore = variableStore;
	}

	/* Package-Private for testing */
	List<String> segmentNaively(String word, Iterable<String> keys) {
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

	/* Package-Private for testing */
	List<String> segment(String word) {
		return segment(word, new ArrayList<String>());
	}

	/* Package-Private for testing */
	List<String> segment(String word, Iterable<String> keys) {
		List<String> segments = new ArrayList<String>();

		StringBuilder buffer = new StringBuilder();
		int length = word.length();
		for (int i = 0; i < length; i++) {
			String substring = word.substring(i);
			String key = getBestMatch(substring, keys);
			if (i == 0) {
				if (key.isEmpty()) {
					buffer.append(word.charAt(0));
				} else {
					buffer.append(key);
					i += key.length() - 1;
				}
			} else {
				char c = word.charAt(i);
				if (isAttachable(c)) {
					buffer.append(c);
					if (isDoubleWidthBinder(c) && i < length - 1) {
						i++;
						buffer.append(word.charAt(i));
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

	private boolean isCombiningClass(int type) {
		return (type == Character.MODIFIER_LETTER) || // LM
		       (type == Character.MODIFIER_SYMBOL) || // SK
		       (type == Character.COMBINING_SPACING_MARK) || // MC
		       (type == Character.NON_SPACING_MARK);         // MN
	}

	private boolean isDoubleWidthBinder(char ch) {
		return ch <= 866 && 860 <= ch;
	}

	private boolean isSuperscriptAsciiDigit(int value) {
		// int literals are decimal char values
		return (value == 178) ||
		       (value == 179) ||
		       (value == 185);
	}

	private boolean isMathematicalSubOrSuper(int value) {
		// int literals are decimal char values
		return (value <= 8304) && (8348 <= value);
	}

	private boolean isCombingNOS(int value) {
		// int literals are decimal char values
		return (value >= 8304) &&
		       (value <= 8348);
	}

	private String getBestMatch(String tail, Iterable<String> keys) {
		String bestMatch = "";
		for (String key : keys) {
			if (tail.startsWith(key) && bestMatch.length() < key.length()) {
				bestMatch = key;
			}
		}

		Matcher backReferenceMatcher = BACKREFERENCE_PATTERN.matcher(tail);
		if (backReferenceMatcher.lookingAt()) {
			bestMatch = backReferenceMatcher.group();
		}

		return bestMatch;
	}

	private boolean isAttachable(Character c) {
		int type = Character.getType(c);
		int value = c;
		return isSuperscriptAsciiDigit(value) ||
		       isMathematicalSubOrSuper(value) ||
		       isCombingNOS(value) ||
		       isCombiningClass(type);
	}

	private StringBuilder clearBuffer(Collection<String> segments, StringBuilder buffer, String key) {
		segments.add(buffer.toString());
		buffer = new StringBuilder();
		buffer.append(key);
		return buffer;
	}
}
