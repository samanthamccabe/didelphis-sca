/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.io;

import org.didelphis.io.FileHandler;

import java.util.Objects;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/15/2014
 */
public abstract class AbstractLexiconIoCommand extends AbstractIoCommand {
	
	private final String handle;

	protected AbstractLexiconIoCommand(String path, String handle, FileHandler handler) {
		super(path, handler);
		this.handle = handle;
	}

	public String getHandle() {
		return handle;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof AbstractLexiconIoCommand)) return false;
		if (!super.equals(o)) return false;
		AbstractLexiconIoCommand that = (AbstractLexiconIoCommand) o;
		return Objects.equals(handle, that.handle);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), handle);
	}
}
