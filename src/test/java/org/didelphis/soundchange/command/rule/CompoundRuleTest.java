/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.rule;

import org.didelphis.language.parsing.FormatterMode;
import org.didelphis.language.phonetic.Lexicon;
import org.didelphis.language.phonetic.SequenceFactory;
import org.didelphis.language.phonetic.features.IntegerFeature;
import org.didelphis.language.phonetic.sequences.Sequence;
import org.didelphis.soundchange.LexiconMap;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompoundRuleTest {

	private static final SequenceFactory<Integer> FACTORY =
			new SequenceFactory<>(
					IntegerFeature.INSTANCE.emptyLoader().getFeatureMapping(),
					FormatterMode.INTELLIGENT);

	@Test
	void testCompound01() {

		Rule<Integer> r1 = new BaseRule<>(
				"a i u > e / #{m p t k}?{a i u}{p t k l n}_",
				FACTORY
		);

		Rule<Integer> r2 = new BaseRule<>(
				"e > 0 / {l n}_{p t k} or {p t k}_l",
				FACTORY
		);
		
		List<? extends Rule<Integer>> rules =
				buildRules("a i u > e / #{m p t k}?{a i u}{p t k l n}_",
						"e > 0 / {l n}_{p t k} or {p t k}_l");

		Lexicon<Integer> lexicon =
				buildLexicon("apana", "kalapan", "takulita", "manatinalu",
						"matapulu");

		Lexicon<Integer> expected =
				buildLexicon("apena", "kalpan", "taklita", "mantinalu",
						"matepulu");

		LexiconMap<Integer> lexiconMap = new LexiconMap<>();
		lexiconMap.addLexicon("default", "", lexicon);
//		new CompoundRule<>(rules, lexiconMap).run();

		for (List<Sequence<Integer>> list : lexicon) {
			for (Sequence<Integer> sequence : list) {
				r1.apply(sequence);
				r2.apply(sequence);
			}
		}

		assertEquals(expected, lexicon);
	}

	private static List<? extends Rule<Integer>> buildRules(String... strings) {
		return Arrays.stream(strings)
				.map(string -> new BaseRule<>(string, FACTORY))
				.collect(Collectors.toList());
	}

	private static Lexicon<Integer> buildLexicon(String... strings) {
		Lexicon<Integer> lexicon = new Lexicon<>();
		for (String string : strings) {
			lexicon.add(FACTORY.toSequence(string));
		}
		return lexicon;
	}

	private static void testRule(Rule<Integer> rule, String seq, String exp) {
		testRule(rule, FACTORY, seq, exp);
	}

	private static void testRule(Rule<Integer> rule,
			SequenceFactory<Integer> factory, String seq, String exp) {
		Sequence<Integer> sequence = factory.toSequence(seq);
		Sequence<Integer> expected = factory.toSequence(exp);
		Sequence<Integer> received = rule.apply(sequence);

		assertEquals(expected, received);
	}
}
