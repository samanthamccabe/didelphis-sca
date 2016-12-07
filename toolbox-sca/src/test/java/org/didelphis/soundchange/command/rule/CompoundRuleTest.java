/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.rule;

import org.didelphis.enums.FormatterMode;
import org.didelphis.phonetic.Lexicon;
import org.didelphis.phonetic.LexiconMap;
import org.didelphis.phonetic.Sequence;
import org.didelphis.phonetic.SequenceFactory;
import org.didelphis.soundchange.command.rule.BaseRule;
import org.didelphis.soundchange.command.rule.CompoundRule;
import org.didelphis.soundchange.command.rule.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by samantha on 10/27/16.
 */
public class CompoundRuleTest {

	private static final FormatterMode INTEL = FormatterMode.INTELLIGENT;
	private static final SequenceFactory FACTORY = new SequenceFactory(INTEL);

	@Test
	public void testCompound01() {
		List<BaseRule> rules = buildRules(
				"a i u > e / #{m p t k}?{a i u}{p t k l n}_",
				"e > 0 / {l n}_{p t k} or {p t k}_l"
		);

		Lexicon lexicon = buildLexicon(
				"apana",
				"kalapan",
				"takulita",
				"manatinalu",
				"matapulu"
		);

		Lexicon expected = buildLexicon(
				"apena",
				"kalpan",
				"taklita",
				"mantinalu",
				"matepulu"
		);

		LexiconMap lexiconMap = new LexiconMap();
		lexiconMap.addLexicon("default", "", lexicon);
		new CompoundRule(rules, lexiconMap).run();
		assertEquals(expected, lexicon);
	}

	private static List<BaseRule> buildRules(String... strings) {
		List<BaseRule> rules = new ArrayList<BaseRule>();
		for (String string : strings) {
			rules.add(new BaseRule(string, FACTORY));
		}
		return rules;
	}

	private static Lexicon buildLexicon(String... strings) {
		Lexicon lexicon = new Lexicon();
		for (String string : strings) {
			lexicon.add(FACTORY.getSequence(string));
		}
		return lexicon;
	}

	private static void testRule(Rule rule, String seq, String exp) {
		testRule(rule, FACTORY, seq, exp);
	}

	private static void testRule(Rule rule, SequenceFactory factory, String seq, String exp) {
		Sequence sequence = factory.getSequence(seq);
		Sequence expected = factory.getSequence(exp);
		Sequence received = rule.apply(sequence);

		assertEquals(expected, received);
	}
}
