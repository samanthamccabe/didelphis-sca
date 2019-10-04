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
import org.didelphis.soundchange.LexiconMap;
import org.didelphis.utilities.Logger;

@ToString
@EqualsAndHashCode(callSuper = true)
public class LexiconCloseCommand<T> extends AbstractLexiconIoCommand {

	private static final Logger LOG = Logger.create(LexiconCloseCommand.class);

	private final LexiconMap<T> lexicons;
	private final FormatterMode mode;

	private final LexiconWriteCommand<T> command;

	public LexiconCloseCommand(
			LexiconMap<T> lexicons,
			String path,
			String handle,
			FileHandler name,
			FormatterMode mode
	) {
		super(path, handle, name);
		this.lexicons = lexicons;
		this.mode = mode;

		command = new LexiconWriteCommand<>(lexicons, path, handle, name, mode);
	}

	@Override
	public void run() {
		command.run();
		// REMOVE data from lexicons
		lexicons.remove(getHandle());
	}
}
