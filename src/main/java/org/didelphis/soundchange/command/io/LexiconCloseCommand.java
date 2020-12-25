/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.io;

import lombok.EqualsAndHashCode;

import org.didelphis.io.FileHandler;
import org.didelphis.language.parsing.FormatterMode;
import org.didelphis.language.phonetic.model.FeatureMapping;
import org.didelphis.soundchange.LexiconMap;

@EqualsAndHashCode(callSuper = true)
public class LexiconCloseCommand extends AbstractLexiconIoCommand {

	private final LexiconMap lexicons;
	private final FormatterMode mode;

	private final LexiconWriteCommand command;

	public LexiconCloseCommand(
			LexiconMap lexicons,
			String path,
			String handle,
			FileHandler name,
			FeatureMapping featureMapping,
			FormatterMode mode
	) {
		super(path, handle, name);
		this.lexicons = lexicons;
		this.mode = mode;

		command = new LexiconWriteCommand(lexicons, path, handle, name, featureMapping, mode);
	}

	@Override
	public void run() {
		command.run();
		// REMOVE data from lexicons
		lexicons.remove(getHandle());
	}

	@Override
	public String toString() {
		return "CLOSE "+getHandle()+" AS '"+getPath()+"'";
	}
}
