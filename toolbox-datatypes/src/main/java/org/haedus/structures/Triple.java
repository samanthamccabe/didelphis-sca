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

package org.haedus.structures;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 4/10/2016
 */
public class Triple<T,U,V> {
	
	private final T element1;
	private final U element2;
	private final V element3;
	
	public Triple(T element1, U element2, V element3) {
		this.element1 = element1;
		this.element2 = element2;
		this.element3 = element3;
	}
	
	public T getFirstElement() {
		return element1;
	}
	
	public U getSecondElement() {
		return element2;
	}
	
	public V getThirdElement() {
		return element3;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;

		Triple<?, ?, ?> triple = (Triple<?, ?, ?>) obj;
		return  element1 != null && element1.equals(triple.element1) && 
				element2 != null && element2.equals(triple.element2) && 
				element3 != null && element3.equals(triple.element3);
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + (element1 != null ? element1.hashCode() : 0);
		result = 31 * result + (element2 != null ? element2.hashCode() : 0);
		result = 31 * result + (element3 != null ? element3.hashCode() : 0);
		return result;
	}
}
