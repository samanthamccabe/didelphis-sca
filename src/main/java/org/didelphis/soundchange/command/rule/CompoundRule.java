/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.rule;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.didelphis.language.phonetic.Lexicon;
import org.didelphis.language.phonetic.sequences.BasicSequence;
import org.didelphis.language.phonetic.sequences.Sequence;
import org.didelphis.soundchange.LexiconMap;

import java.util.List;

/**
 * @author Samantha Fiona McCabe
 * @date 2016-10-23
 */
@EqualsAndHashCode
@ToString
public class CompoundRule<T> implements Rule<T> {

	private final Iterable<? extends Rule<T>> rules;
	private final LexiconMap<T> lexicons;

	public CompoundRule(Iterable<? extends Rule<T>> rules,
			LexiconMap<T> lexicons) {
		this.rules = rules;
		this.lexicons = lexicons;
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
	public Sequence<T> apply(Sequence<T> sequence) {
		Sequence<T> output = new BasicSequence<>(sequence);
		for (int index = 0; index < output.size(); index++) {
			for (Rule<T> rule : rules) {
				rule.applyAtIndex(output, index);
			}
		}
		return output;
	}

	@Override
	public int applyAtIndex(Sequence<T> sequence, int index) {
		return 0;
	}
}
