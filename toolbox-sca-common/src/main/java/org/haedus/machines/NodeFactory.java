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

import org.haedus.datatypes.phonetic.Sequence;

/**
 * Created with IntelliJ IDEA.
 * User: Samantha Fiona Morrigan McCabe
 * Date: 9/10/13
 * Time: 7:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class NodeFactory {

	private static int indexCounter = 0;

	private static final TerminalNode EMPTYNODE = new TerminalNode(0);
    private static final TerminalNode DEADSTATE = new TerminalNode(Integer.MIN_VALUE);

	public static Node<Sequence> getNode() {
		return getNode(false);
	}

	public static Node<Sequence> getNode(boolean accepting) {
		setIndexCounter(getIndexCounter() + 1);
		return new TerminalNode(getIndexCounter(), accepting);
	}

    public static Node<Sequence> getDeadState() { return DEADSTATE; }

	public static Node<Sequence> getEmptyNode() {
		return EMPTYNODE;
	}

	public static int getIndexCounter() {
		return indexCounter;
	}

	public static void setIndexCounter(int indexCounter) {
		NodeFactory.indexCounter = indexCounter;
	}

}
