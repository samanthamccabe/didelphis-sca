/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 * (at your option) any later version.                                        *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 ******************************************************************************/

package org.haedus.tables;

import java.text.DecimalFormat;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 4/17/2016
 */
public abstract class AbstractTable<E> implements Table<E> {

	public static final DecimalFormat DECIMAL_FORMAT =
			new DecimalFormat(" 0.000;-0.000");
	
	protected final int nRows;
	protected final int nCols;

	protected AbstractTable(int k, int l) {
		nRows = k;
		nCols = l;
	}
	
	protected static void rangeCheck(int index, int size) {
		if(index >= size) {
			throw new IndexOutOfBoundsException(
					"Index: "+ index +", Size: "+size
			);
		}
	}

	@Override
	public int getNumberRows() {
		return nRows;
	}

	@Override
	public int getNumberColumns() {
		return nCols;
	}

}
