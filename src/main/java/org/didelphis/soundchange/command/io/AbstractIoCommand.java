/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.io;

import lombok.EqualsAndHashCode;

import lombok.Getter;
import org.didelphis.io.FileHandler;

@Getter
@EqualsAndHashCode
public abstract class AbstractIoCommand implements Runnable {

	private final String path;
	private final FileHandler handler;

	protected AbstractIoCommand(String path, FileHandler handler) {
		this.path = path;
		this.handler = handler;
	}

}
