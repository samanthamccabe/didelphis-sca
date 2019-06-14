/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.io;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.didelphis.io.FileHandler;
import org.didelphis.language.phonetic.Lexicon;
import org.didelphis.language.phonetic.SequenceFactory;
import org.didelphis.soundchange.LexiconMap;
import org.didelphis.utilities.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@ToString
@EqualsAndHashCode(callSuper = true)
public class LexiconOpenCommand<T> extends AbstractLexiconIoCommand {

	private static final Logger LOG = Logger.create(LexiconOpenCommand.class);

	private final LexiconMap<T> lexicons;
	private final SequenceFactory<T> factory;

	public LexiconOpenCommand(LexiconMap<T> lexicons, String path, String handle,
			FileHandler handler, SequenceFactory<T> factory) {
		super(path, handle, handler);
		this.lexicons = lexicons;
		this.factory = factory;
	}

	@Override
	public void run() {
		String path = getPath();
		FileHandler handler = getHandler();

		CharSequence charSequence = null;
		try {
			charSequence = handler.read(path);
		} catch (IOException e) {
			LOG.error("Failed to read from path {}", path, e);
		}
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
}
