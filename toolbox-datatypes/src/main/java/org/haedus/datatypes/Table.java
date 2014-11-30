package org.haedus.datatypes;

import java.util.List;

/**
 * Author: goats
 * Created: 11/30/2014
 */
public interface Table<T> {
	T get(int i, int j);

	void set(T t, int i, int j);

	int totalSize();

	int getNumberRows();

	int getNumberOfColumns();

	List<T> getBackingList();
}
