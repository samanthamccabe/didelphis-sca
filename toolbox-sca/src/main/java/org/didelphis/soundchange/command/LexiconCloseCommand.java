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

package org.didelphis.soundchange.command;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.didelphis.enums.FormatterMode;
import org.didelphis.io.FileHandler;
import org.didelphis.phonetic.Lexicon;
import org.didelphis.phonetic.LexiconMap;
import org.didelphis.phonetic.Sequence;

import java.util.Iterator;
import java.util.List;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/13/2014
 */
public class LexiconCloseCommand extends LexiconIOCommand {

	private final LexiconMap lexicons;
	private final FormatterMode mode;

	public LexiconCloseCommand(LexiconMap lexParam, String path, String handle, FileHandler name, FormatterMode modeParam) {
		super(path, handle, name);
		lexicons = lexParam;
		mode = modeParam;
	}

	@Override
	public void run() {
		// REMOVE data from lexicons
		Lexicon lexicon = lexicons.remove(fileHandle);

		StringBuilder sb = new StringBuilder();
		Iterator<List<Sequence>> i1 = lexicon.iterator();
		while (i1.hasNext()) {
			Iterator<Sequence> i2 = i1.next().iterator();
			while (i2.hasNext()) {
				Sequence sequence = i2.next();
				sb.append(sequence);
				if (i2.hasNext()) { sb.append('\t'); }

			}
			if (i1.hasNext()) { sb.append('\n'); }
		}
		String data = sb.toString().trim();
		String normalized = mode.normalize(data);
		fileHandler.writeString(filePath, normalized);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof LexiconCloseCommand)) { return false; }

		LexiconCloseCommand other = (LexiconCloseCommand) obj;
		return new EqualsBuilder()
			.appendSuper(super.equals(obj))
			.append(mode, other.mode)
			.append(lexicons, other.lexicons)
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.appendSuper(super.hashCode())
			.append(lexicons)
			.toHashCode();
	}

	@Override
	public String toString() {
		return "LexiconCloseCommand{" +
				"lexicons=" + lexicons +
				", mode=" + mode +
				'}';
	}
}
