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
import org.haedus.phonetic.Sequence;
import org.haedus.phonetic.SequenceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 3/7/2015
 */
public class Machine {

	public static final Machine EMPTY_MACHINE = new Machine();

	private static final transient Logger LOGGER = LoggerFactory.getLogger(StateMachine.class);

	private final SequenceFactory factory;

	private final String                        startStateId;
	private final Map<String, Map<Set, String>> acceptingStates;
	private final Map<String, Machine>          stateMachines;

	private Machine() {
		factory = SequenceFactory.getEmptyFactory();
		startStateId = null;
		acceptingStates = null;
		stateMachines = null;
	}

	// For use with NodeFactory only
	Machine(String id, String expressionParam, SequenceFactory factoryParam, ParseDirection direction) {
		factory = factoryParam;

		startStateId = null;
		acceptingStates = new HashMap<String, Map<Set, String>>();
		stateMachines = new HashMap<String, Machine>();

		List<String> strings = factory.getSegmentedString(expressionParam);
		List<Expression> expressions = parse(strings);
	}

	private static Expression updateBuffer(Collection<Expression> list, Expression buffer) {
		list.add(buffer);
		return new Expression();
	}

	private static List<Expression> parse(Collection<String> strings) {
		List<Expression> list = new ArrayList<Expression>();
		if (!strings.isEmpty()) {

			Expression buffer = new Expression();
			for (String symbol : strings) {
				if (symbol.equals("*") || symbol.equals("?") || symbol.equals("+")) {
					buffer.setMetacharacter(symbol);
					buffer = updateBuffer(list, buffer);
				} else if (symbol.equals("!")) {
					// first in an expression
					buffer = updateBuffer(list, buffer);
					buffer.setNegative(true);
				} else {
					if (!buffer.getExpression().isEmpty()) {
						buffer = updateBuffer(list, buffer);
					}
					buffer.setExpression(symbol);
				}
			}
			if (!buffer.getExpression().isEmpty()) {
				list.add(buffer);
			}
		}
		return list;
	}
	
	public boolean matches(int startIndex, Sequence target) {
		return false;
	}
}
