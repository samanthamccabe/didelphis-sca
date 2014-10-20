package org.haedus.soundchange.command;

import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.io.FileHandler;
import java.util.List;
import java.util.Map;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/13/2014
 */
public class LexiconWriteCommand extends LexiconIOCommand {

	private final Map<String, List<Sequence>> lexicons;

	public LexiconWriteCommand(Map<String, List<Sequence>> lexiconParam, String pathParam, String handleParam, FileHandler handlerParam) {
		super(pathParam, handleParam, handlerParam);
		lexicons = lexiconParam;
	}

	@Override
	public void execute() {

		List<Sequence> lexicon = lexicons.get(fileHandle);

		StringBuilder sb = new StringBuilder();
		for (Sequence sequence : lexicon) {
			sb.append(sequence).append("\n");
		}
		fileHandler.writeString(filePath, sb.toString().trim());
	}
}
