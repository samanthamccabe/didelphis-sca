/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.rule;

import org.didelphis.language.phonetic.Lexicon;
import org.didelphis.language.phonetic.sequences.Sequence;
import org.didelphis.soundchange.LexiconMap;
import org.didelphis.soundchange.parser.ParserMemory;

import java.util.List;

/**
 * @since 0.1.0
 */
public class StandardRule implements Rule {

	private final LexiconMap lexicons;
	private final BaseRule rule;

	public StandardRule(String rule, ParserMemory memory) {
		this.rule = new BaseRule(rule, memory);
		lexicons = memory.getLexicons();
	}

	public StandardRule(String rule, ParserMemory memory, boolean debug) {
		this.rule = new BaseRule(rule, memory);
		lexicons = memory.getLexicons();
		if (debug) {
			this.rule.setUseDebug(true);
		}
	}

	@Override
	public Sequence apply(Sequence sequence) {
		return rule.apply(sequence);
	}

	@Override
	public int applyAtIndex(Sequence sequence, int index) {
		return rule.applyAtIndex(sequence, index);
	}

	@Override
	public void run() {
		for (Lexicon lexicon : lexicons.values()) {
			for (List<Sequence> row : lexicon) {
				for (int i = 0; i < row.size(); i++) {
					Sequence sequence = row.get(i);
					Sequence word = apply(sequence);
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
