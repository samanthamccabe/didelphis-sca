/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.io;

import lombok.EqualsAndHashCode;

import org.didelphis.io.FileHandler;

import java.util.Queue;

@EqualsAndHashCode(callSuper = true)
public class ScriptExecuteCommand extends AbstractIoCommand {

	private final Queue<Runnable> commands;

	public ScriptExecuteCommand(String path, FileHandler handler, Queue<Runnable> commands) {
		super(path, handler);
		this.commands = commands;
	}

	@Override
	public void run() {
		for (Runnable command : commands) {
			command.run();
		}
	}

	@Override
	public String toString() {
		return "EXECUTE " + '\'' + getPath() + '\'';
	}
}
