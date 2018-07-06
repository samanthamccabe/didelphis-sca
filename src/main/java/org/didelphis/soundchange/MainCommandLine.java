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

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import org.didelphis.io.DiskFileHandler;
import org.didelphis.language.phonetic.features.FeatureType;
import org.didelphis.language.phonetic.features.IntegerFeature;
import org.didelphis.utilities.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * @author Samantha Fiona McCabe
 * @date 2013-09-28
 */
@UtilityClass
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class MainCommandLine {
	
	Logger LOG = Logger.create(MainCommandLine.class);
	double NANO = 10.0E-9;

	public static void main(String... args) {
		if (args.length == 0) {
			throw new IllegalArgumentException("No arguments were provided!");
		} else {
			for (String arg : args) {
				double startTime = System.nanoTime();
				File file = new File(arg);

				try (BufferedReader reader = new BufferedReader(
						new FileReader(file))) {
					String rules = reader.lines().collect(Collectors.joining());

					// TODO: read dynamically somehow
					FeatureType<?> type = IntegerFeature.INSTANCE;

					SoundChangeScript<?> script =
							new StandardScript<>(arg, type, rules,
									new DiskFileHandler("UTF-8"),
									new ErrorLogger());
					script.process();
				} catch (IOException e) {
					LOG.error("Failed to open script {}",
							file.getAbsolutePath(), e);
				}

				double elapsedTime = System.nanoTime() - startTime;
				double time = elapsedTime * NANO;
				LOG.info("Finished script {} in {} seconds", file.getName(),
						time);
			}
		}
	}
}
