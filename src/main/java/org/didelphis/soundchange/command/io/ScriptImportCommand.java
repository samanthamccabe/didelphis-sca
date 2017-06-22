/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.io;

import org.didelphis.io.FileHandler;
import org.didelphis.soundchange.ErrorLogger;

import java.util.Objects;
import java.util.Queue;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 12/5/2016
 */
public class ScriptImportCommand extends  AbstractIoCommand {

	private final ErrorLogger logger;
	
	private final Queue<Runnable> commands;

	public ScriptImportCommand(String path, FileHandler handler, ErrorLogger logger, Queue<Runnable> commands) {
		super(path, handler);
		this.logger = logger;
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
		return "IMPORT " + '\'' + getPath() + '\'';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ScriptImportCommand)) return false;
		if (!super.equals(o)) return false;
		ScriptImportCommand that = (ScriptImportCommand) o;
		return Objects.equals(logger, that.logger) && Objects.equals(commands, that.commands);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), logger, commands);
	}
}
