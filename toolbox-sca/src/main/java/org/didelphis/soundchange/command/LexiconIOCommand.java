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

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/15/2014
 */
public abstract class LexiconIOCommand implements Runnable  {

	protected final String      filePath;
	protected final String      fileHandle;
	protected final FileHandler fileHandler;

	protected LexiconIOCommand(String pathParam, String handleParam, FileHandler handlerParam) {
		filePath    = pathParam;
		fileHandle  = handleParam;
		fileHandler = handlerParam;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof LexiconIOCommand)) { return false; }
		
		LexiconIOCommand rhs = (LexiconIOCommand) obj;
		return new EqualsBuilder()
				.append(filePath, rhs.filePath)
				.append(fileHandle, rhs.fileHandle)
				.append(fileHandler, rhs.fileHandler)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(filePath)
				.append(fileHandle)
				.append(fileHandler)
				.toHashCode();
	}

	public String getFilePath() {
		return filePath;
	}

	public String getFileHandle() {
		return fileHandle;
	}

	public FileHandler getFileHandler() {
		return fileHandler;
	}
}
