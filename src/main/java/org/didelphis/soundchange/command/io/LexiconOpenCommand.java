/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.io;

import org.didelphis.io.FileHandler;
import org.didelphis.language.phonetic.Lexicon;
import org.didelphis.language.phonetic.LexiconMap;
import org.didelphis.language.phonetic.SequenceFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/13/2014
 */
public class LexiconOpenCommand extends AbstractLexiconIoCommand {

	private final LexiconMap lexicons;
	private final SequenceFactory factory;

	public LexiconOpenCommand(LexiconMap lexicons, String path, String handle, FileHandler handler, SequenceFactory factory) {
		super(path, handle, handler);
		this.lexicons = lexicons;
		this.factory = factory;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof LexiconOpenCommand)) return false;
		if (!super.equals(o)) return false;
		LexiconOpenCommand that = (LexiconOpenCommand) o;
		return Objects.equals(lexicons, that.lexicons) && Objects.equals(factory, that.factory);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), lexicons, factory);
	}

	@Override
	public void run() {
		String path = getPath();
		FileHandler handler = getHandler();

		String data = String.valueOf(handler.read(path));
		Collection<List<String>> rows = new ArrayList<>();
		for (String line : data.split("\r?\n|\r", -1)) {
			List<String> cells = new ArrayList<>();
			Collections.addAll(cells, line.split("\t", -1));
			rows.add(cells);
		}

		Lexicon lexicon = factory.getLexicon(rows);
		lexicons.addLexicon(getHandle(), path, lexicon);
	}
	
	@Override
	public String toString() {
		return "LexiconOpenCommand{" +
				"lexicons=" + lexicons +
				", factory=" + factory +
				'}';
	}
}
