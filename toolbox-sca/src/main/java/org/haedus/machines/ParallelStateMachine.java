/*******************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.haedus.machines;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.haedus.datatypes.ParseDirection;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.SequenceFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by samantha on 12/24/14.
 */
public class ParallelStateMachine extends AbstractNode {

	private final Set<Node<Sequence>> machines;

	@Deprecated
	ParallelStateMachine(String id, String expressionParam, SequenceFactory factoryParam, ParseDirection direction, boolean acceptingParam) {
		super(id, factoryParam, acceptingParam);
		machines = new HashSet<Node<Sequence>>();

		Collection<String> subExpressions = parseSubExpressions(expressionParam);
		for (String subExpression : subExpressions) {
			machines.add(NodeFactory.getStateMachine(subExpression, factoryParam, direction, false));
		}
	}

	private static Collection<String> parseSubExpressions(String expressionParam) {
		Collection<String> subExpressions = new ArrayList<String>();

		StringBuilder buffer = new StringBuilder();
		for (int i =0; i < expressionParam.length(); i++) {
			char c = expressionParam.charAt(i);
			/*  */ if (c == '{') {
				int index = getIndex(expressionParam, '{','}', i);
				buffer.append(expressionParam.substring(i, index));
				i = index - 1;
			} else if (c == '(') {
				int index = getIndex(expressionParam, '(',')', i);
				buffer.append(expressionParam.substring(i, index));
				i = index - 1;
			} else if (c != ' ') {
				buffer .append(c);
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

	@Override
	public boolean matches(int startIndex, Sequence target) {
		return !getMatchIndices(startIndex, target).isEmpty();
	}

	@Override
	public boolean containsStateMachine() {
		return !machines.isEmpty();
	}

	@Override
	public Collection<Integer> getMatchIndices(int startIndex, Sequence target) {
		Collection<Integer> indices = new HashSet<Integer>();
		for (Node<Sequence> machine : machines) {
			Collection<Integer> matchIndices = machine.getMatchIndices(startIndex, target);
			indices.addAll(matchIndices);
		}
		return indices;
	}

	@Override
	public String toString() {
		return getId() + machines;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		ParallelStateMachine rhs = (ParallelStateMachine) obj;
		return new EqualsBuilder()
				.appendSuper(super.equals(obj))
				.append(machines, rhs.machines)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.appendSuper(super.hashCode())
				.append(machines)
				.toHashCode();
	}
}
