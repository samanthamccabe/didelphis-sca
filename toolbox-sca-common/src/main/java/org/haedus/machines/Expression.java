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

import org.haedus.datatypes.ParseDirection;

import java.util.AbstractSequentialList;
import java.util.ArrayList;
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
	private List<String> expression    = new ArrayList<String>();
	private String       metacharacter = "";
	private boolean negative;

	@Override
	public String toString() {
		return (negative ? "!" : "") + expression + metacharacter;
	}

	public List<String> getExpression() {
		return expression;
	}


	public void setExpression(List<String> expParam) {
		expression = expParam;
	}

	public String getMetacharacter() {
		return metacharacter;
	}

	public void setMetacharacter(String metaParam) {
		metacharacter = metaParam;
	}

	public boolean isNegative() {
		return negative;
	}

	public void setNegative(boolean negParam) {
		negative = negParam;
	}
}
