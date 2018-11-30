/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.io;

import lombok.EqualsAndHashCode;
import org.didelphis.io.FileHandler;
import org.didelphis.language.phonetic.features.FeatureType;
import org.didelphis.soundchange.ErrorLogger;
import org.didelphis.soundchange.SoundChangeScript;
import org.didelphis.soundchange.StandardScript;
import org.didelphis.utilities.Logger;

import java.io.IOException;

/**
 * @author Samantha Fiona McCabe
 * @date 2014-10-13
 */
@EqualsAndHashCode(callSuper = true)
public class ScriptExecuteCommand<T> extends AbstractIoCommand {

	private static final Logger LOG = Logger.create(ScriptExecuteCommand.class);

	private final FeatureType<T> type;
	private final ErrorLogger logger;

	public ScriptExecuteCommand(String path, FeatureType<T> type,
			FileHandler handler, ErrorLogger logger) {
		super(path, handler);
		this.type = type;
		this.logger = logger;
	}

	@Override
	public void run() {
		String path = getPath();
		FileHandler handler = getHandler();

		try {
			String data = handler.read(path);
			SoundChangeScript<T> script = new StandardScript<>(
					path,
					type,
					data,
					handler,
					logger
			);
			script.process();
		} catch (IOException e) {
			LOG.error("Failed to read from path {}", path, e);
		}
	}

	@Override
	public String toString() {
		return "EXECUTE " + '\'' + getPath() + '\'';
	}
}
