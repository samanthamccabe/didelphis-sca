/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.rule;

import lombok.extern.slf4j.Slf4j;
import org.didelphis.language.parsing.FormatterMode;
import org.didelphis.language.parsing.ParseException;
import org.didelphis.language.phonetic.SequenceFactory;
import org.didelphis.language.phonetic.features.IntegerFeature;
import org.didelphis.language.phonetic.model.FeatureModelLoader;
import org.didelphis.language.phonetic.sequences.Sequence;
import org.didelphis.soundchange.VariableStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/**
 * Created with IntelliJ IDEA. @author Samantha Fiona McCabe
 *
 * @date 6/22/13 Templates.
 */
@Slf4j
class BaseRuleTest {

	private static final FeatureModelLoader<Integer> LOADER =
			IntegerFeature.emptyLoader();

	private static final SequenceFactory<Integer> INTELLIGENT =
			new SequenceFactory<>(LOADER.getFeatureMapping(),
					FormatterMode.INTELLIGENT);
	private static final SequenceFactory<Integer> FACTORY =
			new SequenceFactory<>(LOADER.getFeatureMapping(),
					FormatterMode.NONE);
	
	private static final boolean TIMEOUT = true;

	@Test
	void testMultipleSourceZeroes() {
		assertThrows(ParseException.class,
				() -> new BaseRule<>("0 0 > a", FACTORY));
	}

	@Test
	void testZeroWithMultipleTarget() {
		assertThrows(ParseException.class,
				() -> new BaseRule<>("0 > a a", FACTORY));
	}

	@Test
	void testMultipleZeroWithMultipleTarget() {
		assertThrows(ParseException.class,
				() -> new BaseRule<>("0 0 > a a", FACTORY));
	}

	@Test
	void testMissingArrow() {
		assertThrows(ParseException.class, () -> new BaseRule<>("a", FACTORY));
	}

	@Test
	void testUnbalancedTransform1() {
		assertThrows(ParseException.class,
				() -> new BaseRule<>("b > a a", FACTORY));
	}

	@Test
	void testUnbalancedTransform2() {
		assertThrows(ParseException.class,
				() -> new BaseRule<>("a b c > x y", FACTORY));
	}

	@Test
	void testDanglingOr1() {
		assertThrows(ParseException.class,
				() -> new BaseRule<>("a > b / _ or", FACTORY));
	}

	@Test
	void testDanglingOr2() {
		assertThrows(ParseException.class,
				() -> new BaseRule<>("a > b / or", FACTORY));
	}

	@Test
	void testBrackets01() {

		VariableStore store = new VariableStore(FormatterMode.INTELLIGENT);
		store.add("VS = a e i o u ə á é í ó ú");
		store.add("VL = ā ē ī ō ū ə̄ â ê î ô û");
		store.add("V  = VS VL");
		store.add("X  = x ʔ");
		store.add("[Obs] = X s");

		SequenceFactory<Integer> factory = new SequenceFactory<>(
				LOADER.getFeatureMapping(),
				store.getKeys(), 
				FormatterMode.INTELLIGENT
		);

		Rule<Integer> rule = new BaseRule<>("X > 0 / [Obs]_V", store, factory);

		testRule(rule, factory, "sxa", "sa");
		testRule(rule, factory, "sʔa", "sa");
		testRule(rule, factory, "xʔe", "xe");
		testRule(rule, factory, "sʔū", "sū");
	}

	@Test
	void testMetathesis01() {
		VariableStore store = new VariableStore(FormatterMode.INTELLIGENT);
		store.add("C = p t k");
		store.add("N = m n");

		SequenceFactory<Integer> factory = new SequenceFactory<>(
				LOADER.getFeatureMapping(),
				store.getKeys(), 
				FormatterMode.INTELLIGENT
		);

		BaseRule<Integer> rule = new BaseRule<>("CN > $2$1", store, factory);

		testRule(rule, "pn", "np");
		testRule(rule, "tn", "nt");
		testRule(rule, "kn", "nk");

		testRule(rule, "pm", "mp");
		testRule(rule, "tm", "mt");
		testRule(rule, "km", "mk");

		testRule(rule, "pt", "pt");
		testRule(rule, "tp", "tp");
		testRule(rule, "kp", "kp");
	}

