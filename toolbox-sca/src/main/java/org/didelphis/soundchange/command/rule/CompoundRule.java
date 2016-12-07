/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.rule;

import org.didelphis.phonetic.Lexicon;
import org.didelphis.phonetic.LexiconMap;
import org.didelphis.phonetic.Sequence;

import java.util.List;

/**
 * Created by samantha on 10/23/16.
 */
public class CompoundRule implements Rule {

	private final Iterable<BaseRule> rules;
	private final LexiconMap lexicons;

	public CompoundRule(Iterable<BaseRule> rules, LexiconMap lexicons) {
		this.rules = rules;
		this.lexicons = lexicons;
	}

	@Override
	public void run() {
		for (Lexicon lexicon : lexicons.values()) {
			for (List<Sequence> row : lexicon) {
				for (int i = 0; i < row.size(); i++) {
					Sequence word = apply(row.get(i));
					row.set(i, word);
				}
			}
		}
	}

	@Override
	public Sequence apply(Sequence input) {
		Sequence output = new Sequence(input);
		for (int index = 0; index < output.size(); index++) {
			for (BaseRule rule : rules) {
				rule.applyAtIndex(output, index);
			}
		}
		return output;
	}

	@Override
	public int applyAtIndex(Sequence input, int index) {
		return 0;
	}
}
