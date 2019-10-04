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

import lombok.experimental.UtilityClass;

import org.didelphis.io.DiskFileHandler;
import org.didelphis.io.FileHandler;
import org.didelphis.language.phonetic.features.FeatureType;
import org.didelphis.language.phonetic.features.IntegerFeature;
import org.didelphis.utilities.Logger;

import java.io.IOException;

@UtilityClass
public final class MainCommandLine {

	private final Logger LOG = Logger.create(MainCommandLine.class);
	private final double NANO = 10.0E-9;

	public static void main(String... args) throws IOException {
		if (args.length == 0) {
			throw new IllegalArgumentException("No arguments were provided!");
		} else {
			for (String arg : args) {
				double startTime = System.nanoTime();
				FileHandler handler = new DiskFileHandler("UTF-8");
				String read = handler.read(arg);
				FeatureType<?> type = IntegerFeature.INSTANCE;
				SoundChangeScript<?> script = new StandardScript<>(arg,
						type,
						read,
						handler,
						new ErrorLogger()
				);
				script.process();

				double elapsedTime = System.nanoTime() - startTime;
				double time = elapsedTime * NANO;
				LOG.info("Finished script {} in {} seconds", arg, time);
			}
		}
	}
}
