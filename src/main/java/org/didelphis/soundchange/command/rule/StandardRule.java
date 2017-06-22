/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.rule;

import org.didelphis.language.phonetic.Lexicon;
import org.didelphis.language.phonetic.LexiconMap;
import org.didelphis.language.phonetic.SequenceFactory;
import org.didelphis.language.phonetic.sequences.Sequence;

import java.util.List;

/**
 * Created by samantha on 10/24/16.
 */
public class StandardRule<T> implements Rule<T> {

	private final LexiconMap<T> lexicons;
	private final BaseRule<T> rule;

	public StandardRule(String rule, LexiconMap<T> lexicons, SequenceFactory<T> factory) {
		this.rule = new BaseRule<>(rule, factory);
		this.lexicons = lexicons;
	}

	public StandardRule(BaseRule<T> rule, LexiconMap<T> lexicons) {
		this.rule = rule;
		this.lexicons = lexicons;
	}

	@Override
	public Sequence<T> apply(Sequence<T> sequence) {
		return rule.apply(sequence);
	}

	@Override
	public int applyAtIndex(Sequence<T> sequence, int index) {
		return rule.applyAtIndex(sequence, index);
	}

	@Override
	public void run() {
		for (Lexicon<T> lexicon : lexicons.values()) {
			for (List<Sequence<T>> row : lexicon) {
				for (int i = 0; i < row.size(); i++) {
					Sequence<T> word = apply(row.get(i));
					row.set(i, word);
				}
			}
		}
	}

	@Override
	public String toString() {
		return rule.toString();
	}
}
