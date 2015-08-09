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

package org.haedus.soundchange;

import org.apache.commons.io.FileUtils;
import org.haedus.enums.FormatterMode;
import org.haedus.exceptions.ParseException;
import org.haedus.io.DiskFileHandler;
import org.haedus.phonetic.Lexicon;
import org.haedus.phonetic.LexiconMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Samantha Fiona Morrigan McCabe
 * Date: 9/28/13
 * Time: 5:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main {
	private static final transient Logger LOGGER = LoggerFactory.getLogger(Main.class);
	public static final String UTF_8 = "UTF-8";

	private Main() {
	}

	public static void main(String[] args) throws IOException {
		
		if (args.length == 0) {
			throw new IllegalArgumentException("No arguments were provided!");
		} else {
			for (String arg : args) {
				double startTime = System.nanoTime();
				File file = new File(arg);
				String rules = FileUtils.readFileToString(file, UTF_8);
				SoundChangeScript script = new StandardScript(
					file.getName(),
					rules,
					new LexiconMap(),
					DiskFileHandler.getDefaultInstance()
				);
				script.process();

				double elapsedTime = System.nanoTime() - startTime;
				double time = elapsedTime / StrictMath.pow(10.0,9.0);
				LOGGER.info("Finished script {} in {} seconds", file.getName(), time);
			}
		}
	}
}
