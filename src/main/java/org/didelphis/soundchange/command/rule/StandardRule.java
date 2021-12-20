/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.rule;

import org.didelphis.language.phonetic.model.FeatureMapping;
import org.didelphis.language.phonetic.sequences.Sequence;
import org.didelphis.soundchange.LexiconMap;
import org.didelphis.soundchange.parser.ParserMemory;
import org.didelphis.utilities.Strings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Set;



/**
 * @since 0.1.0
 */
public class StandardRule implements Rule {

	private static final Logger LOG = LogManager.getLogger(StandardRule.class);

	private final LexiconMap lexicons;
	private final BaseRule rule;
	private final FeatureMapping mapping;

	public StandardRule(String rule, ParserMemory memory) {
		this.rule = new BaseRule(rule, memory);
		lexicons = memory.getLexicons();
		mapping = memory.getFeatureMapping();
	}

	public StandardRule(String rule, ParserMemory memory, boolean debug) {
		this.rule = new BaseRule(rule, memory);
		lexicons = memory.getLexicons();
		mapping = memory.getFeatureMapping();
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
		Set<String> debugKeys = lexicons.getDebugKeys();
		Collection<String> handles = lexicons.getHandles();
		for (String handle : handles) {
			boolean useDebug = debugKeys.contains(handle);
			for (List<Sequence> row : lexicons.getLexicon(handle)) {
				for (int i = 0; i < row.size(); i++) {
					Sequence sequence = row.get(i);
					String string = sequence.toString();
					Sequence word = apply(sequence);
					if (useDebug) {
						LOG.info("{} --> {}",
								Strings.padRight(string, 10),
								mapping.findBestSymbols(word));
					}
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
