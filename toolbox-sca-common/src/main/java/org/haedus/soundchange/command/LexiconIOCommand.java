package org.haedus.soundchange.command;

import org.haedus.io.FileHandler;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/15/2014
 */
public abstract class LexiconIOCommand implements Command  {

	protected final String      filePath;
	protected final String      fileHandle;
	protected final FileHandler fileHandler;

	protected LexiconIOCommand(String pathParam, String handleParam, FileHandler handlerParam) {
		filePath    = pathParam;
		fileHandle  = handleParam;
		fileHandler = handlerParam;
	}
}
