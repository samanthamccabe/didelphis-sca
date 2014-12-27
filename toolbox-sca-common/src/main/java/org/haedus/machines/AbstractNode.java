package org.haedus.machines;

import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.SequenceFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by samantha on 12/24/14.
 */
public abstract class AbstractNode implements Node<Sequence> {

	protected final SequenceFactory factory;
	private final   String          id;

	private final Map<Sequence, Set<Node<Sequence>>> arcs;


	protected AbstractNode(String idParam, SequenceFactory factoryParam) {
		factory = factoryParam;
		arcs = new HashMap<Sequence, Set<Node<Sequence>>>();
		id = idParam;
	}

	@Override
	public String getId() {
		return id;
	}


	@Override
	public void add(Sequence arcValue, Node<Sequence> node) {
		Set<Node<Sequence>> someNodes;
		if (arcs.containsKey(arcValue)) {
			someNodes = arcs.get(arcValue);
			if (!someNodes.contains(node)) {
				someNodes.add(node);
			}
		} else {
			someNodes = new HashSet<Node<Sequence>>();
			someNodes.add(node);
		}
		arcs.put(arcValue, someNodes);
	}


	@Override
	public boolean isEmpty() {
		return arcs.isEmpty();
	}

	@Override
	public void add(Node<Sequence> node) {
		add(null, node);
	}

	@Override
	public boolean hasArc(Sequence arcValue) {
		return arcs.containsKey(arcValue);
	}

	@Override
	public Collection<Node<Sequence>> getNodes(Sequence arcValue) {
		return arcs.get(arcValue);
	}

	@Override
	public Collection<Sequence> getKeys() {
		return arcs.keySet();
	}
}
