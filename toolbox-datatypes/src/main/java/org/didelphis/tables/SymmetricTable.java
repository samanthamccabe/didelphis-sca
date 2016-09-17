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

package org.didelphis.tables;

import java.util.ArrayList;
import java.util.List;

/**
 * @param <E>
 * @author Samantha Fiona Morrigan McCabe
 */
public class SymmetricTable<E> extends AbstractTable<E> {

	private final List<E> array;

	private SymmetricTable(int n) {
		super(n, n);
		array = new ArrayList<E>(n + n * n / 2);
	}

	public SymmetricTable(int n, List<E> array) {
		super(n, n);

		int size = n + n * n / 2;
		
		if (array.size() == size) {
			this.array = new ArrayList<E>(array);
		} else {
			throw new IllegalArgumentException(
					"Array was provided with size " + array.size() + " but " +
							"must be " + size
			);
		}
	}
	
	public SymmetricTable(E defaultValue, int n) {
		this(n);
		
		int number = getIndex(n, n)-1;
		for (int i = 0; i < number; i++) {
			array.add(defaultValue);
		}
	}

	public SymmetricTable(SymmetricTable<E> otherTable) {
		this(otherTable.getNumberRows());
		array.addAll(otherTable.array);
	}

	@Override
	public String getPrettyTable() {

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < getNumberRows(); i++) {
			for (int j = 0; j <= i;j ++) {
				sb.append(get(i,j));
				if (j < i) {
					sb.append('\t');
				}
			}
			if (i < getNumberRows() -1) {
				sb.append('\n');
			}
		}
		return sb.toString();
	}

	@Override
	public void set(E element, int i, int j) {
		int index = getIndex(i, j);
		array.set(index, element);
	}

	@Override
	public E get(int i, int j) {
		return array.get(getIndex(i, j));
	}


	private static int getIndex(int i, int j) {
		if (j > i) {
			return getRowStart(j) + i;
		} else {
			return getRowStart(i) + j;
		}
	}

	private static int getRowStart(int row) {
		int sum = 0;
		for (int i = 0; i <= row; i++) {
			sum += i;
		}
		return sum;
	}

	@Override
	public String toString() {
		return "SymmetricTable{" +
				"array=" + array +
				'}';
	}
}
