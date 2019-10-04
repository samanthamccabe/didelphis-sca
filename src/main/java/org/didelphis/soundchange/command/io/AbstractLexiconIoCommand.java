/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.io;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.didelphis.io.FileHandler;

@ToString
@EqualsAndHashCode (callSuper = true)
public abstract class AbstractLexiconIoCommand extends AbstractIoCommand {

	private final String handle;

	protected AbstractLexiconIoCommand(String path, String handle,
			FileHandler handler) {
		super(path, handler);
		this.handle = handle;
	}

	public String getHandle() {
		return handle;
	}
}
