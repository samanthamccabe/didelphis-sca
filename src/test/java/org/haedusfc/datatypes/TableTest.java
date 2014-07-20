package org.haedusfc.datatypes;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Class TableTest
 * <p/>
 * Description:
 *
 * <p/>
 * Created: 09/19/2013
 */
public class TableTest
{

	@Test
	public void testConstructor01() {
		Table<Integer> table = new Table<Integer>();

		assertTrue(table.size() == 0);
	}

	@Test
	public void testConstructor02() {
		Table<Integer> table = new Table<Integer>(0,4,5);
		Table<Integer> other = table.copy();

		assertTrue(table.equals(other));
		assertFalse(table == other);
	}

	@Test
	public void testIdentity01() {
		Table<String> table = new Table<String>("", 4, 5);

		table.set("A", 2, 2);

		Table<String> other = table.copy();

		assertEquals(table.get(2, 2), other.get(2, 2));
		assertSame(  table.get(2, 2),	 other.get(2, 2));
	}
}
