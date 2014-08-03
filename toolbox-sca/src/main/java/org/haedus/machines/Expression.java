package org.haedus.machines;

import org.haedus.datatypes.phonetic.Segment;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: goats
 * Date: 9/1/13
 * Time: 9:34 PM
 * Expression creates and stores a compact representation of a regular expression string
 * and is used as a preprocessor for the creation of state-machines for regex matching
 */
public class Expression {

	private final Segment segment;

	private boolean isNegative;
	private boolean isParallel;

	private final boolean isOptional;
	private final boolean isRepeatable;

	private final LinkedList<Expression> subexpressions;

//	private Expression rightSibling;

	public Expression() {
		subexpressions = new LinkedList<Expression>();
//		rightSibling = null;

		segment = new Segment();

		isNegative = false;
		isParallel = false;
		isRepeatable = false;
		isOptional = false;
	}

	private Expression(Segment terminal, Expression sibling, boolean repeatable, boolean optional) {
		subexpressions = new LinkedList<Expression>();
//		rightSibling = sibling;

		segment = terminal;

		isNegative = false;
		isParallel = false;
		isRepeatable = repeatable;
		isOptional = optional;
	}

	public Expression(List<String> list, Expression sibling, boolean repeatable, boolean optional) {
		subexpressions = parse(new LinkedList<String>(list));
//		rightSibling = sibling;

		segment = new Segment();

		isNegative = false;
		isParallel = false;
		isRepeatable = repeatable;
		isOptional = optional;
	}

	public Expression(List<String> list) {
		this(list, null);
	}

	public Expression(List<String> list, Expression sibling) {
		this(list, sibling, false, false);
	}

	private LinkedList<Expression> parse(LinkedList<String> list) {
		LinkedList<Expression> expressionList = new LinkedList<Expression>();

		boolean repeatable = false;
		boolean optional = false;

		// Backwards traversal
		for (int i = list.size() - 1; i >= 0; i--) {

			String currentString = list.get(i);

			if ("+*?".contains(currentString)) {

				if (currentString.equals("?")) {
					optional = true;
				} else if (currentString.equals("*")) {
					optional = true;
					repeatable = true;
				} else {
					repeatable = true;
				}

			} else {
				if (")".equals(currentString)) {
					int index = getIndex(list, "(", ")", i);
					List<String> subList = list.subList(index + 1, i);
					Expression sibling = (expressionList.isEmpty()) ? null : expressionList.getFirst();
					expressionList.addFirst(new Expression(subList, sibling, repeatable, optional));
					i = index;

				} else if ("}".equals(currentString)) {
					int index = getIndex(list, "{", "}", i);
					List<String> subList = list.subList(index + 1, i);

					Expression sibling = (expressionList.isEmpty()) ? null : expressionList.getFirst();
					Expression bracketed;
					if (repeatable || optional) {
						Expression meta = new Expression(new Segment(), sibling, repeatable, optional);
						bracketed = new Expression(new Segment(), null, false, false);
						meta.add(bracketed);
						expressionList.addFirst(meta);
					} else {
						bracketed = new Expression(new Segment(), sibling, false, false);
						expressionList.addFirst(bracketed);
					}
					bracketed.setParallel(true);

					// Parses the parallel branches of the OR
					Expression lastSibling = null;
					for (List<String> l : getLists(subList)) {

						Expression ex = new Expression(l,lastSibling);

						List<Expression> subExpressions = ex.getSubExpressions();

						// Avoids redundant nodes
						if (subExpressions.size() == 1) {
							Expression expression = subExpressions.get(0);
//							expression.setRightSibling(lastSibling);
							bracketed.add(expression);
							lastSibling = expression;
						} else {
							bracketed.add(ex);
							lastSibling = ex;
						}
					}
					i = index;

				} else {
					Expression sibling = (expressionList.isEmpty()) ? null : expressionList.getFirst();
					Expression terminal;

					if (repeatable || optional) {
						Expression meta = new Expression(new Segment(), sibling, repeatable, optional);

						terminal = new Expression(new Segment(currentString), null, false, false);
						meta.add(terminal);
						expressionList.addFirst(meta);
					} else {
						terminal = new Expression(new Segment(currentString), sibling, false, false);
						expressionList.addFirst(terminal);
					}
				}
				// Reset
				repeatable = false;
				optional = false;
			}

			if (i > 0 && list.get(i - 1).equals("!")) {
				Expression first = expressionList.getFirst();
				first.setNegative(true);
				i--;
			}
		}
		return expressionList;
	}

