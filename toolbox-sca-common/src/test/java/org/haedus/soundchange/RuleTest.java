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

package org.haedus.soundchange;

import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.VariableStore;
import org.haedus.exceptions.ParseException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Created with IntelliJ IDEA.
 * User: Samantha Fiona Morrigan McCabe
 * Date: 6/22/13
 * Time: 3:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class RuleTest {

    @Test
    public void testDeletion01() throws ParseException {
        Rule rule = new Rule("∅ - > 0");
        testRule(rule, "∅-s-irentu-pʰen", "sirentupʰen");
    }

    @Test
    public void testDeletion02() throws ParseException {
        Rule rule = new Rule("a > 0");
        testRule(rule, "aaaabbba", "bbb");
    }

    @Test
    public void testDeletion03() throws ParseException {
        Rule rule = new Rule("a b > 0");
        testRule(rule, "aaaaccbbccbba", "cccc");
    }

    @Test
    public void testRule01() throws ParseException {
        Rule rule = new Rule("a > b");

        testRule(rule, "aaaaaaccca", "bbbbbbcccb");
    }

    @Test
    public void testRule02() throws ParseException {
        Rule rule = new Rule("a e > æ ɛ");

        testRule(rule, "atereyamane", "ætɛrɛyæmænɛ");
    }

    @Test
    public void testRule03() throws ParseException {
        Rule rule = new Rule("a b c d e f g > A B C D E F G");

        testRule(rule, "abcdefghijk", "ABCDEFGhijk");
    }

    @Test
    public void testConditionalRule01() throws ParseException {
        Rule rule = new Rule("a > o / g_");

        testRule(rule, "adamagara", "adamagora");
    }

    @Test
    public void testConditionalRule02() throws ParseException {
        Rule rule = new Rule("a > e / _c");
        testRule(rule, "abacaba", "abecaba");
        testRule(rule, "ababaca", "ababeca");
        testRule(rule, "acababa", "ecababa");
        testRule(rule, "acabaca", "ecabeca");
    }

    @Test
    public void testConditionalRule03() throws ParseException {
        Rule rule = new Rule("a > e / _c+#");
        testRule(rule, "abac", "abec");
        testRule(rule, "abacc", "abecc");
        testRule(rule, "abaccc", "abeccc");
        testRule(rule, "abacccc", "abecccc");
        testRule(rule, "abaccccc", "abeccccc");
    }

    @Test
    public void testConditionalRule04() throws ParseException {
        Rule rule = new Rule("eʔe aʔa eʔa aʔe > ē ā ā ē");
        testRule(rule, "keʔe", "kē");
        testRule(rule, "kaʔa", "kā");
        testRule(rule, "keʔa", "kā");
        testRule(rule, "kaʔe", "kē");
    }

    @Test
    public void testConditionalRule05() throws ParseException {
        Rule rule = new Rule("rˌh lˌh > ər əl / _a");
        testRule(rule, "krˌha", "kəra");
        testRule(rule, "klˌha", "kəla");
    }

    @Test
    public void testConditionalRule06() throws ParseException {
        Rule rule = new Rule("pʰ tʰ kʰ ḱʰ > b d g ɟ / _{r l}?{a e o ā ē ō}{i u}?{n m l r}?{pʰ tʰ kʰ ḱʰ}");

        testRule(rule, "pʰāḱʰus", "bāḱʰus");
        testRule(rule, "pʰentʰros", "bentʰros");
        testRule(rule, "pʰlaḱʰmēn", "blaḱʰmēn");
        testRule(rule, "pʰoutʰéyet", "boutʰéyet");

        testRule(rule, "pʰɛḱʰus", "pʰɛḱʰus");
    }

    @Test
    public void testConditionalRule07() throws ParseException {
        Rule rule = new Rule("pʰ tʰ kʰ ḱʰ > b d g ɟ / _{a e o}{pʰ tʰ kʰ ḱʰ}");

        testRule(rule, "pʰaḱʰus", "baḱʰus");
        testRule(rule, "pʰāḱʰus", "pʰāḱʰus");
    }

    @Test
    public void testConditionalRule08() throws ParseException {
        Rule rule = new Rule("d > t / _#");

        testRule(rule, "abad", "abat");
        testRule(rule, "abada", "abada");
    }

    @Test
    public void testLoop01() throws ParseException {
        Rule rule = new Rule("q > qn");

        testRule(rule, "aqa", "aqna");
    }

    @Test
    public void testExpansion01() throws ParseException {
        VariableStore vs = new VariableStore();

        vs.put("[E]", "e", "ē", "é", "ê");
        vs.put("[A]", "a", "ā", "á", "â");

        Rule rule = new Rule("[E] > [A] / {x ʕ}_", vs, true);

        String expected = "e ē é ê > a ā á â / {x ʕ}_";

        assertEquals(expected, rule.toString());
    }

    @Test
    public void testExpansion02() throws ParseException {

        VariableStore vs = new VariableStore();

        vs.put("@VS", "a", "e", "i", "o", "u", "ə", "á", "é", "í", "ó", "ú");
        vs.put("@VL", "ā", "ē", "ī", "ō", "ū", "ə̄", "â", "ê", "î", "ô", "û");

        Rule rule = new Rule("@VSī @VSū > @VLi @VLu / _{C #}", vs, true);

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
    public void testUnconditional() throws ParseException {
        Sequence word = new Sequence("h₁óh₁es-");
        Sequence expected = new Sequence("ʔóʔes-");

        Rule rule = new Rule("h₁ h₂ h₃ h₄ > ʔ x ɣ ʕ");

        assertEquals(expected, rule.apply(word));
    }

    @Test
    public void testUnconditional02() throws ParseException {
        Sequence expected = new Sequence("telə");

        Rule rule = new Rule("eʔé > ê");

        assertEquals(expected, rule.apply(expected));
    }

    @Test
    public void testDebug01() throws ParseException {
        Sequence original = new Sequence("mlan");
        Sequence expected = new Sequence("blan");

        VariableStore vs = new VariableStore();
        vs.put("V", new String[]{"a", "e", "i", "o", "u"}, true);

        Rule rule = new Rule("ml > bl / #_V", vs, true);

        assertEquals(expected, rule.apply(original));
    }

    @Test
    public void testUnconditional03() throws ParseException {
        Rule rule = new Rule("ox > l");

        testRule(rule, "oxoxoxox", "llll");
        testRule(rule, "moxmoxmoxmoxmox", "mlmlmlmlml");
        testRule(rule, "mmoxmmoxmmoxmmoxmmox", "mmlmmlmmlmmlmml");
    }

    // "trh₂-we"
    @Test
    public void testDebug02() throws ParseException {
        Sequence original = new Sequence("trh₂we");
        Sequence expected = new Sequence("tə̄rwe");

	    VariableStore vs = new VariableStore();
	    vs.add("X  = h₁  h₂ h₃ h₄", true);
	    vs.add("A  = r   l  m  n",  true);
	    vs.add("W  = y   w",        true);
	    vs.add("Q  = kʷʰ kʷ gʷ",    true);
	    vs.add("K  = kʰ  k  g",     true);
	    vs.add("KY = cʰ  c  ɟ",     true);
	    vs.add("T  = pʰ  p  b",     true);
	    vs.add("P  = tʰ  t  d",     true);

	    vs.add("[PLOSIVE] = P T K KY Q",   true);
	    vs.add("[OBSTRUENT] = [PLOSIVE] s", true);
	    vs.add("C = [OBSTRUENT] A W",       true);

        Rule rule1 = new Rule("rX lX nX mX > r̩X l̩X n̩X m̩X / [OBSTRUENT]_", vs, true);
        Rule rule2 = new Rule("r l > r̩ l̩ / [OBSTRUENT]_{C #}", vs, true);
        Rule rule3 = new Rule("r̩ l̩ > r l / C_N{C #}", vs, true);
        Rule rule4 = new Rule("r̩X l̩X > ə̄r ə̄l   / _{C #}", vs, true);

        Sequence sequence = rule1.apply(original);

        sequence = rule2.apply(sequence);
        sequence = rule3.apply(sequence);
        sequence = rule4.apply(sequence);

        assertEquals(expected, sequence);
    }

    @Test
    public void testDebug03() throws ParseException {
        Sequence original = new Sequence("pʰabopa");
        Sequence expected = new Sequence("papoba");

        Rule rule = new Rule("pʰ p b > p b p");

        Sequence received = rule.apply(original);
        assertEquals(expected, received);
    }

    /*======================================================================+
     | Exception Tests                                                      |
     +======================================================================*/
    @Test(expected = ParseException.class)
    public void testRuleException01() throws ParseException {
        new Rule(" > ");
    }

    @Test(expected = ParseException.class)
    public void testRuleException02() throws ParseException {
        new Rule("a > b /");
    }

    @Test(expected = ParseException.class)
    public void testRuleException03() throws ParseException {
        new Rule("a > / b");
    }

    @Test(expected = ParseException.class)
    public void testRuleException04() throws ParseException {
        new Rule(" > a / b");
    }

    @Test(expected = ParseException.class)
    public void testRuleException05() throws ParseException {
        new Rule(" > / b");
    }

    private void testRule(Rule rule, String seq, String exp) {
        Sequence sequence = new Sequence(seq);
        Sequence expected = new Sequence(exp);
        Sequence received = rule.apply(sequence);

        assertEquals(expected, received);
    }
}
