/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.io;

import lombok.EqualsAndHashCode;

import org.didelphis.io.FileHandler;
import org.didelphis.language.phonetic.Lexicon;
import org.didelphis.language.phonetic.SequenceFactory;
import org.didelphis.soundchange.LexiconMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
public class LexiconOpenCommand extends AbstractLexiconIoCommand {

	private static final Logger LOG = LogManager.getLogger(LexiconOpenCommand.class);

	private final LexiconMap lexicons;
	private final SequenceFactory factory;

	public LexiconOpenCommand(LexiconMap lexicons, String path, String handle,
			FileHandler handler, SequenceFactory factory) {
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

		Lexicon lexicon = Lexicon.fromRows(factory, rows);
		lexicons.addLexicon(getHandle(), path, lexicon);
	}

	@Override
	public String toString() {
		return "OPEN '"+getPath()+"' AS "+getHandle();
	}
}
