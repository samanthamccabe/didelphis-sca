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

package org.didelphis.structures;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 4/10/2016
 */
public class Tuple<L, R> {
	
	private final L left;
	private final R right;
	
	public Tuple(L left, R right) {
		this.left  = left;
		this.right = right;
	}
	
	public L getLeft() {
		return left;
	}
	
	public R getRight() {
		return right;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof Tuple)) { return false; }

		Tuple<?, ?> tuple = (Tuple<?, ?>) obj;
		return left.equals(tuple.left) && right.equals(tuple.right);

	}

	@Override
	public int hashCode() {
		int result = 11;
		result = 31 * result + (left  !=null ? left.hashCode()  : 0);
		result = 31 * result + (right !=null ? right.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "<" + left + ", " + right + '>';
	}
}
