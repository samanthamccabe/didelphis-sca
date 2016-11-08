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

import java.util.List;
import java.util.Set;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 4/10/2016
 */
public interface TwoKeyMultiMap<T,U,V> extends Iterable<Triple<T,U,Set<V>>> {

	Set<V> get(T k1, U k2);

	void put(T k1, U k2, Set<V> values);
	
	void add(T k1, U k2, V value);
	
	boolean contains(T k1, U k2);
	
	List<Tuple<T,U>> listKeys();
}
