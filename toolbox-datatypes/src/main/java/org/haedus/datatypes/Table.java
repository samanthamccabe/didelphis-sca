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

package org.haedus.datatypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Samantha Fiona Morrigan McCabe
 *
 * @param <T>
 */
public class Table<T> {

	private final int     numberOfRows;
	private final int     numberOfColumns;
	private final List<T> table;

	/**
	 * Construct an empty Table
	 */
	public Table() {
		this(0, 0);
	}

    private Table(int k, int l) {
	    numberOfRows    = k;
        numberOfColumns = l;

		table = new ArrayList<T>(numberOfRows * numberOfColumns);
	}

    /**
     * Construct a Table with the specified dimensions
     * @param k the number of columns
     * @param l the number of rows
     */
    public Table(int k, int l, Collection<T> array) {
		this(k,l);
		table.addAll(array);
	}

	/**
	 *
	 * @param defaultValue
	 * @param k
	 * @param l
	 */
    public Table(T defaultValue, int k, int l) {
		this(k,l);
        for (int i = 0; i < k*l; i++) {
            table.add(defaultValue);
        }
	}

	/**
	 * Retrieve the element at the specified location
	 * @param i the index for column
	 * @param j the index for row
	 * @return the object stored at these coordinates
	 */
	public T get(int i, int j) {
		return table.get(getIndex(i, j));
	}

	/**
	 * Put an element into the specified location in the Table
	 * @param t the object to place at the specified coordinates
	 * @param i the index for column
	 * @param j the index for row
	 */
	public void set(T t, int i, int j) {
		int index = getIndex(i, j);
		table.set(index, t);
	}

	/**
	 * Computes and returns the absolute index of the internal array based on the provided coordinates.
	 * @param i the column position
	 * @param j the row position
	 * @return the absolute index of the internal array based on the provided coordinates.
	 */
	private int getIndex(int i, int j) {
	    return (i + (j * numberOfRows));
	}

	public Table<T> copy() {
	    return new Table<T>(numberOfRows, numberOfColumns, table);
	}

	@Override
	public String toString() {
		String s = "";
		for (int i = 0; i < size(); i++) {
			s = s.concat(table.get(i).toString() + " ");
			if ((i + 1) % numberOfColumns == 0) {
				s = s.concat("\n");
			}
		}
		return s;
	}

    public int size() {
        return table.size();
    }

	@Override
	public boolean equals(Object obj) {

		if (obj == null) return false;
		if (obj.getClass() != getClass()) return false;

		Table<?> other = (Table<?>) obj;

		return (numberOfColumns == other.getNumberOfColumns()) &&
				numberOfRows == other.getNumberRows() &&
				table.equals(other.getTable());
	}

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

	@Override
	public int hashCode() {
		int hashCode = 33;
		for (int i = 0; i < table.size(); i++) {
			hashCode += table.get(i).hashCode() + i;
		}
		return hashCode * numberOfColumns * numberOfRows * 31;
	}

	public int getNumberRows() {
        return numberOfRows;
    }

	public int getNumberOfColumns() {
		return numberOfColumns;
	}

	public List<T> getTable() {
		return table;
	}
}
