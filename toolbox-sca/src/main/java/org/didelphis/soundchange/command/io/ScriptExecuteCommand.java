/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.io;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.didelphis.io.FileHandler;
import org.didelphis.soundchange.ErrorLogger;
import org.didelphis.soundchange.SoundChangeScript;
import org.didelphis.soundchange.StandardScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		
		String data = handler.read(path);
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

		if (o == null || getClass() != o.getClass()) return false;
		ScriptExecuteCommand that = (ScriptExecuteCommand) o;
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(logger, that.logger)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(logger)
				.toHashCode();
	}
}
