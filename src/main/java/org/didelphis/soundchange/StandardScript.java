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
import org.didelphis.soundchange.parser.ScriptParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;
import java.util.Queue;

public class StandardScript implements SoundChangeScript {

	private static final Logger LOG = LogManager.getLogger(StandardScript.class);
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.000");

	private final FileHandler handler;
	private final String filePath;
	private final Queue<Runnable> commands;
	private final LexiconMap lexicons;

	private final boolean isInitialized;

	@Override
	public boolean isInitialized() {
		return isInitialized;
	}

	public StandardScript(String filePath, String script, FileHandler handler) {
		this.filePath = filePath;
		this.handler = handler;

		ScriptParser parser = new ScriptParser(filePath, script, handler);
		isInitialized = parser.parse();

		lexicons = parser.getMemory().getLexicons();
		commands = parser.getCommands();
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
	public LexiconMap getLexicons() {
		return lexicons;
	}

	@Override
	public void process() {
		if (isInitialized) {
			for (Runnable command : commands) {
				LOG.info("Running rule: {}", command);
				long startTime = System.nanoTime();
				command.run();
				double delta = (System.nanoTime() - startTime) / Math.pow(10, 9);
				LOG.debug("Finished in {} seconds", DECIMAL_FORMAT.format(delta));
			}
		} else {
			throw new IllegalStateException("Script is not initialized");
		}
	}

	@Override
	public String toString() {
		return "StandardScript{" + filePath + '}';
	}
}
