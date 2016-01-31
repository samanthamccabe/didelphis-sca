/******************************************************************************
 * Copyright (c) 2016. Samantha Fiona McCabe                                  *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *     http://www.apache.org/licenses/LICENSE-2.0                             *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 ******************************************************************************/

package org.haedus.machines;

import org.haedus.phonetic.Sequence;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 1/28/2016
 */
public class Graph {
	private final Map<String, Map<Sequence, Set<String>>> map;

	public Graph() {
		map = new HashMap<String, Map<Sequence, Set<String>>>();
	}

	public Graph(Graph graph) {
		map = new HashMap<String, Map<Sequence, Set<String>>>(graph.map);
	}

	@Override
	public int hashCode() {
		return map.hashCode();
	}

	public Map<String, Map<Sequence, Set<String>>> getMap() {
		return map;
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public Set<String> getKeys() {
		return map.keySet();
	}

	public Map<Sequence, Set<String>> remove(String k1) {
		return map.remove(k1);
	}

	public void clear() {
		map.clear();
	}

	public Map<Sequence, Set<String>> get(String k1) {
		return map.get(k1);
	}

	public Set<String> get(String k1, Sequence k2) {
		return map.get(k1).get(k2);
	}

	public boolean contains(String k1) {
		return map.containsKey(k1);
	}

	public Set<Map.Entry<Sequence, Set<String>>> getEntries(String k1) {
		return map.get(k1).entrySet();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (null == obj) { return false; }
		if (getClass() != obj.getClass()) { return false; }
		Graph other = (Graph) obj;
		return map.equals(other.getMap());
	}

	private Set<Sequence> getKeys(String k1) {
		return map.get(k1).keySet();
	}

	public void put(String k1, Sequence k2, String value) {
		Set<String> set = new HashSet<String>();
		set.add(value);
		put(k1,k2,set);
	}

	public void put(String k1, Sequence k2, Set<String> values) {
		Map<Sequence, Set<String>> innerMap;
		if (map.containsKey(k1)) {
			innerMap = map.get(k1);
		} else {
			innerMap = new HashMap<Sequence, Set<String>>();
		}

		Set<String> set;
		if (innerMap.containsKey(k2)) {
			set = innerMap.get(k2);
		} else {
			set = new HashSet<String>();
		}
		set.addAll(values);
		innerMap.put(k2, set);
		map.put(k1, innerMap);
	}

	public boolean contains(String k1, Sequence k2) {
		return map.containsKey(k1) && map.get(k1).containsKey(k2);
	}

	@Override
	public String toString() {
		return "Graph{" +
			"map=" + map +
			'}';
	}
}