	@Test
	void testMetathesis02() {
		VariableStore store = new VariableStore(FormatterMode.INTELLIGENT);
		store.add("C = p t k");
		store.add("N = m n");
		store.add("V = a i u");

		SequenceFactory<Integer> factory = new SequenceFactory<>(
				LOADER.getFeatureMapping(), 
				store.getKeys(),
				FormatterMode.INTELLIGENT
		);


		BaseRule<Integer> rule = new BaseRule<>("CVN > $3V$1", store, factory);

		testRule(rule, "pan", "nap");
		testRule(rule, "tin", "nit");
		testRule(rule, "kun", "nuk");

		testRule(rule, "pam", "map");
		testRule(rule, "tim", "mit");
		testRule(rule, "kum", "muk");

		testRule(rule, "pat", "pat");
		testRule(rule, "tip", "tip");
		testRule(rule, "kup", "kup");
	}

	@Test
	void testMetathesis03() {
		VariableStore store = new VariableStore(FormatterMode.INTELLIGENT);
		store.add("C = p t k");
		store.add("G = b d g");
		store.add("N = m n");
		SequenceFactory<Integer> factory =
				new SequenceFactory<>(LOADER.getFeatureMapping(),
						store.getKeys(), FormatterMode.INTELLIGENT);

		BaseRule<Integer> rule = new BaseRule<>("CN > $2$G1", store, factory);

		testRule(rule, "pn", "nb");
		testRule(rule, "tn", "nd");
		testRule(rule, "kn", "ng");

		testRule(rule, "pm", "mb");
		testRule(rule, "tm", "md");
		testRule(rule, "km", "mg");

		testRule(rule, "pt", "pt");
		testRule(rule, "tp", "tp");
		testRule(rule, "kp", "kp");
	}

	@Test
	void testDeletion01() {
		Rule<Integer> rule = new BaseRule<>("∅ - > 0", FACTORY);
		testRule(rule, FACTORY, "∅-s-irentu-pʰen", "sirentupʰen");
	}

	@Test
	void testDeletion02() {
		Rule<Integer> rule = new BaseRule<>("a > 0", FACTORY);
		testRule(rule, FACTORY, "aaaabbba", "bbb");
	}

	@Test
	void testDeletion03() {
		Rule<Integer> rule = new BaseRule<>("a b > 0", FACTORY);
		testRule(rule, FACTORY, "aaaaccbbccbba", "cccc");
	}

	@Test
	void testRule01() {
		Rule<Integer> rule = new BaseRule<>("a > b", FACTORY);

		testRule(rule, FACTORY, "aaaaaaccca", "bbbbbbcccb");
	}

	@Test
	void testRule02() {
		Rule<Integer> rule = new BaseRule<>("a e > æ ɛ", FACTORY);

		testRule(rule, FACTORY, "ate", "ætɛ");
		testRule(rule, FACTORY, "atereyamane", "ætɛrɛyæmænɛ");
	}

	@Test
	void testRule03() {
		Rule<Integer> rule = new BaseRule<>("a b c d e f g > A B C D E F G", FACTORY);

		testRule(rule, FACTORY, "abcdefghijk", "ABCDEFGhijk");
	}

	@Test
	void testConditional01() {
		Rule<Integer> rule = new BaseRule<>("a > o / g_", FACTORY);

		testRule(rule, FACTORY, "ga", "go");
		testRule(rule, FACTORY, "adamagara", "adamagora");
	}

	@Test
	void testConditional02() {
		Rule<Integer> rule = new BaseRule<>("a > e / _c", FACTORY);
		testRule(rule, FACTORY, "abacaba", "abecaba");
		testRule(rule, FACTORY, "ababaca", "ababeca");
		testRule(rule, FACTORY, "acababa", "ecababa");
		testRule(rule, FACTORY, "acabaca", "ecabeca");
	}

	@Test
	void testConditional03() {
		Rule<Integer> rule = new BaseRule<>("a > e / _c+#", INTELLIGENT);
		testRule(rule, INTELLIGENT, "abac", "abec");
		testRule(rule, INTELLIGENT, "abacc", "abecc");
		testRule(rule, INTELLIGENT, "abaccc", "abeccc");
		testRule(rule, INTELLIGENT, "abacccc", "abecccc");
		testRule(rule, INTELLIGENT, "abaccccc", "abeccccc");
	}

