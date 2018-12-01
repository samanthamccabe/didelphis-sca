/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.io;

import lombok.EqualsAndHashCode;
import org.didelphis.io.FileHandler;
import org.didelphis.soundchange.ErrorLogger;
import org.didelphis.utilities.Logger;

import java.util.Queue;

/**
 * @author Samantha Fiona McCabe
 * @date 2014-10-13
 */
@EqualsAndHashCode(callSuper = true)
public class ScriptExecuteCommand<T> extends AbstractIoCommand {

	private static final Logger LOG = Logger.create(ScriptExecuteCommand.class);

	private final ErrorLogger logger;

	private final Queue<Runnable> commands;

	public ScriptExecuteCommand(String path, FileHandler handler,
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
		return "EXECUTE " + '\'' + getPath() + '\'';
	}
}
