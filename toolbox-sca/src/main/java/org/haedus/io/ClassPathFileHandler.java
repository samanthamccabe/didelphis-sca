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

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/11/2014
 */
public class ClassPathFileHandler implements FileHandler {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(ClassPathFileHandler.class);

	private static final String               DEFAULT_ENCODING = "UTF-8";
	private static final ClassPathFileHandler DEFAULT_INSTANCE = new ClassPathFileHandler(DEFAULT_ENCODING);

	private final String encoding;

	public static ClassPathFileHandler getDefaultInstance() {
		return DEFAULT_INSTANCE;
	}
	
	public ClassPathFileHandler(String encodingParam) {
		encoding = encodingParam;
	}
	
	@Override
	public List<String> readLines(String path) {

		List<String> lines = new ArrayList<String>();

		InputStream inputStream = ClassPathFileHandler.class.getClassLoader().getResourceAsStream(path);
		try {
			lines.addAll(IOUtils.readLines(inputStream, encoding));
			inputStream.close();
		} catch (IOException e) {
			LOGGER.error("Error when reading from path \"{}\"!", path, e);
		}
		return lines;
	}

	@Override
	public List<List<String>> readTable(String path) {
		List<List<String>> table = new ArrayList<List<String>>();

		for (String line : readLines(path)) {
			List<String> row = new ArrayList<String>();
			Collections.addAll(row, line.split("\t"));
			table.add(row);
		}
		return table;
	}

	@Override
	public void writeString(String path, String data) {
		throw new UnsupportedOperationException(
			"Trying to write using an instance of "
				+ ClassPathFileHandler.class.getCanonicalName()
		);
	}

	@Override
	public void writeLines(String path, List<String> data) {
		throw new UnsupportedOperationException(
			"Trying to write using an instance of "
			+ ClassPathFileHandler.class.getCanonicalName()
		);
	}

	@Override
	public String toString() {
		return "ClassPathFileHandler:" + encoding;
	}
}
