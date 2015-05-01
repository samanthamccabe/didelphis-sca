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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.haedus.exceptions.ParseException;
import org.haedus.soundchange.StandardScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/13/2014
 */
public class ScriptExecuteCommand implements Command {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(ScriptExecuteCommand.class);

	private final StandardScript sca;

	public ScriptExecuteCommand(String pathParam) {
		File file = new File(pathParam);
		String data;
		try {
			data = FileUtils.readFileToString(file, "UTF-8");
		} catch (IOException e) {
			throw new ParseException(e);
		}
		sca = new StandardScript(data);
	}

	@Override
	public void execute() {
		sca.process();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		ScriptExecuteCommand rhs = (ScriptExecuteCommand) obj;
		return new EqualsBuilder()
				.append(sca, rhs.sca)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(sca)
				.append(ScriptExecuteCommand.class)
				.toHashCode();
	}
}
