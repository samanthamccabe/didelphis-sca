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

import java.util.*;
import java.util.Map.Entry;

/**
 * Created with IntelliJ IDEA.
 * User: Samantha Fiona Morrigan McCabe
 * Date: 8/3/13
 * Time: 1:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class TerminalNode<T> implements Node<T> {

	private       boolean isAccepting;
	private final int     id;

	private final Map<T, Set<Node<T>>> arcs;

	protected TerminalNode(int i) {
		id = i;
		isAccepting = false;
		arcs = new HashMap<T, Set<Node<T>>>();
	}

	protected TerminalNode(int i, boolean accepting) {
		id = i;
		isAccepting = accepting;
		arcs = new HashMap<T, Set<Node<T>>>();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)                    return false;
		if (!(obj instanceof TerminalNode)) return false;

		TerminalNode other = (TerminalNode) obj;
		return id == other.getId() &&
		       arcs.equals(other.arcs) &&
		       isAccepting == other.isAccepting();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Node " + id + "\n");

		for (Entry<T, Set<Node<T>>> entry : arcs.entrySet()) {
			T key = entry.getKey();
			Set<Node<T>> value = entry.getValue();

			sb.append(key);
			sb.append(" > [ ");
			for (Node node : value) {
				sb.append(node.getId());
				sb.append(" ");
			}
			sb.append("]");
		}

		return sb.toString();
	}

	@Override
	public int hashCode() {
		int code = 11;

		code *= 31 + id;
		code *= 31 + ((isAccepting) ? 1 : 0);
		code *= 31 + arcs.hashCode();

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
	public void add(Node node) {
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
	public void setAccepting(boolean isAccepting) {
		this.isAccepting = isAccepting;
	}

	@Override
	public int getId() {
		return id;
	}
}