	@Test
	void testUnconditional04() {
		Rule<Integer> rule =
				new BaseRule<>("eʔe aʔa eʔa aʔe > ē ā ā ē", INTELLIGENT);
		testRule(rule, INTELLIGENT, "keʔe", "kē");
		testRule(rule, INTELLIGENT, "kaʔa", "kā");
		testRule(rule, INTELLIGENT, "keʔa", "kā");
		testRule(rule, INTELLIGENT, "kaʔe", "kē");
	}

	@Test
	void testConditional05() {
		Rule<Integer> rule =
				new BaseRule<>("rˌh lˌh > ər əl / _a", INTELLIGENT);
		testRule(rule, INTELLIGENT, "krˌha", "kəra");
		testRule(rule, INTELLIGENT, "klˌha", "kəla");
		testRule(rule, INTELLIGENT, "klˌhe", "klˌhe");
	}

	@Test
	void testConditional06() {
		Rule<Integer> rule = new BaseRule<>(
				"pʰ tʰ kʰ ḱʰ > b d g ɟ / _{r l}?{a e o ā ē ō}{i u}?{n m l r}?{pʰ tʰ kʰ ḱʰ}",
				INTELLIGENT);

		testRule(rule, INTELLIGENT, "pʰāḱʰus", "bāḱʰus");
		testRule(rule, INTELLIGENT, "pʰentʰros", "bentʰros");
		testRule(rule, INTELLIGENT, "pʰlaḱʰmēn", "blaḱʰmēn");
		testRule(rule, INTELLIGENT, "pʰoutʰéyet", "boutʰéyet");
		testRule(rule, INTELLIGENT, "pʰɛḱʰus", "pʰɛḱʰus");
	}

	@Test
	void testConditional07() {
		Rule<Integer> rule =
				new BaseRule<>("pʰ tʰ kʰ ḱʰ > b d g ɟ / _{a e o}{pʰ tʰ kʰ ḱʰ}",
						INTELLIGENT);

		testRule(rule, INTELLIGENT, "pʰaḱʰus", "baḱʰus");
		testRule(rule, INTELLIGENT, "pʰāḱʰus", "pʰāḱʰus");
	}

	@Test
	void testConditional08() {
		Rule<Integer> rule = new BaseRule<>("d > t / _#", FACTORY);

		testRule(rule, FACTORY, "abad", "abat");
		testRule(rule, FACTORY, "abada", "abada");
	}

	@Test
	void testInsertion01() {
		Rule<Integer> rule = new BaseRule<>("q > qn", FACTORY);

		testRule(rule, FACTORY, "aqa", "aqna");
	}

	@Test
	void testInsertion02() {
		Rule<Integer> rule = new BaseRule<>("0 > n / q_", FACTORY);

		testRule(rule, FACTORY, "aqa", "aqna");
	}

	@Test
	void testInsertion03() {
		Rule<Integer> rule = new BaseRule<>("0 > a / x_x", FACTORY);

		testRule(rule, FACTORY, "xx", "xax");
		testRule(rule, FACTORY, "xxx", "xaxax");
		testRule(rule, FACTORY, "xxxx", "xaxaxax");
		testRule(rule, FACTORY, "xzxx", "xzxax");
	}

	@Test
	void testUnconditional() {
		Sequence<Integer> word = INTELLIGENT.toSequence("h₁óh₁es-");
		Sequence<Integer> expected = INTELLIGENT.toSequence("ʔóʔes-");

		Rule<Integer> rule =
				new BaseRule<>("h₁ h₂ h₃ h₄ > ʔ x ɣ ʕ", INTELLIGENT);

		assertEquals(expected, rule.apply(word));
	}

	@Test
	void testUnconditional02() {
		Sequence<Integer> expected = INTELLIGENT.toSequence("telə");
		Rule<Integer> rule = new BaseRule<>("eʔé > ê", INTELLIGENT);

		assertEquals(expected, rule.apply(expected));
	}

