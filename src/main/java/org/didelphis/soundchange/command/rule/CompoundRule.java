/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.rule;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.didelphis.language.phonetic.Lexicon;
import org.didelphis.language.phonetic.sequences.PhoneticSequence;
import org.didelphis.language.phonetic.sequences.Sequence;
import org.didelphis.soundchange.LexiconMap;
import org.didelphis.structures.contracts.Delegating;

import java.util.List;

@EqualsAndHashCode
@ToString
public class CompoundRule
		implements Rule, Delegating<Iterable<? extends Rule>> {

	private final Iterable<? extends Rule> rules;
	private final LexiconMap lexicons;

	public CompoundRule(Iterable<? extends Rule> rules,
			LexiconMap lexicons) {
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
	public Sequence apply(Sequence sequence) {
		Sequence output = new PhoneticSequence(sequence);
		for (int index = 0; index < output.size(); index++) {
			for (Rule rule : rules) {
				rule.applyAtIndex(output, index);
			}
		}
		return output;
	}

	@Override
	public int applyAtIndex(Sequence sequence, int index) {
		return 0;
	}

	@Override
	public Iterable<? extends Rule> getDelegate() {
		return rules;
	}
}
