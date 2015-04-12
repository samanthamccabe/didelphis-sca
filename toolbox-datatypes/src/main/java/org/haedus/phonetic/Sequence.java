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

package org.haedus.phonetic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Samantha Fiona Morrigan McCabe
 */
public class Sequence implements Iterable<Segment>, ModelBearer {

	public static final Sequence DOT_SEQUENCE   = new Sequence(Segment.DOT_SEGMENT);
	public static final Sequence EMPTY_SEQUENCE = new Sequence(Segment.EMPTY_SEGMENT);

	private static final transient Logger LOGGER = LoggerFactory.getLogger(Sequence.class);
	private final List<Segment> sequence;
	private final FeatureModel  featureModel;

	public Sequence(Sequence q) {
		sequence = new ArrayList<Segment>(q.getSegments());
		featureModel = q.getModel();
	}

	public Sequence(Segment g) {
		this(g.getModel());
		sequence.add(g);
	}

	// Used to produce empty copies with the same model
	public Sequence(FeatureModel modelParam) {
		sequence = new LinkedList<Segment>();
		featureModel = modelParam;
	}

	// Visible for testing
	Sequence(String word) {
		this();
		for (char c : word.toCharArray()) {
			sequence.add(new Segment(new String(new char[]{c}), featureModel));
		}
	}

	private Sequence() {
		this(FeatureModel.EMPTY_MODEL);
	}

	private Sequence(Collection<Segment> segments, FeatureModel featureTable) {
		sequence = new LinkedList<Segment>(segments);
		featureModel = featureTable;
	}

	public void add(Segment s) {
		validateModelOrFail(s);
		sequence.add(s);
	}

	public void add(Sequence otherSequence) {
		validateModelOrFail(otherSequence);
		for (Segment s : otherSequence) {
			sequence.add(s);
		}
	}

	public void insert(Sequence q, int index) {
		validateModelOrFail(q);
		sequence.addAll(index, q.getSegments());
	}

	public Segment get(int i) {
		return sequence.get(i);
	}

	public Segment getFirst() {
		return get(0);
	}

	public Segment getLast() {
		return get(sequence.size() - 1);
	}

	public void set(int i, Segment s) {
		sequence.set(i, s);
	}

	public int size() {
		return sequence.size	();
	}

	public Sequence getSubsequence(int i) {
		return getSubsequence(i, sequence.size());
	}

	/**
	 * Returns a new sub-sequence spanning the provided indices
	 * @param i the starting index, inclusive - must be greater than zero
	 * @param k the ending index, exclusive - must be less than the sequence length
	 * @return
	 */
	public Sequence getSubsequence(int i, int k) {
		int index = k <= size() ? k : size();
		return new Sequence(sequence.subList(i, index), featureModel);
	}

	public int indexOf(Segment s) {
		validateModelOrWarn(s);
		int index = -1;

		for (int i = 0; i < sequence.size() && index == -1; i++) {
			Segment segment = sequence.get(i);
			index = segment.matches(s) ? i : -1;
		}
		return index;
	}

	public Segment remove(int index) {
		return sequence.remove(index);
	}

	public Sequence remove(int start, int end) {
		Sequence q = new Sequence(featureModel);
		for (int i = 0; i < end - start; i++) {
			q.add(remove(start));
		}
		return q;
	}

	/**
	 * Determines if a sequence is consistent with this sequence.
	 * Sequences must be of the same length
	 * Two sequences are consistent if each other if all corresponding segments are consistent; i.e. if
	 * if, for ever segment in each sequence, all corresponding features are equal OR if one is NaN
	 * @param aSequence a sequence to check against this one
	 * @return true if, for each segment in both sequences, all specified (non NaN) features in either segment are equal
	 */
	public boolean matches(Sequence aSequence) {
		validateModelOrFail(aSequence);
		boolean matches = false;
		if (featureModel == FeatureModel.EMPTY_MODEL) {
			matches = equals(aSequence);
		} else {
		int size = size();
		if (size == aSequence.size()) {
			matches = true;
			for (int i = 0; i < size && matches; i++) {
				Segment a = get(i);
				Segment b = aSequence.get(i);
				matches = a.matches(b);
			}
		}
		}
		return matches;
	}

