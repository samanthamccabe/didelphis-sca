/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.haedus.datatypes.phonetic;

/**
 *
 * @author Goats
 */
public class Alignment {

	private final Sequence left;
	private final Sequence right;
	private       double   score;

	public Alignment(Sequence l, Sequence r) {
		left  = l;
		right = r;
		score = -1.0;
	}

	public Alignment(Segment l, Segment r) {
		left  = new Sequence(l);
		right = new Sequence(r);
		score = -1.0;
    }

    public Alignment() {
        left = new Sequence();
        right = new Sequence();
        score = -1;
    }

	public Alignment(Alignment alignment) {
		left  = new Sequence(alignment.getLeft());
		right = new Sequence(alignment.getRight());
		score = 1.0;
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

    public double score(FeatureModel aModel) {
        score = aModel.computeScore(left, right);
        return score;
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

    public boolean equals(Alignment other) {
        return (other.getLeft().equals(left) &&
                other.getRight().equals(right));
    }

    public Sequence getLeft() {
        return left;
    }

    public Sequence getRight() {
        return right;
    }
}
