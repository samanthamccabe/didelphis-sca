package org.haedusfc.soundchange;

import org.haedusfc.datatypes.phonetic.Segment;
import org.haedusfc.datatypes.phonetic.Sequence;
import org.haedusfc.machines.StateMachine;
import org.haedusfc.soundchange.exceptions.RuleFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: goats
 * Date: 4/28/13
 * Time: 2:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class Condition {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(Condition.class);

	private final String        conditionText;
//	private final Expression    preCondition;
//	private final Expression    postCondition;
	private final StateMachine  preCondition;
	private final StateMachine  postCondition;
	private final VariableStore variableStore;
//	private final Deque<State>  backStack;     // Needed to store backtrack states when matching

	public Condition() {
		conditionText = "";
		preCondition  = new StateMachine();
		postCondition = new StateMachine();
		variableStore = new VariableStore();
//		backStack     = new LinkedList<State>();
	}

	public Condition(String condition) throws RuleFormatException {
		this(condition, new VariableStore());
	}

	public Condition(String condition, VariableStore variables) throws RuleFormatException {
		condition = cleanup(condition);
		conditionText = condition;
		variableStore = variables;
//		backStack     = new LinkedList<State>();

		if (condition.contains("_")) {
			String[] conditions = condition.split("_");
//			Set<String> keys = variableStore.getKeys();
			if (conditions.length == 1) {
				preCondition  = new StateMachine(conditions[0], variableStore, false);
				postCondition = new StateMachine();
			} else if (conditions.length == 2) {
				preCondition  = new StateMachine(conditions[0], variableStore, false);
				postCondition = new StateMachine(conditions[1], variableStore, true);
			} else if (conditions.length == 0) {
				postCondition = new StateMachine();
				preCondition  = new StateMachine();
			} else {
				throw new RuleFormatException("Malformed Condition, multiple _ characters");
			}
		} else {
			throw new RuleFormatException("Malformed Condition, no _ character");
		}
	}

	@Override
	public String toString() {
		return conditionText;
	}

	private String cleanup(String s) {
		s = s.replaceAll("\\s+", " ");
		s = s.replaceAll("([\\[\\{\\(]) ", "$1");
		s = s.replaceAll(" ([\\]\\}\\)])", "$1");
		return s;
	}

	public boolean isMatch(Sequence word, int index) {
		return isMatch(word, index, index + 1);
	}

	/**
	 * Checks if this condition is applicable to the Sequence at the provided index
	 * @param word       the Sequence to check
	 * @param startIndex the first index of the targeted Sequence; cannot be negative
	 * @param endIndex   the last index of the targeted Sequence (exclusive); cannot be negative
	 * @return Returns true if the condition isMatch
	 */
	public boolean isMatch(Sequence word, int startIndex, int endIndex) {
		boolean preconditionMatch = false;
		boolean postconditionMatch = false;

		if (endIndex <= word.size() && startIndex < endIndex) {
			Sequence head = word.getSubsequence(0, startIndex);
			Sequence tail = word.getSubsequence(endIndex);

			head.addFirst(new Segment("#"));
			tail.add(new Segment("#"));

//			preconditionMatch  = matches(preCondition,  head.getReverseSequence());
//			postconditionMatch = matches(postCondition, tail);
			preconditionMatch = preCondition.matches(head.getReverseSequence());
			postconditionMatch = postCondition.matches(tail);
		}
		return preconditionMatch && postconditionMatch;
	}
	/*
		private boolean matches(Expression expression, Sequence sequence) {
			LOGGER.trace("Checking if {} matches {}", sequence.toStringClean(), expression);
			boolean isMatch = getNextState(expression, sequence, 0, 0).getIndex() >= 0;
			backStack.clear();
			return isMatch;
		}


		private State getNextState(Expression expression, Sequence sequence, int index, int timesVisited) {

			Expression nextExpression   = expression;
			int        nextIndex        = index; // default or fallback
			int        nextTimesVisited = timesVisited;

			if (nextIndex >= sequence.size()) {
				nextIndex = Integer.MIN_VALUE;
			}

			if (nextIndex >= 0) {

				if (nextExpression.isOptional() && timesVisited == 0) {
					State state = new State(nextExpression, nextIndex, 1);
					LOGGER.trace("PUSH {}", state);
					backStack.push(state);
				} else {

					if (!nextExpression.isTerminal()) {
						LOGGER.trace("{} Not Terminal", nextExpression);

						if (nextExpression.isParallel()) {
							// TODO: do we need to match more than one?
							State nextState  = getNextState(nextExpression.getFirstChild(), sequence, nextIndex, 0);
							Expression child = nextState.getExpression();
							int testIndex    = nextState.getIndex();

							while (child.hasSibling() && testIndex < 0) {
								nextState = getNextState(child.getNextSibling(), sequence, nextIndex, 0);
								child     = nextState.getExpression();
								testIndex = nextState.getIndex();
							}

							nextIndex = testIndex;
	//						if (nextIndex >= 0) {
	//							nextTimesVisited = nextState.getTimesVisited();
	//						}

						} else if (nextExpression.hasSubexpressions()) {
							State nextState  = getNextState(nextExpression.getFirstChild(), sequence, nextIndex, 0);
							Expression child = nextState.getExpression();
							nextIndex        = nextState.getIndex();
							nextTimesVisited = nextState.getTimesVisited();

							while (child.hasSibling() && nextIndex >= 0) {
								nextState  = getNextState(child.getNextSibling(), sequence, nextIndex, 0);
								LOGGER.info("Advancing to {}",nextState);
								child            = nextState.getExpression();
								nextIndex        = nextState.getIndex();
								nextTimesVisited = nextState.getTimesVisited();
							}

							if (child.hasSubexpressions()) {
								nextExpression = child;
							}
						}

						if (nextIndex >= 0) {
							pushIfRepeatable(nextExpression, nextIndex, nextTimesVisited);
						}

					} else {
						Sequence subsequence = sequence.getSubsequence(nextIndex);
						Segment  segment     = expression.getSegment();

						if (subsequence.startsWith(segment)) {
							nextIndex++;
							LOGGER.info("Matched literal {} at {}", segment, nextIndex);
	//						pushIfRepeatable(nextExpression, nextIndex, nextTimesVisited);
						} else {
							// FAIL
							if (!backStack.isEmpty()) {

								State state = backStack.pop();
								LOGGER.info("failed - pop {}", state);
								Expression altExpression = state.getExpression();
								int altIndex        = state.getIndex();
								int altTimesVisited = state.getTimesVisited();

								State altState;
								if (altExpression.isOptional() && altTimesVisited <= 1) {
									altState = getNextState(altExpression, sequence, altIndex, altTimesVisited);
								} else {
									altState = getNextState(altExpression, sequence, nextIndex, altTimesVisited);
								}
								nextExpression   = altState.getExpression();
								nextIndex        = altState.getIndex();
								nextTimesVisited = altState.getTimesVisited();
							} else {
								if (nextIndex == 0) {
									nextIndex = Integer.MIN_VALUE;
								} else {
									nextIndex *= -1;
								}
							}
						}
					}
				}
			} else {
				LOGGER.error("Index overran sequence length!");
			}

			State state = new State(nextExpression, nextIndex, nextTimesVisited);
			LOGGER.info("returning {}",state);
			return state;
		}

	private void pushIfRepeatable(Expression nextExpression, int nextIndex, int nextTimesVisited) {
		if (nextExpression.isRepeatable()) {
			State state = new State(nextExpression, nextIndex, nextTimesVisited + 1);
			LOGGER.trace("PUSH {}", state);

			if (backStack.isEmpty() || !backStack.peekLast().equals(state)) {
				backStack.push(state);
			}
		}
	}
	*/

	public boolean isEmpty() {
		return preCondition.isEmpty() && postCondition.isEmpty();
	}

	StateMachine getPreCondition() {
		return preCondition;
	}

	StateMachine getPostCondition() {
		return postCondition;
	}

//	private class State {
//		private final Expression expression;
//		private final int        index;
//		private final int        timesVisited;
//
//		private State(Expression exp, int i, int v) {
//			expression    = exp;
//			index         = i;
//			timesVisited  = v;
//		}
//
//		private Expression getExpression() {
//			return expression;
//		}
//
//		private int getIndex() {
//			return index;
//		}
//
//		private int getTimesVisited() {
//			return timesVisited;
//		}
//
//		public String toString() {
//			return "<" + expression + ", " + index + ", " + timesVisited + ">";
//		}
//
//		public boolean equals(Object object) {
//			if (object == null) return false;
//			if (!object.getClass().equals(getClass())) return false;
//
//			State state = (State) object;
//
//			return expression.equals(state.getExpression()) &&
//					index == state.getIndex() &&
//					timesVisited == state.getTimesVisited();
//		}
//	}
}




