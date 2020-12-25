/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.io;

import lombok.EqualsAndHashCode;

import org.didelphis.io.FileHandler;
import org.didelphis.language.parsing.FormatterMode;
import org.didelphis.language.phonetic.Lexicon;
import org.didelphis.language.phonetic.model.FeatureMapping;
import org.didelphis.language.phonetic.sequences.Sequence;
import org.didelphis.soundchange.LexiconMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
public class LexiconWriteCommand extends AbstractLexiconIoCommand {

	private static final Logger LOG = LogManager.getLogger(LexiconWriteCommand.class);

	private final FeatureMapping featureMapping;
	private final LexiconMap lexicons;
	private final FormatterMode mode;

	public LexiconWriteCommand(
			LexiconMap lexParam,
			String path,
			String handle,
			FileHandler name,
			FeatureMapping featureMapping,
			FormatterMode modeParam) {
		super(path, handle, name);
		this.featureMapping = featureMapping;
		lexicons = lexParam;
		mode = modeParam;
	}

	@Override
	public void run() {
		// GET data from lexicons
		Lexicon lexicon = lexicons.getLexicon(getHandle());

		StringBuilder sb = new StringBuilder();
		Iterator<List<Sequence>> i1 = lexicon.iterator();
		while (i1.hasNext()) {
			Iterator<Sequence> i2 = i1.next().iterator();
			while (i2.hasNext()) {
				Sequence sequence = i2.next();

				sb.append(featureMapping.findBestSymbols(sequence));

				if (i2.hasNext()) {
					sb.append('\t');
				}

			}
			if (i1.hasNext()) {
				sb.append('\n');
			}
		}

		String normalized = mode.normalize(sb.toString());
		String path = getPath();
		try {
			getHandler().writeString(path, normalized);
		} catch (IOException e) {
			LOG.error("Failed to write to path {}", path, e);
		}
	}

	@Override
	public String toString() {
		return "WRITE "+getHandle()+" AS '"+getPath()+"'";
	}
}
