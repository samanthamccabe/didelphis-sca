/*******************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.haedus.tables;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Samantha Fiona Morrigan McCabe
 *
 * @param <E>
 */
public final class RectangularTable<E> extends AbstractTable<E> {

	private final List<E> array;

	private RectangularTable(int row, int col) {
		super(row, col);
		array = new ArrayList<E>(row * col);
	}

	public RectangularTable(E defaultValue, int row, int col) {
		this(row, col);

		for (int i = 0; i < row * col; i++) {
			array.add(defaultValue);
		}
	}

	public RectangularTable(RectangularTable<E> table) {
		this(table.getNumberRows(), table.getNumberColumns());
		array.addAll(table.array);
	}

	/**
	 * Retrieve the element at the specified location
	 * @param i the index for column
	 * @param j the index for row
	 * @return the object stored at these coordinates
	 */
	@Override
	public E get(int i, int j) {
		rangeCheck(i, nRows);
		rangeCheck(j, nCols);
		return array.get(getIndex(i, j));
	}

	/**
	 * Put an element into the specified location in the Table
	 * @param element the object to place at the specified coordinates
	 * @param i the index for column
	 * @param j the index for row
	 */
	@Override
	public void set(E element, int i, int j) {
		rangeCheck(i, nRows);
		rangeCheck(j, nCols);
		int index = getIndex(i, j);
		array.set(index, element);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		RectangularTable<?> that = (RectangularTable<?>) o;

		return array.equals(that.array) &&
				nRows != that.nRows &&
				nCols != that.nCols;
	}

	@Override
	public int hashCode() {
		int result = array.hashCode();
		result = 31 * result + nRows;
		result = 31 * result + nCols;
		return result;
	}

	/**
	 * Computes and returns the absolute index of the internal array based on
	 * the provided coordinates.
	 * @param i the column position
	 * @param j the row position
	 * @return the absolute index of the internal array based on the provided
	 * coordinates.
	 */
	private int getIndex(int i, int j) {
		return i + j * nRows;
	}

	@Override
	public String getPrettyTable() {
		StringBuilder sb = new StringBuilder(array.size() * 8);

		int i = 1;
		for (E e : array) {
			if (e instanceof Double) {
				sb.append(DECIMAL_FORMAT.format(e));
				if (i % nCols == 0) {
					sb.append("\n");
				} else {
					sb.append("  ");
				}
				i++;
			}
		}

		return sb.toString();
	}

	@Override
	public String toString() {
		return "RectangularTable{" +
				"numberRows=" + nRows +
				", numberColumns=" + nCols +
				", array=" + array +
				'}';
	}
}
