package org.haedus.machines;

import org.haedus.datatypes.phonetic.FeatureModel;
import org.haedus.datatypes.Segmenter;
import org.haedus.datatypes.phonetic.Segment;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.VariableStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by goats on 11/10/13.
 */
public class StateMachine {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(StateMachine.class);

	private final Node          startNode;
	private final VariableStore variableStore;
	private final FeatureModel  featureModel;

	public StateMachine() {
		variableStore = new VariableStore();
		startNode     = new Node(0);
		featureModel  = new FeatureModel();
	}

	public StateMachine(String expression, VariableStore store, FeatureModel model, boolean isForward) {
		variableStore = store;
		featureModel  = model;
		
		startNode = parseCharSequence(expression, isForward);
	}

	public StateMachine(String expression, VariableStore store, boolean isForward) {
		this(expression, store, new FeatureModel(), isForward);
	}

    // Determines if the Sequence is accepted by this machine
	public boolean matches(Sequence sequence) {

		sequence.add(new Segment("#"));
		// At the beginning of the process, we are in the start-state
		// so we find out what arcs leave the node.
		List<MatchState> states = new LinkedList<MatchState>();
		List<MatchState> swap   = new LinkedList<MatchState>();

		states.add(new MatchState(0, startNode));

		// if the condition is empty, it will always match
		boolean match = startNode.isEmpty();
		while (!match && !states.isEmpty()) {
			for (MatchState state : states) {

				Node currentNode = state.getNode();
				int index = state.getIndex();

				if (!currentNode.isAccepting()) {
					updateSwapStates(sequence, swap, currentNode, index);
				} else {
					match = true;
					break;
				}
			}
			states = swap;
			swap = new LinkedList<MatchState>();
		}
		return match;
	}

	public boolean isEmpty() {
		return startNode.isEmpty();
	}

    private void updateSwapStates(Sequence testSequence, Collection<MatchState> swap, Node currentNode, int index) {
        Sequence tail = testSequence.getSubsequence(index);

        for (Sequence symbol : currentNode.getKeys()) {
            for (Node nextNode : currentNode.getNodes(symbol)) {
                if (variableStore.contains(symbol)) {
                    for (Sequence s : variableStore.get(symbol)) {
                        if (tail.startsWith(s)) {
                            swap.add(new MatchState(index + s.size(), nextNode));
                        }
                    }
                } else if (tail.startsWith(symbol)) {
                    swap.add(new MatchState(index + symbol.size(), nextNode));
                } else if (symbol.isEmpty()) {
                    swap.add(new MatchState(index, nextNode));
                }
            }
        }
    }

    //
	private Node parseCharSequence(String string, boolean isForward) {
		Collection<String> keys = new ArrayList<String>();
		
		keys.addAll(variableStore.getKeys());
		keys.addAll(featureModel.getSymbols());
		
		List<String> list = Segmenter.segment(string, keys);

		Node root;
		if (list.isEmpty()) {
			root = NodeFactory.getEmptyNode();
		} else {
			root = NodeFactory.getNode();
			Expression ex   = new Expression(list);
			Node       last = parse(ex, root, isForward);
			last.setAccepting(true);
			LOGGER.trace(ExpressionUtil.getGML(ex));
		}
		return root;
	}

    //
	private Node parse(Expression expression, Node root, boolean forward) {
		Node current = root;

		if (expression.isParallel()) {
            Node tail = NodeFactory.getNode();
            if (expression.isNegative()) {
                for (Expression ex : expression.getSubExpressions(forward)) {
                    Node next = getNode(forward, current, ex);
                    next.add(tail);
                }
            } else {

                for (Expression ex : expression.getSubExpressions(forward)) {
                    Node next = getNode(forward, current, ex);
                    next.add(tail);
                }
                current = tail;
            }
		} else {
			for (Expression ex : expression.getSubExpressions(forward)) {
				current = getNode(forward, current, ex);
			}
		}
		return current;
	}

	/**
	 * Processes an Expression into a state machine
	 * @param forward determines if the Expression will be parsed forwards (left-to-right)
	 * @param start the starting node of the machine
	 * @param ex the Expression we wish to process
	 * @return the last node in the machine.
	 */
	private Node getNode(boolean forward, Node start, Expression ex) {

		if (ex.isTerminal()) {
			Segment segment = ex.getSegment();

			if (ex.isOptional() && ex.isRepeatable()) {
				start.add(segment, start);
			} else {
				Node next = NodeFactory.getNode();
				start.add(segment, next);
				if (ex.isRepeatable())
					next.add(start);
				else if (ex.isOptional())
					start.add(next);
				start = next;
			}
		} else {
			// This provides the start and end states of our machine
			Node next = NodeFactory.getNode();
			Node last = parse(ex, next, forward);

			start.add(next);

			if (ex.isOptional() && ex.isRepeatable()) {
				last.add(start);
				Node alpha = NodeFactory.getNode();
				start.add(alpha);
				start = alpha;
			} else {
				if (ex.isRepeatable())
					last.add(start);
				else if (ex.isOptional())
					next.add(last);
				start = last;
			}
		}
		return start;
	}

	public String getGml() {
		return Node.getGml(startNode);
	}

	/**
	 * Utility class for matching strings
	 */
	private class MatchState {

		private final Integer index;
		private final Node    node;

		private MatchState(Integer i, Node n) {
			index = i;
			node  = n;
		}

		public int getIndex() {
			return index;
		}

		public Node getNode() {
			return node;
		}
		
		@Override
		public String toString() {
			return "<" + index + ", " + node.getId() + ">";
		}
		
		@Override
		public int hashCode() {
			return 13 * index.hashCode() * node.hashCode();
		}
	}
}
