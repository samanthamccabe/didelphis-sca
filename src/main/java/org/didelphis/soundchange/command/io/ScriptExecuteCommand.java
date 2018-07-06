/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.io;

import org.didelphis.io.FileHandler;
import org.didelphis.language.phonetic.features.FeatureType;
import org.didelphis.soundchange.ErrorLogger;
import org.didelphis.soundchange.SoundChangeScript;
import org.didelphis.soundchange.StandardScript;

import java.util.Objects;

/**
 * @author Samantha Fiona McCabe
 * @date 2014-10-13
 */
public class ScriptExecuteCommand<T> extends AbstractIoCommand {

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

		CharSequence data = handler.read(path);
		SoundChangeScript<T> script =
				new StandardScript<>(path, type, data, handler, logger);
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
		ScriptExecuteCommand<?> that = (ScriptExecuteCommand<?>) o;
		return Objects.equals(logger, that.logger);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), logger);
	}
}
