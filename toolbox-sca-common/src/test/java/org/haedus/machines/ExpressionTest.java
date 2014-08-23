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

import org.haedus.datatypes.Segmenter;
import org.haedus.soundchange.exceptions.RuleFormatException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: Samantha Fiona Morrigan McCabe
 * Date: 9/1/13
 * Time: 9:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExpressionTest {
	private static final transient Logger LOGGER = LoggerFactory.getLogger(ExpressionTest.class);

	@Test
	public void testConstructor1() {
		test("a");
	}

	@Test
	public void testConstructorStar() {
		test("a*");
	}

	@Test
	public void testConstructorPlus() {
		test("a+");
	}

	@Test
	public void testConstructorQuestion() {
		test("a?");
	}

	@Test
	public void testSubexpression1() {
		test("al(hamb)+ra?");
	}

	@Test
	public void testSubexpression2() {
		test("(al)+(ham?b)*ra?");
	}

	@Test
	public void testSubexpression3() {
		test("(al+(ham)?b)*ra?");
	}

	@Test
	public void testMultipleNested1() {
		test("a((ba)?ca)+a(cb)*");
	}

	@Test
	public void testMultipleNested2() {
		test("a(b(ca(ba)+)?)+");
	}

	@Test
	public void testGroups01() {
		test("(abcd)(ef)");
		test("(ab)(cdef)");
		test("(ab)(cd)(ef)");
		test("((ab)(cd))(ef)");
		test("(ab)((cd)(ef))");
	}

	@Test
	public void testGroups02() {
		test("(ab)*(cd)(ef)");
		test("(ab)(cd)*(ef)");
		test("(ab)(cd)(ef)*");
	}

	@Test
	public void testSets01() {
		test("{ab cd ef}","{(ab) (cd) (ef)}");
	}

	@Test
	public void testSets02() {
		test("{ab {cd ef} gh}","{(ab) {(cd) (ef)} (gh)}");
	}

	@Test
	public void testSets03() {
		test("{a b c}ds");
	}

	@Test
	public void testSets04() {
		test("{ab* (cd?)+ ((ae)*f)+}tr","{(ab*) (cd?)+ ((ae)*f)+}tr");
	}

	@Test
	public void testComplex01() {
		test("{r l}?{a e o}{i u}?{n m l r}?{pʰ tʰ kʰ}");
	}

	@Test
	public void testNegative01() {
		test("!{a b c}ds");
	}

	@Test
	public void testNegative02() {
		test("ab!cd");
	}

	@Test
	public void testVariables01() {
		Collection<String> variables = new ArrayList<String>();

		Collections.addAll(variables, "@X", "[I]", "[U]", "V");

		String expected  = "@X{([I]) ([U]) y w}V";

		List<String> segments = Segmenter.segment(expected);
		Expression expression = new Expression(segments);
		String s = removeParentheses(expression);
		assertEquals(expected, s);
	}

	private String removeParentheses(Expression expression) {
		return expression.toString().replaceAll("^\\((.*)\\)$","$1");
	}

	private void test(String string) {
		test(string, string);
	}

	private void test(String original, String expected) {
		List<String> segments = Segmenter.segment(original);
		Expression expression = new Expression(segments);
		String s = removeParentheses(expression);
		assertEquals(expected, s);
	}

}
