/******************************************************************************
 * Copyright (c) 2016. Samantha Fiona McCabe                                  *
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

import org.haedus.enums.FormatterMode;
import org.haedus.enums.ParseDirection;
import org.haedus.phonetic.FeatureModel;
import org.haedus.phonetic.SequenceFactory;
import org.haedus.phonetic.VariableStore;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashSet;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 1/31/2016
 */
public class NegativeStateMachineTest extends MachineTestBase {

	@BeforeClass
	public static void setFactory() {
		FACTORY = SequenceFactory.getEmptyFactory();
	}

	@Test
	public void testBasic01() {
		Machine machine = getMachine("!a");
		fail(machine, "a");
		fail(machine, "aa");

		test(machine, "b");
		test(machine, "c");
	}

	@Test
	public void testBasic02() {
		Machine machine = getMachine("!a?b#");
		fail(machine, "ab");
		fail(machine, "c");

		test(machine, "bb");
		test(machine, "b");
	}

	@Test
	public void testBasic03() {
		Machine machine = getMachine("!a*b#");
		fail(machine, "ab");
		fail(machine, "aab");
		fail(machine, "aaab");
		fail(machine, "c");

		fail(machine, "bab");
		fail(machine, "bbab");

		test(machine, "b");
		test(machine, "bb");
		test(machine, "bbb");
	}

	@Test
	public void testGroup01() {
		Machine machine = getMachine("!(ab)");
		fail(machine, "ab");

		test(machine, "aa");
		test(machine, "ac");
		test(machine, "aab");

		// These are too short
		fail(machine, "a");
		fail(machine, "b");
		fail(machine, "c");
	}

	@Test
	public void testGroup02() {
		Machine machine = getMachine("!(ab)");
		fail(machine, "ab");

		test(machine, "aa");
		test(machine, "ac");
		test(machine, "aab");

		// These are too short
		fail(machine, "a");
		fail(machine, "b");
		fail(machine, "c");
	}

	@Test
	public void testGroup03() {
		Machine machine = getMachine("!(ab)*xy#");
		fail(machine, "abxy");
		fail(machine, "ababxy");

		test(machine, "xy");
		test(machine, "xyxy");
		test(machine, "xyxyxy");

		// These are too short
		fail(machine, "aab");
		fail(machine, "bab");
		fail(machine, "cab");
	}

	@Test
	public void testeGroup04() {
		Machine machine = getMachine("!(ab)+xy#");
		fail(machine, "abxy");
		fail(machine, "ababxy");
		fail(machine, "abababxy");

		fail(machine, "aaabxy");
		fail(machine, "acabxy");

		test(machine, "aaxy");
		test(machine, "acxy");

		// These are too short
		fail(machine, "aab");
		fail(machine, "bab");
		fail(machine, "cab");
	}

	@Test
	public void testSet01() {
		Machine machine = getMachine("!{a b c}");
		fail(machine, "a");
		fail(machine, "b");
		fail(machine, "c");

		test(machine, "x");
		test(machine, "y");
		test(machine, "z");
	}

	@Test
	public void testSet02() {
		Machine machine = getMachine("#!{a b c}#");
		fail(machine, "#a");
		fail(machine, "#b");
		fail(machine, "#c");

		test(machine, "#x");
		test(machine, "#y");
		test(machine, "#z");
	}

	@Test
	public void testSet03() {
		Machine machine = getMachine("!{a b c}+#");
		fail(machine, "a");
		fail(machine, "b");
		fail(machine, "c");

		// Length 2 - exhaustive
		fail(machine, "aa");
		fail(machine, "ba");
		fail(machine, "ca");

		fail(machine, "ab");
		fail(machine, "bb");
		fail(machine, "cb");

		fail(machine, "ac");
		fail(machine, "bc");
		fail(machine, "cc");

		// Length 3 - partial
		fail(machine, "acb");
		fail(machine, "bac");
		fail(machine, "cba");
		fail(machine, "acc");
		fail(machine, "baa");
		fail(machine, "cbb");
		fail(machine, "aca");
		fail(machine, "bab");
		fail(machine, "cbc");

		// Length 3 - tail
		// This is important as a distinction between:
		//     A) !{a b c}+
		//     B) !{a b c}+#
		// in that (A) will accept these but (B) will not:
		fail(machine, "xa");
		fail(machine, "yb");
		fail(machine, "zc");

		// Pass
		test(machine, "x");
		test(machine, "y");
		test(machine, "z");

		test(machine, "xxy");
		test(machine, "yyz");
		test(machine, "zzx");
	}

	@Test
	public void testVariables01() {

		VariableStore store = new VariableStore();
		store.add("C = p t k");

		String expression = "!C";

		StateMachine machine = getStateMachine(store, expression);

		test(machine, "a");
		test(machine, "b");
		test(machine, "c");

		fail(machine, "p");
		fail(machine, "t");
		fail(machine, "k");
	}

	@Test
	public void testVariables02() {

		VariableStore store = new VariableStore();
		store.add("C = ph th kh");

		String expression = "!C";

		StateMachine machine = getStateMachine(store, expression);

		test(machine, "pp");
		test(machine, "tt");
		test(machine, "kk");

		// These are too short
		fail(machine, "p");
		fail(machine, "t");
		fail(machine, "k");

		fail(machine, "ph");
		fail(machine, "th");
		fail(machine, "kh");
	}

	@Test
	public void testVariables03() {

		VariableStore store = new VariableStore();
		store.add("C = ph th kh kwh");

		String expression = "!C";

		StateMachine machine = getStateMachine(store, expression);

		test(machine, "pp");
		test(machine, "tt");
		test(machine, "kk");
		test(machine, "kw");
		test(machine, "kkw");

		// These are too short
		fail(machine, "p");
		fail(machine, "t");
		fail(machine, "k");

		fail(machine, "ph");
		fail(machine, "th");
		fail(machine, "kh");

		fail(machine, "kwh");
	}

	private static StateMachine getStateMachine(VariableStore store, String expression) {
		SequenceFactory factory = new SequenceFactory(
			FeatureModel.EMPTY_MODEL, store, new HashSet<String>(), FormatterMode.NONE);
		return StateMachine.create("M0", expression, factory, ParseDirection.FORWARD);
	}
}
