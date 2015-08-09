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

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/11/2014
 */
public class DiskFileHandler implements FileHandler {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(DiskFileHandler.class);
	
	private static final String          DEFAULT_ENCODING = "UTF-8";
	private static final DiskFileHandler DEFAULT_INSTANCE = new DiskFileHandler(DEFAULT_ENCODING);

	private final String encoding;

	public static DiskFileHandler getDefaultInstance() {
		return DEFAULT_INSTANCE;
	}
	
	public DiskFileHandler() {
		this(DEFAULT_ENCODING);
	}
	
	public DiskFileHandler(String encodingParam) {
		encoding = encodingParam;
	}

	@Override
	public String read(String path) {
		File file = new File(path);
		String string;
		try {
			string = FileUtils.readFileToString(file, encoding);
		} catch (IOException e) {
			LOGGER.error("Error when reading file {} from path \"{}\"!", path, file.getAbsolutePath(), e);
			string = "";
		}
		return string;
	}

	@Override
	public List<String> readLines(String path) {
		File file = new File(path);
		List<String> lines = new ArrayList<String>();
		try {
			lines.addAll(FileUtils.readLines(file, encoding));
		} catch (IOException e) {
			LOGGER.error("Error when reading file {} from path \"{}\"!", path, file.getAbsolutePath(), e);
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
		File file = new File(path);
		try {
			FileUtils.write(file, data, encoding);
		} catch (IOException e) {
			LOGGER.error("Error when writing to \"{}\"! Full path: {}", path, file.getAbsolutePath(), e);
		}
	}

	@Override
	public void writeLines(String path, List<String> data) {
		File file = new File(path);
		try {
			FileUtils.writeLines(file, encoding, data);
		} catch (IOException e) {
			LOGGER.error("Error when writing to \"{}\"!", path, e);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true;  }
		if (o == null) { return false; }
		if (getClass() != o.getClass()) { return false; }

		DiskFileHandler that = (DiskFileHandler) o;
		return !encoding.equals(that.encoding);
	}

	@Override
	public int hashCode() {
		return DiskFileHandler.class.hashCode() * encoding.hashCode();
	}

	@Override
	public String toString() {
		return "DiskFileHandler:" + encoding;
	}
}
