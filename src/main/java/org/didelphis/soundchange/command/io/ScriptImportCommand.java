/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.io;

import lombok.EqualsAndHashCode;
import org.didelphis.io.FileHandler;
import org.didelphis.soundchange.ErrorLogger;

import java.util.Queue;

/**
 * @author Samantha Fiona McCabe
 * @date 2016-12-05
 */
@EqualsAndHashCode (callSuper = true)
public class ScriptImportCommand extends AbstractIoCommand {

	private final ErrorLogger logger;

	private final Queue<Runnable> commands;

	public ScriptImportCommand(String path, FileHandler handler,
			ErrorLogger logger, Queue<Runnable> commands) {
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
}