	@Test
	void testDebug01() {

		VariableStore store = new VariableStore(FormatterMode.INTELLIGENT);
		store.add("V = a e i o u");

		SequenceFactory<Integer> factory = new SequenceFactory<>(
				LOADER.getFeatureMapping(),
				store.getKeys(),
				FormatterMode.INTELLIGENT
		);

		Sequence<Integer> original = factory.toSequence("mlan");
		Sequence<Integer> expected = factory.toSequence("blan");

		Rule<Integer> rule = new BaseRule<>("ml > bl / #_V", store, factory);

		assertEquals(expected, rule.apply(original));
	}

	@Test
	void testUnconditional03() {
		Rule<Integer> rule = new BaseRule<>("ox > l", FACTORY);

		testRule(rule, FACTORY, "oxoxoxox", "llll");
		testRule(rule, FACTORY, "moxmoxmoxmoxmox", "mlmlmlmlml");
		testRule(rule, FACTORY, "mmoxmmoxmmoxmmoxmmox", "mmlmmlmmlmmlmml");
	}

	@Test
	void testDebug02() {
		VariableStore store = new VariableStore(FormatterMode.INTELLIGENT);
		store.add("X  = h₁  h₂ h₃ h₄");
		store.add("A  = r   l  m  n");
		store.add("W  = y   w");
		store.add("Q  = kʷʰ kʷ gʷ");
		store.add("K  = kʰ  k  g");
		store.add("KY = cʰ  c  ɟ");
		store.add("T  = pʰ  p  b");
		store.add("P  = tʰ  t  d");

		store.add("[PLOSIVE] = P T K KY Q");
		store.add("[OBSTRUENT] = [PLOSIVE] s");
		store.add("C = [OBSTRUENT] A W");

		SequenceFactory<Integer> factory =
				new SequenceFactory<>(LOADER.getFeatureMapping(),
						store.getKeys(), FormatterMode.INTELLIGENT);

		Sequence<Integer> original = factory.toSequence("trh₂we");
		Sequence<Integer> expected = factory.toSequence("tə̄rwe");

		Rule<Integer> rule1 = new BaseRule<>("rX lX nX mX > r̩X l̩X n̩X m̩X / [OBSTRUENT]_", store, factory);
		Rule<Integer> rule2 = new BaseRule<>("r l > r̩ l̩ / [OBSTRUENT]_{C #}", factory);
		Rule<Integer> rule3 = new BaseRule<>("r̩ l̩ > r l / C_N{C #}", store, factory);
		Rule<Integer> rule4 = new BaseRule<>("r̩X l̩X > ə̄r ə̄l   / _{C #}", store, factory);

		Sequence<Integer> sequence = rule1.apply(original);

		sequence = rule2.apply(sequence);
		sequence = rule3.apply(sequence);
		sequence = rule4.apply(sequence);

		assertEquals(expected, sequence);
	}

	@Test
	void testDebug03() {
		Sequence<Integer> original = FACTORY.toSequence("pʰabopa");
		Sequence<Integer> expected = FACTORY.toSequence("papoba");

		Rule<Integer> rule = new BaseRule<>("pʰ p b > p b p", FACTORY);

		Sequence<Integer> received = rule.apply(original);
		assertEquals(expected, received);
	}

	@Test
	void testCompound01() {
		Rule<Integer> rule = new BaseRule<>("a > b / x_ OR _y", FACTORY);

		testRule(rule, FACTORY, "axa", "axb");
		testRule(rule, FACTORY, "aya", "bya");
		testRule(rule, FACTORY, "ayxa", "byxb");
		testRule(rule, FACTORY, "axya", "axya");
	}

	@Test
	void testCompound02() {
		Rule<Integer> rule = new BaseRule<>("a > b / x_ NOT _y", FACTORY);

		testRule(rule, FACTORY, "axa", "axb");
		testRule(rule, FACTORY, "axay", "axay");
		testRule(rule, FACTORY, "xayxa", "xayxb");
	}

	@Test
	void testCompound03() {
		VariableStore store = new VariableStore(FormatterMode.INTELLIGENT);
		store.add("C = x y z");

		SequenceFactory<Integer> factory = new SequenceFactory<>(LOADER.getFeatureMapping(),
				store.getKeys(),
				FormatterMode.INTELLIGENT);

		Rule<Integer> rule =
				new BaseRule<>("a > b / C_ NOT x_", store, factory);

		testRule(rule, factory, "axa", "axa");
		testRule(rule, factory, "aya", "ayb");
		testRule(rule, factory, "aza", "azb");
		testRule(rule, factory, "a", "a");
	}

