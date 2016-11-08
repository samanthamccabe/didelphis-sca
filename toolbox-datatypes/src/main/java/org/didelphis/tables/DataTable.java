/******************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe                                  *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *     http://www.apache.org/licenses/LICENSE-2.0                             *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 ******************************************************************************/

package org.didelphis.tables;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 8/23/2015
 */
public final class DataTable<E> implements ColumnTable<E> {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(DataTable.class);

	private final Map<String, List<E>> columns;
	
	private final List<List<E>> rows;
	private final List<String>  keys;

	private final int nRows;
	
	public DataTable(Map<String, List<E>> map) {
		columns = new LinkedHashMap<String, List<E>>();
		keys = new ArrayList<String>();
		rows = new ArrayList<List<E>>();
		
		int numberOfRows = 0;
		
		// Ensure we can 
		if (!map.isEmpty()) {
			Iterator<List<E>> iterator = map.values().iterator();
			numberOfRows = iterator.next().size();
			while (iterator.hasNext()) {
				rangeCheck(numberOfRows, iterator);
			}
			columns.putAll(map);
			keys.addAll(map.keySet());
			
			for ( int i =0; i < numberOfRows; i++) {
				List<E> row = new ArrayList<E>();
				for (String key : keys) {
					row.add(columns.get(key).get(i));
				}
				rows.add(row);
			}
		}
		nRows = numberOfRows;
	}

	private void rangeCheck(int n, Iterator<List<E>> iterator) {
		if (n != iterator.next().size()) {
			throw new IllegalArgumentException(
					"DataTable cannot be instantiated using a Map whose " + 
							"values are Lists of inconsistent length");
		}
	}

	@Override
	public boolean hasKey(String key) {
		return keys.contains(key);
	}

	@Override
	public List<String> getKeys() {
		return Collections.unmodifiableList(keys);
	}

	@Override
	public List<E> getColumn(String key) {
		if (hasKey(key)) {
			return Collections.unmodifiableList(columns.get(key));
		} else {
			return null;
		}
	}

	@Override
	public Map<String, E> getRowAsMap(int index) {
		Map<String, E> map = new LinkedHashMap<String, E>();
		for (String key : keys) {
			map.put(key, columns.get(key).get(index));
		}
		return map;
	}

	@Override
	public List<E> getRow(int index) {
		checkRowIndex(index);
		return Collections.unmodifiableList(rows.get(index));
	}

	@Override
	public E get(int i, int j) {
		checkRowIndex(j);
		return columns.get(keys.get(i)).get(j);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (!(o instanceof  DataTable)) { return false; }

		DataTable<?> dataTable = (DataTable<?>) o;

		return new EqualsBuilder()
				.append(nRows, dataTable.nRows)
				.append(columns, dataTable.columns)
				.append(rows, dataTable.rows)
				.build();
	}

	@Override
	public int hashCode() {
		int result = columns.hashCode();
		result = 31 * result + rows.hashCode();
		result = 31 * result + keys.hashCode();
		result = 31 * result + nRows;
		return result;
	}

	@Override
	
	public void set(E element, int i, int j) {
		checkRowIndex(j);
		columns.get(keys.get(i)).set(j, element);
	}

	@Override
	public int getNumberRows() {
		return nRows;
	}

	@Override
	public int getNumberColumns() {
		return keys.size();	
	}

	@Override
	public String getPrettyTable() {
		StringBuilder sb = new StringBuilder();

		Iterator<String> keyItr = keys.iterator();
		while (keyItr.hasNext()) {
			sb.append(keyItr.next());
			if (keyItr.hasNext()) sb.append('\t');
		}
		sb.append('\n');

		for (List<E> row : rows) {
			Iterator<E> rowItr = row.iterator();
			while (rowItr.hasNext()) {
				sb.append(rowItr.next());
				if (rowItr.hasNext()) sb.append('\t');
			}
			sb.append('\n');
		}

		return sb.toString();
	}

	@Override
	public Iterator<List<E>> iterator() {
		return Collections.unmodifiableCollection(rows).iterator();
	}

	private void checkRowIndex(int index) {
		if (nRows <= index) {
			throw new IndexOutOfBoundsException("Attempting to access row " +
					index + " of a table with only " + nRows + " rows");
		}
	}

	@Override
	public String toString() {
		return "DataTable{" +
				"columns=" + columns +
				", rows=" + rows +
				", keys=" + keys +
				", nRows=" + nRows +
				'}';
	}
}
