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

package org.haedus.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/13/2014
 */
public class NullFileHandler implements FileHandler {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(NullFileHandler.class);

	@Override
	public List<String> readLines(String path) {
		return null;
	}

	@Override
	public List<List<String>> readTable(String path) {
		return null;
	}

	@Override
	public void writeString(String path, String data) {
		LOGGER.warn("Received data for path {} : {}", path, data);
	}

	@Override
	public void writeLines(String path, List<String> data) {
		LOGGER.warn("Received data for path {} : {}", path, data);
	}
}
