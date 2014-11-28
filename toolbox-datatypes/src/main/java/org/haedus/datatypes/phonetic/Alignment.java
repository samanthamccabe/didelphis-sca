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
package org.haedus.datatypes.phonetic;

/**
 *
 * @author Samantha Fiona Morrigan McCabe
 */
public class Alignment {

	private final Sequence left;
	private final Sequence right;
    private double score = Double.NaN;

	public Alignment(Sequence l, Sequence r) {
		left  = l;
		right = r;
	}

	public Alignment(Segment l, Segment r) {
		left  = new Sequence(l);
		right = new Sequence(r);
    }

    public Alignment() {
        left = new Sequence();
        right = new Sequence();
    }

	public Alignment(Alignment alignment) {
		left  = new Sequence(alignment.getLeft());
		right = new Sequence(alignment.getRight());
	}

    public void add(Segment a, Segment b) {
        left.add(a);
        right.add(b);
    }

    public void add(Alignment a) {
        left.add(a.getLeft());
        right.add(a.getRight());
    }

    public int size() {
        return left.size();
    }

	@Deprecated
    public double score(FeatureModel aModel) {
        return aModel.computeScore(left, right);
    }

    @Override
    public String toString() {
        return left.toString() + "\t" + right.toString();
    }

    public Alignment get(int i) {
        Segment l = left.get(i);
        Segment r = right.get(i);
        return new Alignment(l,r);
    }

    public Alignment getLast() {
        return get(size()-1);
    }

    public void setScore(double scoreParam) {
        score = scoreParam;
    }

	@Override
    public boolean equals(Object obj) {
        if (obj == null)
	        return false;
		if (obj.getClass() != getClass())
			return false;

		Alignment alignment = (Alignment) obj;

		return alignment.getLeft().equals(left) &&
		       alignment.getRight().equals(right);
	}

    public Sequence getLeft() {
        return left;
    }

    public Sequence getRight() {
        return right;
    }
}
