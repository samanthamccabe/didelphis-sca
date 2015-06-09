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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Samantha Fiona Morrigan McCabe
 */
public class Alignment implements ModelBearer, Iterable<Alignment> {

	private final FeatureModel   featureModel;
	private final List<Sequence> left;
	private final List<Sequence> right;
	
	private double score;

	public Alignment(FeatureModel modelParam) {
		featureModel = modelParam;
		left = new ArrayList<Sequence>();
		right = new ArrayList<Sequence>();
	}

	public Alignment(List<Sequence> leftParam, List<Sequence> rightParam) {
		this(leftParam.get(0).getModel());
		left.addAll(leftParam);
		right.addAll(rightParam);
	}

	public Alignment(Sequence l, Sequence r) {
		this(l.getModel());
		modelConsistencyCheck(l, r);
		left.add(l);
		right.add(r);
	}

	public Alignment(Segment l, Segment r) {
		this(new Sequence(l), new Sequence(r));
	}

	public Alignment(Alignment alignment) {
		this(alignment.getModel());
		left.addAll(alignment.getLeft());
		right.addAll(alignment.getRight());
	}

	public void add(Segment l, Segment r) {
		modelConsistencyCheck(l, r);
		left.add(new Sequence(l));
		right.add(new Sequence(r));
	}
	
	public void add(Sequence l, Sequence r) {
		left.add(l);
		right.add(r);
	}

	public void add(Alignment a) {
		validateModelOrFail(a);
		left.addAll(a.getLeft());
		right.addAll(a.getRight());
	}

	public int size() {
		return left.size();
	}

	public String toStringPretty() {
		StringBuilder leftBuilder  = new StringBuilder();
		StringBuilder rightBuilder = new StringBuilder();
		for (int i = 0; i < left.size(); i++) {
			String l = left.get(i).toString();
			String r = right.get(i).toString();
			
			int leftVisible  = 0;
			int rightVisible = 0;

			for (char c : l.toCharArray()) {
				if (Character.getType(c) != Character.NON_SPACING_MARK) {
					leftVisible++;
				}
			}

			for (char c : r.toCharArray()) {
				if (Character.getType(c) != Character.NON_SPACING_MARK) {
					rightVisible++;
				}
			}

			leftBuilder.append(l).append(' ');
			rightBuilder.append(r).append(' ');
			while (leftVisible > rightVisible) {
				rightBuilder.append(' ');
				rightVisible++;
			}
			
			while (leftVisible < rightVisible) {
				leftBuilder.append(' ');
				leftVisible++;
			}
		}
		return leftBuilder.toString().trim() + '\t' + rightBuilder.toString().trim();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		Iterator<Sequence> l = left.iterator();
		while (l.hasNext()) {
			Sequence next = l.next();
			
			for (Segment segment : next) {
//				List<Double> features = segment.getFeatures();
//				String symbol;
//				if (features.equals(featureModel.getBlankArray())) {
//					symbol = segment.getSymbol();
//				} else {
//					symbol = featureModel.getBestSymbol(features);
//				}
				sb.append(segment);
			}

			if (l.hasNext()) {
				sb.append(' ');
			}
		}
		sb.append('\t');
		Iterator<Sequence> r = right.iterator();
		while (r.hasNext()) {
			Sequence next = r.next();

			for (Segment segment : next) {
//				List<Double> features = segment.getFeatures();
//				String symbol;
//				if (features.equals(featureModel.getBlankArray())) {
//					symbol = segment.getSymbol();
//				} else {
//					symbol = featureModel.getBestSymbol(features);
//				}
				sb.append(segment);
			}
			
			if (r.hasNext()) {
				sb.append(' ');
			}
		}
		return sb.toString();
	}

	public Alignment get(int i) {
		List<Sequence> l = left.subList(i, i + 1);
		List<Sequence> r = right.subList(i, i + 1);
		return new Alignment(l, r);
	}

	public Alignment getLast() {
		return get(size()-1);
    }

    public void setScore(double scoreParam) {
        score = scoreParam;
    }

	public double getScore() {
		return score;
	}

	@Override
    public boolean equals(Object obj) {
		if (obj == this)                 { return true;  }
        if (obj == null)                 { return false; }
		if (!(obj instanceof Alignment)) { return false; }

		Alignment alignment = (Alignment) obj;

		return left.equals(alignment.left) &&
			right.equals(alignment.right);
	}

    public List<Sequence> getLeft() {
        return left;
    }

    public List<Sequence> getRight() {
        return right;
    }

    @Override
	public FeatureModel getModel() {
		return featureModel;
	}

	@Override
	public Iterator<Alignment> iterator() {
		Collection<Alignment> pairs = new ArrayList<Alignment>();
		for (int i = 0; i < left.size(); i++) {
			pairs.add(new Alignment(
				left.get(i),
				right.get(i))
			);
		}
		return pairs.iterator();
	}

	@Override
	public int hashCode() {
		int result;
		result = featureModel.hashCode();
		result = 31 * result + left.hashCode();
		result = 31 * result + right.hashCode();
		return result;
	}

	private void validateModelOrFail(ModelBearer that) {
		FeatureModel thatFeatureModel = that.getModel();
		if (!featureModel.equals(thatFeatureModel)) {
			throw new RuntimeException(
				"Attempting to add " + that.getClass() + " with an incompatible model!\n" +
					'\t' + this + '\t' + featureModel.getFeatureNames() + '\n' +
					'\t' + that + '\t' + thatFeatureModel.getFeatureNames()
			);
		}
	}

	private static void modelConsistencyCheck(ModelBearer l, ModelBearer r) {
		FeatureModel mL = l.getModel();
		FeatureModel mR = r.getModel();
		if (!mL.equals(mR)) {
			throw new RuntimeException(
				"Attempting to create Alignment using incompatible models!\n" +
					'\t' + l + '\t' + mL.toString() + '\n' +
					'\t' + r + '\t' + mR.toString() + '\n'
			);
		}
	}
}
