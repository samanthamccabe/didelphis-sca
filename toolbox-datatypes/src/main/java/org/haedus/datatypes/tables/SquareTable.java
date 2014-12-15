package org.haedus.datatypes.tables;

/**
 * Author: goats
 * Created: 12/14/2014
 */
public class SquareTable<T> extends RectangularTable<T> {

	protected SquareTable(int r, int c) {
		super(r, c);
	}

	public SquareTable(T defaultValue, int n) {
		super(defaultValue, n, n);
	}
}
