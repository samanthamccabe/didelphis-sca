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
public class LexiconCloseCommand<T> extends AbstractLexiconIoCommand {

	private final LexiconMap<T> lexicons;
	private final FormatterMode mode;

	public LexiconCloseCommand(LexiconMap<T> lexParam, String path, String handle,
			FileHandler name, FormatterMode modeParam) {
		super(path, handle, name);
		lexicons = lexParam;
		mode = modeParam;
	}

	@Override
	public void run() {
		// REMOVE data from lexicons
		Lexicon<T> lexicon = lexicons.remove(getHandle());

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
				sb +='\n';
			}
		}
		String data = sb.trim();
		String normalized = mode.normalize(data);
		getHandler().writeString(getPath(), normalized);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof LexiconCloseCommand)) return false;
		if (!super.equals(o)) return false;
		LexiconCloseCommand<?> that = (LexiconCloseCommand<?>) o;
		return Objects.equals(lexicons, that.lexicons) && mode == that.mode;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), lexicons, mode);
	}

	@Override
	public String toString() {
		return "LexiconCloseCommand{" + "lexicons=" + lexicons + ", mode=" +
				mode + '}';
	}
}
