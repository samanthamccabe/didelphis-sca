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

package org.haedus.machines;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Samantha F M McCabe on 12/21/14.
 */
public class TerminalNode<T> implements Node<T> {

	private       boolean isAccepting;
	private final String  id;

	private final Map<T, Set<Node<T>>> arcs;

	protected TerminalNode(String idParam) {
		id = idParam;
		isAccepting = false;
		arcs = new HashMap<T, Set<Node<T>>>();
	}

	protected TerminalNode(String idParam, boolean accepting) {
		id = idParam;
		isAccepting = accepting;
		arcs = new HashMap<T, Set<Node<T>>>();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)                    return false;
		if (!(obj instanceof TerminalNode)) return false;

		TerminalNode other = (TerminalNode) obj;
		return id.equals(other.getId()) &&
		       arcs.equals(other.arcs) &&
		       isAccepting == other.isAccepting;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Node " + id + '\n');

		for (Map.Entry<T, Set<Node<T>>> entry : arcs.entrySet()) {
			T key = entry.getKey();
			Set<Node<T>> value = entry.getValue();

			sb.append(key);
			sb.append(" > [ ");
			for (Node node : value) {
				sb.append(node.getId());
				sb.append(' ');
			}
			sb.append(']');
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		int code = 11;

		code *= 31 + id.hashCode();
		code *= 31 + (isAccepting ? 1 : 0);

		for (Map.Entry<T, Set<Node<T>>> entry : arcs.entrySet()) {
			if (entry != null) {
				T key = entry.getKey();
				code *= (key != null ? key.hashCode() : 0) + entry.getValue().size();
			}
		}
		return code;
	}

	@Override
	public boolean isEmpty() {
		return arcs.isEmpty();
	}

	@Override
	public boolean matches(T target) {
		// Because the terminal node does not contain a state machine
		// it will trivially match any input
		return true;
	}

	@Override
	public void add(Node<T> node) {
		add(null, node);
	}

	@Override
	public void add(T sequence, Node<T> node) {
		Set<Node<T>> someNodes;
		if (arcs.containsKey(sequence)) {
			someNodes = arcs.get(sequence);
			if (!someNodes.contains(node)) {
				someNodes.add(node);
			}
		} else {
			someNodes = new HashSet<Node<T>>();
			someNodes.add(node);
		}
		arcs.put(sequence, someNodes);
	}

	@Override
	public boolean hasArc(T arcValue) {
		return false;
	}

	@Override
	public Set<Node<T>> getNodes(T s) {
		return arcs.get(s);
	}

	@Override
	public Set<T> getKeys() {
		return arcs.keySet();
	}

	@Override
	public boolean isAccepting() {
		return isAccepting;
	}

	@Override
	public void setAccepting(boolean acceptingParam) {
		isAccepting = acceptingParam;
	}

	@Override
	public String getId() {
		return String.valueOf(id);
	}
}
