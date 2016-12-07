/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.io;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.didelphis.io.FileHandler;
import org.didelphis.phonetic.Lexicon;
import org.didelphis.phonetic.LexiconMap;
import org.didelphis.phonetic.SequenceFactory;

import java.util.List;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/13/2014
 */
public class LexiconOpenCommand extends AbstractLexiconIoCommand {

	private final LexiconMap      lexicons;
	private final SequenceFactory factory;

	public LexiconOpenCommand(LexiconMap lexicons, String path, String handle, FileHandler handler, SequenceFactory factory) {
		super(path, handle, handler);
		this.lexicons = lexicons;
		this.factory = factory;
	}

	@Override
	public void run() {
		String path = getPath();
		FileHandler handler = getHandler();
		
		List<List<String>> rows = handler.readTable(path);
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		LexiconOpenCommand that = (LexiconOpenCommand) o;

		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(lexicons, that.lexicons)
				.append(factory, that.factory)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(lexicons)
				.append(factory)
				.toHashCode();
	}
}
