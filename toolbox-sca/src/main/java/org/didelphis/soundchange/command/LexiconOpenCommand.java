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
import org.didelphis.io.FileHandler;
import org.didelphis.phonetic.Lexicon;
import org.didelphis.phonetic.LexiconMap;
import org.didelphis.phonetic.SequenceFactory;

import java.util.List;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/13/2014
 */
public class LexiconOpenCommand extends LexiconIOCommand {

	private final LexiconMap      lexicons;
	private final SequenceFactory factory;

	public LexiconOpenCommand(LexiconMap lexiconParam, String pathParam, String handleParam, FileHandler handlerParam, SequenceFactory factoryParam) {
		super(pathParam, handleParam, handlerParam);
		lexicons = lexiconParam;
		factory = factoryParam;
	}

	@Override
	public void run() {
		List<List<String>> rows = fileHandler.readTable(filePath);
		Lexicon lexicon = factory.getLexicon(rows);
		lexicons.addLexicon(fileHandle, lexicon);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof LexiconOpenCommand)) { return false; }
		
		LexiconOpenCommand rhs = (LexiconOpenCommand) obj;
		return new EqualsBuilder()
				.appendSuper(super.equals(obj))
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.appendSuper(super.hashCode())
				.toHashCode();
	}

	@Override
	public String toString() {
		return "LexiconOpenCommand{" +
				"lexicons=" + lexicons +
				", factory=" + factory +
				'}';
	}
}
