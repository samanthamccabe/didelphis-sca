package org.haedus.soundchange.command;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.haedus.exceptions.ParseException;
import org.haedus.soundchange.SoundChangeApplier;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/13/2014
 */
public class ScriptExecuteCommand implements Command {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(ScriptExecuteCommand.class);

	private final SoundChangeApplier sca;

	public ScriptExecuteCommand(String pathParam){
		File file = new File(pathParam);
		List<String> list;
		try {
			list = FileUtils.readLines(file, "UTF-8");
		} catch (IOException e) {
			throw new ParseException(e.getMessage());
		}
		sca = new SoundChangeApplier(list);
	}

	@Override
	public void execute() {
		sca.process();
	}
}
