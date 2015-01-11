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
		List<List<String>> lexicon = fileHandler.readTable(filePath);
		soundChangeApplier.addLexicon(fileHandle, lexicon);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		LexiconOpenCommand rhs = (LexiconOpenCommand) obj;
		return new EqualsBuilder()
				.appendSuper(super.equals(obj))
				.append(this.soundChangeApplier, rhs.soundChangeApplier)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.appendSuper(super.hashCode())
				.append(soundChangeApplier)
				.toHashCode();
	}
}
