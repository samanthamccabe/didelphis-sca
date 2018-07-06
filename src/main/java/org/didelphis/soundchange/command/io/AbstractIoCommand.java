/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.io;

import org.didelphis.io.FileHandler;

import java.util.Objects;

/**
 * @author Samantha Fiona McCabe
 * @date 2016-12-04
 */
public abstract class AbstractIoCommand implements Runnable {

	private final String path;
	private final FileHandler handler;

	protected AbstractIoCommand(String path, FileHandler handler) {
		this.path = path;
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
		if (!(o instanceof AbstractIoCommand)) return false;
		AbstractIoCommand that = (AbstractIoCommand) o;
		return Objects.equals(path, that.path) &&
				Objects.equals(handler, that.handler);
	}

	@Override
	public int hashCode() {
		return Objects.hash(path, handler);
	}
}
