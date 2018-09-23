/*******************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this filePath except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.didelphis.soundchange;

import org.didelphis.io.FileHandler;
import org.didelphis.language.phonetic.features.FeatureType;
import org.didelphis.soundchange.parser.ScriptParser;

import java.util.Queue;

/**
 * @author Samantha Fiona McCabe
 * @date 2013-04-13
 */
public class StandardScript<T> implements SoundChangeScript<T> {

	private final FileHandler handler;
	private final String filePath;
	private final Queue<Runnable> commands;
	private final LexiconMap<T> lexicons;

	public StandardScript(String filePath, FeatureType<T> type,
			String script, FileHandler handler, ErrorLogger logger) {
		this.filePath = filePath;
		this.handler = handler;

		ScriptParser<T> scriptParser = new ScriptParser<>(
				filePath,
				type,
				script,
				handler,
				logger
		);
		scriptParser.parse();

		lexicons = scriptParser.getMemory().getLexicons();
		commands = scriptParser.getCommands();
	}

	@Override
	public FileHandler getHandler() {
		return handler;
	}

	@Override
	public Queue<Runnable> getCommands() {
		return commands;
	}

	@Override
	public LexiconMap<T> getLexicons() {
		return lexicons;
	}

	@Override
	public void process() {
		for (Runnable command : commands) {
			command.run();
		}
	}

	@Override
	public String toString() {
		return "StandardScript{" + filePath + '}';
	}
}
