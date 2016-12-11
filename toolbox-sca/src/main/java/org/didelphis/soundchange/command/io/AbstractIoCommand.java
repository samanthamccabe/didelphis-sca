/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.io;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.didelphis.io.FileHandler;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 12/4/2016
 */
public abstract class AbstractIoCommand implements Runnable {
	
	private final String      path;
	private final FileHandler handler;

	protected AbstractIoCommand(String path, FileHandler handler) {
		this.path    = path;
		this.handler = handler;
	}

	public String getPath() {
		return path;
	}

	public FileHandler getHandler() {
		return handler;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AbstractIoCommand ioCommand = (AbstractIoCommand) o;
		return new EqualsBuilder()
				.append(path, ioCommand.path)
				.append(handler, ioCommand.handler)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31)
				.append(path)
				.append(handler)
				.toHashCode();
	}
}
