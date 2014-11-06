package org.haedus.soundchange.command;

import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.io.FileHandler;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/13/2014
 */
public class LexiconWriteCommand extends LexiconIOCommand {

	private final Map<String, List<List<Sequence>>> lexicons;

	public LexiconWriteCommand(Map<String, List<List<Sequence>>> lexiconParam, String pathParam, String handleParam, FileHandler handlerParam) {
		super(pathParam, handleParam, handlerParam);
		lexicons = lexiconParam;
	}

	@Override
	public void execute() {

		List<List<Sequence>> lexicon = lexicons.get(fileHandle);
		StringBuilder sb = new StringBuilder();
		Iterator<List<Sequence>> i1 = lexicon.iterator();
		while (i1.hasNext()) {
			Iterator<Sequence> i2 = i1.next().iterator();
			while (i2.hasNext()) {
				Sequence sequence = i2.next();
				sb.append(sequence);
				if (i2.hasNext()) sb.append("\t");

			}
			if (i1.hasNext()) sb.append("\n");
		}
		fileHandler.writeString(filePath, sb.toString().trim());
	}
}
