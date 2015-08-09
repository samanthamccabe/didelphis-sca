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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/13/2014
 * This mock handler simply uses maps to simulate a crude file-system
 * The map is from 'path' to data, so a test can instantiate the class
 * with this object, either providing it data, or reading from it;
 */
public class MockFileHandler implements FileHandler {

	private final Map<String, String> mockFileSystem;

	public MockFileHandler(Map<String, String> input) {
		mockFileSystem = input;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		MockFileHandler rhs = (MockFileHandler) obj;
		return new EqualsBuilder()
				.append(mockFileSystem, rhs.mockFileSystem)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(mockFileSystem)
				.toHashCode();
	}

	private String readString(String path) {
		return mockFileSystem.get(path);
	}

	@Override
	public String read(String path) {
		return mockFileSystem.get(path);
	}

	@Override
	public List<String> readLines(String path) {
		String data = readString(path);

		List<String> list = new ArrayList<String>();
		Collections.addAll(list, data.split("\r?\n"));
		return list;
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
		mockFileSystem.put(path, data);
	}

	@Override
	public void writeLines(String path, List<String> data) {

		StringBuilder sb = new StringBuilder();
		for (String line : data) {
			sb.append(line).append('\n');
		}
		mockFileSystem.put(path, sb.toString());
	}

	@Override
	public String toString() {
		return "MockFileHandler:" + mockFileSystem;
	}
}
