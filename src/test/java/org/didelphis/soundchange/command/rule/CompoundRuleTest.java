/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.rule;

import org.didelphis.language.enums.FormatterMode;
import org.didelphis.language.phonetic.Lexicon;
import org.didelphis.language.phonetic.LexiconMap;
import org.didelphis.language.phonetic.SequenceFactory;
import org.didelphis.language.phonetic.features.IntegerFeature;
import org.didelphis.language.phonetic.sequences.Sequence;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by samantha on 10/27/16.
 */
public class CompoundRuleTest {

	private static final FormatterMode INTEL = FormatterMode.INTELLIGENT;
	private static final SequenceFactory<Integer> FACTORY = new SequenceFactory<>(
			IntegerFeature.emptyLoader().getFeatureMapping(), FormatterMode.INTELLIGENT);

	@Test
	public void testCompound01() {
		List<BaseRule<Integer>> rules = buildRules   (
				"a i u > e / #{m p t k}?{a i u}{p t k l n}_",
				"e > 0 / {l n}_{p t k} or {p t k}_l"
		);

		Lexicon<Integer>lexicon = buildLexicon(
				"apana",
				"kalapan",
				"takulita",
				"manatinalu",
				"matapulu"
		);

		Lexicon<Integer>expected = buildLexicon(
				"apena",
				"kalpan",
				"taklita",
				"mantinalu",
				"matepulu"
		);

		LexiconMap<Integer> lexiconMap = new LexiconMap<>();
		lexiconMap.addLexicon("default", "", lexicon);
		new CompoundRule<>(rules, lexiconMap).run();
		assertEquals(expected, lexicon);
	}

	private static List<BaseRule<Integer>> buildRules(String... strings) {
		List<BaseRule<Integer>> rules = new ArrayList<>();
		for (String string : strings) {
			rules.add(new BaseRule<>(string, FACTORY));
		}
		return rules;
	}

	private static Lexicon<Integer>buildLexicon(String... strings) {
		Lexicon<Integer>lexicon = new Lexicon<>();
		for (String string : strings) {
			lexicon.add(FACTORY.getSequence(string));
		}
		return lexicon;
	}

	private static void testRule(Rule<Integer> rule, String seq, String exp) {
		testRule(rule, FACTORY, seq, exp);
	}

	private static void testRule(Rule<Integer> rule, SequenceFactory<Integer> factory, String seq, String exp) {
		Sequence<Integer> sequence = factory.getSequence(seq);
		Sequence<Integer> expected = factory.getSequence(exp);
		Sequence<Integer> received = rule.apply(sequence);

		assertEquals(expected, received);
	}
}
