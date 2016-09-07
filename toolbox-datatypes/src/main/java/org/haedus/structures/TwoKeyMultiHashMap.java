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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 4/10/2016
 */
public class TwoKeyMultiHashMap<T,U,V> implements TwoKeyMultiMap<T,U,V> {
	
	private final Map<T,Map<U,Set<V>>> map;

	public TwoKeyMultiHashMap() {
		map = new HashMap<T, Map<U, Set<V>>>();
	}
	
	@Override
	public Set<V> get(T k1, U k2) {
		return map.get(k1).get(k2);
	}

	@Override
	public void put(T k1, U k2, Set<V> values) {
		if (map.containsKey(k1)) {
			Map<U, Set<V>> subMap = map.get(k1);
			if (subMap.containsKey(k2)) {
				subMap.get(k2).addAll(values);
			} else {
				subMap.put(k2, values);
			}
		} else {
			Map<U,Set<V>> subMap = new HashMap<U, Set<V>>();
			subMap.put(k2, values);
			map.put(k1,subMap);
		}
	}

	@Override
	public void add(T k1, U k2, V value) {
		if (map.containsKey(k1)) {
			Map<U, Set<V>> subMap = map.get(k1);
			if (subMap.containsKey(k2)) {
				subMap.get(k2).add(value);
			} else {
				Set<V> set = new HashSet<V>();
				set.add(value);
				subMap.put(k2, set);
			}
		} else {
			Map<U,Set<V>> subMap = new HashMap<U, Set<V>>();
			Set<V> set = new HashSet<V>();
			set.add(value);
			subMap.put(k2, set);
			map.put(k1,subMap);
		}
	}

	@Override
	public boolean contains(T k1, U k2) {
		return map.containsKey(k1) && map.get(k1).containsKey(k2);
	}

	@Override
	public List<Tuple<T, U>> listKeys() {
		List<Tuple<T, U>> list = new ArrayList<Tuple<T, U>>();
		for (Map.Entry<T, Map<U, Set<V>>> e : map.entrySet()) {
			T t = e.getKey();
			for (U u : e.getValue().keySet()) {
				list.add(new Tuple<T, U>(t,u));
			}
		}
		return list;
	}

	@Override
	public Iterator<Triple<T, U, Set<V>>> iterator() {
		List<Triple<T,U,Set<V>>> list = new ArrayList<Triple<T, U, Set<V>>>();
		for (Map.Entry<T, Map<U, Set<V>>> e1 : map.entrySet()) {
			T k1 = e1.getKey();
			for (Map.Entry<U, Set<V>> e2 : e1.getValue().entrySet()) {
				U k2 = e2.getKey();
				Set<V> value = e2.getValue();
				list.add(new Triple<T, U, Set<V>>(k1,k2,value));
			}
		}
		return list.iterator();
	}

	@Override
	public String toString() {
		return "TwoKeyMultiHashMap{" + map + '}';
	}
}
