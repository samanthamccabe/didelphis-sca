package org.haedus.machines;

import org.haedus.datatypes.phonetic.Segment;
import org.haedus.datatypes.phonetic.Sequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: goats
 * Date: 8/3/13
 * Time: 1:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class Node {

	private boolean isAccepting;

	private final int id;

	private final HashMap<Sequence, List<Node>> arcs;

	protected Node(int i) {
		id = i;
		isAccepting = false;
		arcs = new HashMap<Sequence, List<Node>>();
	}

	protected Node(int i, boolean accepting) {
		id = i;
		isAccepting = accepting;
		arcs = new HashMap<Sequence, List<Node>>();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj.getClass().equals(getClass())) return false;

		Node other = (Node) obj;
		
		return other.id == id && arcs.equals(other.arcs);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Node " + id + "\n");
		
		for (Entry<Sequence, List<Node>> entry : arcs.entrySet()) {
			Sequence   key   = entry.getKey();
			List<Node> value = entry.getValue();
			
			sb.append(key);
			sb.append(" > [ ");
			for (Node n : value) {
				sb.append(n.id);
				sb.append(" ");
			}
			sb.append("]");
		}
		
		return sb.toString();
	}

	@Override
	public int hashCode() {
		int code = 65402751;
		int moss = id * 5 - arcs.keySet().hashCode();
		return code * moss;
	}

	public boolean isEmpty() {
		return arcs.isEmpty();
	}

	public void add(Node node) {
		add(new Sequence(), node);
	}

	public void add(Segment segment, Node node) {
		Sequence sequence = new Sequence(segment);

		add(sequence, node);
	}

	public void add(Sequence sequence, Node node) {
		addPositive(sequence, node);
	}

	public void addPositive(Sequence sequence, Node node) {
		List<Node> someNodes;
		if (arcs.containsKey(sequence)) {
			someNodes = arcs.get(sequence);
			if (!someNodes.contains(node)) {
				someNodes.add(node);
			}
		} else {
			someNodes = new ArrayList<Node>();
			someNodes.add(node);
		}
		arcs.put(sequence, someNodes);
	}

	public List<Node> getNodes(Sequence s) {
		return arcs.get(s);
	}


	public Set<Sequence> getKeys() {
		return arcs.keySet();
	}

	public boolean isAccepting() {
		return isAccepting;
	}

	public void setAccepting(boolean isAccepting) {
		this.isAccepting = isAccepting;
	}

	public boolean isDead() {
		return id == Integer.MIN_VALUE;
	}

	public int getId() {
		return id;
	}

	public static String getGml(Node start) {

		Set<Node> nodes = new HashSet<Node>();

		nodes.add(start);


		recurseNodes(start, nodes);


		StringBuilder sb = new StringBuilder();

		sb.append("graph [\n");

		for (Node node : nodes) {
			sb.append("\tnode [\n");
			sb.append("\t\tid ");
			sb.append(node.getId());
			sb.append("\n");
			sb.append("\t\tlabel \"");
			sb.append(node.getId());
			sb.append("\"\n");
			sb.append("\t]\n");
		}

		for (Node source : nodes) {
			for (Sequence sequence : source.getKeys()) {
				for (Node target : source.getNodes(sequence)) {

					sb.append("\tedge [\n");
					sb.append("\t\t source ");
					sb.append(source.getId());
					sb.append("\n");
					sb.append("\t\t target ");
					sb.append(target.getId());
					sb.append("\n");
					sb.append("\t\t label \"");
					sb.append(sequence);
					sb.append("\"\n");
					sb.append("\t]\n	");
				}
			}
		}

		sb.append("\n]");


		return sb.toString();
	}

	public static void recurseNodes(Node start, Set<Node> nodes) {
		for (Sequence segments : start.getKeys()) {
			for (Node node : start.getNodes(segments)) {
				if (!nodes.contains(node)) {
					nodes.add(node);
					recurseNodes(start,nodes);
				}
			}
		}
	}
}
