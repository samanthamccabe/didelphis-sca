package org.haedus.datatypes.phonetic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.haedus.datatypes.SegmentationMode;
import org.haedus.datatypes.Segmenter;

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

	private final Segment boundarySegmentNAN;

	private final FeatureModel     featureModel;
	private final VariableStore    variableStore;	// VariableStore is only accessed for its keys
	private final SegmentationMode segmentationMode;

	public SequenceFactory() {
		this(new FeatureModel(), new VariableStore(), SegmentationMode.DEFAULT);
	}

	public SequenceFactory(FeatureModel modelParam, VariableStore storeParam) {
		this(modelParam, storeParam, SegmentationMode.DEFAULT);
	}

	public SequenceFactory(FeatureModel modelParam, VariableStore storeParam, SegmentationMode modeParam) {
		featureModel     = modelParam;
		variableStore    = storeParam;
		segmentationMode = modeParam;

		List<Double> list = new ArrayList<Double>();
		for (int i = 0; i < featureModel.getNumberOfFeatures(); i++) {
			list.add(Double.NaN);
		}
		boundarySegmentNAN = new Segment("#", list, featureModel);
	}

	public Segment getBoundarySegment() {
		return boundarySegmentNAN;
	}

	public Segment getSegment(String string) {
		return Segmenter.getSegment(string, featureModel, variableStore, segmentationMode);
	}

	public Sequence getSequence(String word) {
		return Segmenter.getSequence(word, featureModel,variableStore,segmentationMode);
	}

	public boolean hasVariable(Sequence label) {
		return variableStore.contains(label);
	}

	public List<Sequence> getVariableValues(Sequence label) {
		return variableStore.get(label);
	}

	public List<String> getSegmentedString(String string) {
		Collection<String> keys = new ArrayList<String>();

		keys.addAll(variableStore.getKeys());
		keys.addAll(featureModel.getSymbols());

		return Segmenter.getSegmentedString(string, keys, segmentationMode);
	}
	public FeatureModel getFeatureModel() {
		return featureModel;
	}

	public SegmentationMode getSegmentationMode() {
		return segmentationMode;
	}

	public VariableStore getVariableStore() {
		return variableStore;
	}

	private static boolean isCombiningClass(int type) {
		return type == Character.MODIFIER_LETTER        || // LM
			   type == Character.MODIFIER_SYMBOL        || // SK
			   type == Character.COMBINING_SPACING_MARK || // MC
			   type == Character.NON_SPACING_MARK;         // MN
	}

	private static boolean isDoubleWidthBinder(char ch) {
		return (int) ch <= 866 && 860 <= (int) ch;
	}

	private static boolean isSuperscriptAsciiDigit(int value) {
		// int literals are decimal char values
		return value == 178 ||
		       value == 179 ||
		       value == 185;
	}

	private static boolean isMathematicalSubOrSuper(int value) {
		// int literals are decimal char values
		return value <= 8304 && 8348 <= value;
	}

	private static boolean isCombingNOS(int value) {
		// int literals are decimal char values
		return value >= 8304 &&
		       value <= 8348;
	}

	private static String getBestMatch(String tail, Iterable<String> keys) {
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

	private static boolean isAttachable(Character c) {
		int type = Character.getType(c);
		int value = c;
		return isSuperscriptAsciiDigit(value) ||
		       isMathematicalSubOrSuper(value) ||
		       isCombingNOS(value) ||
		       isCombiningClass(type);
	}

	private static StringBuilder clearBuffer(Collection<String> segments, StringBuilder buffer, String key) {
		segments.add(buffer.toString());
		buffer = new StringBuilder();
		buffer.append(key);
		return buffer;
	}
}
