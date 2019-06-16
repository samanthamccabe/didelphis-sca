/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.io;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.didelphis.io.FileHandler;
import org.didelphis.language.parsing.FormatterMode;
import org.didelphis.language.phonetic.Lexicon;
import org.didelphis.language.phonetic.sequences.Sequence;
import org.didelphis.soundchange.LexiconMap;
import org.didelphis.utilities.Logger;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

@ToString
@EqualsAndHashCode(callSuper = true)
public class LexiconWriteCommand<T> extends AbstractLexiconIoCommand {

	private static final Logger LOG = Logger.create(LexiconOpenCommand.class);

	private final LexiconMap<T> lexicons;
	private final FormatterMode mode;

	public LexiconWriteCommand(LexiconMap<T> lexParam, String path, String handle,
			FileHandler name, FormatterMode modeParam) {
		super(path, handle, name);
		lexicons = lexParam;
		mode = modeParam;
	}

	@Override
	public void run() {
		// GET data from lexicons
		Lexicon<T> lexicon = lexicons.getLexicon(getHandle());

		String sb = "";
		Iterator<List<Sequence<T>>> i1 = lexicon.iterator();
		while (i1.hasNext()) {
			Iterator<Sequence<T>> i2 = i1.next().iterator();
			while (i2.hasNext()) {
				Sequence<T> sequence = i2.next();
				sb += sequence;
				if (i2.hasNext()) {
					sb += '\t';
				}

			}
			if (i1.hasNext()) {
				sb += '\n';
			}
		}
		String data = sb.trim();
		String normalized = mode.normalize(data);
		String path = getPath();
		try {
			getHandler().writeString(path, normalized);
		} catch (IOException e) {
			LOG.error("Failed to write to path {}", path, e);
		}
	}
}
