/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.io;

import org.didelphis.common.io.FileHandler;
import org.didelphis.soundchange.ErrorLogger;
import org.didelphis.soundchange.SoundChangeScript;
import org.didelphis.soundchange.StandardScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/13/2014
 */
public class ScriptExecuteCommand extends AbstractIoCommand {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(ScriptExecuteCommand.class);
	
	private final ErrorLogger logger;
	
	public ScriptExecuteCommand(String path, FileHandler handler, ErrorLogger logger) {
		super(path, handler);
		this.logger = logger;
	}

	@Override
	public void run() {
		String path = getPath();
		FileHandler handler = getHandler();
		
		CharSequence data = handler.read(path);
		SoundChangeScript script = new StandardScript(path, data, handler, logger);
		script.process();
	}

	@Override
	public String toString() {
		return "EXECUTE " + '\'' + getPath() + '\'';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ScriptExecuteCommand)) return false;
		if (!super.equals(o)) return false;
		ScriptExecuteCommand that = (ScriptExecuteCommand) o;
		return Objects.equals(logger, that.logger);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), logger);
	}
}
