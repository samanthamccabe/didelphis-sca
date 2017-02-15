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

package org.didelphis.soundchange;

import org.didelphis.common.io.DiskFileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.stream.Collectors;

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
	private static final double NANO = 10.0E-9;

	private Main() {
	}

	public static void main(String[] args) {
		
		if (args.length == 0) {
			throw new IllegalArgumentException("No arguments were provided!");
		} else {
			for (String arg : args) {
				double startTime = System.nanoTime();
				File file = new File(arg);
				
				try (BufferedReader reader = new BufferedReader(new FileReader(file))){
					String rules = reader.lines().collect(Collectors.joining());

					SoundChangeScript script = new StandardScript(
							arg,
							rules,
							DiskFileHandler.getDefaultInstance(),
							new ErrorLogger()
					);
					script.process();
				} catch (IOException e) {
					e.printStackTrace();
				}

				double elapsedTime = System.nanoTime() - startTime;
				double time = elapsedTime * NANO;
				LOGGER.info("Finished script {} in {} seconds", file.getName(), time);
			}
		}
	}
}
