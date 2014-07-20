package org.haedusfc.soundchange;

import org.haedusfc.datatypes.phonetic.Sequence;
import org.haedusfc.soundchange.exceptions.RuleFormatException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Created with IntelliJ IDEA.
 * User: goats
 * Date: 6/22/13
 * Time: 3:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class RuleTest {

	public static final String SPACES = " +";

	@Test
	public void testRule01() throws RuleFormatException {
		Rule rule = new Rule("a > b");

		testRule(rule, "aaaaaaccca", "bbbbbbcccb");
	}

	@Test
	public void testRule02() throws RuleFormatException {
		Rule rule = new Rule("a e > æ ɛ");

		testRule(rule, "atereyamane", "ætɛrɛyæmænɛ");
	}

	@Test
	public void testRule03() throws RuleFormatException {
		Rule rule = new Rule("a b c d e f g > A B C D E F G");

		testRule(rule, "abcdefghijk", "ABCDEFGhijk");
	}

	@Test
	public void testConditionalRule01() throws RuleFormatException {
		Rule rule = new Rule("a > o / g_");

		testRule(rule, "adamagara", "adamagora");
	}

	@Test
	public void testConditionalRule02() throws RuleFormatException {
		Rule rule = new Rule("a > e / _c");
		testRule(rule, "abacaba", "abecaba");
		testRule(rule, "ababaca", "ababeca");
		testRule(rule, "acababa", "ecababa");
		testRule(rule, "acabaca", "ecabeca");
	}

	@Test
	public void testConditionalRule03() throws RuleFormatException {
		Rule rule = new Rule("a > e / _c+#");
		testRule(rule, "abac", "abec");
		testRule(rule, "abacc", "abecc");
		testRule(rule, "abaccc", "abeccc");
		testRule(rule, "abacccc", "abecccc");
		testRule(rule, "abaccccc", "abeccccc");
	}

	@Test
	public void testConditionalRule04() throws RuleFormatException {
		Rule rule = new Rule("eʔe aʔa eʔa aʔe > ē ā ā ē");
		testRule(rule, "keʔe", "kē");
		testRule(rule, "kaʔa", "kā");
		testRule(rule, "keʔa", "kā");
		testRule(rule, "kaʔe", "kē");
	}

	@Test
	public void testConditionalRule05() throws RuleFormatException {
		Rule rule = new Rule("rˌh lˌh > ər əl / _a");
		testRule(rule, "krˌha", "kəra");
		testRule(rule, "klˌha", "kəla");
	}

	@Test
	public void testConditionalRule06() throws RuleFormatException {
		Rule rule = new Rule("pʰ tʰ kʰ ḱʰ > b d g ɟ / _{r l}?{a e o ā ē ō}{i u}?{n m l r}?{pʰ tʰ kʰ ḱʰ}");

		testRule(rule, "pʰāḱʰus", "bāḱʰus");
		testRule(rule, "pʰentʰros", "bentʰros");
		testRule(rule, "pʰlaḱʰmēn", "blaḱʰmēn");
		testRule(rule, "pʰoutʰéyet", "boutʰéyet");

		testRule(rule, "pʰɛḱʰus", "pʰɛḱʰus");
	}

	@Test
	public void testConditionalRule07() throws RuleFormatException {
		Rule rule = new Rule("pʰ tʰ kʰ ḱʰ > b d g ɟ / _{a e o}{pʰ tʰ kʰ ḱʰ}");

		testRule(rule, "pʰaḱʰus", "baḱʰus");
		testRule(rule, "pʰāḱʰus", "pʰāḱʰus");
	}

	@Test
	public void testConditionalRule08() throws RuleFormatException {
		Rule rule = new Rule("d > t / _#");

		testRule(rule, "abad", "abat");
		testRule(rule, "abada", "abada");
	}

	@Test
	public void testLoop01() throws RuleFormatException {
		Rule rule = new Rule("q > qn");

		testRule(rule, "aqa", "aqna");
	}

	@Test
	public void testExpansion01() throws RuleFormatException {
		VariableStore vs = new VariableStore();

		vs.add("[E]", "e ē é ê".split(SPACES));
		vs.add("[A]", "a ā á â".split(SPACES));

		Rule rule = new Rule("[E] > [A] / {x ʕ}_", vs);

		String expected = "e ē é ê > a ā á â / {x ʕ}_";

		assertEquals(expected, rule.toString());
	}

	@Test
	public void testExpansion02() throws RuleFormatException {

		VariableStore vs = new VariableStore();

		vs.add("@VS", "a e i o u ə á é í ó ú".split(SPACES));
		vs.add("@VL", "ā ē ī ō ū ə̄  â ê î ô û".split(SPACES));

		Rule rule = new Rule("@VSī @VSū > @VLi @VLu / _{C #}", vs);

		String expected = "" +
				"aī eī iī oī uī əī áī éī íī óī úī " +
				"aū eū iū oū uū əū áū éū íū óū úū " +
				"> " +
				"āi ēi īi ōi ūi ə̄i âi êi îi ôi ûi " +
				"āu ēu īu ōu ūu ə̄u âu êu îu ôu ûu " +
				"/ _{C #}";

		assertEquals(expected, rule.toString());
	}

	@Test
	public void testUnconditional() throws RuleFormatException {
		Sequence word = new Sequence("h₁óh₁es-");
		Sequence expected = new Sequence("ʔóʔes-");

		Rule rule = new Rule("h₁ h₂ h₃ h₄ > ʔ x ɣ ʕ");

		assertEquals(expected, rule.apply(word));
	}

	@Test
	public void testUnconditional02() throws RuleFormatException {
		Sequence expected = new Sequence("telə");

		Rule rule = new Rule("eʔé > ê");

		assertEquals(expected, rule.apply(expected));
	}

	@Test
	public void testDebug01() throws RuleFormatException {
		Sequence original = new Sequence("mlan");
		Sequence expected = new Sequence("blan");

		VariableStore vs = new VariableStore();
		vs.add("V", "a e i o u".split(SPACES));

		Rule rule = new Rule("ml > bl / #_V", vs);

		assertEquals(expected, rule.apply(original));
	}

	@Test
	public void testUnconditional03() throws RuleFormatException {
		Rule rule = new Rule("ox > l");

		testRule(rule, "oxoxoxox", "llll");
		testRule(rule, "moxmoxmoxmoxmox", "mlmlmlmlml");
		testRule(rule, "mmoxmmoxmmoxmmoxmmox", "mmlmmlmmlmmlmml");
	}

	// "trh₂-we"
	@Test
	public void testDebug02() throws RuleFormatException {
		Sequence original = new Sequence("trh₂we");
		Sequence expected = new Sequence("tə̄rwe");

		VariableStore vs = new VariableStore();
		vs.add("X", "h₁ h₂ h₃ h₄".split(SPACES));

		vs.add("A", "r l m n".split(SPACES));
		vs.add("W", "y w".split(SPACES));

		vs.add("Q", "kʷʰ kʷ gʷ".split(SPACES));
		vs.add("K", "kʰ  k  g".split(SPACES));
		vs.add("KY", "cʰ  c  ɟ".split(SPACES));
		vs.add("T", "pʰ  p  b".split(SPACES));
		vs.add("P", "tʰ  t  d".split(SPACES));
		vs.add("[PLOSIVE]", "P T K KY Q".split(SPACES));
		vs.add("[OBSTRUENT]", "[PLOSIVE] s".split(SPACES));
		vs.add("C", "[OBSTRUENT] A W".split(SPACES));

		Rule rule1 = new Rule("rX lX nX mX > r̩X l̩X n̩X m̩X / [OBSTRUENT]_", vs);
		Rule rule2 = new Rule("r l > r̩ l̩ / [OBSTRUENT]_{C #}", vs);
		Rule rule3 = new Rule("r̩ l̩ > r l / C_N{C #}", vs);
		Rule rule4 = new Rule("r̩X l̩X > ə̄r ə̄l   / _{C #}", vs);


		Sequence sequence = rule1.apply(original);

		sequence = rule2.apply(sequence);
		sequence = rule3.apply(sequence);
		sequence = rule4.apply(sequence);

		assertEquals(expected, sequence);
	}

	/*======================================================================+
	 | Exception Tests                                                      |
	 +======================================================================*/
	@Test(expected = RuleFormatException.class)
	public void testRuleException01() throws RuleFormatException {
		new Rule(" > ");
	}

	@Test(expected = RuleFormatException.class)
	public void testRuleException02() throws RuleFormatException {
		new Rule("a > b /");
	}

	@Test(expected = RuleFormatException.class)
	public void testRuleException03() throws RuleFormatException {
		new Rule("a > / b");
	}

	@Test(expected = RuleFormatException.class)
	public void testRuleException04() throws RuleFormatException {
		new Rule(" > a / b");
	}

	@Test(expected = RuleFormatException.class)
	public void testRuleException05() throws RuleFormatException {
		new Rule(" > / b");
	}

	private void testRule(Rule rule, String seq, String exp) {
		Sequence sequence = new Sequence(seq);
		Sequence expected = new Sequence(exp);
		Sequence received = rule.apply(sequence);

		assertEquals(expected, received);
	}
}
