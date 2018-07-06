/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.io;

import org.didelphis.io.FileHandler;
import org.didelphis.language.phonetic.Lexicon;
import org.didelphis.language.phonetic.SequenceFactory;
import org.didelphis.soundchange.LexiconMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Samantha Fiona McCabe
 * @date 2014-10-13
 */
public class LexiconOpenCommand<T> extends AbstractLexiconIoCommand {

	private final LexiconMap<T> lexicons;
	private final SequenceFactory<T> factory;

	public LexiconOpenCommand(LexiconMap<T> lexicons, String path, String handle,
			FileHandler handler, SequenceFactory<T> factory) {
		super(path, handle, handler);
		this.lexicons = lexicons;
		this.factory = factory;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof LexiconOpenCommand)) return false;
		if (!super.equals(o)) return false;
		LexiconOpenCommand<?> that = (LexiconOpenCommand<?>) o;
		return Objects.equals(lexicons, that.lexicons) &&
				Objects.equals(factory, that.factory);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), lexicons, factory);
	}

	@Override
	public void run() {
		String path = getPath();
		FileHandler handler = getHandler();

		CharSequence charSequence = handler.read(path);
		String data = charSequence == null ? "null" : charSequence.toString();
		Collection<List<String>> rows = new ArrayList<>();
		for (String line : data.split("\r?\n|\r", -1)) {
			List<String> cells = new ArrayList<>();
			Collections.addAll(cells, line.split("\t", -1));
			rows.add(cells);
		}

		Lexicon<T> lexicon = Lexicon.fromRows(factory, rows);
		lexicons.addLexicon(getHandle(), path, lexicon);
	}

	@Override
	public String toString() {
		return "LexiconOpenCommand{" + "lexicons=" + lexicons + ", factory=" +
				factory + '}';
	}
}
