package org.haedusfc.datatypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author goats
 *
 * @param <T>
 */
public class SymmetricTable<T> {

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

		Collections.addAll(this.array,array);
	}

    public SymmetricTable(T defaultValue, int n) {
        this(n);
        for( int i = 0; i < array.size(); i++) {
            array.set(i, defaultValue);
        }
	}

	public T get(int i, int j) {
		return array.get(getIndex(i,j));
	}

	public void set(T t, int i, int j) {
	    array.set(getIndex(i, j), t);
	}

	public int getIndex(int i, int j) {
	    return (i + (j * (i+1)));
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

	public List<T> getArray() {
	    return Collections.unmodifiableList(array);
	}

    public int size() {
        return array.size();
    }

    @Override
    public boolean equals(Object obj) {

		if (obj == null) return false;
		if (obj.getClass() != getClass()) return false;

		SymmetricTable other = (SymmetricTable) obj;

		return (dimension == other.getDimension()) &&  array.equals(other.getArray());
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    @Override
    public int hashCode() {
		int tableHash = 0;
		for (int i = 0; i < array.size(); i++) {
			int factor = dimension * (i + 1);
			tableHash += factor * array.get(i).hashCode();
		}
		int hashCode = (tableHash * 837);
		hashCode += dimension * 43;
		return hashCode;
	}

    public int getDimension() {
        return dimension;
    }
}
