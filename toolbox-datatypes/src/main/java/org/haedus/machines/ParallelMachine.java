/******************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe                                  *
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

import org.haedus.enums.ParseDirection;
import org.haedus.phonetic.SequenceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 4/6/2015
 */
public class ParallelMachine extends AbstractMachine {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(ParallelMachine.class);


	public ParallelMachine(String id, String expressionParam, SequenceFactory factoryParam, ParseDirection direction) {

		super(id, factoryParam, direction);
//		List<Expression> expressions = factory.getExpressions(expressionParam);

		int i = 1;
		for (String subExpression : parseSubExpressions(expressionParam)) {
			List<Expression> expressions = factory.getExpressions(subExpression);
			process(i, expressions, direction);
			i++;
		}

		LOGGER.info("");
//		process(id+"-S", expressions, direction);
	}

	private void process(int branchNumber, List<Expression> expressions, ParseDirection direction) {
		nodes.add(startStateId);

		if (direction == ParseDirection.BACKWARD) { Collections.reverse(expressions); }

		int nodeId = 0;
		String previousNode = startStateId;

		for (Iterator<Expression> it = expressions.iterator(); it.hasNext(); ) {
			Expression expression = it.next();

			nodeId++;

			String expr = expression.getExpression();
			String meta = expression.getMetacharacter();

			String currentNode = machineId + ':' + branchNumber + '-' + nodeId;
			if (!it.hasNext()) {
				acceptingStates.add(currentNode);
			}
			nodes.add(currentNode);

			if (expr.startsWith("(")) {
				String substring = expr.substring(1, expr.length() - 1);
				StandardMachine machine = new StandardMachine(currentNode, substring, factory, direction);
				machinesMap.put(currentNode, machine);

				String nextNode = currentNode + 'X';
				nodes.add(nextNode);
				previousNode = constructRecursiveNode(nextNode, previousNode, currentNode, meta);
			} else if (expr.startsWith("{")) {
				String substring = expr.substring(1, expr.length() - 1);
				ParallelMachine machine = new ParallelMachine(currentNode, substring, factory, direction);
				machinesMap.put(currentNode, machine);

				String nextNode = currentNode + 'X';
				nodes.add(nextNode);
				previousNode = constructRecursiveNode(nextNode, previousNode, currentNode, meta);
			} else {
				previousNode = constructTerminalNode(previousNode, currentNode, expr, meta);
			}
		}
	}

	private static Collection<String> parseSubExpressions(String expressionParam) {
		Collection<String> subExpressions = new ArrayList<String>();

		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < expressionParam.length(); i++) {
			char c = expressionParam.charAt(i);
			/*  */
			if (c == '{') {
				int index = getIndex(expressionParam, '{', '}', i);
				buffer.append(expressionParam.substring(i, index));
				i = index - 1;
			} else if (c == '(') {
				int index = getIndex(expressionParam, '(', ')', i);
				buffer.append(expressionParam.substring(i, index));
				i = index - 1;
			} else if (c != ' ') {
				buffer.append(c);
			} else if (buffer.length() > 0) { // No isEmpty() call available
				subExpressions.add(buffer.toString());
				buffer = new StringBuilder();
			}
		}

		if (buffer.length() > 0) {
			subExpressions.add(buffer.toString());
		}
		return subExpressions;
	}

	private static int getIndex(CharSequence string, char left, char right, int startIndex) {
		int count = 1;
		int endIndex = -1;

		boolean matched = false;
		for (int i = startIndex + 1; i <= string.length() && !matched; i++) {
			char ch = string.charAt(i);
			if (ch == right && count == 1) {
				matched = true;
				endIndex = i;
			} else if (ch == right) {
				count++;
			} else if (ch == left) {
				count--;
			}
		}
		return endIndex;
	}
}