	public int indexOf(Sequence aSequence) {
		validateModelOrWarn(aSequence);
		int size = aSequence.size();
		int index = -1;

		if (size <= size() && !aSequence.isEmpty()) {
			index = indexOf(aSequence.getFirst());
			if (index >= 0 && index + size <= size()) {
				Sequence u = getSubsequence(index, index + size);
				// originally was equals, but use matches instead
				if (!aSequence.matches(u)) {
					index = -1;
				}
			}
		}
		return index;
	}

	public int indexOf(Sequence target, int start) {
		validateModelOrWarn(target);
		int index = -1;
		if (start < size()) {
			Sequence subsequence = getSubsequence(start);
			index = subsequence.indexOf(target);
			if (index > -1) {
				index += start;
			}
		}
		return index;
	}

	public Sequence replaceAll(Sequence source, Sequence target) {
		validateModelOrFail(source);
		validateModelOrFail(target);
		Sequence result = new Sequence(this);

		int index = result.indexOf(source);
		while (index >= 0) {
			if (index + source.size() <= result.size()) {
				result.remove(index, index + source.size());
				result.insert(target, index);
			}
			index = result.indexOf(source, index + target.size());
		}
		return result;
	}

	@Override
	public Iterator<Segment> iterator() {
		return sequence.iterator();
	}

	@Override
	public int hashCode() {
		int hash = 23;
		hash *= sequence.hashCode();
		hash *= featureModel.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj.getClass() != getClass()) { return false; }
		Sequence object = (Sequence) obj;
		boolean sequenceEquals = sequence.equals(object.sequence);
		boolean featuresEquals = featureModel.equals(object.featureModel);
		return sequenceEquals && featuresEquals;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Segment a_sequence : sequence) {
			sb.append(a_sequence.getSymbol());
		}
		return sb.toString();
	}

	public boolean isEmpty() {
		return sequence.isEmpty();
	}

	public Sequence getReverseSequence() {
		Sequence reversed = new Sequence(featureModel);
		for (Segment g : sequence) {
			reversed.addFirst(g);
		}
		return reversed;
	}

	public List<Segment> getSegments() {
		return new ArrayList<Segment>(sequence);
	}

	public void addFirst(Segment g) {
		validateModelOrFail(g);
		sequence.add(0, g);
	}

	public boolean contains(Sequence sequence) {
		validateModelOrWarn(sequence);
		return indexOf(sequence) >= 0;
	}

	public boolean startsWith(Segment aSegment) {
		validateModelOrWarn(aSegment);
		return !isEmpty() && sequence.get(0).equals(aSegment);
	}

	public boolean startsWith(Sequence aSequence) {
		validateModelOrWarn(aSequence);
		return indexOf(aSequence) == 0;
	}

	@Override
	public FeatureModel getModel() {
		return featureModel;
	}

	// Visible for testing
	List<Integer> indicesOf(Sequence q) {
		List<Integer> indices = new ArrayList<Integer>();

		int index = indexOf(q);

		while (index >= 0) {
			indices.add(index);
			index = indexOf(q, index + 1);
		}
		return indices;
	}

	private void validateModelOrWarn(ModelBearer that) {
		if (!featureModel.equals(that.getModel())) {
			LOGGER.warn("Attempting to check a {} with an incompatible model!\n\t{}\t{}\n\t{}\t{}",
				that.getClass(), this, that, featureModel.getFeatureNames(), that.getModel().getFeatureNames());
		}
	}

	private void validateModelOrFail(ModelBearer that) {
		if (!featureModel.equals(that.getModel())) {
			throw new RuntimeException(
				"Attempting to add " + that.getClass() + " with an incompatible model!\n" +
					'\t' + this + '\t' + featureModel.getFeatureNames() + '\n' +
					'\t' + that + '\t' + that.getModel().getFeatureNames()
			);
		}
	}
}