	@Test
	void testCompound04() {
		VariableStore store = new VariableStore(FormatterMode.INTELLIGENT);
		store.add("C = x y z");

		SequenceFactory<Integer> factory =
				new SequenceFactory<>(LOADER.getFeatureMapping(),
						store.getKeys(), FormatterMode.INTELLIGENT);

		Rule<Integer> rule = new BaseRule<>("a > b / not x_", store, factory);

		testRule(rule, factory, "xaa", "xab");
		testRule(rule, factory, "axa", "bxa");
		testRule(rule, factory, "aya", "byb");
		testRule(rule, factory, "aza", "bzb");
		testRule(rule, factory, "a", "b");
	}

	@Test
	void testCompound05() {
		VariableStore store = new VariableStore(FormatterMode.INTELLIGENT);

		SequenceFactory<Integer> factory =
				new SequenceFactory<>(LOADER.getFeatureMapping(),
						store.getKeys(), FormatterMode.INTELLIGENT);

		Rule<Integer> rule =
				new BaseRule<>("a > b / not x_ not _y", store, factory);

		testRule(rule, factory, "xaa", "xab");
		testRule(rule, factory, "axa", "bxa");
		testRule(rule, factory, "aya", "ayb");
		testRule(rule, factory, "aza", "bzb");
		testRule(rule, factory, "a", "b");
	}

	@Test
	void testCompound06() {
		VariableStore store = new VariableStore(FormatterMode.INTELLIGENT);

		SequenceFactory<Integer> factory =
				new SequenceFactory<>(LOADER.getFeatureMapping(),
						store.getKeys(), FormatterMode.INTELLIGENT);
		
		assertThrows(ParseException.class,
				() -> new BaseRule<>("a > b / not x_ or _y", store, factory));
	}

	@Test
	void testCompound07() {
		Rule<Integer> rule =
				new BaseRule<>("a > b / x_ OR _y NOT _a NOT b_", FACTORY);

		testRule(rule, FACTORY, "axa", "axb");
		testRule(rule, FACTORY, "aya", "bya");
		testRule(rule, FACTORY, "ayxa", "byxb");
		testRule(rule, FACTORY, "axya", "axya");

		testRule(rule, FACTORY, "axbay", "axbay");
		testRule(rule, FACTORY, "bayay", "bayby");
		testRule(rule, FACTORY, "ayxaa", "byxaa");
	}

	/*======================================================================+
	 | Exception Tests                                                      |
	 +======================================================================*/
	@Test
	void testRuleException01() {
		assertThrows(ParseException.class,
				() -> new BaseRule<>(" > ", FACTORY));
	}

	@Test
	void testRuleException02() {
		assertThrows(ParseException.class,
				() -> new BaseRule<>("a > b /", FACTORY));
	}

	@Test
	void testRuleException03() {
		assertThrows(ParseException.class,
				() -> new BaseRule<>("a > / b", FACTORY));
	}

	@Test
	void testRuleException04() {
		assertThrows(ParseException.class,
				() -> new BaseRule<>(" > a / b", FACTORY));
	}

	@Test
	void testRuleException05() {
		assertThrows(ParseException.class,
				() -> new BaseRule<>(" > / b", FACTORY));
	}

	private static void testRule(Rule<Integer> rule, String seq, String exp) {
		testRule(rule, FACTORY, seq, exp);
	}

	private static <T> void testRule(
			Rule<T> rule, 
			SequenceFactory<T> factory, 
			String seq, 
			String exp
	) {
		Executable executable = () -> {
			Sequence<T> sequence = factory.toSequence(seq);
			Sequence<T> expected = factory.toSequence(exp);
			Sequence<T> received = rule.apply(sequence);
			assertEquals(expected, received);
		};
		
		if (TIMEOUT) {
			assertTimeoutPreemptively(Duration.ofSeconds(5), executable);
		} else {
			try {
				executable.execute();
			} catch (Throwable throwable) {
				log.error("Unexpected failure encountered: {}", throwable);
			}
		}
	}
}
