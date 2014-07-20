package org.haedusfc.datatypes;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * User: goats
 * Date: 9/21/13
 * Time: 7:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class SegmenterTest
{
	@Test
	public void testEmpty() {
		List<String> expected = new ArrayList<String>();
		List<String> received = Segmenter.segment("");

		Collections.addAll(expected, "");

		assertEquals(expected, received);
	}

	@Test
	public void testBasic() {
		List<String> expected = new ArrayList<String>();
		List<String> received = Segmenter.segment("abcdef");

		Collections.addAll(expected, "a", "b", "c", "d", "e", "f");

		assertEquals(expected, received);
	}

	@Test
	public void testModifierLetters() {
		List<String> expected = new ArrayList<String>();
		List<String> received = Segmenter.segment("pˠtˡtˢtˣpˤtˀpˁkʰgʱdʲtʳtʴtʵgʶkʷnʸ");

		Collections.addAll(expected, "pˠ","tˡ","tˢ","tˣ","pˤ","tˀ","pˁ","kʰ","gʱ","dʲ","tʳ","tʴ","tʵ","gʶ","kʷ","nʸ");

		assertEquals(expected, received);
	}

	@Test
	public void testSubscripts() {
		List<String> expected = new ArrayList<String>();
		List<String> received = Segmenter.segment("h₁óh₁es-");

		Collections.addAll(expected, "h₁", "ó", "h₁", "e", "s", "-");
		assertEquals(expected, received);
	}

//	@Test
//	public void testCombiningMarks() {
//		List<String> expected = new ArrayList<String>();
//		List<String> received = Segmenter.segment("o̜e̝r̟m̤ṿz̥t̪p̫wg̑a̐ủe͚u͛n͇x͈");
//
//		Collections.addAll(expected, "o̜","e̝","r̟","m̤","ṿ","z̥","t̪","p̫","w","g̑","a̐","ủ","e͚","u͛","n͇","x͈");
//
//		assertEquals(expected, received);
//	}
// NB: this test fails when using Canonical Composition.

	@Test
	public void testDoubleWidth() {
		List<String> expected = new ArrayList<String>();
		List<String> received = Segmenter.segment("k͡pg͡bt͜sd͜zp͡fɔ͝aa͟ʕɛ͠ʌ");
		Collections.addAll(expected,"k͡p", "g͡b", "t͜s", "d͜z", "p͡f", "ɔ͝a", "a͟ʕ", "ɛ͠ʌ");

		assertEquals(expected, received);
	}

	@Test
	public void testDoubleWidthPlusModifiers() {
		List<String> expected = new ArrayList<String>();
		List<String> received = Segmenter.segment("k͡pʰg͡b̤ˁt͜sˠd͜zʷ");

		Collections.addAll(expected,"k͡pʰ","g͡b̤ˁ","t͜sˠ","d͜zʷ");

		assertEquals(expected, received);
	}

	@Test
	public void testBasicVariables01() {
		List<String> expected  = new ArrayList<String>();
		List<String> variables = new ArrayList<String>();
		Collections.addAll(variables, "CH", "C", "V" );
		List<String> received = Segmenter.segment("aCatVaCHo", variables);

		Collections.addAll(expected, "a", "C", "a", "t", "V", "a", "CH", "o");

		assertEquals(expected, received);
	}

	@Test
	public void testBasicVariables02() {
		List<String> expected  = new ArrayList<String>();
		List<String> variables = new ArrayList<String>();
		Collections.addAll(variables, "CH", "C", "V", "H" );

		List<String> received = Segmenter.segment("CHatVaCHoC", variables);

		Collections.addAll(expected, "CH", "a", "t", "V", "a", "CH", "o", "C");

		assertEquals(expected, received);
	}

	@Test
	public void testBasicVariables03() {
		List<String> expected  = new ArrayList<String>();
		List<String> variables = new ArrayList<String>();
		Collections.addAll(variables, "CH", "C", "[+Approximant]", "H" );

		List<String> received = Segmenter.segment("CHatVa[+Approximant]oC", variables);

		Collections.addAll(expected, "CH", "a", "t", "V", "a", "[+Approximant]", "o", "C");

		assertEquals(expected, received);
	}
}
