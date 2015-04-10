/**
 * ***************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe                                  *
 * *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 * http://www.apache.org/licenses/LICENSE-2.0                             *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 * ****************************************************************************
 */

package org.haedus.machines;

import org.haedus.enums.ParseDirection;
import org.haedus.phonetic.SequenceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 3/7/2015
 */
public class StandardMachine extends AbstractMachine {

	public static final StandardMachine EMPTY_MACHINE = new StandardMachine();

	private static final transient Logger LOGGER = LoggerFactory.getLogger(StateMachine.class);

	public static void main(String[] args) throws IOException {
		int size = 1000;

		int start = 9622 - 1;
		int end = 9631;
		int delta = end - start;

		File file = new File("quadrants.txt");
		Writer out = new BufferedWriter(new OutputStreamWriter(
			new FileOutputStream(file), "UTF8"));

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < 140; j++) {
				double random = Math.random();

				Double value = Math.ceil(delta * random);
				int integer = value.intValue() + start;

				out.write(integer);
			}
			out.write("\n");
		}
		out.flush();
		out.close();
	}

	private StandardMachine() {
		super("E", SequenceFactory.getEmptyFactory(), ParseDirection.FORWARD);
	}

	public StandardMachine(String id, String expressionParam, SequenceFactory factoryParam, ParseDirection direction) {

		super(id, factoryParam, direction);
		List<Expression> expressions = factory.getExpressions(expressionParam);
		process(expressions, direction);
	}

	private void process(List<Expression> expressions, ParseDirection direction) {
		nodes.add(startStateId);

		if (direction == ParseDirection.BACKWARD) { Collections.reverse(expressions); }

		int nodeId = 0;
		String previousNode = startStateId;

		for (Iterator<Expression> it = expressions.iterator(); it.hasNext(); ) {
			Expression expression = it.next();

			nodeId++;

			String expr = expression.getExpression();
			String meta = expression.getMetacharacter();

			String currentNode = machineId + ':' + nodeId;
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

	private static Expression updateBuffer(Collection<Expression> list, Expression buffer) {
		list.add(buffer);
		return new Expression();
	}
}
