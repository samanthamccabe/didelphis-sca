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

package org.haedus.tables;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Samantha Fiona Morrigan McCabe
 *
 * @param <T>
 */
public class RectangularTable<T> implements Table<T> {

	private   final   int   numberRows;
	private   final   int   numberColumns;
	protected final List<T> array;

	protected RectangularTable(int k, int l) {
		numberRows    = k;
		numberColumns = l;

		array = new ArrayList<T>();
	}

	public RectangularTable(T defaultValue, int k, int l) {
		this(k, l);
		for (int i = 0; i < k * l; i++) {
			array.add(defaultValue);
		}
	}

	/**
	 * Retrieve the element at the specified location
	 * @param i the index for column
	 * @param j the index for row
	 * @return the object stored at these coordinates
	 */
	@Override
	public T get(int i, int j) {
		return array.get(getIndex(i, j));
	}

	/**
	 * Put an element into the specified location in the Table
	 * @param t the object to place at the specified coordinates
	 * @param i the index for column
	 * @param j the index for row
	 */
	@Override
	public void set(T t, int i, int j) {
		int index = getIndex(i, j);
		array.set(index, t);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		RectangularTable rhs = (RectangularTable) obj;
		return new EqualsBuilder()
				.append(numberRows, rhs.numberRows)
				.append(numberColumns, rhs.numberColumns)
				.append(array, rhs.array)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(numberRows)
				.append(numberColumns)
				.append(array)
				.toHashCode();
	}

	/**
	 * Computes and returns the absolute index of the internal array based on the provided coordinates.
	 * @param i the column position
	 * @param j the row position
	 * @return the absolute index of the internal array based on the provided coordinates.
	 */
	private int getIndex(int i, int j) {
	    return i + j * numberRows;
	}

	@Override
	public int getNumberRows() {
        return numberRows;
    }

	@Override
	public int getNumberColumns() {
		return numberColumns;
	}

	@Override
	public String getPrettyTable() {
		return "";
	}
}
