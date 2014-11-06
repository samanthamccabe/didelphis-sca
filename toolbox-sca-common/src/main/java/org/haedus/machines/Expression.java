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

import org.haedus.datatypes.phonetic.Segment;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Samantha Fiona Morrigan McCabe
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

	public Expression() {
		subexpressions = new LinkedList<Expression>();

		segment = new Segment();

		isNegative   = false;
		isParallel   = false;
		isRepeatable = false;
		isOptional   = false;
	}

	private Expression(Segment terminal, boolean repeatable, boolean optional) {
		subexpressions = new LinkedList<Expression>();

		segment = terminal;

		isNegative = false;
		isParallel = false;
		isRepeatable = repeatable;
		isOptional = optional;
	}

	public Expression(List<String> list, boolean repeatable, boolean optional) {
		subexpressions = parse(new LinkedList<String>(list));

		segment    = new Segment();
		isNegative = false;
		isParallel = false;
		isRepeatable = repeatable;
		isOptional = optional;
	}

	public Expression(List<String> list) {
		this(list, false, false);
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

			} else if (!currentString.isEmpty()) {
				if (")".equals(currentString)) {
					int index = getIndex(list, "(", ")", i);
					List<String> subList = list.subList(index + 1, i);
					expressionList.addFirst(new Expression(subList, repeatable, optional));
					i = index;

				} else if ("}".equals(currentString)) {
					int index = getIndex(list, "{", "}", i);
					List<String> subList = list.subList(index + 1, i);

					Expression bracketed;
					if (repeatable || optional) {
						Expression meta = new Expression(new Segment(), repeatable, optional);
						bracketed = new Expression(new Segment(), false, false);
						meta.add(bracketed);
						expressionList.addFirst(meta);
					} else {
						bracketed = new Expression(new Segment(), false, false);
						expressionList.addFirst(bracketed);
					}
					bracketed.setParallel(true);

					// Parses the parallel branches of the OR
					for (List<String> l : getLists(subList)) {

						Expression ex = new Expression(l);

						List<Expression> subExpressions = ex.getSubExpressions();

						// Avoids redundant nodes
						if (subExpressions.size() == 1) {
							Expression expression = subExpressions.get(0);
							bracketed.add(expression);
						} else {
							bracketed.add(ex);
						}
					}
					i = index;

				} else {
					Expression terminal;
					if (repeatable || optional) {
						Expression meta = new Expression(new Segment(),  repeatable, optional);

						terminal = new Expression(new Segment(currentString), false, false);
						meta.add(terminal);
						expressionList.addFirst(meta);
					} else {
						terminal = new Expression(new Segment(currentString), false, false);
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
	private int getIndex(List<String> segments, String left, String right, int i) {
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
