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
import java.util.Collections;
import java.util.List;

/**
 * @param <T>
 * @author Samantha Fiona Morrigan McCabe
 */
public class SymmetricTable<T> implements Table<T> {

	private final int     dimension;
	private final List<T> array;

	public SymmetricTable() {
		dimension = 0;
		array = new ArrayList<T>();
	}

	public SymmetricTable(int n) {
		dimension = n;
		array = new ArrayList<T>(n);
	}

	public SymmetricTable(int n, T[] array) {
		dimension = n;
		this.array = new ArrayList<T>();

		Collections.addAll(this.array, array);
	}

	public SymmetricTable(T defaultValue, int n) {
		this(n);
		for (int i = 0; i < array.size(); i++) {
			array.set(i, defaultValue);
		}
	}

	public SymmetricTable(SymmetricTable<T> otherTable) {
		dimension = otherTable.getNumberOfColumns();
		array     = new ArrayList<T>(otherTable.getBackingList());
	}

	@Override
	public T get(int i, int j) {
		return array.get(getIndex(i, j));
	}

	@Override
	public void set(T t, int i, int j) {
		array.set(getIndex(i, j), t);
	}

	@Override
	public int totalSize() {
		return array.size();
	}

	@Override
	public int getNumberRows() {
		return dimension;
	}

	@Override
	public int getNumberOfColumns() {
		return dimension;
	}

	@Override
	public List<T> getBackingList() {
		return Collections.unmodifiableList(array);
	}

	@Override
	public int hashCode() {
		int hashCode = 33;
		for (int i = 0; i < array.size(); i++) {
			hashCode += array.get(i).hashCode() + i;
		}
		return hashCode * dimension;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null)
			return false;
		if (obj.getClass() != getClass())
			return false;

		SymmetricTable<?> other = (SymmetricTable<?>) obj;

		return (dimension == other.getNumberOfColumns()) && array.equals(other.getBackingList());
	}

	@Override
	public String toString() {
		String s = "";
		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < i; j++) {
				s = s.concat(get(i, j).toString());
			}
		}
		return s;
	}

	private int getIndex(int i, int j) {
		return (i + (j * (i + 1)));
	}
}
