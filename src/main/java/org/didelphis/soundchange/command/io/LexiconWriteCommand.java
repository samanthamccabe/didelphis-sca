/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.io;

import org.didelphis.io.FileHandler;
import org.didelphis.language.parsing.FormatterMode;
import org.didelphis.language.phonetic.Lexicon;
import org.didelphis.language.phonetic.sequences.Sequence;
import org.didelphis.soundchange.LexiconMap;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;


/**
 * @author Samantha Fiona McCabe
 * @date 2014-10-13
 */
public class LexiconWriteCommand extends AbstractLexiconIoCommand {

	private final LexiconMap lexicons;
	private final FormatterMode mode;

	public LexiconWriteCommand(LexiconMap lexParam, String path, String handle,
			FileHandler name, FormatterMode modeParam) {
		super(path, handle, name);
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
				sb.append(sequence);
				if (i2.hasNext()) {
					sb.append('\t');
				}

			}
			if (i1.hasNext()) {
				sb.append('\n');
			}
		}
		String data = sb.toString().trim();
		String normalized = mode.normalize(data);
		getHandler().writeString(getPath(), normalized);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof LexiconWriteCommand)) return false;
		if (!super.equals(o)) return false;
		LexiconWriteCommand that = (LexiconWriteCommand) o;
		return Objects.equals(lexicons, that.lexicons) && mode == that.mode;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), lexicons, mode);
	}

	@Override
	public String toString() {
		return "LexiconWriteCommand{" + "lexicons=" + lexicons + ", mode=" +
				mode + '}';
	}
}
