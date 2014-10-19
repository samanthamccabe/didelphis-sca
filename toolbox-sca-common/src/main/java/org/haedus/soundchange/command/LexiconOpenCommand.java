package org.haedus.soundchange.command;

import org.haedus.io.FileHandler;
import org.haedus.soundchange.SoundChangeApplier;

import java.util.List;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/13/2014
 */
public class LexiconOpenCommand extends LexiconIOCommand implements Command {

	private final SoundChangeApplier soundChangeApplier;

	public LexiconOpenCommand( String pathParam, String handleParam, FileHandler handlerParam, SoundChangeApplier scaParam) {
		super(pathParam, handleParam, handlerParam);
		soundChangeApplier = scaParam;
	}

	@Override
	public void execute() {
		List<String> lines = fileHandler.readLines(filePath);
		soundChangeApplier.addLexicon(fileHandle, lines);
	}
}
