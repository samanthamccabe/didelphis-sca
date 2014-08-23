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

import org.junit.Test;

import static org.junit.Assert.*;

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
		assertSame(table.get(2, 2), other.get(2, 2));
	}
}
