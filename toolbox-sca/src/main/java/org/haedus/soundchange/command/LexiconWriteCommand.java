/*******************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.haedus.soundchange.command;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.haedus.datatypes.phonetic.Lexicon;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.io.FileHandler;
import org.haedus.datatypes.phonetic.LexiconMap;

import java.util.Iterator;
import java.util.List;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/13/2014
 */
public class LexiconWriteCommand extends LexiconIOCommand {

	private final LexiconMap lexicons;

	public LexiconWriteCommand(LexiconMap lexiconParam, String pathParam, String handleParam, FileHandler handlerParam) {
		super(pathParam, handleParam, handlerParam);
		lexicons = lexiconParam;
	}

	@Override
	public void execute() {

		Lexicon lexicon = lexicons.get(fileHandle);
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

	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		LexiconWriteCommand rhs = (LexiconWriteCommand) obj;
		return new EqualsBuilder()
				.appendSuper(super.equals(obj))
				.append(lexicons, rhs.lexicons)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.appendSuper(super.hashCode())
				.append(lexicons)
				.toHashCode();
	}
}
