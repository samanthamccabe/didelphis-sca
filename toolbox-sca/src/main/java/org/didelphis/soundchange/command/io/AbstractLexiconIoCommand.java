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
		if (o == null || getClass() != o.getClass()) return false;
		AbstractLexiconIoCommand that = (AbstractLexiconIoCommand) o;
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(handle, that.handle)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(handle)
				.toHashCode();
	}
}
