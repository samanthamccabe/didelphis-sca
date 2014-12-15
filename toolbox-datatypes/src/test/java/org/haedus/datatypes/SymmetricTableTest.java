package org.haedus.datatypes;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.haedus.datatypes.tables.SymmetricTable;
import org.haedus.datatypes.tables.Table;

/**
 * Author: goats
 * Created: 12/11/2014
 */
public class SymmetricTableTest {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(SymmetricTableTest.class);


	private static Table<String> indexedList;

	@BeforeClass
	public static void init() {
		indexedList = new SymmetricTable<String>("", 6);
	}

	@Test
	public void testGet01() {
		String prettyTable = indexedList.getPrettyTable();

		LOGGER.info("\n{}", prettyTable);
	}
}