	private Iterable<List<String>> getLists(List<String> list) {
		String space = " |";
		int spaceIndex = list.size();

		Collection<List<String>> lists = new LinkedList<List<String>>();

		for (int i = list.size() - 1; i >= 0; i--) {
			String currentString = list.get(i);
			if (space.contains(currentString)) {
				LinkedList<String> subList = new LinkedList<String>(list.subList(i + 1, spaceIndex));

				boolean hasPairBraces = (subList.contains("{") == subList.contains("}"));
				boolean hasPairParens = (subList.contains("(") == subList.contains(")"));

				if (hasPairBraces && hasPairParens) {
					lists.add(subList);
					spaceIndex = i;
				}
			}
		}
		lists.add(list.subList(0, spaceIndex));
		return lists;
	}

	/**
	 * Finds the index of the bracket which matches the one at the provided index
	 *
	 * @param segments the Strings we wish to check for matching brackets
	 * @param left     the left-bracket symbol
	 * @param right    the right-bracket symbol
	 * @param i        location of the current right-bracket
	 * @return the index where the matching left-bracket is found
	 */
	private int getIndex(LinkedList<String> segments, String left, String right, int i) {
		int count = 0;
		int index = i;

		boolean notMatched = true;

		while (notMatched && (index >= 0) && (index <= segments.size())) {
			String current = segments.get(index);
			if (current.equals(left) && count == 1) {
				notMatched = false;
			} else if (current.equals(right)) {
				count++;
			} else if (current.equals(left)) {
				count--;
			}
			if (notMatched) {
				index--;
			}
		}
		if (notMatched) {
			index = -1;
		}
		return index;
	}

	public boolean isEmpty() {
		return (segment.isEmpty() && isTerminal());
	}

	private void add(Expression ex) {
		subexpressions.addFirst(ex);
	}

	public boolean isNegative() {
		return isNegative;
	}

	public void setNegative(boolean state) {
		isNegative = state;
	}

	public boolean isParallel() {
		return isParallel;
	}

	public void setParallel(boolean parallel) {
		isParallel = parallel;
	}

	public boolean isTerminal() {
		return subexpressions.isEmpty() && !segment.isEmpty();
	}

	public Segment getSegment() {
		return segment;
	}

	public boolean isRepeatable() {
		return isRepeatable;
	}

	public boolean isOptional() {
		return isOptional;
	}

	public boolean hasSubexpressions() {
		return !subexpressions.isEmpty();
	}

	public Expression getFirstChild() {
		return subexpressions.getFirst();
	}

//	public boolean hasSibling() {
//		return rightSibling != null;
//	}

//	public Expression getNextSibling() {
//		return rightSibling;
//	}

	private List<Expression> getSubExpressions() {
		return subexpressions;
	}

	public List<Expression> getSubExpressions(boolean isForward) {
		List<Expression> list = getSubExpressions();
		if (!isForward) {
			Collections.reverse(list);
		}
		return list;
	}

	public int subexpressionSize() {
		return subexpressions.size();
	}

//	private void setRightSibling(Expression sibling)
//	{
//		rightSibling = sibling;
//	}

	public int getSize() {
		return subexpressions.size();
	}

	public Expression getSubExpression(int i) {
		return subexpressions.get(i);
	}

	public String toString() {
		String string = "";
		if (isTerminal()) {
			string = segment.toString();
		} else {
			for (Expression exp : subexpressions) {
				String str = exp.toString();

				if (isParallel) {
					string = string.concat(str + " ");
				} else {
					string = string.concat(str);
				}
			}

			if (isParallel) {
				string = "{" + string.trim() + "}";
			} else if (subexpressions.size() > 1) {
				string = "(" + string.trim() + ")";
			}
		}

		if (isNegative) {
			string = "!" + string;
		}

		return string.trim() + getSymbol();
	}

	public String getSymbol() {
		String symbol = "";

		if (isRepeatable && isOptional) {
			symbol = "*";
		} else if (isRepeatable) {
			symbol = "+";
		} else if (isOptional) {
			symbol = "?";
		}
		return symbol;
	}
}
