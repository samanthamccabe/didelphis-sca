package org.haedus.datatypes.tables;

/**
 * Author: goats
 * Created: 11/30/2014
 */
public interface Table<T> {
	T get(int i, int j);

	void set(T t, int i, int j);

	int getNumberRows();

	int getNumberColumns();

	String getPrettyTable();
}
