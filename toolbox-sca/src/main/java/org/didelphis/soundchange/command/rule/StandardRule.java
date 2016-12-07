/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.rule;

import org.didelphis.phonetic.Lexicon;
import org.didelphis.phonetic.LexiconMap;
import org.didelphis.phonetic.Sequence;
import org.didelphis.phonetic.SequenceFactory;

import java.util.List;

/**
 * Created by samantha on 10/24/16.
 */
public class StandardRule implements Rule {

	private final LexiconMap lexicons;
	private final BaseRule rule;

	public StandardRule(String rule, LexiconMap lexicons, SequenceFactory factory) {
		this.rule = new BaseRule(rule, factory);
		this.lexicons = lexicons;
	}

	public StandardRule(BaseRule rule, LexiconMap lexicons) {
		this.rule = rule;
		this.lexicons = lexicons;
	}

	@Override
	public Sequence apply(Sequence input) {
		return rule.apply(input);
	}

	@Override
	public int applyAtIndex(Sequence input, int index) {
		return rule.applyAtIndex(input, index);
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
	public String toString() {
		return rule.toString();
	}
}
