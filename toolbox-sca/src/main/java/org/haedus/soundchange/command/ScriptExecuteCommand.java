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
import org.haedus.soundchange.SoundChangeScript;
import org.haedus.soundchange.StandardScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/13/2014
 */
public class ScriptExecuteCommand implements Runnable {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(ScriptExecuteCommand.class);

	private final String path;
	private final FileHandler handler;

	public ScriptExecuteCommand(String pathParam, FileHandler handlerParam) {
		path    = pathParam;
		handler = handlerParam;
	}

	@Override
	public void run() {
		String data = handler.read(path);
		SoundChangeScript script = new StandardScript(path, data, handler);
		script.process();
	}

	@Override
	public String toString() {
		return "EXECUTE " + '\'' + path + '\'';
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof ScriptExecuteCommand)) { return false; }
		
		ScriptExecuteCommand rhs = (ScriptExecuteCommand) obj;
		return new EqualsBuilder()
				.append(path, rhs.path)
				.append(handler, rhs.handler)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(path)
				.append(handler)
				.append(ScriptExecuteCommand.class)
				.toHashCode();
	}
}
