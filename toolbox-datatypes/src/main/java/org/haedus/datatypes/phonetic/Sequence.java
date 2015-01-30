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

package org.haedus.datatypes.phonetic;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Samantha Fiona Morrigan McCabe
 */
public class Sequence implements Iterable<Segment>, ModelBearer {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(Sequence.class);

	public static final Sequence EMPTY_SEQUENCE = new Sequence(Segment.EMPTY_SEGMENT);

	private final List<Segment> sequence;
	private final FeatureModel  featureModel;

	public Sequence(Sequence q) {
		sequence = new ArrayList<Segment>(q.getSegments());
		featureModel = q.getFeatureModel();
	}

	public Sequence(Segment g) {
		this(g.getFeatureModel());
		sequence.add(g);
	}

	// Used to produce empty copies with the same model
	public Sequence(FeatureModel modelParam) {
		sequence = new LinkedList<Segment>();
		featureModel = modelParam;
	}

	@VisibleForTesting
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
		return sequence.size();
	}

	public Sequence getSubsequence(int i) {
		return getSubsequence(i, sequence.size());
	}

	/**
	 * Returns a new sub-sequence spanning the provided indices
	 *
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
		return sequence.indexOf(s);
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

	public int indexOf(Sequence aSequence) {
		validateModelOrWarn(aSequence);
		int size = aSequence.size();
		int index = -1;

		if (size <= size() && !aSequence.isEmpty()) {
			index = indexOf(aSequence.getFirst());
			if (index >= 0 && index + size <= size()) {
				Sequence u = getSubsequence(index, index + size);
				if (!aSequence.equals(u)) {
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
		String s = "";

		for (Segment a_sequence : sequence) {
			s += a_sequence.getSymbol();
		}
		return s;
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

	public boolean startsWith(Segment segment) {
		validateModelOrWarn(segment);
		return !isEmpty() && sequence.get(0).equals(segment);
	}

	public boolean startsWith(Sequence sequence) {
		validateModelOrWarn(sequence);
		return indexOf(sequence) == 0;
	}

	@Override
	public FeatureModel getFeatureModel() {
		return featureModel;
	}

	@VisibleForTesting
	void add(Segment[] segments) {
		Collections.addAll(sequence, segments);
	}

	@VisibleForTesting
	int[] indicesOf(Sequence q) {
		int[] indices = new int[0];

		int index = indexOf(q);

		while (index >= 0) {
			indices = ArrayUtils.add(indices, index);
			index = indexOf(q, index + 1);
		}
		return indices;
	}

	private void validateModelOrWarn(ModelBearer that) {
		if (!featureModel.equals(that.getFeatureModel())) {
			LOGGER.warn("Attempting to check a {} with an incompatible model!\n\t{}\t{}\n\t{}\t{}",
				that.getClass(),this, that,featureModel.getFeatureNames(),that.getFeatureModel().getFeatureNames());
		}
	}

	private void validateModelOrFail(ModelBearer that) {
		if (!featureModel.equals(that.getFeatureModel())) {
			throw new RuntimeException(
				"Attempting to add " + that.getClass() + " with an incompatible model!\n" +
					'\t' + this + '\t' + featureModel.getFeatureNames() + '\n' +
					'\t' + that + '\t' + that.getFeatureModel().getFeatureNames()
			);
		}
	}
}
